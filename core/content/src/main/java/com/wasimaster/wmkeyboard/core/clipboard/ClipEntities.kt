package com.wasimaster.wmkeyboard.core.clipboard

import androidx.annotation.StringRes
import com.wasimaster.wmkeyboard.content.R
import com.wasimaster.wmkeyboard.core.engine.CustomRustNative
import com.wasimaster.wmkeyboard.core.location.LocationParser
import com.wasimaster.wmkeyboard.core.location.ParsedLocation
import com.wasimaster.wmkeyboard.core.phone.PhoneNumbers

enum class ClipEntityKind {
    OTP,
    PHONE,
    EMAIL,
    URL,
    PLACE;

    @get:StringRes
    val labelRes: Int
        get() = when (this) {
            OTP -> R.string.core_content_clip_entity_otp_label
            PHONE -> R.string.core_content_clip_entity_phone_label
            EMAIL -> R.string.core_content_clip_entity_email_label
            URL -> R.string.core_content_clip_entity_url_label
            PLACE -> R.string.core_content_clip_entity_place_label
        }

    @get:StringRes
    val tagRes: Int
        get() = when (this) {
            OTP -> R.string.core_content_clip_entity_otp_tag
            PHONE -> R.string.core_content_clip_entity_phone_tag
            EMAIL -> R.string.core_content_clip_entity_email_tag
            URL -> R.string.core_content_clip_entity_url_tag
            PLACE -> R.string.core_content_clip_entity_place_tag
        }
}

data class ClipEntity(
    val kind: ClipEntityKind,
    val value: String,
    val sourceId: Long,
    val sourceText: String,
    val start: Int,
    val end: Int,
    val display: String = value,
) {
    val key: String get() = "${kind.name}:$value"
}

/**
 * Clipboard entity extraction backed by Rust (`wm-engine::clip_entities`) for
 * scheme URLs, emails, OTP codes (EN/AR/ES + custom), and phone candidate spans.
 */
object ClipEntities {

    private const val MAX_SCANNED_CLIPS = 40
    private const val MAX_SCANNED_CHARS = 4000
    private const val MAX_ENTITIES = 12
    private const val PHONE_SEPARATORS = " -()."

    private val BARE_URL = Regex(
        """(?<![\w@./-])(${ClipLinks.BARE_HOST})(?![\w@-])([/?#][^\s<>"'`]*)?""",
        RegexOption.IGNORE_CASE,
    )

    /** Kept for compatibility with notification OTP extractor; tuned for Arabic & Spanish. */
    internal const val CJK_OTP_KEYWORDS =
        "رمز التحقق|كود التحقق|رمز التفعيل|كود التفعيل|رمز التأكيد|كود التأكيد|" +
            "código de verificación|codigo de verificacion|código de seguridad|clave temporal"

    fun emailIn(text: String): String? = CustomRustNative.nativeClipFindFirstEmail(text)

    fun setUserOtpKeywords(words: List<String>) {
        CustomRustNative.nativeClipSetUserOtpKeywords(words.toTypedArray())
    }

    fun parseOtpKeywords(raw: String): List<String> =
        CustomRustNative.nativeClipParseOtpKeywords(raw).toList()

    internal fun isDeniedKeyword(text: String, start: Int): Boolean =
        CustomRustNative.nativeClipIsDeniedOtpKeyword(text, start)

    fun extract(
        text: String,
        sourceId: Long = 0L,
        phoneFormats: List<PhoneMask> = emptyList(),
    ): List<ClipEntity> {
        val claimed = ArrayList<Pair<Int, Int>>()
        val found = ArrayList<ClipEntity>()

        fun claim(
            kind: ClipEntityKind,
            value: String,
            start: Int,
            end: Int = start + value.length,
            display: String = value,
        ): Boolean {
            if (value.isEmpty()) return false
            if (claimed.any { (from, to) -> start < to && from < end }) return false
            claimed += start to end
            found += ClipEntity(kind, value, sourceId, text, start, end, display)
            return true
        }

        val nativeHits = CustomRustNative.nativeClipScanText(text)
        val nativeUrls = ArrayList<Triple<Int, Int, String>>()
        val nativeEmails = ArrayList<Triple<Int, Int, String>>()
        val nativeOtps = ArrayList<Triple<Int, Int, String>>()
        val nativePhones = ArrayList<Triple<Int, Int, String>>()

        for (line in nativeHits) {
            val p1 = line.indexOf('\t')
            if (p1 < 0) continue
            val p2 = line.indexOf('\t', p1 + 1)
            if (p2 < 0) continue
            val p3 = line.indexOf('\t', p2 + 1)
            if (p3 < 0) continue
            val kind = line.substring(0, p1)
            val s = line.substring(p1 + 1, p2).toIntOrNull() ?: continue
            val e = line.substring(p2 + 1, p3).toIntOrNull() ?: continue
            val v = line.substring(p3 + 1)
            when (kind) {
                "URL" -> nativeUrls += Triple(s, e, v)
                "EMAIL" -> nativeEmails += Triple(s, e, v)
                "OTP" -> nativeOtps += Triple(s, e, v)
                "PHONE" -> nativePhones += Triple(s, e, v)
            }
        }

        for ((start, _, rawUrl) in nativeUrls) {
            val url = CustomRustNative.nativeClipTrimUrlTail(ClipLinkChain.head(rawUrl))
            if (url.length >= 8 && url.substringAfterLast('.').isNotBlank()) {
                claim(ClipEntityKind.URL, url, start)
            }
        }

        for ((start, end, email) in nativeEmails) {
            claim(ClipEntityKind.EMAIL, email, start, end)
        }

        for (match in BARE_URL.findAll(text)) {
            val host = match.groups[1]?.value ?: continue
            val url = CustomRustNative.nativeClipTrimUrlTail(ClipLinkChain.head(match.value))
            if (!ClipLinks.isBareHost(host, url.length > host.length)) continue
            claim(ClipEntityKind.URL, url, match.range.first)
        }

        if (found.any { it.kind == ClipEntityKind.URL }) {
            for ((start, end, token) in ClipLinkChain.tokens(text)) {
                val anchor = found.filter { it.kind == ClipEntityKind.URL && it.end <= start }.maxByOrNull { it.end } ?: continue
                val resolved = ClipLinkChain.resolve(anchor.value, token) ?: continue
                claim(ClipEntityKind.URL, resolved, start, end)
            }
        }

        for (place in LocationParser.findAll(text)) {
            if (place.kind != ParsedLocation.Kind.LINK) claim(ClipEntityKind.PLACE, place.text, place.start)
        }

        for ((start, end, code) in nativeOtps) {
            claim(ClipEntityKind.OTP, code, start, end)
        }

        for ((start, end, rawPhone) in nativePhones) {
            if (isDiallable(rawPhone, phoneFormats)) {
                claim(
                    ClipEntityKind.PHONE,
                    rawPhone,
                    start,
                    end,
                    display = PhoneFormats.display(rawPhone, phoneFormats),
                )
            }
        }

        return found
    }

    fun entitiesIn(
        items: List<ClipItem>,
        phoneFormats: List<PhoneMask> = emptyList(),
    ): List<ClipEntity> {
        val seen = HashSet<String>()
        val found = ArrayList<ClipEntity>()
        for (item in items.take(MAX_SCANNED_CLIPS)) {
            if (!item.kind.isTextual || item.sensitive) continue
            val text = item.text.take(MAX_SCANNED_CHARS)
            val whole = text.trim()
            for (entity in extract(text, item.id, phoneFormats)) {
                if (entity.value == whole) continue
                if (seen.add(sameKey(entity))) found += entity
            }
        }
        return found.sortedWith(compareBy({ it.kind.ordinal }, { phoneRank(it) })).take(MAX_ENTITIES)
    }

    private fun sameKey(entity: ClipEntity): String {
        if (entity.kind != ClipEntityKind.PHONE) return entity.key
        val number = PhoneNumbers.e164(entity.value) ?: return entity.key
        return "${entity.kind.name}:$number"
    }

    private fun phoneRank(entity: ClipEntity): Int {
        if (entity.kind != ClipEntityKind.PHONE) return 0
        return if (PhoneNumbers.type(entity.value).canBeMobile) 0 else 1
    }

    fun byClip(
        items: List<ClipItem>,
        phoneFormats: List<PhoneMask> = emptyList(),
    ): Map<Long, List<ClipEntity>> {
        val out = HashMap<Long, List<ClipEntity>>()
        for (item in items) {
            if (!item.kind.isTextual || item.sensitive) continue
            val text = item.text.take(MAX_SCANNED_CHARS)
            val whole = text.trim()
            val found = extract(text, item.id, phoneFormats)
                .filter { it.value != whole }
                .distinctBy(::sameKey)
                .sortedWith(compareBy({ it.kind.ordinal }, { it.start }))
            if (found.isNotEmpty()) out[item.id] = found
        }
        return out
    }

    private fun isDiallable(raw: String, phoneFormats: List<PhoneMask>): Boolean {
        val digits = raw.count { it.isDigit() }
        val floor = if (phoneFormats.isEmpty()) 7 else 4
        if (digits !in floor..15) return false
        if (phoneFormats.isNotEmpty()) return PhoneFormats.matches(raw, phoneFormats)
        PhoneNumbers.check(raw)?.let { return it }
        val punctuated = raw.any { it in PHONE_SEPARATORS }
        if (!punctuated && !raw.startsWith("+") && digits > 11) return false
        return true
    }
}

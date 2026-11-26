package com.wasimaster.wmkeyboard.core.emoji

import com.wasimaster.wmkeyboard.core.engine.CustomRustNative

/**
 * Word -> emoji suggestion facade backed natively by Rust (`wm-engine::emoji_suggester`).
 */
class EmojiSuggester(
    entries: List<EmojiEntry>,
    private val triggers: EmojiTriggers = EmojiTriggers.EMPTY,
    private val shortcodes: EmojiShortcodes = EmojiShortcodes.EMPTY,
) {
    private val handle: Long

    init {
        val baseEntries = entries.filter { it.parent == null }
        val emojis = Array(baseEntries.size) { baseEntries[it].emoji }
        val keywords = Array(baseEntries.size) { baseEntries[it].keywords.joinToString("\u001F") }
        handle = CustomRustNative.nativeBuildEmojiSuggester(emojis, keywords)
    }

    fun suggest(word: String, previous: String = "", limit: Int = 4): List<String> {
        val cleaned = word.trim().lowercase()
        if (cleaned.length < 2 || limit <= 0) return emptyList()

        val prev = previous.trim().lowercase()
        val phraseHit = if (prev.length >= 2) shortcodes.exact("${prev}_$cleaned").orEmpty() else ""

        val extraExact = buildDynamicLayer(cleaned)
        val extraSingular = if (cleaned.length >= 4 && cleaned.endsWith("s")) {
            buildDynamicLayer(cleaned.dropLast(1))
        } else {
            emptyArray()
        }

        return CustomRustNative.nativeSuggestEmoji(
            handle = handle,
            word = cleaned,
            phraseHit = phraseHit,
            extraExact = extraExact,
            extraSingular = extraSingular,
            limit = limit,
        ).toList()
    }

    private fun buildDynamicLayer(token: String): Array<String> {
        val out = ArrayList<String>(4)
        out.addAll(triggers.of(token))
        if (token.length >= MIN_SHORTCODE_LENGTH) {
            shortcodes.exact(token)?.let { out.add(it) }
        }
        return out.toTypedArray()
    }

    companion object {
        private const val MIN_SHORTCODE_LENGTH = 3
    }
}

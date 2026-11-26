package com.wasimaster.wmkeyboard.core.emoji

import com.wasimaster.wmkeyboard.core.engine.CustomRustNative

/**
 * Semantic emoji search backed natively by Rust (`wm-engine::emoji_search`).
 * Preserves the exact public API while keeping the inverted index, stem index,
 * multilingual synonyms (EN/AR/ES), prefix search, and fuzzy matcher in Rust.
 */
class EmojiSearch(
    private val entries: List<EmojiEntry>,
    private val shortcodes: EmojiShortcodes = EmojiShortcodes.EMPTY,
    private val stem: ((List<String>) -> List<String>?)? = null,
) {
    private val indexByEmoji = HashMap<String, Int>(entries.size)
    private val handle: Long

    init {
        val packedKeywords = Array(entries.size) { idx ->
            val entry = entries[idx]
            indexByEmoji.putIfAbsent(entry.emoji, idx)
            entry.keywords.joinToString("\u001F")
        }
        handle = CustomRustNative.nativeBuildEmojiSearch(packedKeywords)
    }

    fun search(query: String, limit: Int = 40): List<EmojiEntry> {
        val code = EmojiShortcodes.normalize(query)
        if (code.isBlank() || limit <= 0) return emptyList()

        val bonusMap = LinkedHashMap<Int, Int>()
        shortcodes.exact(code)?.let { emoji ->
            indexByEmoji[emoji]?.let { bonusMap[it] = (bonusMap[it] ?: 0) + 1_000 }
        }
        if (code.length >= 2) {
            shortcodes.prefix(code, limit).forEachIndexed { rank, emoji ->
                indexByEmoji[emoji]?.let { bonusMap[it] = (bonusMap[it] ?: 0) + (500 - rank) }
            }
        }

        val bonusIndices = IntArray(bonusMap.size)
        val bonusScores = IntArray(bonusMap.size)
        var pos = 0
        for ((idx, sc) in bonusMap) {
            bonusIndices[pos] = idx
            bonusScores[pos] = sc
            pos++
        }

        val indices = CustomRustNative.nativeSearchEmoji(
            handle = handle,
            normalizedCode = code,
            limit = limit,
            enableStem = stem != null,
            langTag = "en",
            bonusIndices = bonusIndices,
            bonusScores = bonusScores,
        )
        val results = ArrayList<EmojiEntry>(indices.size)
        for (idx in indices) {
            entries.getOrNull(idx)?.let { results.add(it) }
        }
        return results
    }

    fun completions(typed: String, context: List<String> = emptyList(), limit: Int = 6): List<String> {
        val prefix = typed.trim().lowercase()
        if (prefix.isEmpty() || limit <= 0) return emptyList()
        val extraShortcodes = shortcodes.namesWithPrefix(prefix, limit * SHORTCODE_OVERDRAW).toTypedArray()
        return CustomRustNative.nativeEmojiCompletions(
            handle = handle,
            typed = prefix,
            context = context.toTypedArray(),
            extraShortcodes = extraShortcodes,
            limit = limit,
        ).toList()
    }

    companion object {
        private const val SHORTCODE_OVERDRAW = 8
    }
}

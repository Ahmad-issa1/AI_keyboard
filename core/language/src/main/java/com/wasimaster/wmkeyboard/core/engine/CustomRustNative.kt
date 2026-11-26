package com.wasimaster.wmkeyboard.core.engine

/**
 * JNI bridge to our Rust-ported EmojiSearch, EmojiSuggester, and ClipEntities
 * engines inside `libwm_engine_jni`.
 */
object CustomRustNative {
    init {
        NativeEngine.requireLoaded()
    }

    external fun nativeBuildEmojiSearch(keywordsPerEntry: Array<String>): Long
    external fun nativeSearchEmoji(
        handle: Long,
        normalizedCode: String,
        limit: Int,
        enableStem: Boolean,
        langTag: String,
        bonusIndices: IntArray,
        bonusScores: IntArray,
    ): IntArray
    external fun nativeEmojiCompletions(
        handle: Long,
        typed: String,
        context: Array<String>,
        extraShortcodes: Array<String>,
        limit: Int,
    ): Array<String>

    external fun nativeBuildEmojiSuggester(
        emojis: Array<String>,
        keywordsPerEntry: Array<String>,
    ): Long
    external fun nativeSuggestEmoji(
        handle: Long,
        word: String,
        phraseHit: String,
        extraExact: Array<String>,
        extraSingular: Array<String>,
        limit: Int,
    ): Array<String>

    external fun nativeClipFindFirstEmail(text: String): String?
    external fun nativeClipSetUserOtpKeywords(words: Array<String>)
    external fun nativeClipParseOtpKeywords(raw: String): Array<String>
    external fun nativeClipIsDeniedOtpKeyword(text: String, startUtf16: Int): Boolean
    external fun nativeClipTrimUrlTail(raw: String): String
    external fun nativeClipScanText(text: String): Array<String>
}

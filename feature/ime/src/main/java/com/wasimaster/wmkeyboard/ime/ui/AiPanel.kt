package com.wasimaster.wmkeyboard.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import com.wasimaster.wmkeyboard.core.tools.AiClient
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.os.SystemClock
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextDecoration
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.wasimaster.wmkeyboard.core.localllm.LocalLlmCatalog
import com.wasimaster.wmkeyboard.core.localllm.LocalLlmDownloadManager
import com.wasimaster.wmkeyboard.core.localllm.LocalLlmStore
import com.wasimaster.wmkeyboard.config.BuildConfig
import com.wasimaster.wmkeyboard.core.providers.ProviderTypes
import com.wasimaster.wmkeyboard.core.settings.AiProvider
import com.wasimaster.wmkeyboard.core.settings.AiProviders
import com.wasimaster.wmkeyboard.core.settings.aiRows
import com.wasimaster.wmkeyboard.core.settings.KeyboardSettings
import com.wasimaster.wmkeyboard.core.settings.ToolbarTool
import com.wasimaster.wmkeyboard.core.tools.AiActionSpec
import com.wasimaster.wmkeyboard.core.tools.AiMarkdown
import com.wasimaster.wmkeyboard.core.tools.AiPhase
import com.wasimaster.wmkeyboard.core.tools.AiThinking
import com.wasimaster.wmkeyboard.core.tools.AiToolActivity
import com.wasimaster.wmkeyboard.core.tools.AiToolApproval
import com.wasimaster.wmkeyboard.core.tools.BuiltInAiActions
import com.wasimaster.wmkeyboard.core.tools.TextDiff
import com.wasimaster.wmkeyboard.core.tools.TranslateClient
import com.wasimaster.wmkeyboard.core.tools.visibleAiActions
import com.wasimaster.wmkeyboard.ime.AiChatAction
import com.wasimaster.wmkeyboard.ime.AiUi
import com.wasimaster.wmkeyboard.ime.FocusRegion
import com.wasimaster.wmkeyboard.ime.KeyboardClipboard
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.R
import kotlinx.coroutines.delay
import com.wasimaster.wmkeyboard.app.MainActivityContract
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.ui.DisabledReason

/**
 * AI writing tool: one-tap actions (rewrite, summarize, translate …) that
 * run on the focused field's text through the provider configured in the
 * tool's settings (Claude, OpenAI, Gemini, Ollama, LM Studio — BYOK — or a
 * downloaded model running on-device). The result replaces the field or
 * inserts at the cursor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AiPanel(
    state: KeyboardUiState,
    onAction: (AiActionSpec) -> Unit,
    onRetry: () -> Unit,
    onRunCustom: () -> Unit,
    onPickModel: (AiProvider, String?) -> Unit,
    onToggleStripMarkdown: () -> Unit,
    onSetShowDiff: (Boolean) -> Unit,
    onReport: () -> Unit,
    onOpenToolSettings: (ToolbarTool) -> Unit,
    /** The chat's callback, which also carries the answer to a tool that asks first (#573). */
    onChat: (AiChatAction) -> Unit,
) {
    // Disabled in custom slim build
}

internal fun grayThinking(text: String, gray: Color): AnnotatedString {
    val open = text.indexOf("<think>")
    // Search for the closer only after the opener so grayEnd can never precede
    // grayStart (which would crash substring). Still handles the opener-in-
    // prompt case (open == -1) by searching from 0.
    val close = text.indexOf("</think>", startIndex = if (open != -1) open else 0)
    val grayStart = if (open != -1) open else 0
    val grayEnd = when {
        close != -1 -> close + "</think>".length
        open != -1 -> text.length
        else -> -1
    }
    return buildAnnotatedString {
        if (grayEnd == -1) {
            append(text)
        } else {
            append(text.substring(0, grayStart))
            withStyle(SpanStyle(color = gray)) { append(text.substring(grayStart, grayEnd)) }
            append(text.substring(grayEnd))
        }
    }
}

/**
 * Renders a comparison as one styled string.
 *
 * Deliberately not a composable, so it can be tested on the JVM.
 *
 * Every changed run carries a decoration as well as a colour — a strikethrough
 * for what went, an underline and heavier weight for what arrived. Colour alone
 * would be unreadable on a photo background, in a high-contrast theme, and for
 * a colour-blind reader. Whitespace-only runs get a visible stand-in for the
 * same reason: a deleted paragraph break is a real change and an invisible span
 * says nothing.
 *
 * Each changed run is wrapped in the Unicode isolate characters, so a deleted
 * Arabic word cannot reorder the untouched text around it. Those go only into
 * what is drawn, never into [TextDiff.Span.text], which is what Replace
 * commits.
 */
internal fun diffAnnotated(
    result: TextDiff.Result,
    colors: DiffColors,
): AnnotatedString = buildAnnotatedString {
    for (span in result.spans) {
        when (span.op) {
            TextDiff.Op.KEEP -> append(span.text)
            TextDiff.Op.DELETE -> withStyle(
                SpanStyle(
                    color = colors.deleted,
                    textDecoration = TextDecoration.LineThrough,
                ),
            ) {
                append(FIRST_STRONG_ISOLATE)
                append(visibleWhitespace(span.text))
                append(POP_DIRECTIONAL_ISOLATE)
            }
            TextDiff.Op.ADD -> withStyle(
                SpanStyle(
                    color = colors.added,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium,
                ),
            ) {
                append(FIRST_STRONG_ISOLATE)
                append(visibleWhitespace(span.text))
                append(POP_DIRECTIONAL_ISOLATE)
            }
        }
    }
}

/*
 * Escapes rather than the characters themselves: both are invisible, so written
 * literally they are a pair of empty-looking string constants that no reader —
 * and no reviewer — can tell apart or verify. Lint says the same thing under
 * BidiSpoofing.
 */

/** U+2068: the run inside decides its own direction. */
private const val FIRST_STRONG_ISOLATE = "\u2068"

/** U+2069: back to the direction outside. */
private const val POP_DIRECTIONAL_ISOLATE = "\u2069"

/**
 * Draws a run that is nothing but whitespace with stand-in marks, so a changed
 * space or paragraph break is visible. A run with any other character in it is
 * left exactly as it is: the marks are for the case that would otherwise show
 * nothing at all.
 */
private fun visibleWhitespace(text: String): String {
    if (text.isEmpty() || !text.all(Char::isWhitespace)) return text
    return buildString {
        for (ch in text) append(if (ch == '\n') '¶' else '·')
    }
}

/** One entry in the panel's model picker. */
internal class ModelPick(
    // Stable identity across reorders so the LazyRow can animate the selected
    // chip sliding to the front instead of snapping.
    val key: String,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

/**
 * One-tap switcher across everything usable right now: each configured
 * cloud/server provider, plus every downloaded on-device model. Hidden via
 * the "model picker on the panel" setting, or when there's nothing to
 * switch between.
 *
 * The row scrolls horizontally, so the selected entry is pulled to the front
 * — otherwise the one thing the user needs to see is the one thing off the
 * right edge.
 */
/**
 * The picker's entries, selected first, or an empty list when there is only
 * one model to choose from (a picker with one choice is noise). Hoisted out
 * of [ModelPickerRow] so [AiPanel] can publish the list as the focus ring's
 * CHIPS region with the exact indices the row draws.
 */
@Composable
internal fun rememberModelPicks(
    state: KeyboardUiState,
    onPickModel: (AiProvider, String?) -> Unit,
): List<ModelPick> {
    val filesDir = LocalContext.current.filesDir
    val settings = state.settings
    // Cheap file stats; keyed on ai state so re-opening after a download or
    // delete re-scans.
    val localIds = remember(state.ai, settings.ai.localModelId) {
        LocalLlmCatalog.models
            .filter { LocalLlmStore.isDownloaded(filesDir, it) }
            .map { it.id to it.displayName } +
            LocalLlmStore.customModels(filesDir).map {
                (LocalLlmStore.CUSTOM_PREFIX + it.name) to it.name.substringBeforeLast('.')
            }
    }
    // The AI list's rows that can answer, in the list's order. The on-device
    // row stands for every model downloaded, so it gives one chip per model.
    val rows = remember(settings) { settings.aiRows() }
    val remote = rows.filter { AiProviders.of(it.typeId).let { p -> p != null && p != AiProvider.ON_DEVICE } }
    val local = if (rows.any { it.typeId == ProviderTypes.ON_DEVICE_AI.id }) localIds else emptyList()
    if (remote.size + local.size < 2) return emptyList()

    val selectedLocalId = settings.ai.localModelId
        .takeIf { id -> local.any { it.first == id } }
        ?: local.singleOrNull()?.first

    val picks = local.map { (id, name) ->
        ModelPick(
            key = "local:$id",
            label = name,
            selected = settings.ai.provider == AiProvider.ON_DEVICE && id == selectedLocalId,
            onClick = { onPickModel(AiProvider.ON_DEVICE, id) },
        )
    } + remote.map { row ->
        val provider = AiProviders.of(row.typeId) ?: AiProvider.OPENAI_COMPATIBLE
        ModelPick(
            key = "remote:${row.key}",
            label = row.customName.ifEmpty { row.type?.let { stringResource(it.labelRes) } ?: row.typeId },
            selected = settings.ai.providerKey == row.key,
            // A remote pick hands over the row's key where an on-device one hands over its model.
            onClick = { onPickModel(provider, row.key) },
        )
    }
    // Stable sort: the selected entry moves to the front, everything else
    // keeps catalog order.
    return picks.sortedByDescending { it.selected }
}

/**
 * The name of the model in use, and, once pressed, the ones the provider lists
 * (#1038). A row of its own because the picker above is about providers.
 */
internal fun translateToChoices(current: String, keyboard: List<String>, translateTool: String): List<String> {
    val first = keyboard.map { it.substringBefore(" (").trim() } + translateTool + "English"
    return (first + TranslateClient.languages.map { it.second })
        .filter { it.isNotBlank() && !it.equals(current.trim(), ignoreCase = true) }
        .distinctBy { it.lowercase() }
}

@Composable
internal fun ModelPickerRow(picks: List<ModelPick>, focused: Int? = null) {
    // Disabled in custom slim build
}

@Composable
internal fun aiActionLabel(spec: AiActionSpec): String =
    BuiltInAiActions.labelRes(spec)?.let { stringResource(it) } ?: spec.name


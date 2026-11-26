package com.wasimaster.wmkeyboard.ime.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ErrorOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ExpandMore
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Handyman
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Language
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Psychology
import com.wasimaster.wmkeyboard.core.tools.AiMemoryNote
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Search
import com.wasimaster.wmkeyboard.core.tools.AiPhase
import com.wasimaster.wmkeyboard.core.tools.AiToolActivity
import com.wasimaster.wmkeyboard.core.tools.AiToolApproval
import com.wasimaster.wmkeyboard.core.tools.AiToolSource
import com.wasimaster.wmkeyboard.core.tools.AiTools
import com.wasimaster.wmkeyboard.core.tools.AiResearch
import com.wasimaster.wmkeyboard.core.tools.AiResearchText
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.CheckBox
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.CheckBoxOutlineBlank
import com.wasimaster.wmkeyboard.core.tools.TranslateClient
import com.wasimaster.wmkeyboard.ime.R

/**
 * The tools a model called for one answer, drawn over that answer: what it
 * searched for and the results it was handed, the pages it read, and the calls
 * that failed and why (#470).
 *
 * Folded to one line by default — "Searched the web for “…”", or "3 tool
 * calls" — because the answer is what the user came for. A tap unfolds it.
 * While a call is running that one line says what is happening right now,
 * with a spinner, which is the whole of what "Using a tool" used to say.
 *
 * Shared by the keyboard's AI panel (chat and actions) and the chat screen in
 * the settings app, like [ChatMarkdown], so colours come in rather than off a
 * theme. Links come out through [onOpen] and [onCopy] for the same reason: the
 * keyboard has no selection handles to offer and opens a browser itself, the
 * app has its own clipboard and URI handler. [onOpen] is only ever handed an
 * http or https address; anything else a model wrote can be copied, never
 * opened.
 *
 * A call to an MCP server's tool that has to ask first (#573) shows a card
 * under the line, open whether or not the record is unfolded: what the tool
 * is, where it runs, the values the model wrote for it, and Deny / Allow once
 * / Always allow, which go out through [onApproval]. A surface that passes no
 * [onApproval] shows the question without the buttons.
 *
 * A call that saved to the AI memory adds a line under the record, open or
 * folded: "Saved to memory", the fact, and Undo, which goes out through
 * [onUndoMemory] with the call's index in [uses]. A surface that passes none
 * shows the line without Undo.
 *
 * With [animate] the record moves: a live one slides in, the spinner shrinks
 * into the mark of what the call did, the header's words cross-fade, the
 * detail unrolls and rolls back up, and a call added while it is open arrives
 * on its own. The keyboard passes its `motion.any`; the app, whose screens do
 * not carry the keyboard's theme, leaves it on. Off, every state is drawn
 * straight, with no animation wrapped round it at all.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatToolUses(
    uses: List<AiToolActivity>,
    colors: ChatMarkdownColors,
    errorColor: Color,
    onOpen: (String) -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    onApproval: ((AiToolApproval) -> Unit)? = null,
    animate: Boolean = true,
    onUndoMemory: ((Int) -> Unit)? = null,
) {
    // Disabled in custom slim build
}

@Composable
internal fun MemorySaveLine(note: AiMemoryNote, colors: ChatMarkdownColors, fontSize: TextUnit, onUndo: (() -> Unit)?) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SourceLine(
    source: AiToolSource,
    fullAddress: Boolean,
    colors: ChatMarkdownColors,
    fontSize: TextUnit,
    onOpen: (String) -> Unit,
    onCopy: (String) -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun toolLine(use: AiToolActivity): String {
    if (use.isMcp) {
        return when {
            use.awaitingApproval -> stringResource(R.string.ime_ai_chat_tool_mcp_waiting, use.tool, use.server)
            use.running -> stringResource(R.string.ime_ai_chat_tool_mcp_running, use.tool, use.server)
            use.failed -> stringResource(R.string.ime_ai_chat_tool_mcp_failed, use.tool, use.server)
            else -> stringResource(R.string.ime_ai_chat_tool_mcp_done, use.tool, use.server)
        }
    }
    builtInLabels[use.tool]?.let { return builtInLine(use, it) }
    val target = use.target
    val page = when {
        use.tool != AiTools.WEB_FETCH -> ""
        use.sources.isNotEmpty() -> use.sources.first().domain
        target.isNotEmpty() -> AiToolActivity.domainOf(target)
        else -> ""
    }
    return when (use.tool) {
        AiTools.WEB_SEARCH -> when {
            target.isEmpty() && use.running -> stringResource(AiPhase.USING_TOOL.labelRes)
            target.isEmpty() -> stringResource(
                R.string.ime_ai_chat_tool_use_failed,
                stringResource(R.string.ime_ai_chat_tool_name_search),
            )
            use.running -> stringResource(R.string.ime_ai_chat_tool_searching, target)
            use.failed -> stringResource(R.string.ime_ai_chat_tool_search_failed, target)
            else -> stringResource(R.string.ime_ai_chat_tool_searched, target)
        }
        AiTools.WEB_FETCH -> when {
            page.isEmpty() && use.running -> stringResource(AiPhase.USING_TOOL.labelRes)
            page.isEmpty() -> stringResource(
                R.string.ime_ai_chat_tool_use_failed,
                stringResource(R.string.ime_ai_chat_tool_name_fetch),
            )
            use.running -> stringResource(R.string.ime_ai_chat_tool_reading, page)
            use.failed -> stringResource(R.string.ime_ai_chat_tool_read_failed, page)
            else -> stringResource(R.string.ime_ai_chat_tool_read, page)
        }
        // A tool the model made up: there is nothing to say but its name.
        else -> if (use.running) {
            stringResource(AiPhase.USING_TOOL.labelRes)
        } else {
            stringResource(R.string.ime_ai_chat_tool_use_failed, use.tool)
        }
    }
}

/** What the chat calls one of the keyboard's own tools (#698), and says while and after it runs. */
private class ToolLabels(
    @StringRes val name: Int,
    /** While it runs; %1$s is [AiToolActivity.target]. */
    @StringRes val running: Int,
    /** Once it has run; %1$s is [AiToolActivity.target]. */
    @StringRes val done: Int,
)

private val builtInLabels: Map<String, ToolLabels> = mapOf(
    AiTools.IMAGE_SEARCH to ToolLabels(
        R.string.ime_ai_tool_name_image_search,
        R.string.ime_ai_tool_image_search_running,
        R.string.ime_ai_tool_image_search_done,
    ),
    AiTools.WEATHER to ToolLabels(
        R.string.ime_ai_tool_name_weather,
        R.string.ime_ai_tool_weather_running,
        R.string.ime_ai_tool_weather_done,
    ),
    AiTools.WM_DOCS to ToolLabels(
        R.string.ime_ai_tool_name_wm_docs,
        R.string.ime_ai_tool_wm_docs_running,
        R.string.ime_ai_tool_wm_docs_done,
    ),
    AiTools.WIKIPEDIA to ToolLabels(
        R.string.ime_ai_tool_name_wikipedia,
        R.string.ime_ai_tool_wikipedia_running,
        R.string.ime_ai_tool_wikipedia_done,
    ),
    AiTools.DEFINE_WORD to ToolLabels(
        R.string.ime_ai_tool_name_define_word,
        R.string.ime_ai_tool_define_word_running,
        R.string.ime_ai_tool_define_word_done,
    ),
    AiTools.TRANSLATE to ToolLabels(
        R.string.ime_ai_tool_name_translate,
        R.string.ime_ai_tool_translate_running,
        R.string.ime_ai_tool_translate_done,
    ),
    AiTools.CONVERT_CURRENCY to ToolLabels(
        R.string.ime_ai_tool_name_convert_currency,
        R.string.ime_ai_tool_convert_running,
        R.string.ime_ai_tool_convert_done,
    ),
    AiTools.CONVERT_UNITS to ToolLabels(
        R.string.ime_ai_tool_name_convert_units,
        R.string.ime_ai_tool_convert_running,
        R.string.ime_ai_tool_convert_done,
    ),
    AiTools.CALCULATE to ToolLabels(
        R.string.ime_ai_tool_name_calculate,
        R.string.ime_ai_tool_calculate_running,
        R.string.ime_ai_tool_calculate_done,
    ),
    AiTools.CALENDAR_EVENTS to ToolLabels(
        R.string.ime_ai_tool_name_calendar_events,
        R.string.ime_ai_tool_calendar_events_running,
        R.string.ime_ai_tool_calendar_events_done,
    ),
    AiTools.SEARCH_NOTES to ToolLabels(
        R.string.ime_ai_tool_name_search_notes,
        R.string.ime_ai_tool_search_notes_running,
        R.string.ime_ai_tool_search_notes_done,
    ),
    AiTools.SEARCH_SNIPPETS to ToolLabels(
        R.string.ime_ai_tool_name_search_snippets,
        R.string.ime_ai_tool_search_snippets_running,
        R.string.ime_ai_tool_search_snippets_done,
    ),
    AiTools.SEARCH_CLIPBOARD to ToolLabels(
        R.string.ime_ai_tool_name_search_clipboard,
        R.string.ime_ai_tool_search_clipboard_running,
        R.string.ime_ai_tool_search_clipboard_done,
    ),
    AiTools.CURRENT_LOCATION to ToolLabels(
        R.string.ime_ai_tool_name_current_location,
        R.string.ime_ai_chat_tool_builtin_running,
        R.string.ime_ai_chat_tool_builtin_done,
    ),
    AiTools.HOME_ASSISTANT to ToolLabels(
        R.string.ime_ai_tool_name_home_assistant,
        R.string.ime_ai_tool_home_assistant_running,
        R.string.ime_ai_tool_home_assistant_done,
    ),
    AiTools.HOME_ASSISTANT_STATES to ToolLabels(
        R.string.ime_ai_tool_name_home_assistant_states,
        R.string.ime_ai_tool_home_assistant_states_running,
        R.string.ime_ai_tool_home_assistant_states_done,
    ),
    AiTools.ADD_CALENDAR_EVENT to ToolLabels(
        R.string.ime_ai_tool_name_add_calendar_event,
        R.string.ime_ai_tool_add_calendar_event_running,
        R.string.ime_ai_tool_add_calendar_event_done,
    ),
    AiTools.REMEMBER to ToolLabels(
        R.string.ime_ai_tool_name_remember,
        R.string.ime_ai_tool_remember_running,
        R.string.ime_ai_tool_remember_done,
    ),
    AiTools.FORGET to ToolLabels(
        R.string.ime_ai_tool_name_forget,
        R.string.ime_ai_tool_forget_running,
        R.string.ime_ai_tool_forget_done,
    ),
    AiTools.ADD_NOTE to ToolLabels(
        R.string.ime_ai_tool_name_add_note,
        R.string.ime_ai_tool_add_note_running,
        R.string.ime_ai_tool_add_note_done,
    ),
    AiTools.SET_ALARM to ToolLabels(
        R.string.ime_ai_tool_name_set_alarm,
        R.string.ime_ai_tool_set_alarm_running,
        R.string.ime_ai_tool_set_alarm_done,
    ),
    AiTools.START_TIMER to ToolLabels(
        R.string.ime_ai_tool_name_start_timer,
        R.string.ime_ai_tool_start_timer_running,
        R.string.ime_ai_tool_start_timer_done,
    ),
    AiTools.OPEN_APP to ToolLabels(
        R.string.ime_ai_tool_name_open_app,
        R.string.ime_ai_tool_open_app_running,
        R.string.ime_ai_tool_open_app_done,
    ),
    AiTools.MEDIA_CONTROL to ToolLabels(
        R.string.ime_ai_tool_name_media_control,
        R.string.ime_ai_chat_tool_builtin_running,
        R.string.ime_ai_chat_tool_builtin_done,
    ),
    AiTools.SEND_TO_PC to ToolLabels(
        R.string.ime_ai_tool_name_send_to_pc,
        R.string.ime_ai_tool_send_to_pc_running,
        R.string.ime_ai_tool_send_to_pc_done,
    ),
    AiTools.READ_IMAGE_TEXT to ToolLabels(
        R.string.ime_ai_tool_name_read_image_text,
        R.string.ime_ai_tool_read_image_text_running,
        R.string.ime_ai_tool_read_image_text_done,
    ),
    AiTools.DESCRIBE_IMAGE to ToolLabels(
        R.string.ime_ai_tool_name_describe_image,
        R.string.ime_ai_tool_describe_image_running,
        R.string.ime_ai_tool_describe_image_done,
    ),
    AiTools.REVERSE_IMAGE_SEARCH to ToolLabels(
        R.string.ime_ai_tool_name_reverse_image_search,
        R.string.ime_ai_tool_reverse_image_search_running,
        R.string.ime_ai_tool_reverse_image_search_done,
    ),
    AiTools.RENAME_CHAT to ToolLabels(
        R.string.ime_ai_tool_name_rename_chat,
        R.string.ime_ai_tool_rename_chat_running,
        R.string.ime_ai_tool_rename_chat_done,
    ),
    // Deep research's own steps, drawn among its searches and pages.
    AiTools.RESEARCH_PLAN to ToolLabels(
        R.string.ime_ai_tool_name_research_plan,
        R.string.ime_ai_tool_research_plan_running,
        R.string.ime_ai_tool_research_plan_done,
    ),
    AiTools.RESEARCH_REVIEW to ToolLabels(
        R.string.ime_ai_tool_name_research_review,
        R.string.ime_ai_tool_research_review_running,
        R.string.ime_ai_tool_research_review_done,
    ),
)

/** The search tools, whose record wears the search mark. */
@Composable
fun aiToolName(tool: String): String = builtInLabels[tool]?.let { stringResource(it.name) } ?: tool

/** [toolLine] for one of the keyboard's own tools. */
@Composable
private fun builtInLine(use: AiToolActivity, labels: ToolLabels): String {
    val name = stringResource(labels.name)
    // A model writes the language as a code; the line reads it as a name.
    val target = if (use.tool == AiTools.TRANSLATE && use.target.isNotEmpty()) {
        TranslateClient.languageName(use.target)
    } else {
        use.target
    }
    // Labels that are the generic pair take the tool's name, not a target.
    val generic = labels.running == R.string.ime_ai_chat_tool_builtin_running
    return when {
        use.awaitingApproval -> stringResource(R.string.ime_ai_chat_tool_builtin_waiting, name)
        use.tool == AiTools.RESEARCH_PLAN && use.error == AiResearch.DECLINED -> stringResource(R.string.ime_ai_research_plan_declined)
        use.failed -> stringResource(R.string.ime_ai_chat_tool_use_failed, name)
        (target.isEmpty() || generic) && use.running -> stringResource(R.string.ime_ai_chat_tool_builtin_running, name)
        target.isEmpty() || generic -> stringResource(R.string.ime_ai_chat_tool_builtin_done, name)
        use.running -> stringResource(labels.running, target)
        else -> stringResource(labels.done, target)
    }
}

/**
 * The question an MCP call asks before it runs (#573): which tool, on which
 * server, with which values, and the three answers. The values are shown in
 * full, as the model wrote them, because they are what the user is agreeing
 * to send.
 */
@Composable
internal fun ApprovalCard(
    use: AiToolActivity,
    colors: ChatMarkdownColors,
    fontSize: TextUnit,
    onApproval: ((AiToolApproval) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // Disabled in custom slim build
}

@Composable
internal fun ResearchChecklist(checklist: String, colors: ChatMarkdownColors, fontSize: TextUnit, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

internal fun openToolLink(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * A record, or a call inside one, sliding up into place as it fades in.
 * ModulateAlpha so the fade never cuts off a line drawn past the block's edge.
 */
internal fun Modifier.arrival(progress: Animatable<Float, AnimationVector1D>): Modifier = graphicsLayer {
    val p = progress.value
    if (p >= 1f) return@graphicsLayer
    alpha = p
    translationY = (1f - p) * ARRIVE_RISE.toPx()
    compositingStrategy = CompositingStrategy.ModulateAlpha
}

private val ARRIVE_RISE = 6.dp

/** The detail unrolling and rolling back up; the approval card comes and goes with it. */

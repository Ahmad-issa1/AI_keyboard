package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.app.assistant.SettingsAssistantGate
import com.wasimaster.wmkeyboard.core.settings.AI_STRIP_SUGGEST_CHARS_MAX
import com.wasimaster.wmkeyboard.core.settings.AI_STRIP_SUGGEST_CHARS_MIN
import com.wasimaster.wmkeyboard.core.settings.AiStripComposingDelayRange
import com.wasimaster.wmkeyboard.core.settings.AiStripIdleRange
import com.wasimaster.wmkeyboard.core.settings.AiStripTemperatures
import com.wasimaster.wmkeyboard.core.settings.AiStripTimeoutRange
import com.wasimaster.wmkeyboard.core.settings.SettingsDefaults
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Refresh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Edit
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.core.tools.AiResearchDepth
import com.wasimaster.wmkeyboard.core.tools.AiActionSpec
import com.wasimaster.wmkeyboard.core.tools.AiActionSurface
import com.wasimaster.wmkeyboard.core.tools.BuiltInAiReplyTones
import com.wasimaster.wmkeyboard.core.tools.ReplyStyle
import com.wasimaster.wmkeyboard.core.tools.AiInputMode
import com.wasimaster.wmkeyboard.core.tools.AiInsertMode
import com.wasimaster.wmkeyboard.core.tools.BuiltInAiActions
import com.wasimaster.wmkeyboard.core.tools.orderedAiActions
import com.wasimaster.wmkeyboard.core.tools.visibleAiActions
import androidx.compose.ui.platform.LocalContext
import com.wasimaster.wmkeyboard.ime.wmtranslate.WmTranslateBridge
import com.wasimaster.wmkeyboard.core.providers.ProviderTool
import com.wasimaster.wmkeyboard.core.settings.AiProvider
import com.wasimaster.wmkeyboard.core.settings.AiProviders
import com.wasimaster.wmkeyboard.core.settings.aiRows
import com.wasimaster.wmkeyboard.core.settings.AiToolRoundsRange
import com.wasimaster.wmkeyboard.core.settings.ToolbarTool
import com.wasimaster.wmkeyboard.core.settings.hasSearchKey
import com.wasimaster.wmkeyboard.core.settings.toolBlocker
import com.wasimaster.wmkeyboard.core.ui.toolAccentPaint
import com.wasimaster.wmkeyboard.core.tools.AiClient
import com.wasimaster.wmkeyboard.core.tools.AiPrompts
import com.wasimaster.wmkeyboard.BuildConfig
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Check
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * The AI screen on the settings root (#759): the two AI tools, the chat and
 * the history, which were otherwise reached only through the Tools list.
 *
 * It holds doors, not settings. The provider, key and model stay on the AI
 * writing tools page, because AI reply, the chat and the settings assistant
 * all read them from there, and a second copy here would be two places to
 * keep in step.
 */
@Composable
internal fun AiHubSettings(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun AiToolSettings(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

internal fun aiActionEditRoute(actionId: String): String = "ai_action_edit/$actionId"

// ---- AI actions ----

/**
 * The buttons on the AI panel: reorder them, turn them off, edit one, or write
 * a new one.
 *
 * A shipped action is never deleted. Editing one stores a spec under the same
 * id that shadows it, so "Reset" drops that spec and the shipped version comes
 * back; turning one off only takes it off the panel.
 */
@Composable
internal fun AiActionsSettings(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
    // The AI tool's actions, or the AI reply tool's tones (#234): the same
    // list, editor and storage, each showing only its own surface.
    surface: AiActionSurface = AiActionSurface.TRANSFORM,
) {
    // Disabled in custom slim build
}

@Composable
internal fun aiActionName(spec: AiActionSpec): String =
    (BuiltInAiActions.labelRes(spec) ?: BuiltInAiReplyTones.labelRes(spec))?.let { stringResource(it) } ?: spec.name
/** The one-line recap under an action's name on the list screen. */
@Composable
internal fun AiActionEditor(
    repository: SettingsRepository,
    settings: LiveSettings,
    actionId: String,
    onDone: () -> Unit,
) {
    // Disabled in custom slim build
}


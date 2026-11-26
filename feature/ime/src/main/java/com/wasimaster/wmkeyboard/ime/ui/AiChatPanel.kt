package com.wasimaster.wmkeyboard.ime.ui

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Input
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.OpenInNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ArrowUpward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AttachFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Psychology
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AudioFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Description
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FormatQuote
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PhotoCamera
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PhotoLibrary
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Image
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PictureAsPdf
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Screenshot
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VideoFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Check
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.CheckBox
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.CheckBoxOutlineBlank
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.DriveFileRenameOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.SelectAll
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Edit
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.OutlinedFlag
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Refresh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentPaste
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ExpandMore
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Image
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wasimaster.wmkeyboard.app.ChannelFeatures
import com.wasimaster.wmkeyboard.config.BuildConfig
import com.wasimaster.wmkeyboard.app.AiChatPickActivity
import com.wasimaster.wmkeyboard.core.aichat.AiChatConversation
import com.wasimaster.wmkeyboard.core.aichat.AiChatFile
import com.wasimaster.wmkeyboard.core.aichat.AiChatMessage
import com.wasimaster.wmkeyboard.core.aichat.AiChatStore
import com.wasimaster.wmkeyboard.core.settings.AiProvider
import com.wasimaster.wmkeyboard.core.settings.ToolbarTool
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.core.tools.AiThinking
import com.wasimaster.wmkeyboard.core.tools.AiToolActivity
import com.wasimaster.wmkeyboard.core.tools.AiAnswerFormat
import com.wasimaster.wmkeyboard.core.tools.AiChatMode
import com.wasimaster.wmkeyboard.core.tools.AiFollowUps
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Share
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tune
import com.wasimaster.wmkeyboard.ime.AiChatAction
import com.wasimaster.wmkeyboard.ime.FocusRegion
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.aichat.AiChatController
import com.wasimaster.wmkeyboard.ime.aichat.AiChatController.ModelChoice
import com.wasimaster.wmkeyboard.ime.aichat.AiChatImages
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.wasimaster.wmkeyboard.common.R as CommonR

/*
 * The AI panel's chat mode (#280): a conversation with the model, on the
 * keyboard, next to the field the answer is for.
 *
 * What it draws is AiChatController's — the saved transcript and the one live
 * answer — read here exactly as the chat screen in the settings app reads them,
 * so the two are one chat on two surfaces. What it adds is what only a keyboard
 * has: a composer its own keys write into, the field behind it to quote from,
 * and Insert, which is the point of chatting here at all.
 *
 * No SelectionContainer anywhere. Selection handles and the copy toolbar are
 * popup windows, and a popup over an IME window is the untrusted-touch trap;
 * a whole answer, or one code block, is copied by a button instead.
 */

/**
 * How tall the panel asks to stand while the composer has the keys under it.
 * More than the other panels ask for: a conversation is read, not glanced at
 * (#352). What the screen cannot spare comes off the panel, never off the
 * keys: the body clamps it to the room left above them.
 */
internal val AiChatCompactHeight = 320.dp

/** How far the chat grows the keyboard while nothing is being typed; was 120dp before #352. */
internal val AiChatExtraHeight = 200.dp

/** The header's mode switch. [base] indices 0 and 1 of the ACTIONS region are these two. */
@Composable
internal fun AiModeSwitch(state: KeyboardUiState, focusedAction: Int?, onChat: (AiChatAction) -> Unit) {
    // Disabled in custom slim build
}

internal const val AiChatHeaderActionCount = 3

/**
 * The rest of the header in chat mode: which conversation this is (a tap opens
 * the list), a new chat, and the way over to the app. Slots [base]..[base]+2 of
 * the ACTIONS region; [activateAiChatHeader] is the same three for Enter.
 */
@Composable
internal fun RowScope.AiChatHeaderActions(
    state: KeyboardUiState,
    focusedAction: Int?,
    base: Int,
    onChat: (AiChatAction) -> Unit,
) {
    // Disabled in custom slim build
}

internal fun activateAiChatHeader(index: Int, onChat: (AiChatAction) -> Unit) {
    when (index) {
        0 -> onChat(AiChatAction.ToggleSessions)
        1 -> onChat(AiChatAction.NewChat)
        2 -> onChat(AiChatAction.OpenInApp())
    }
}

@Composable
internal fun AiChatPanel(
    state: KeyboardUiState,
    onChat: (AiChatAction) -> Unit,
    onOpenToolSettings: (ToolbarTool) -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun CodeAction(label: String, colors: ChatMarkdownColors, onClick: () -> Unit) {
    // Disabled in custom slim build
}

fun fileIcon(file: AiChatFile): ImageVector = when {
    file.mime.startsWith("audio/") -> Icons.Outlined.AudioFile
    file.mime.startsWith("video/") -> Icons.Outlined.VideoFile
    file.mime == "application/pdf" -> Icons.Outlined.PictureAsPdf
    else -> Icons.Outlined.Description
}


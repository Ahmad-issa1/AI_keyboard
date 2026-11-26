package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.app.share.AppShareSheet
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import com.wasimaster.wmkeyboard.app.AiChatPickActivity
import com.wasimaster.wmkeyboard.core.aichat.AiChatFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ArrowUpward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AttachFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Image
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.DriveFileRenameOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.SelectAll
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.focusRequester
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Psychology
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Edit
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.OutlinedFlag
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentPaste
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PhotoCamera
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PhotoLibrary
import com.wasimaster.wmkeyboard.ime.aichat.AiChatFiles
import com.wasimaster.wmkeyboard.ime.aichat.AiChatImages
import com.wasimaster.wmkeyboard.ime.ui.fileIcon
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowBack
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Lock
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Refresh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Mic
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.RecordVoiceOver
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.BuildConfig
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.aichat.AiChatConversation
import com.wasimaster.wmkeyboard.core.aichat.AiChatMessage
import com.wasimaster.wmkeyboard.core.aichat.AiChatStore
import com.wasimaster.wmkeyboard.core.settings.AiProvider
import com.wasimaster.wmkeyboard.core.support.Support
import com.wasimaster.wmkeyboard.core.tools.AiThinking
import com.wasimaster.wmkeyboard.core.tools.AiClient
import com.wasimaster.wmkeyboard.core.tools.AiPrivacy
import com.wasimaster.wmkeyboard.core.settings.aiThrough
import com.wasimaster.wmkeyboard.core.tools.AiToolActivity
import com.wasimaster.wmkeyboard.ime.aichat.AiChatController
import com.wasimaster.wmkeyboard.ime.aichat.AiChatController.ModelChoice
import com.wasimaster.wmkeyboard.core.aicore.GeminiNano
import com.wasimaster.wmkeyboard.ime.ui.ChatMarkdown
import com.wasimaster.wmkeyboard.ime.ui.ChatMarkdownColors
import com.wasimaster.wmkeyboard.ime.ui.ChatChain
import com.wasimaster.wmkeyboard.ime.ui.hintRes
import com.wasimaster.wmkeyboard.ime.ui.icon
import com.wasimaster.wmkeyboard.ime.ui.labelRes
import com.wasimaster.wmkeyboard.ime.aichat.AiChatExport
import com.wasimaster.wmkeyboard.core.tools.AiAnswerFormat
import com.wasimaster.wmkeyboard.core.tools.AiChatMode
import com.wasimaster.wmkeyboard.core.tools.AiFollowUps
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ExpandMore
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Share
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SuggestionChip
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.ime.R as ImeR

/**
 * The chat conversation list: Tools > AI > Chat, and the launcher shortcut's
 * landing screen. Rows come straight from [AiChatStore]; the store is not a
 * flow, so [AiChatController.storeVersion] re-reads it after every change.
 */
@Composable
internal fun AiChatListScreen(
    anim: AnimatedVisibilityScope? = null,
    onOpenChat: (Long) -> Unit,
    onNewChat: () -> Unit,
    onAutoNew: () -> Unit,
    onBack: () -> Unit,
) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiChatScreen(
    settings: LiveSettings,
    conversationId: Long,
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    /**
     * Text the first message carries: the screen the floating AI button was
     * pressed over, or a selection sent with Ask AI (#697). Shown above the
     * composer, where it can be read or removed before anything is sent.
     */
    initialAttachment: String = "",
    /** What the attachment is, for its line above the composer; blank for the plain count. */
    attachmentLabel: String = "",
    /**
     * Drawn as the Ask AI sheet over another app (#697) rather than as a
     * settings screen: no status-bar padding at the top, a close glyph
     * rather than a back arrow.
     */
    inSheet: Boolean = false,
    /** A line under the model chips, such as why no screen came along. */
    notice: (@Composable () -> Unit)? = null,
    /**
     * What the message box starts with, unsent: a question the assistant's
     * menu (#1143) wrote for the user to send or change.
     */
    initialDraft: String = "",
    /**
     * [initialDraft] was said, not written (#1402): the assistant's Ask by
     * voice. It is sent as soon as a model is there, and its answer read out
     * when Read answers aloud allows; [spokenTag] is the language it was
     * heard in, for the voice that reads the answer.
     */
    sendDraft: Boolean = false,
    spokenTag: String = "",
) {
    // Disabled in custom slim build
}


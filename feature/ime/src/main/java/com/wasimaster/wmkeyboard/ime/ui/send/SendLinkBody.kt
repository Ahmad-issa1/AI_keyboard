package com.wasimaster.wmkeyboard.ime.ui.send

import android.os.SystemClock
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowBack
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowForward
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Send
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ErrorOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Lock
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.OpenInBrowser
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.QrCode2
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Share
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Upload
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Visibility
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VisibilityOff
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeState
import com.wasimaster.wmkeyboard.core.send.SendInstances
import com.wasimaster.wmkeyboard.core.send.SendServerConfig
import com.wasimaster.wmkeyboard.core.send.SendUpload
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.send.BrowserUi
import com.wasimaster.wmkeyboard.ime.send.DirectUi
import com.wasimaster.wmkeyboard.ime.send.JoinStep
import com.wasimaster.wmkeyboard.ime.send.LinkStage
import com.wasimaster.wmkeyboard.ime.send.ReceiveUi
import com.wasimaster.wmkeyboard.ime.send.SendAction
import com.wasimaster.wmkeyboard.ime.send.SendDelivered
import com.wasimaster.wmkeyboard.ime.send.SendDraftFile
import com.wasimaster.wmkeyboard.ime.send.SendSheet
import com.wasimaster.wmkeyboard.ime.send.SendSource
import com.wasimaster.wmkeyboard.ime.send.SendTextField
import com.wasimaster.wmkeyboard.ime.send.SendUi
import com.wasimaster.wmkeyboard.ime.ui.ConfettiBurst
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.LocalKeyPressFeedback
import com.wasimaster.wmkeyboard.ime.ui.ScrambledText
import com.wasimaster.wmkeyboard.ime.ui.SearchQueryText
import com.wasimaster.wmkeyboard.ime.ui.ToolPanelChip
import com.wasimaster.wmkeyboard.ime.ui.cardShape
import com.wasimaster.wmkeyboard.ime.ui.chipBorder
import com.wasimaster.wmkeyboard.ime.ui.chipShape
import com.wasimaster.wmkeyboard.ime.ui.clock.ClockCard
import com.wasimaster.wmkeyboard.ime.ui.clock.PillButton
import com.wasimaster.wmkeyboard.ime.ui.frostGlass
import com.wasimaster.wmkeyboard.ime.ui.kit.ActionBar
import com.wasimaster.wmkeyboard.ime.ui.kit.KitAction
import com.wasimaster.wmkeyboard.ime.ui.kit.popIn
import com.wasimaster.wmkeyboard.ime.ui.kit.pressScale
import com.wasimaster.wmkeyboard.ime.ui.maxContrastOn
import kotlinx.coroutines.delay

/*
 * The faces of the Send files panel (#729): the tray with its destinations
 * and sources, the sending face a transfer wears, the receipt a finished
 * link becomes, a link being received, what went wrong, and the one box
 * that has the keys while something is typed. The panel picks which is up
 * (see SendPanel.kt); each face draws what the controller publishes.
 */

// ---- the box while the keys type into it ----

/**
 * The panel collapsed over the key rows: the one box that has the keys, with
 * what can be done to it. A password shows dots unless asked to show itself.
 */
@Composable
internal fun TypingBar(state: KeyboardUiState, ui: SendUi, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun TrayFace(ui: SendUi, hub: KdeState, dest: SendDest, local: SendPanelLocal, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SendingFace(
    look: DestLook,
    sent: Long,
    total: Long,
    startedAtMs: Long,
    files: List<SendDraftFile>,
    fileCount: Int,
    note: String,
) {
    // Disabled in custom slim build
}

@Composable
internal fun ReceiptFace(ui: SendUi, stage: LinkStage.Done, local: SendPanelLocal, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun ReceiveFace(ui: SendUi, r: ReceiveUi, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun FailedFace(stage: LinkStage.Failed, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}


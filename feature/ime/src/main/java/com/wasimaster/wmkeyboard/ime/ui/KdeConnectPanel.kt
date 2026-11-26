package com.wasimaster.wmkeyboard.ime.ui

import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.ui.DisabledReason
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.KeyboardArrowLeft
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.KeyboardArrowRight
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.KeyboardReturn
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.VolumeOff
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.VolumeUp
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AttachFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Computer
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentPaste
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.KeyboardArrowDown
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.KeyboardArrowUp
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Laptop
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Phonelink
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Repeat
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.RepeatOne
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Settings
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Shuffle
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Smartphone
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tablet
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.TextFields
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeClick
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeClipboardPause
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeConnectEngine
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeDevice
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeDeviceType
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeLoop
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeMediaAction
import com.wasimaster.wmkeyboard.core.kdeconnect.KdePairFailure
import com.wasimaster.wmkeyboard.core.kdeconnect.KdePairState
import com.wasimaster.wmkeyboard.core.kdeconnect.KdePlayer
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeSpecialKey
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeState
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransfer
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransferState
import com.wasimaster.wmkeyboard.core.media.MediaSnapshot
import com.wasimaster.wmkeyboard.core.netlog.InternetPermission
import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.ime.FocusRegion
import com.wasimaster.wmkeyboard.ime.KdeAction
import com.wasimaster.wmkeyboard.ime.KdeModKey
import com.wasimaster.wmkeyboard.ime.KdeTab
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.kdeconnect.KdeConnectHub
import kotlinx.coroutines.delay

/** How tall the panel stays while the key rows are up under it, typing on the computer. */
internal val KdeTypingCompactHeight = 168.dp

/**
 * The KDE Connect tool (issue #285).
 *
 * Two sources of truth, kept apart on purpose. Everything that arrives over
 * the network — devices, pairing, players, volumes, transfers — is read here
 * from [KdeConnectHub.state] directly; the keyboard's own [KeyboardUiState]
 * carries only what the *keys* need (which tab, which buffer). So a pointer
 * drag goes finger → hub → socket without touching the state the key rows are
 * compared against on every keystroke, and a player's position tick redraws
 * this panel and nothing else.
 *
 * The panel is a ladder of states, each of which says what to do next:
 * off → looking for devices → pairing → connected (tabs) — with "not
 * reachable" folding back into the device list rather than being a dead end.
 */
@Composable
internal fun KdeConnectPanel(state: KeyboardUiState, capture: CaptureCallbacks) {
    // Disabled in custom slim build
}

@Composable
private fun CompactLines(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified), content = content)
}

/**
 * The whole header after the back button: the tool's name, the device chip
 * (opens the list) and settings. The panel passes no title of its own so the
 * name lives here, where the chip can be the one that gives way: a long
 * computer name ellipsizes before it pushes the settings button off the row.
 */
@Composable
internal fun RowScope.KdeHeaderActions(state: KeyboardUiState, onKde: (KdeAction) -> Unit) = CompactLines {
    val kb = LocalKbTheme.current
    val hub by KdeConnectHub.state.collectAsState()
    val enabled = state.settings.kdeConnect.enabled
    val device = hub.device(state.kde.deviceId)?.takeIf { it.paired && it.reachable } ?: hub.connected.firstOrNull()
    Text(
        stringResource(R.string.ime_tool_kde_connect),
        color = kb.secondaryText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
    )
    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
        if (enabled && (device != null || hub.devices.isNotEmpty())) {
            DeviceChip(kb, state, device, onKde)
        }
    }
    Spacer(Modifier.width(6.dp))
    Box(
        modifier = Modifier.size(30.dp).clip(CircleShape).background(kb.toolCircle)
            .clickable { onKde(KdeAction.OpenSettings) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Settings, stringResource(R.string.ime_kde_settings), tint = kb.toolbarIcon, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun DeviceChip(kb: KbTheme, state: KeyboardUiState, device: KdeDevice?, onKde: (KdeAction) -> Unit) {
    Row(
        modifier = Modifier
            .clip(kb.chipShape())
            .frostGlass(kb, kb.chipShape(), FrostSurface.CHIPS)
            .background(if (state.kde.showDevices) kb.chipActive else kb.chip)
            .chipBorder(kb, kb.chipShape())
            .clickable { onKde(KdeAction.ShowDevices(!state.kde.showDevices)) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = if (state.kde.showDevices) kb.chipActiveText else kb.chipText
        Box(
            Modifier.size(7.dp).clip(CircleShape)
                .background(if (device != null) ConnectedGreen else kb.secondaryText),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            device?.name ?: stringResource(R.string.ime_kde_devices),
            color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        device?.battery?.let { battery ->
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.ime_kde_battery, battery.charge),
                color = tint.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1,
            )
        }
    }
}

// ---- off ----

private val ConnectedGreen = Color(0xFF43A047)

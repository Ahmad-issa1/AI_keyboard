package com.wasimaster.wmkeyboard.ime.ui.send

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ChevronRight
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Computer
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Devices
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Inventory2
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Language
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Laptop
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Link
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.QrCodeScanner
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Refresh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Smartphone
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tablet
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tv
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Wifi
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeDevice
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeDeviceType
import com.wasimaster.wmkeyboard.core.kdeconnect.KdePairState
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeState
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransfer
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransferState
import com.wasimaster.wmkeyboard.core.send.LanTransfer
import com.wasimaster.wmkeyboard.core.settings.SendMode
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.send.BrowserFailure
import com.wasimaster.wmkeyboard.ime.send.BrowserUi
import com.wasimaster.wmkeyboard.ime.send.DirectFailure
import com.wasimaster.wmkeyboard.ime.send.DirectUi
import com.wasimaster.wmkeyboard.ime.send.JoinStep
import com.wasimaster.wmkeyboard.ime.send.LanPeer
import com.wasimaster.wmkeyboard.ime.send.PeerVia
import com.wasimaster.wmkeyboard.ime.send.SendAction
import com.wasimaster.wmkeyboard.ime.send.SendSheet
import com.wasimaster.wmkeyboard.ime.send.SendUi
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.LocalKeyPressFeedback
import com.wasimaster.wmkeyboard.ime.ui.ToolPanelChip
import com.wasimaster.wmkeyboard.ime.ui.cardShape
import com.wasimaster.wmkeyboard.ime.ui.chipBorder
import com.wasimaster.wmkeyboard.ime.ui.clock.ClockCard
import com.wasimaster.wmkeyboard.ime.ui.clock.PillButton
import com.wasimaster.wmkeyboard.ime.ui.clock.RoundButton
import com.wasimaster.wmkeyboard.ime.ui.frostGlass
import com.wasimaster.wmkeyboard.ime.ui.kit.popIn
import com.wasimaster.wmkeyboard.ime.ui.kit.pressScale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Where a draft can go (#729): the TO row of destination cards, the radar
 * that is Nearby's device view, the transfers, and the status chips the tray
 * shows for a nearby destination. Network state is read straight from
 * [KdeConnectHub.state], as the KDE panel does, so a progress tick never
 * enters the keyboard state. The controller's mode is still Link | Nearby:
 * picking a card sets it, so the engine runs exactly when a nearby
 * destination is in play.
 */

/** A place the tray can send to. */
internal sealed interface SendDest {
    /** The Send server: an encrypted link. */
    data object Link : SendDest

    /** A paired or found device from the KDE Connect engine. */
    data class Device(val id: String) : SendDest

    /** The page served to any browser on this Wi-Fi (#927). */
    data object Browser : SendDest

    /** A Wi-Fi this phone opens for the other one. */
    data object Direct : SendDest

    /** A device with LocalSend, or a PairDrop page. */
    data class Peer(val id: String) : SendDest
}

/** Which nearby destination the cards have picked, besides a device. */
internal enum class NearbyPick { DEVICE, BROWSER, DIRECT, PEER }

/**
 * The panel's own small state: what is UI and nothing the controller needs
 * to know. Lost with the composition, which is fine for toggles.
 */
@Stable
internal class SendPanelLocal {
    /** The radar is up in place of the tray. */
    var radarOpen by mutableStateOf(false)
    var pick by mutableStateOf(NearbyPick.DEVICE)

    /** Which LocalSend or PairDrop device, when [pick] is [NearbyPick.PEER]. */
    var peerId by mutableStateOf<String?>(null)

    /** The pile is spread into tiles with their ✕. */
    var pileOpen by mutableStateOf(false)
    var transfersOpen by mutableStateOf(false)

    /** The receipt's password chip shows the letters. */
    var passwordShown by mutableStateOf(false)

    /** The nearby transfer set whose sending face was put away, so the tray can be used meanwhile. */
    var hiddenSendKey by mutableStateOf<String?>(null)
}

/** The card the Send circle sends to, from the controller's mode and the panel's pick. */
internal fun selectedDest(ui: SendUi, hub: KdeState, local: SendPanelLocal): SendDest {
    if (ui.mode == SendMode.LINK || !ui.nearbyAvailable) return SendDest.Link
    return when (local.pick) {
        NearbyPick.BROWSER -> SendDest.Browser
        NearbyPick.DIRECT -> SendDest.Direct
        NearbyPick.PEER -> local.peerId?.takeIf { id -> ui.peers.peers.any { it.id == id } }?.let { SendDest.Peer(it) } ?: SendDest.Link
        NearbyPick.DEVICE -> {
            val id = hub.device(ui.nearby.targetId)?.id ?: hub.connected.firstOrNull()?.id ?: hub.paired.firstOrNull()?.id
            if (id != null) SendDest.Device(id) else SendDest.Link
        }
    }
}

/** The look of a destination wherever it is drawn: a card, the sending face's centre. */
internal class DestLook(val icon: ImageVector, val name: String, val sub: String, val subColor: Color? = null)

@Composable
internal fun destLook(dest: SendDest, ui: SendUi, hub: KdeState): DestLook {
    val context = LocalContext.current
    return when (dest) {
        SendDest.Link -> DestLook(Icons.Outlined.Link, stringResource(R.string.ime_send_dest_link), hostText(ui.instance) + " · " + bytesText(context, ui.maxBytes))
        is SendDest.Peer -> {
            val peer = ui.peers.peers.firstOrNull { it.id == dest.id }
            if (peer == null) {
                DestLook(Icons.Outlined.Devices, stringResource(R.string.ime_send_devices_count_zero), stringResource(R.string.ime_send_nearby_unreachable))
            } else {
                peerLook(peer)
            }
        }
        is SendDest.Device -> {
            val device = hub.device(dest.id)
            if (device == null) {
                DestLook(Icons.Outlined.Devices, stringResource(R.string.ime_send_devices_count_zero), stringResource(R.string.ime_send_nearby_unreachable))
            } else {
                deviceLook(device)
            }
        }
        SendDest.Browser -> DestLook(
            Icons.Outlined.Language,
            stringResource(R.string.ime_send_dest_browser),
            when (val b = ui.nearby.browser) {
                BrowserUi.Off -> stringResource(R.string.ime_send_dest_browser_sub)
                BrowserUi.Starting -> stringResource(R.string.ime_send_direct_starting)
                is BrowserUi.Serving -> b.hotspot?.ssid ?: b.url.removePrefix("http://").substringBefore('/')
                is BrowserUi.Failed -> stringResource(R.string.ime_send_transfer_failed)
            },
            if (ui.nearby.browser is BrowserUi.Failed) FailedRed else if (ui.nearby.browser is BrowserUi.Serving) ConnectedGreen else null,
        )
        SendDest.Direct -> DestLook(
            Icons.Outlined.WifiTethering,
            stringResource(R.string.ime_send_dest_direct),
            when (val d = ui.nearby.direct) {
                DirectUi.Off -> stringResource(R.string.ime_send_dest_direct_sub)
                DirectUi.Starting -> stringResource(R.string.ime_send_direct_starting)
                is DirectUi.Hosting -> d.joined ?: stringResource(R.string.ime_send_direct_hosting_short)
                is DirectUi.Joining -> if (d.step == JoinStep.LINKED) d.code.name else stringResource(R.string.ime_send_direct_step_short)
                is DirectUi.Failed -> stringResource(R.string.ime_send_transfer_failed)
            },
            when (val d = ui.nearby.direct) {
                is DirectUi.Failed -> FailedRed
                is DirectUi.Hosting -> if (d.joined != null) ConnectedGreen else null
                is DirectUi.Joining -> if (d.step == JoinStep.LINKED) ConnectedGreen else null
                else -> null
            },
        )
    }
}

@Composable
private fun peerLook(peer: LanPeer): DestLook = DestLook(
    when {
        peer.via == PeerVia.PAIRDROP -> Icons.Outlined.Language
        peer.deviceType == "desktop" || peer.deviceType == "headless" || peer.deviceType == "server" -> Icons.Outlined.Computer
        else -> Icons.Outlined.Smartphone
    },
    peer.name,
    stringResource(if (peer.via == PeerVia.LOCALSEND) R.string.ime_send_dest_localsend_sub else R.string.ime_send_dest_pairdrop_sub),
)

@Composable
private fun deviceLook(device: KdeDevice): DestLook = DestLook(
    deviceIcon(device.type),
    device.name,
    when {
        device.pairState == KdePairState.REQUESTED_BY_PEER -> stringResource(R.string.ime_send_nearby_wants_pair)
        device.pairState == KdePairState.REQUESTED -> stringResource(R.string.ime_send_direct_step_pairing)
        !device.paired -> stringResource(R.string.ime_send_nearby_pair)
        device.reachable -> stringResource(R.string.ime_send_nearby_connected)
        else -> stringResource(R.string.ime_send_nearby_unreachable)
    },
    when {
        device.pairState == KdePairState.REQUESTED_BY_PEER || !device.paired -> LocalKbTheme.current.accent
        device.reachable -> ConnectedGreen
        else -> null
    },
)

// ---- the TO row ----

/**
 * One carousel of destination cards: the Link server, every device the
 * engine knows, the browser page, the direct Wi-Fi, a code to scan, and the
 * radar. The chosen one wears the active-chip fill and an accent ring.
 */
@Composable
internal fun DestinationRow(ui: SendUi, hub: KdeState, dest: SendDest, local: SendPanelLocal, act: (SendAction) -> Unit, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

@Composable
internal fun DestBadge(look: DestLook, modifier: Modifier = Modifier, size: Dp = 110.dp) {
    // Disabled in custom slim build
}

internal fun nearbyStatusVisible(ui: SendUi, hub: KdeState, dest: SendDest): Boolean {
    if (anyTransfers(ui, hub) || ui.peers.offer != null) return true
    if (dest != SendDest.Link && (ui.peers.rtcMissing || ui.peers.pairDropOffline)) return true
    return when (dest) {
        SendDest.Link -> false
        is SendDest.Peer -> ui.draft.isEmpty && ui.nearby.sentTo == ui.peers.peers.firstOrNull { it.id == dest.id }?.name
        is SendDest.Device -> {
            val device = hub.device(dest.id) ?: return false
            device.pairState != KdePairState.PAIRED || (ui.draft.isEmpty && ui.nearby.sentTo == device.name)
        }
        SendDest.Browser -> ui.nearby.browser != BrowserUi.Off
        SendDest.Direct -> ui.nearby.direct != DirectUi.Off
    }
}

internal fun anyTransfers(ui: SendUi, hub: KdeState): Boolean =
    hub.devices.any { it.transfers.isNotEmpty() } || (ui.nearby.browser as? BrowserUi.Serving)?.transfers?.isNotEmpty() == true ||
        ui.peers.transfers.isNotEmpty()

/** Every transfer the sheet lists. */
@Composable
internal fun RowScope.NearbyStatusChips(ui: SendUi, hub: KdeState, dest: SendDest, local: SendPanelLocal, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun RadarFace(ui: SendUi, hub: KdeState, local: SendPanelLocal, act: (SendAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun TransfersSheet(ui: SendUi, hub: KdeState, act: (SendAction) -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

@Composable
internal fun TransferRow(device: KdeDevice, t: KdeTransfer, act: (SendAction) -> Unit, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

internal fun deviceIcon(type: KdeDeviceType): ImageVector = when (type) {
    KdeDeviceType.DESKTOP -> Icons.Outlined.Computer
    KdeDeviceType.LAPTOP -> Icons.Outlined.Laptop
    KdeDeviceType.PHONE -> Icons.Outlined.Smartphone
    KdeDeviceType.TABLET -> Icons.Outlined.Tablet
    KdeDeviceType.TV -> Icons.Outlined.Tv
}

/** The destination cards' size, as the mockup had them. */

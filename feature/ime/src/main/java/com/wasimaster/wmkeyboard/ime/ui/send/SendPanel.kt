package com.wasimaster.wmkeyboard.ime.ui.send

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Check
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.History
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.QrCodeScanner
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Settings
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeDevice
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeState
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransfer
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeTransferState
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.kdeconnect.KdeConnectHub
import com.wasimaster.wmkeyboard.ime.send.DirectUi
import com.wasimaster.wmkeyboard.ime.send.LinkStage
import com.wasimaster.wmkeyboard.ime.send.ReceiveUi
import com.wasimaster.wmkeyboard.ime.send.SendAction
import com.wasimaster.wmkeyboard.ime.send.SendCallbacks
import com.wasimaster.wmkeyboard.ime.send.SendSheet
import com.wasimaster.wmkeyboard.ime.send.SendUi
import com.wasimaster.wmkeyboard.ime.ui.CaptureCallbacks
import com.wasimaster.wmkeyboard.ime.ui.FullBleedTool
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.MotionCrossfade
import com.wasimaster.wmkeyboard.ime.ui.blockTouches
import com.wasimaster.wmkeyboard.ime.ui.clock.PillButton
import com.wasimaster.wmkeyboard.ime.ui.clock.RoundButton

/**
 * The Send files tool's panel (#729). One face at a time, cross-faded: the
 * tray with its destinations (the default), the radar that is Nearby's
 * device view, the sending face a transfer wears, the receipt a finished
 * link becomes, a link being received, or what went wrong. Sheets on a
 * scrim over it; a notice line that fades.
 *
 * Which face is up follows the controller's stage first, then the panel's
 * own toggles ([SendPanelLocal]): the controller owns the upload, the
 * panel owns which view of it the user asked for.
 *
 * Tall while a sheet needs the room (the QR code, the lists). Collapsed over
 * the key rows while one of its boxes (a password, a link, an address) has
 * the keys, the way the KDE panel collapses for its address box.
 */
// The capture bundle is the keyboard's; the panel's boxes draw through
// SearchQueryText, which reads the caret from the same local. Kept as a
// parameter so the dispatcher's shape matches the KDE panel's.
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun SendPanel(
    state: KeyboardUiState,
    callbacks: SendCallbacks,
    capture: CaptureCallbacks,
    onClose: () -> Unit,
) {
    // Disabled in custom slim build
}


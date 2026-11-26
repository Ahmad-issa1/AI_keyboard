package com.wasimaster.wmkeyboard.ime.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Mouse
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.kdeconnect.KdeClick
import com.wasimaster.wmkeyboard.core.kdeconnect.PointerAcceleration
import com.wasimaster.wmkeyboard.core.kdeconnect.ScrollAccumulator
import com.wasimaster.wmkeyboard.core.settings.KdeConnectSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the pad reports. Plain lambdas: a drag calls [onMove] every frame. */
internal class KdePadCallbacks(
    val onMove: (Double, Double) -> Unit,
    val onScroll: (Double, Double) -> Unit,
    val onClick: (KdeClick) -> Unit,
    /** The left button went down ([Boolean] true) or came up: the two ends of a drag. */
    val onHold: (Boolean) -> Unit,
)

/**
 * The computer's touchpad (issue #285).
 *
 * The same raw gesture loop as [TrackpadField], for the same reasons — one
 * `awaitEachGesture` over every pointer, the centroid re-anchored when a finger
 * arrives or leaves — but it reports *distance*, not caret steps, and it speaks
 * a laptop touchpad's vocabulary:
 *
 * - one finger drags: move the pointer
 * - tap: click · two-finger tap: right click · three-finger tap: middle click
 * - two fingers drag: scroll
 * - hold still, then drag — or tap and drag straight after — to drag with the
 *   button held, and let go to drop
 *
 * Nothing here reads touch state in composition. The finger's dot is two floats
 * and a version the draw lambda alone observes; so is the ring a tap-click
 * throws out from where it landed, the pad's word that the click went out.
 */
@Composable
internal fun KdeRemotePad(
    settings: KdeConnectSettings,
    callbacks: KdePadCallbacks,
    modifier: Modifier = Modifier,
    hint: String = "",
    subHint: String = "",
    description: String = "",
) {
    // Disabled in custom slim build
}

@Composable
internal fun KdePointerPad(
    onMove: (Double, Double) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
) {
    // Disabled in custom slim build
}


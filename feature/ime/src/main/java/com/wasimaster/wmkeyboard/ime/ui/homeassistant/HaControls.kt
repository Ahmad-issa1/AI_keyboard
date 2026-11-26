package com.wasimaster.wmkeyboard.ime.ui.homeassistant

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.ime.ui.frostGlass
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.core.homeassistant.HaActions
import com.wasimaster.wmkeyboard.core.homeassistant.HaFeatures
import com.wasimaster.wmkeyboard.core.homeassistant.HaInsertFormat
import com.wasimaster.wmkeyboard.core.homeassistant.HaKind
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ArrowDownward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ArrowUpward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Edit
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Pause
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PlayArrow
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Refresh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Remove
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.SkipNext
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.SkipPrevious
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Stop
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VolumeOff
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VolumeUp
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.homeassistant.HaAction
import com.wasimaster.wmkeyboard.ime.homeassistant.HaSnapshotUi
import com.wasimaster.wmkeyboard.ime.homeassistant.HaTile
import com.wasimaster.wmkeyboard.ime.ui.LocalHapticFeedback
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.cardShape
import com.wasimaster.wmkeyboard.ime.ui.chipShape
import com.wasimaster.wmkeyboard.ime.ui.clock.ClockSwitch
import com.wasimaster.wmkeyboard.ime.ui.clock.PillButton
import com.wasimaster.wmkeyboard.ime.ui.clock.RoundButton
import com.wasimaster.wmkeyboard.ime.ui.clock.SectionLabel
import com.wasimaster.wmkeyboard.ime.ui.clock.clockChip
import com.wasimaster.wmkeyboard.ime.ui.clock.digits
import com.wasimaster.wmkeyboard.ime.ui.gallery.ColorRow
import com.wasimaster.wmkeyboard.ime.ui.maxContrastOn
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** The controls inside an open tile's card, by the entity's kind. */
@Composable
@Suppress("detekt.CyclomaticComplexMethod", "detekt.LongMethod")
internal fun HaKindControls(tile: HaTile, snapshot: HaSnapshotUi?, insertFormat: HaInsertFormat, confirmRisky: Boolean, animate: Boolean, act: (HaAction) -> Unit) {
    // Disabled in custom slim build
}

@Composable
@Suppress("detekt.LongParameterList")
internal fun HaSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String = "",
    step: Float = 1f,
    track: Brush? = null,
    live: ((Float) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") animate: Boolean = true,
    onDone: (Float) -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun ChipRow(options: List<String>, selected: String?, labels: @Composable (String) -> String = { it.replace('_', ' ').replaceFirstChar { c -> c.uppercase() } }, onPick: (String) -> Unit) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HoldButton(
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    primary: Boolean = false,
    hold: Boolean,
    onFire: () -> Unit,
) {
    // Disabled in custom slim build
}

@Suppress("unused")
internal fun onAccent(kb: com.wasimaster.wmkeyboard.ime.ui.KbTheme): Color = maxContrastOn(kb.accent)

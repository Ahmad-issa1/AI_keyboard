package com.wasimaster.wmkeyboard.ime.ui.homeassistant

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.homeassistant.HaKind
import com.wasimaster.wmkeyboard.core.homeassistant.HaPrimary
import com.wasimaster.wmkeyboard.core.icons.symbols.filled.Star
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ChevronRight
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PlayArrow
import com.wasimaster.wmkeyboard.core.settings.HaTileSize
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.ime.FocusRegion
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.homeassistant.HaAction
import com.wasimaster.wmkeyboard.ime.homeassistant.HaTile
import com.wasimaster.wmkeyboard.ime.homeassistant.HaUi
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.PanelFocusTarget
import com.wasimaster.wmkeyboard.ime.ui.cardShape
import com.wasimaster.wmkeyboard.ime.ui.chipBorder
import com.wasimaster.wmkeyboard.ime.ui.clock.ClockSwitch
import com.wasimaster.wmkeyboard.ime.ui.clock.digits
import com.wasimaster.wmkeyboard.ime.ui.focusRing
import com.wasimaster.wmkeyboard.ime.ui.frostGlass
import com.wasimaster.wmkeyboard.ime.ui.maxContrastOn
import com.wasimaster.wmkeyboard.ime.ui.shortLandscape

/**
 * The grid of tiles for the open tab. Two across, three on a wide or short
 * screen, four when the user asked for compact tiles. Keyed by entity id so a
 * tile keeps its state when the list reorders, and staggered in on open.
 */
@Composable
internal fun HaTileGrid(state: KeyboardUiState, ui: HaUi, animate: Boolean, act: (HaAction) -> Unit) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Suppress("detekt.LongMethod", "detekt.CyclomaticComplexMethod")
internal fun EntityTile(
    tile: HaTile,
    compact: Boolean,
    focused: Boolean,
    animate: Boolean,
    shakeNonce: Long?,
    ranAt: Long?,
    confirmRisky: Boolean,
    modifier: Modifier = Modifier,
    act: (HaAction) -> Unit,
) {
    // Disabled in custom slim build
}

@Suppress("unused")
internal fun hvacColor(action: String?, kb: com.wasimaster.wmkeyboard.ime.ui.KbTheme): Color = when (action) {
    "heating" -> Color(0xFFFF7043)
    "cooling" -> Color(0xFF42A5F5)
    else -> kb.accent
}

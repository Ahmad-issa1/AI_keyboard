package com.wasimaster.wmkeyboard.ime.ui

import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Interests
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.wasimaster.wmkeyboard.core.settings.StickerSuggestStyle
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.StickerOffer
import com.wasimaster.wmkeyboard.ime.StickerOfferItem
import com.wasimaster.wmkeyboard.ime.dynstickers.DynLivePlayer
import com.wasimaster.wmkeyboard.ime.dynstickers.DynThumbs
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * What the stickers offered while typing reach (#329). Rides [ToolHoldCallbacks]
 * for the reason its other passengers do: the caller cannot afford another
 * parameter.
 */
data class StickerOfferCallbacks(
    /** A sticker was tapped: send it, taking its trigger back out if the setting says so. */
    val onPick: (StickerOfferItem) -> Unit = {},
    /** The chip or the "more" button: open the tray with every match. */
    val onExpand: () -> Unit = {},
    /** The tray's ✕, or a hold on the strip's thumbnails: not these, not for this text. */
    val onDismiss: () -> Unit = {},
    /** An offer of emoji mixes: the button at the tray's end that opens the Mixes panel on its emoji. */
    val onOpenMixes: (StickerOffer) -> Unit = {},
)

/** Height of the tray row: a sticker at a size you can tell apart, and a little air. */
internal fun stickerTrayShows(state: KeyboardUiState): Boolean {
    val offer = state.stickerOffer ?: return false
    return when {
        offer.inEmojiPanel -> state.panel == PanelMode.EMOJI
        state.panel != PanelMode.NONE -> false
        offerStyle(state, offer) == StickerSuggestStyle.TRAY -> true
        else -> offer.expanded
    }
}

/** How [offer] is drawn on the strip: the sticker setting's style, or the mixes' or dynamic stickers' own. */
internal fun offerStyle(state: KeyboardUiState, offer: StickerOffer): StickerSuggestStyle = when {
    offer.dynamic -> state.settings.gif.dynamic.style
    offer.mix -> state.settings.emoji.mix.suggestStyle
    else -> state.settings.gif.stickerSuggestStyle
}

/** Whether the suggestion strip draws the offer: the two narrow styles, on the keys. */
internal fun stickerStripShows(state: KeyboardUiState): Boolean {
    val offer = state.stickerOffer ?: return false
    return !offer.inEmojiPanel && state.panel == PanelMode.NONE &&
        offerStyle(state, offer) != StickerSuggestStyle.TRAY
}

/**
 * The tray, as its own row of the bar stack ([com.wasimaster.wmkeyboard.core.settings.BarRow.STICKERS]):
 * on top by default, wherever Rows & bars puts it otherwise. It grows in
 * and out the way the selection macro row does, inside a docked frame that
 * holds still (see [RevealingBarRow]), so a match arriving resizes the window
 * once rather than on every frame of the move.
 *
 * The offer it last drew is kept for the way out: the service drops the offer
 * in the same update that hides the row, and a row that empties as it starts
 * to shrink would collapse in one step instead of sliding away.
 */
@Composable
internal fun ColumnScope.StickerTrayRow(state: KeyboardUiState, callbacks: StickerOfferCallbacks) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StickerStripOffer(
    offer: StickerOffer,
    style: StickerSuggestStyle,
    callbacks: StickerOfferCallbacks,
    modifier: Modifier = Modifier,
) {
    // Disabled in custom slim build
}


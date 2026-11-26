package com.wasimaster.wmkeyboard.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as CropRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.mlkit.MlKitInit
import com.wasimaster.wmkeyboard.core.stickers.OutlineSpec
import com.wasimaster.wmkeyboard.core.stickers.ProcessedSticker
import com.wasimaster.wmkeyboard.core.stickers.StickerAddResult
import com.wasimaster.wmkeyboard.core.stickers.StickerEditState
import com.wasimaster.wmkeyboard.core.stickers.StickerImage
import com.wasimaster.wmkeyboard.core.stickers.StickerOutline
import com.wasimaster.wmkeyboard.core.stickers.StickerPackStore
import com.wasimaster.wmkeyboard.core.stickers.SubjectCutout
import com.wasimaster.wmkeyboard.core.upscale.UpscaleKind
import com.wasimaster.wmkeyboard.core.upscale.Upscaler
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.wasimaster.wmkeyboard.common.R as CommonR
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AutoFixNormal
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.BorderStyle
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Crop
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Restore

/** Settings route hosting the sticker editor. */
internal const val STICKER_EDITOR_ROUTE = "sticker_editor"

/**
 * What the editor is about to edit, and where the result goes.
 *
 * Both entry points reduce to bytes: the photo picker has already read them,
 * and a re-edit reads the stored WebP. One code path, and no `content://`
 * permission to keep alive across a navigation.
 */
internal class StickerEditRequest(
    val packId: String,
    val bytes: ByteArray,
    /** Non-null when replacing an existing sticker in place. */
    val stickerId: String?,
)

/**
 * The pending edit. Process-level for the same reason [PhotoSelection] is: a
 * route argument is a string in a URL, this is a megabyte of image, and the
 * app carries no view models. The screen captures it once, so clearing it on
 * the way out cannot make the destination pop itself twice.
 */
internal object StickerEditHandoff {
    @Volatile
    var current: StickerEditRequest? = null
}

/**
 * What one open of the editor starts from: the picture, and the edit that was
 * last saved over it, if there was one.
 *
 * Read off the disk in one place so the screen never has to think about which
 * of the three files exist. A mask without a state, or a state whose mask has
 * gone, simply restores what is there.
 */
@Composable
internal fun StickerEditorScreen(request: StickerEditRequest, onDone: () -> Unit) {
    // Disabled in custom slim build
}


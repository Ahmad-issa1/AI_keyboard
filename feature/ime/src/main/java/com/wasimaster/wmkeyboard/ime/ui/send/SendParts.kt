package com.wasimaster.wmkeyboard.ime.ui.send

import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.InsertDriveFile
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Send
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AudioFile
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Description
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FolderZip
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Image
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Movie
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wasimaster.wmkeyboard.core.send.SendError
import com.wasimaster.wmkeyboard.core.send.SendInstances
import com.wasimaster.wmkeyboard.core.send.SendLink
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.core.tools.QrCodeGen
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.send.SendDraftFile
import com.wasimaster.wmkeyboard.ime.send.SendLinkOffer
import com.wasimaster.wmkeyboard.ime.send.SendNotice
import com.wasimaster.wmkeyboard.ime.ui.KbTheme
import com.wasimaster.wmkeyboard.ime.ui.LocalKbTheme
import com.wasimaster.wmkeyboard.ime.ui.LocalKeyPressFeedback
import com.wasimaster.wmkeyboard.ime.ui.cardShape
import com.wasimaster.wmkeyboard.ime.ui.chipBorder
import com.wasimaster.wmkeyboard.ime.ui.chipShape
import com.wasimaster.wmkeyboard.ime.ui.frostGlass
import com.wasimaster.wmkeyboard.ime.ui.kit.kitSpringGentle
import com.wasimaster.wmkeyboard.ime.ui.kit.popIn
import com.wasimaster.wmkeyboard.ime.ui.kit.pressScale
import java.util.Locale
import kotlin.math.max

// The pieces the Send files panel is built from (#729): file tiles and the
// pile they fan into, the source chips, the dashed trough, the progress ring,
// the QR image, the scrim and notice line, and the strip's "receive this
// link" chip. Everything reads the keyboard theme; nothing here holds state
// of its own beyond an animation.

/** "12 MB", in the user's locale. */
internal fun bytesText(context: Context, bytes: Long): String = Formatter.formatShortFileSize(context, max(0L, bytes))

/** "2 days", "3 hr", "5 min": the largest unit that is not zero, short form. */
internal fun durationText(seconds: Long): String {
    val format = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
    val (value, unit) = when {
        seconds >= 2 * 86_400 -> seconds / 86_400 to MeasureUnit.DAY
        seconds >= 3_600 -> seconds / 3_600 to MeasureUnit.HOUR
        seconds >= 60 -> seconds / 60 to MeasureUnit.MINUTE
        else -> seconds.coerceAtLeast(0) to MeasureUnit.SECOND
    }
    return format.format(Measure(value, unit))
}

/** The host shown for an instance or a link. */
internal fun hostText(base: String): String = SendInstances.displayHost(base)

internal fun errorText(error: SendError): Int = when (error) {
    SendError.OFFLINE -> R.string.ime_send_error_offline
    SendError.TOO_BIG -> R.string.ime_send_error_too_big
    SendError.BAD_LIMITS -> R.string.ime_send_error_bad_limits
    SendError.UNAUTHORIZED -> R.string.ime_send_error_unauthorized
    SendError.NOT_FOUND -> R.string.ime_send_error_not_found
    SendError.SERVER -> R.string.ime_send_error_server
    SendError.CANCELLED -> R.string.ime_send_error_cancelled
    SendError.WRONG_KEY -> R.string.ime_send_error_wrong_key
    SendError.WRONG_PASSWORD -> R.string.ime_send_error_wrong_password
    SendError.CORRUPT -> R.string.ime_send_error_corrupt
    SendError.BAD_LINK -> R.string.ime_send_error_bad_link
    SendError.UNREADABLE -> R.string.ime_send_error_unreadable
}

@Composable
internal fun noticeText(notice: SendNotice): String = when (notice.kind) {
    SendNotice.Kind.COPIED -> stringResource(R.string.ime_send_notice_copied)
    SendNotice.Kind.INSERTED -> stringResource(R.string.ime_send_notice_inserted)
    SendNotice.Kind.SHARED -> stringResource(R.string.ime_send_notice_shared)
    SendNotice.Kind.SAVED -> stringResource(R.string.ime_send_notice_saved)
    SendNotice.Kind.DELETED -> stringResource(R.string.ime_send_notice_deleted)
    SendNotice.Kind.SENT -> stringResource(R.string.ime_send_notice_sent, notice.text)
    SendNotice.Kind.FAILED -> stringResource(R.string.ime_send_notice_failed)
    SendNotice.Kind.NO_FILES -> stringResource(R.string.ime_send_notice_no_files)
    SendNotice.Kind.EMPTY_FIELD -> stringResource(R.string.ime_send_notice_empty_field)
    SendNotice.Kind.EMPTY_CLIPBOARD -> stringResource(R.string.ime_send_notice_empty_clipboard)
    SendNotice.Kind.TOO_MANY -> stringResource(R.string.ime_send_notice_too_many, notice.text)
    SendNotice.Kind.PASSWORD_GENERATED -> stringResource(R.string.ime_send_notice_password_generated)
    SendNotice.Kind.INSTANCE_SET -> stringResource(R.string.ime_send_notice_instance_set, notice.text)
    SendNotice.Kind.INSTANCE_BAD -> stringResource(R.string.ime_send_notice_instance_bad)
    SendNotice.Kind.INSTANCE_ADDED -> stringResource(R.string.ime_send_notice_instance_added, notice.text)
    SendNotice.Kind.INSTANCE_REMOVED -> stringResource(R.string.ime_send_notice_instance_removed)
    SendNotice.Kind.NOT_ACCEPTED -> stringResource(R.string.ime_send_notice_not_accepted)
    SendNotice.Kind.OPENED -> stringResource(R.string.ime_send_notice_opened)
    SendNotice.Kind.LINK_BAD -> stringResource(R.string.ime_send_notice_link_bad)
    SendNotice.Kind.DATA_SAVER -> stringResource(R.string.ime_send_notice_data_saver)
}

/** Which glyph stands for a file of [type]. */
internal fun fileIcon(type: String, name: String = ""): ImageVector = when {
    type.startsWith("image/") -> Icons.Outlined.Image
    type.startsWith("video/") -> Icons.Outlined.Movie
    type.startsWith("audio/") -> Icons.Outlined.AudioFile
    type == "application/pdf" || name.endsWith(".pdf", true) -> Icons.Outlined.PictureAsPdf
    type.contains("zip") || type.contains("compressed") || type.contains("tar") || name.endsWith(".zip", true) -> Icons.Outlined.FolderZip
    type.startsWith("text/") || type.contains("json") || type.contains("xml") -> Icons.Outlined.Description
    else -> Icons.AutoMirrored.Outlined.InsertDriveFile
}

/** A place to add from: a small accent "+" and the name, on one compact chip. */
@Composable
internal fun SourceChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun FileTile(
    file: SendDraftFile,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)?,
    compact: Boolean = false,
    tileSize: Dp = 56.dp,
) {
    // Disabled in custom slim build
}

@Composable
internal fun FilePile(
    files: List<SendDraftFile>,
    animate: Boolean,
    modifier: Modifier = Modifier,
    tileSize: Dp = 44.dp,
    shown: Int = PILE_SHOWN,
    onClick: (() -> Unit)? = null,
) {
    // Disabled in custom slim build
}

@Composable
internal fun Modifier.dashedTrough(kb: KbTheme): Modifier {
    val shape = kb.cardShape()
    val line = kb.divider
    val fill = kb.chip.copy(alpha = kb.chip.alpha * 0.28f)
    return this
        .clip(shape)
        .background(fill)
        .drawBehind {
            val r = kb.chipRadiusDp.dp.toPx().coerceAtMost(size.minDimension / 2)
            val inset = 0.75.dp.toPx()
            drawRoundRect(
                line,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(r),
                style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))),
            )
        }
}

/** A thin bar: how much of the instance's cap the draft takes; red past it. */
@Composable
internal fun SizeMeter(fraction: Float, over: Boolean, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

@Composable
internal fun ProgressRing(
    fraction: Float?,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    stroke: Dp = 6.dp,
    content: (@Composable BoxScope.(shown: Float) -> Unit)? = null,
) {
    // Disabled in custom slim build
}

@Composable
internal fun QrImage(text: String, modifier: Modifier = Modifier, sizePx: Int = 512) {
    // Disabled in custom slim build
}

@Composable
internal fun Radar(modifier: Modifier = Modifier, animate: Boolean = true) {
    // Disabled in custom slim build
}

@Composable
internal fun SendScrim(visible: Boolean, onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SheetCard(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SendNoticeLine(notice: SendNotice?, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

@Composable
internal fun MetaText(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    // Disabled in custom slim build
}

@Composable
internal fun TinyLabel(text: String, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

@Composable
internal fun SendLinkOfferChip(
    offer: SendLinkOffer,
    reduceMotion: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Disabled in custom slim build
}

@Composable
internal fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    // Disabled in custom slim build
}

internal fun KbTheme.dimmed(color: Color): Color = color.copy(alpha = color.alpha * 0.5f)

internal val ConnectedGreen = Color(0xFF43A047)
internal val FailedRed = Color(0xFFE53935)
internal val SOURCE_CHIP_HEIGHT = 30.dp

/** Tiles the pile shows before it counts the rest. */
private const val PILE_SHOWN = 4

/** Degrees between neighbours in the fan. */

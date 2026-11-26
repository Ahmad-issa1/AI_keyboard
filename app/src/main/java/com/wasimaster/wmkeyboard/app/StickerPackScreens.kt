package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wasimaster.wmkeyboard.BuildConfig
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.addons.AddonType
import com.wasimaster.wmkeyboard.core.icons.SymbolIcons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AutoAwesome
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FileOpen
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.MoreVert
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Public
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Share
import com.wasimaster.wmkeyboard.core.stickers.CustomSticker
import com.wasimaster.wmkeyboard.core.stickers.StickerAddResult
import com.wasimaster.wmkeyboard.core.stickers.StickerImage
import com.wasimaster.wmkeyboard.core.notify.BackgroundTasks
import com.wasimaster.wmkeyboard.core.notify.TaskState
import com.wasimaster.wmkeyboard.core.stickers.StickerImportResult
import com.wasimaster.wmkeyboard.core.stickers.StickerKeywords
import com.wasimaster.wmkeyboard.core.stickers.StickerPack
import com.wasimaster.wmkeyboard.core.stickers.StickerPackFile
import com.wasimaster.wmkeyboard.core.stickers.StickerPackStore
import com.wasimaster.wmkeyboard.core.util.readBytesCapped
import com.wasimaster.wmkeyboard.core.util.requireOutputStream
import com.wasimaster.wmkeyboard.ime.ui.rememberMediaImageLoader
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One sticker pack's page, as flights name it.
 *
 * The navigation route with its argument filled in, which is what a flight is
 * keyed on: the pattern (`sticker_pack/{packId}`) is the same string for every
 * pack and would hang one key on all of them.
 */
internal fun stickerPackRoute(packId: String): String = "sticker_pack/$packId"

/**
 * The sticker packs the user owns, and everything that edits them.
 *
 * The keyboard can only view, send, and save-a-search-result into a pack —
 * an IME has no activity window, so a photo picker, a rename field or a
 * delete confirmation all have to live here.
 */
@Composable
internal fun StickerPacksScreen(onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun StickerPackScreen(
    packId: String,
    openPicker: Boolean = false,
    /** A sticker whose title and keywords open for editing at once: the keyboard's hold sheet (#1157). */
    editStickerId: String? = null,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

internal fun StickerImportResult.describe(context: Context): String = when (this) {
    is StickerImportResult.Imported -> buildString {
        append(
            context.resources.getQuantityString(
                R.plurals.import_stickers_done,
                pack.stickers.size,
                pack.name,
                pack.stickers.size,
            ),
        )
        if (repairs.isNotEmpty()) {
            append("\n\n").append(context.getString(R.string.import_repairs_title))
            // The reader hands back a resource and its arguments, so the note
            // is worded here.
            for (line in repairs.take(MAX_SHOWN_REPAIRS)) append("\n• ${line.resolve(context)}")
            appendMoreRepairs(context, repairs.size - MAX_SHOWN_REPAIRS)
        }
    }
    StickerImportResult.NotAStickerPack -> context.getString(R.string.import_not_a_sticker_pack)
    is StickerImportResult.NoStickers -> buildString {
        append(context.getString(R.string.import_stickers_none_read))
        for (line in repairs.take(MAX_SHOWN_REPAIRS)) append("\n• ${line.resolve(context)}")
        appendMoreRepairs(context, repairs.size - MAX_SHOWN_REPAIRS)
    }
    StickerImportResult.TooManyPacks -> context.resources.getQuantityString(
        R.plurals.import_stickers_too_many,
        StickerPackStore.MAX_PACKS,
        StickerPackStore.MAX_PACKS,
    )
    StickerImportResult.Failed -> context.getString(R.string.import_file_unreadable)
}

private fun StringBuilder.appendMoreRepairs(context: Context, extra: Int) {
    if (extra <= 0) return
    append("\n• ")
    append(context.resources.getQuantityString(R.plurals.import_repairs_more, extra, extra))
}

/** How many photos one trip through the picker may add. */
private const val MAX_SHOWN_REPAIRS = 5

/**
 * Adds every picture at [uris] to [packId], stopping at the first that fills
 * the pack. Reads and encodes on the IO dispatcher.
 *
 * Shared by the pack page's photo picker and the WhatsApp screen's document
 * picker: two ways of choosing files, one way of adding them.
 */
internal suspend fun addPickedStickers(
    context: Context,
    store: StickerPackStore,
    packId: String,
    uris: List<Uri>,
): AddOutcome = withContext(Dispatchers.IO) {
    var added = 0
    var tooLarge = 0
    var unreadable = 0
    var full = false
    for (uri in uris) {
        val bytes = runCatching {
            context.contentResolver.readBytesCapped(uri)
        }.getOrNull()
        if (bytes == null) {
            unreadable++
            continue
        }
        when (val processed = StickerImage.process(bytes)) {
            is StickerImage.Result.Ok ->
                when (
                    store.addSticker(
                        packId,
                        processed.sticker,
                        // Kept so "Edit image" starts from the photo and not
                        // from the 512-pixel copy of it this batch just made.
                        original = StickerImage.encodeOriginal(bytes),
                    )
                ) {
                    is StickerAddResult.Added -> added++
                    StickerAddResult.PackFull -> full = true
                    else -> unreadable++
                }
            StickerImage.Result.TooLarge -> tooLarge++
            StickerImage.Result.NotAnImage -> unreadable++
        }
        if (full) break
    }
    AddOutcome(added, tooLarge, unreadable, full)
}

internal data class AddOutcome(
    val added: Int,
    val tooLarge: Int,
    val unreadable: Int,
    val packFull: Boolean,
) {
    /** One message, in the language [context] is configured for. */
    fun describe(context: Context): String = buildString {
        append(
            if (added == 0) {
                context.getString(R.string.import_stickers_none_added)
            } else {
                context.resources.getQuantityString(R.plurals.import_stickers_added, added, added)
            },
        )
        if (tooLarge > 0) {
            append(" ")
            append(
                context.resources.getQuantityString(
                    R.plurals.import_stickers_too_large,
                    tooLarge,
                    tooLarge,
                ),
            )
        }
        if (unreadable > 0) {
            append(" ")
            append(
                context.resources.getQuantityString(
                    R.plurals.import_stickers_unreadable,
                    unreadable,
                    unreadable,
                ),
            )
        }
        if (packFull) append(" ").append(context.getString(R.string.import_sticker_pack_now_full))
    }
}

@Composable
internal fun NameDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(stringResource(R.string.import_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = value.isNotBlank()) {
                Text(stringResource(CommonR.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.common_cancel)) }
        },
    )
}

/** [BackgroundTasks] keys for the Sticker packs screen's file work (#1522). */

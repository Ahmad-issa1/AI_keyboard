package com.wasimaster.wmkeyboard.app

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FileOpen
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Folder
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.notify.BackgroundTasks
import com.wasimaster.wmkeyboard.core.notify.TaskState
import com.wasimaster.wmkeyboard.core.stickers.StickerImportResult
import com.wasimaster.wmkeyboard.core.stickers.StickerPack
import com.wasimaster.wmkeyboard.core.stickers.StickerPackAdoption
import com.wasimaster.wmkeyboard.core.stickers.StickerPackStore
import com.wasimaster.wmkeyboard.core.stickers.whatsapp.WaStickersFile
import com.wasimaster.wmkeyboard.core.util.readBytesCapped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The WhatsApp stickers screen. */
internal const val WHATSAPP_STICKERS_ROUTE = "whatsapp_stickers"

/**
 * Where WhatsApp keeps every sticker that was sent or received on this phone,
 * relative to the primary shared storage. It sits under `Android/media` rather
 * than `Android/data`, which is what makes it readable at all: since Android
 * 11 the document picker refuses `Android/data` and `Android/obb`, and
 * `Android/media` is the folder WhatsApp moved its media to for exactly this
 * reason. The folder carries a `.nomedia`, so the photo picker the pack page
 * uses never lists these files; the document picker does.
 */
internal const val WHATSAPP_STICKER_FOLDER = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers"

@Composable
internal fun WhatsAppStickersScreen(onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}


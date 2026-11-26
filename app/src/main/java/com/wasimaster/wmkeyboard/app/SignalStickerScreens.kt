package com.wasimaster.wmkeyboard.app

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.OpenInNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.content.R as ContentR
import com.wasimaster.wmkeyboard.core.addons.ImportLink
import com.wasimaster.wmkeyboard.core.addons.SignalStickerDownloads
import com.wasimaster.wmkeyboard.core.settings.MeteredDecision
import com.wasimaster.wmkeyboard.core.stickers.ApngFrames
import com.wasimaster.wmkeyboard.core.notify.BackgroundTasks
import com.wasimaster.wmkeyboard.core.notify.TaskState
import com.wasimaster.wmkeyboard.core.stickers.StickerImportResult
import com.wasimaster.wmkeyboard.core.stickers.StickerPack
import com.wasimaster.wmkeyboard.core.stickers.StickerPackStore
import com.wasimaster.wmkeyboard.core.stickers.signal.SignalPackManifest
import com.wasimaster.wmkeyboard.core.tools.ToolHttp
import com.wasimaster.wmkeyboard.ime.ui.rememberMediaImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** The Signal sticker packs screen. */
internal const val SIGNAL_STICKERS_ROUTE = "signal_stickers"

/**
 * The community's gallery of Signal packs. Opened in the browser and never
 * read by the app: its terms ask that it is not built into other software, and
 * Signal and Molly send their users to it the same way.
 */
internal fun signalPackRoute(packId: String, packKey: String): String = "signal_pack/$packId/$packKey"

/**
 * Signal sticker packs: what a Signal or Molly user taps "Install" on, brought
 * into the user's own packs.
 *
 * Signal has no sticker shop. A pack is public to whoever holds its link, and
 * the link is all there is: `signal.art/addstickers/#pack_id=…&pack_key=…`. So
 * the way in is a field to paste one into. The field starts empty and is never
 * filled from the clipboard, for the reason the link importer gives: this app
 * is a keyboard, and reading the clipboard to be helpful is the one thing it
 * must not do on its own.
 *
 * There is no list of packs of the app's own choosing, on purpose. The packs
 * Signal ships with are art Signal commissioned, and its terms keep every right
 * to it, so a keyboard has no business offering them. A pack gets here because
 * whoever holds its link brought it, like any picture they add to a pack.
 */
@Composable
internal fun SignalStickersScreen(onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SignalPackScreen(
    packId: String,
    packKey: String,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    // Disabled in custom slim build
}


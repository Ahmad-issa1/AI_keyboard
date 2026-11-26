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
import com.wasimaster.wmkeyboard.BuildConfig
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.content.R as ContentR
import com.wasimaster.wmkeyboard.core.addons.ImportLink
import com.wasimaster.wmkeyboard.core.addons.TelegramStickerDownloads
import com.wasimaster.wmkeyboard.core.endpoints.ServiceEndpoint
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.OpenInNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Link
import com.wasimaster.wmkeyboard.core.settings.MeteredDecision
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.notify.BackgroundTasks
import com.wasimaster.wmkeyboard.core.notify.TaskState
import com.wasimaster.wmkeyboard.core.stickers.StickerImportResult
import com.wasimaster.wmkeyboard.core.stickers.StickerPack
import com.wasimaster.wmkeyboard.core.stickers.StickerPackStore
import com.wasimaster.wmkeyboard.core.stickers.telegram.TelegramSticker
import com.wasimaster.wmkeyboard.core.stickers.telegram.TelegramStickerFormat
import com.wasimaster.wmkeyboard.core.stickers.telegram.TelegramStickerSet
import com.wasimaster.wmkeyboard.core.stickers.telegram.TgsFrames
import com.wasimaster.wmkeyboard.core.tools.ToolHttp
import com.wasimaster.wmkeyboard.ime.ui.rememberMediaImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** The Telegram sticker sets screen. */
internal const val TELEGRAM_STICKERS_ROUTE = "telegram_stickers"

/** The bot that makes bots, where a token comes from. */
internal fun telegramPackRoute(setName: String): String = "telegram_pack/$setName"

/**
 * Telegram sticker sets, brought into the user's own packs.
 *
 * Telegram has no anonymous way to a set the way Signal's sticker storage is
 * one. The Bot API is the door that needs no Telegram account, and it needs a
 * bot, so the screen is the token first and the link second. A bot is free,
 * the user makes it with @BotFather, and the token is theirs: there is no
 * built-in one, because a token shared by every install would be a token
 * every install could have revoked.
 *
 * Like the Signal screen, the link field starts empty and is never filled
 * from the clipboard, and there is no list of sets of the app's own choosing.
 */
@Composable
internal fun TelegramStickersScreen(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun TelegramPackScreen(
    setName: String,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    // Disabled in custom slim build
}


package com.wasimaster.wmkeyboard.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.tools.offlinegif.OfflineGifPacks

/**
 * The GIF tool's packs on the device: CleverKeys' offline GIF packs and packs
 * of the user's own GIF and WebP files, which the panel offers under
 * **Offline** with no network and no API key. Each pack installed has a
 * Delete; the row under them fetches more (in the browser) and imports them,
 * a zip of pictures or loose pictures included.
 */
@Composable
internal fun OfflineGifPacksGroup() {
    // Disabled in custom slim build
}


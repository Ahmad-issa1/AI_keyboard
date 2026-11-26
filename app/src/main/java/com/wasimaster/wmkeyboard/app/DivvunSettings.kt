package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.InternetPermission
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.dictionaries.DivvunSpellerCatalog
import com.wasimaster.wmkeyboard.core.dictionaries.DivvunSpellerDownloads
import com.wasimaster.wmkeyboard.core.dictionaries.DivvunSpellerDownloads.ImportResult
import com.wasimaster.wmkeyboard.core.dictionaries.DivvunSpellerDownloads.Status
import com.wasimaster.wmkeyboard.core.modules.ModuleState
import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import com.wasimaster.wmkeyboard.core.prediction.DivvunRuntime
import com.wasimaster.wmkeyboard.core.settings.MeteredDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Whether [langId]'s Dictionaries group carries [SpellerRow]. */
internal fun offersSpeller(langId: String): Boolean = DivvunSpellerCatalog.offers(langId)

/**
 * The GiellaLT speller row on a language's Dictionaries group, for a
 * language GiellaLT builds a speller for. Download fetches the pinned
 * release where the catalog has one; Import takes a `.bhfst`, `.zhfst` or a
 * whole `.pkt.tar.zst` the user fetched (GiellaLT's nightly releases, for
 * the languages without a versioned one). Both need the spelling library,
 * so until it is on the device the row carries the module banner instead,
 * with its own Download. Nothing here is fetched from the keyboard.
 */
@Composable
internal fun SpellerRow(langId: String, settings: LiveSettings) {
    // Disabled in custom slim build
}


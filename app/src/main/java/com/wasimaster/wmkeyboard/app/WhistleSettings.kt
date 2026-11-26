package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.InternetPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Close
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.settings.MeteredDecision
import com.wasimaster.wmkeyboard.core.voice.whistle.WhistleEngine
import com.wasimaster.wmkeyboard.core.voice.whistle.WhistleModelDownloadManager
import com.wasimaster.wmkeyboard.core.voice.whistle.WhistleModelDownloadManager.Status
import com.wasimaster.wmkeyboard.core.voice.whistle.WhistleModelStore

/**
 * The one Whistle model (#548, first drafted by @Guillermode20): download,
 * progress, delete. Whistle has no catalog and no per-language routing, so
 * this is a single row where [WhisperModelManager] is a whole screen.
 * Shown only while Whistle is the engine and the device can run it.
 */
@Composable
internal fun WhistleModelManager(settings: LiveSettings) {
    // Disabled in custom slim build
}


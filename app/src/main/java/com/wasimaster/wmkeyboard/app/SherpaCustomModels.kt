package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.voice.executorch.EtCustomModels
import com.wasimaster.wmkeyboard.core.voice.executorch.EtEngine
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The models the user brought to [engine] (#1005 for Sherpa, #1437 for
 * Qwen3-ASR): what is here, the import from files picked on the phone, and the
 * one from a Hugging Face repository.
 */
@Composable
internal fun EtCustomModelsGroup(engine: EtEngine, settings: LiveSettings) {
    // Disabled in custom slim build
}

@Composable
internal fun <T> ModelImportDialog(
    settings: LiveSettings,
    title: String,
    picked: List<Uri>,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    onBusy: (Boolean) -> Unit,
    install: (files: List<File>, name: String, languages: Set<String>) -> T,
    onDone: (T?) -> Unit,
) {
    // Disabled in custom slim build
}


package com.wasimaster.wmkeyboard.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.plugins.PluginActionSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginManifestCodec
import com.wasimaster.wmkeyboard.core.plugins.PluginProviderSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginSettingSpec
import com.wasimaster.wmkeyboard.core.plugins.PythonTooling
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import com.wasimaster.wmkeyboard.common.R as CommonR

private val listJson = Json { ignoreUnknownKeys = true }

/** A list of manifest rows that survives the dialog being recreated, kept as its JSON. */
@Composable
internal fun <T> rememberSaveableList(initial: List<T>, serializer: KSerializer<T>): MutableState<List<T>> {
    val list = ListSerializer(serializer)
    return rememberSaveable(
        saver = Saver(
            save = { listJson.encodeToString(list, it.value) },
            restore = { mutableStateOf(runCatching { listJson.decodeFromString(list, it) }.getOrDefault(initial)) },
        ),
    ) { mutableStateOf(initial) }
}

/** A heading over a group of rows, with an Add button. */
@Composable
internal fun ProviderFields(providers: List<PluginProviderSpec>, onChange: (List<PluginProviderSpec>) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun ActionFields(actions: List<PluginActionSpec>, onChange: (List<PluginActionSpec>) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SettingFields(settings: List<PluginSettingSpec>, onChange: (List<PluginSettingSpec>) -> Unit) {
    // Disabled in custom slim build
}


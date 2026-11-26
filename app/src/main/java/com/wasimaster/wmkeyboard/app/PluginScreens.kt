package com.wasimaster.wmkeyboard.app

import com.wasimaster.wmkeyboard.core.netlog.OfflineBuild
import com.wasimaster.wmkeyboard.core.util.requireOutputStream
import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowForward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Block
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.CheckCircle
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Code
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Preview
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.DeleteSweep
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Handyman
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.MoreVert
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PowerSettingsNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Settings
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ToggleOn
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Widgets
import com.wasimaster.wmkeyboard.core.settings.PluginToolPlace
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.settings.providerChain
import com.wasimaster.wmkeyboard.core.settings.pluginToolPlaceIn
import com.wasimaster.wmkeyboard.ime.ui.PluginIcon
import com.wasimaster.wmkeyboard.ime.ui.toolColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.addons.AddonType
import com.wasimaster.wmkeyboard.core.plugins.PluginFile
import com.wasimaster.wmkeyboard.core.plugins.PluginImportResult
import com.wasimaster.wmkeyboard.core.plugins.PluginLog
import com.wasimaster.wmkeyboard.core.plugins.PluginManifestResult
import com.wasimaster.wmkeyboard.core.plugins.ParkedPlugin
import com.wasimaster.wmkeyboard.core.plugins.InstalledPlugin
import com.wasimaster.wmkeyboard.core.plugins.displayDescription
import com.wasimaster.wmkeyboard.core.plugins.displayName
import com.wasimaster.wmkeyboard.core.plugins.label
import com.wasimaster.wmkeyboard.core.plugins.PluginPermission
import com.wasimaster.wmkeyboard.core.plugins.PluginSecrets
import com.wasimaster.wmkeyboard.core.plugins.PluginSettingSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginReminderAlarms
import com.wasimaster.wmkeyboard.core.plugins.PluginReminders
import com.wasimaster.wmkeyboard.core.plugins.PluginStorage
import com.wasimaster.wmkeyboard.core.plugins.PluginStore
import com.wasimaster.wmkeyboard.core.plugins.PluginWorkspace
import com.wasimaster.wmkeyboard.core.plugins.PythonNative
import com.wasimaster.wmkeyboard.core.plugins.resolve
import com.wasimaster.wmkeyboard.core.notify.WmNotifications
import com.wasimaster.wmkeyboard.core.util.requireInputStream
import com.wasimaster.wmkeyboard.ime.ui.pluginActionIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.ime.R as ImeR

/**
 * One plugin's page, as flights name it.
 *
 * The navigation route with its argument filled in, which is what a flight is
 * keyed on: the pattern (`plugin/{pluginId}`) is the same string for every
 * plugin and would hang one key on all of them.
 */
internal fun pluginRoute(pluginId: String): String = "plugin/$pluginId"

/**
 * Managing plugins: the master switch, what is installed, and what each one is
 * allowed to do.
 *
 * The whole subsystem is **off until asked for**. Nothing installs and nothing
 * runs before the user turns it on, and the explainer beside the switch says in
 * plain words what a plugin can and cannot reach — which is a short list,
 * because the sandbox has no API for reading text, the clipboard or the network.
 */
@Composable
internal fun PluginsScreen(repository: SettingsRepository, settings: LiveSettings, onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}

internal fun pluginCombinationWarning(
    context: android.content.Context,
    permissions: List<PluginPermission>,
    hosts: List<String>,
): String? {
    if (!PluginPermission.combinationRisk(permissions)) return null
    return if (PluginPermission.Network in permissions && hosts.isNotEmpty()) {
        context.getString(com.wasimaster.wmkeyboard.plugins.R.string.core_plugins_permission_combination_warning, hosts.joinToString(", "))
    } else {
        context.getString(com.wasimaster.wmkeyboard.plugins.R.string.core_plugins_permission_combination_ai_warning)
    }
}

/**
 * What a plugin can and cannot do, built from [PluginPermission.all] so the
 * list cannot drift from what the sandbox enforces: what every plugin may do,
 * what one may do if it lists it before install, what waits for the user's
 * switch, and what none may do whatever it lists.
 */
@Composable
internal fun PluginDetailScreen(
    pluginId: String,
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    // Disabled in custom slim build
}


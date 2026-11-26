package com.wasimaster.wmkeyboard.app.homeassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.app.BannerTone
import com.wasimaster.wmkeyboard.app.ChoiceDetail
import com.wasimaster.wmkeyboard.app.ChoiceSetting
import com.wasimaster.wmkeyboard.app.LiveSettings
import com.wasimaster.wmkeyboard.app.NavRow
import com.wasimaster.wmkeyboard.app.ProvidersGroup
import com.wasimaster.wmkeyboard.app.ReorderableColumn
import com.wasimaster.wmkeyboard.app.SettingLevel
import com.wasimaster.wmkeyboard.app.SettingsGroup
import com.wasimaster.wmkeyboard.app.StateBanner
import com.wasimaster.wmkeyboard.app.SwipeAction
import com.wasimaster.wmkeyboard.app.SwipeActionsRow
import com.wasimaster.wmkeyboard.app.SwipeTone
import com.wasimaster.wmkeyboard.app.ToggleSetting
import com.wasimaster.wmkeyboard.core.homeassistant.HaDomains
import com.wasimaster.wmkeyboard.core.homeassistant.HaInsertFormat
import com.wasimaster.wmkeyboard.core.homeassistant.HaPhase
import com.wasimaster.wmkeyboard.core.homeassistant.HaStateFormat
import com.wasimaster.wmkeyboard.core.homeassistant.backend.HomeRef
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VisibilityOff
import com.wasimaster.wmkeyboard.core.providers.ProviderTool
import com.wasimaster.wmkeyboard.core.settings.HaLinkLifetime
import com.wasimaster.wmkeyboard.core.settings.HaOpenOn
import com.wasimaster.wmkeyboard.core.settings.HaTileSize
import com.wasimaster.wmkeyboard.core.settings.HomeAssistantSettings
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.settings.ToolbarTool
import com.wasimaster.wmkeyboard.core.settings.assistHome
import com.wasimaster.wmkeyboard.core.settings.homes
import com.wasimaster.wmkeyboard.ime.homeassistant.HomeStatus
import com.wasimaster.wmkeyboard.ime.homeassistant.SmartHomeHub
import kotlinx.coroutines.launch

/**
 * The smart home tool's own page: the switch, the homes as a provider list,
 * then how the panel lays itself out, Assist (with a Home Assistant on the
 * list), and how long the connections are held. The hub is fed the settings
 * here too, so the banners read the real connections even when the keyboard
 * has not run since the last change.
 */
@Composable
@Suppress("detekt.LongMethod", "detekt.CyclomaticComplexMethod")
internal fun SmartHomeToolSettings(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Suppress("detekt.LongMethod", "detekt.CyclomaticComplexMethod")
internal fun HaEntitiesScreen(repository: SettingsRepository, settings: LiveSettings) {
    // Disabled in custom slim build
}

internal const val ENTITIES_ROUTE = "homeassistant/entities"

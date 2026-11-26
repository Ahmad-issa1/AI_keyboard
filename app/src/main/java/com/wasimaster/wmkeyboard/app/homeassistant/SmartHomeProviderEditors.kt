package com.wasimaster.wmkeyboard.app.homeassistant

import com.wasimaster.wmkeyboard.core.modules.ModuleState
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.app.StoredTextField
import com.wasimaster.wmkeyboard.app.findActivity
import com.wasimaster.wmkeyboard.app.moduleFailedText
import com.wasimaster.wmkeyboard.core.homeassistant.HaFound
import com.wasimaster.wmkeyboard.core.homeassistant.HaHttpException
import com.wasimaster.wmkeyboard.core.homeassistant.backend.HaConnection
import com.wasimaster.wmkeyboard.core.homeassistant.backend.HomeKeys
import com.wasimaster.wmkeyboard.core.homeassistant.backend.HomeKind
import com.wasimaster.wmkeyboard.core.providers.ProviderCredential
import com.wasimaster.wmkeyboard.core.providers.ProviderType
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.smarthome.hue.HueApi
import com.wasimaster.wmkeyboard.core.smarthome.hue.HueBridgeInfo
import com.wasimaster.wmkeyboard.core.smarthome.hue.HueUrl
import com.wasimaster.wmkeyboard.core.smarthome.tuya.TuyaRegions
import com.wasimaster.wmkeyboard.ime.homeassistant.HaAndroidMdns
import com.wasimaster.wmkeyboard.ime.homeassistant.HomeBackends
import com.wasimaster.wmkeyboard.ime.homeassistant.HueAndroidMdns
import com.wasimaster.wmkeyboard.ime.homeassistant.googlehome.GoogleHomeSupport
import com.wasimaster.wmkeyboard.ime.wmpass.WmPassBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * What the provider editor shows under its plain fields for a home: the
 * server found on the LAN, the sign-in or the pairing, the test. Each kind
 * has its own way in, and each way in ends with the credential saved
 * through [onSave], which is the editor's own Save: the sign-ins need the
 * row stored before the browser comes back.
 */
@Composable
internal fun HomeProviderExtras(
    repository: SettingsRepository,
    type: ProviderType,
    /** What WM Pass calls this home's token, for the WM Pass button on it. */
    keyTitle: String,
    draft: ProviderCredential,
    isNew: Boolean,
    onDraft: (ProviderCredential) -> Unit,
    onSave: (ProviderCredential) -> Unit,
    onDismiss: () -> Unit,
) {
    // Disabled in custom slim build
}

internal fun homeReady(type: ProviderType, draft: ProviderCredential): Boolean = when (HomeKind.of(type.id)) {
    HomeKind.HOME_ASSISTANT -> draft.url.isNotBlank()
    HomeKind.HUE -> draft.url.isNotBlank() && draft.apiKey.isNotBlank()
    HomeKind.OPENHAB -> draft.url.isNotBlank()
    HomeKind.TUYA -> draft.url.isNotBlank() && draft.apiKey.isNotBlank() && !draft.extra[HomeKeys.CLIENT_ID].isNullOrBlank()
    HomeKind.SWITCHBOT -> draft.apiKey.isNotBlank() && !draft.extra[HomeKeys.SECRET].isNullOrBlank()
    HomeKind.SMARTTHINGS, HomeKind.GOOGLE_HOME, null -> true
}

// ---- Home Assistant ----------------------------------------------------------


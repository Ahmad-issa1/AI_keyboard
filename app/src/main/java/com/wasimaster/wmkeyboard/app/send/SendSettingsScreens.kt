package com.wasimaster.wmkeyboard.app.send

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.app.ActionRow
import com.wasimaster.wmkeyboard.app.BannerTone
import com.wasimaster.wmkeyboard.app.ChoiceSetting
import com.wasimaster.wmkeyboard.app.LiveSettings
import com.wasimaster.wmkeyboard.app.NavRow
import com.wasimaster.wmkeyboard.app.PermissionDisclosures
import com.wasimaster.wmkeyboard.app.SettingLevel
import com.wasimaster.wmkeyboard.app.SettingsGroup
import com.wasimaster.wmkeyboard.app.StateBanner
import com.wasimaster.wmkeyboard.app.SwipeAction
import com.wasimaster.wmkeyboard.app.SwipeActionsRow
import com.wasimaster.wmkeyboard.app.TextFieldSetting
import com.wasimaster.wmkeyboard.core.send.PairDropClient
import com.wasimaster.wmkeyboard.app.ToggleSetting
import com.wasimaster.wmkeyboard.app.deleteSwipeAction
import com.wasimaster.wmkeyboard.app.rememberDisclosedPermissionRequest
import com.wasimaster.wmkeyboard.app.toolRoute
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.OpenInNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.net.InternetGate
import com.wasimaster.wmkeyboard.core.net.NetLogInterceptor
import com.wasimaster.wmkeyboard.core.netlog.NetSource
import com.wasimaster.wmkeyboard.core.send.SendClient
import com.wasimaster.wmkeyboard.core.send.SendError
import com.wasimaster.wmkeyboard.core.send.SendException
import com.wasimaster.wmkeyboard.core.send.SendInfo
import com.wasimaster.wmkeyboard.core.send.SendInstance
import com.wasimaster.wmkeyboard.core.send.SendInstances
import com.wasimaster.wmkeyboard.core.send.SendServerConfig
import com.wasimaster.wmkeyboard.core.send.SendStore
import com.wasimaster.wmkeyboard.core.send.SendUpload
import com.wasimaster.wmkeyboard.core.settings.SendAfterUpload
import com.wasimaster.wmkeyboard.core.settings.SendPasswordMode
import com.wasimaster.wmkeyboard.core.settings.SendSettings
import com.wasimaster.wmkeyboard.core.settings.SettingsDefaults
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.settings.ToolbarTool
import com.wasimaster.wmkeyboard.ime.send.DirectWifi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The Send files tool's page (#729): the instance, the defaults a new link
 * starts from, receiving, Nearby, and the share sheet. Under the common
 * header rows the tool page frame draws.
 */
@Composable
internal fun SendToolSettings(repository: SettingsRepository, settings: LiveSettings, onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun SendInstancesScreen(repository: SettingsRepository, settings: LiveSettings) {
    // Disabled in custom slim build
}

@Composable
internal fun SendHistoryScreen(repository: SettingsRepository, settings: LiveSettings) {
    // Disabled in custom slim build
}

internal fun shortDuration(context: Context, seconds: Long): String {
    val format = android.icu.text.MeasureFormat.getInstance(
        context.resources.configuration.locales[0],
        android.icu.text.MeasureFormat.FormatWidth.SHORT,
    )
    val (value, unit) = when {
        seconds >= 2 * 86_400 -> seconds / 86_400 to android.icu.util.MeasureUnit.DAY
        seconds >= 3_600 -> seconds / 3_600 to android.icu.util.MeasureUnit.HOUR
        seconds >= 60 -> seconds / 60 to android.icu.util.MeasureUnit.MINUTE
        else -> seconds.coerceAtLeast(0) to android.icu.util.MeasureUnit.SECOND
    }
    return format.format(android.icu.util.Measure(value, unit))
}

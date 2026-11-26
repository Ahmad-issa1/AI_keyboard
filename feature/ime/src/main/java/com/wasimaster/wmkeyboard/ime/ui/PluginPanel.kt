package com.wasimaster.wmkeyboard.ime.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Handyman
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.MoreVert
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PowerSettingsNew
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Settings
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Widgets
import com.wasimaster.wmkeyboard.core.settings.PluginToolPlace
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.settings.pluginToolPlaceIn
import com.wasimaster.wmkeyboard.core.ui.SwipeAction
import com.wasimaster.wmkeyboard.core.ui.SwipeTone
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.plugins.InstalledPlugin
import com.wasimaster.wmkeyboard.core.plugins.PluginEvent
import com.wasimaster.wmkeyboard.core.plugins.PluginPermission
import com.wasimaster.wmkeyboard.core.plugins.PluginStore
import com.wasimaster.wmkeyboard.core.plugins.PluginWidget
import com.wasimaster.wmkeyboard.core.plugins.PythonSession
import com.wasimaster.wmkeyboard.core.plugins.displayDescription
import com.wasimaster.wmkeyboard.core.plugins.displayName
import com.wasimaster.wmkeyboard.core.plugins.resolve
import com.wasimaster.wmkeyboard.core.plugins.ui.LocalPluginPanelStyle
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginInputHost
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginOverlayBox
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginPanelStyle
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginWidgetList
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ChevronRight
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Extension
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Info
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Public
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tune
import com.wasimaster.wmkeyboard.core.theme.FrostSurface
import com.wasimaster.wmkeyboard.ime.FocusRegion
import com.wasimaster.wmkeyboard.ime.KeyboardUiState
import com.wasimaster.wmkeyboard.ime.PanelMode
import com.wasimaster.wmkeyboard.ime.PluginPanelUi
import com.wasimaster.wmkeyboard.ime.R
import com.wasimaster.wmkeyboard.ime.ui.send.QrImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Plugins panel: the installed list, and whichever plugin is running.
 *
 * Everything a plugin draws is drawn *here*, by the keyboard, from a plain data
 * description the script handed back. The script never gets a Compose handle, a
 * colour, or a pixel — which is why a plugin cannot make itself look like the
 * keyboard's own UI, cannot draw outside its panel, and cannot cover a system
 * prompt. The header above it always says which plugin is running.
 *
 * Text inputs are the delicate part. There are no `TextField`s anywhere in the
 * IME (they fight the `InputConnection`), so a plugin's input is a tap target
 * that, once focused, makes the keyboard route keystrokes into
 * [KeyboardUiState.pluginInputs] instead of the user's app. The service owns
 * that switch and clears it whenever the panel closes.
 */
@Composable
internal fun PluginPanel(
    state: KeyboardUiState,
    onOpenPlugin: (String) -> Unit,
    onEvent: (PluginEvent) -> Unit,
    onInputFocus: (String?) -> Unit,
    onToolInsert: (String) -> Unit,
    onCopy: (String) -> Unit,
    onPaste: (String) -> Unit,
    /** Opens a settings route: `plugins` for Manage, `plugin/<id>` for one plugin's page. */
    onOpenRoute: (String) -> Unit,
) {
    // Disabled in custom slim build
}

internal const val ALLOW_EVENT = "wm.allow"
internal const val DENY_EVENT = "wm.deny"

/**
 * A call needed a risky permission the user has not turned on. Asked the way
 * Android asks for one: a dialog over the plugin with its icon and name, what
 * it wants and what that lets it do, and Allow or Don't allow. Drawn by the
 * keyboard, never by the plugin, and over the whole panel so it is answered
 * before the plugin can be used further.
 */
internal fun PluginPanelUi.Running.showsAccessMark(): Boolean =
    plugin.grants(PluginPermission.Network) && PluginPermission.Network in plugin.declaredPermissions ||
        plugin.grants(PluginPermission.FieldRead) && PluginPermission.FieldRead in plugin.declaredPermissions ||
        plugin.grants(PluginPermission.Ai) && PluginPermission.Ai in plugin.declaredPermissions

/**
 * The header's right end while a plugin runs: the access mark, then the
 * buttons the plugin asked for with `@plugin.command`. The keyboard draws
 * them, three at most, as it draws its own header buttons.
 */
@Composable
internal fun PluginHeaderActions(panel: PluginPanelUi.Running, onEvent: (PluginEvent) -> Unit) {
    // Disabled in custom slim build
}

internal fun keyboardPluginStyle(
    kb: KbTheme,
    pluginId: String? = null,
    store: PluginStore? = null,
    /** Plugin grammars for `ui.code`, on top of the keyboard's own colours. */
    grammars: ((String) -> com.wasimaster.wmkeyboard.core.plugins.PluginSyntax.Grammar?)? = null,
    /** The font a file preview of a font file draws `ui.font_sample` in. */
    previewFont: androidx.compose.ui.text.font.FontFamily? = null,
): PluginPanelStyle =
    PluginPanelStyle(
        text = kb.keyText,
        secondaryText = kb.secondaryText,
        surface = kb.chip,
        onSurface = kb.chipText,
        accent = kb.accent,
        cardShape = kb.cardShape(),
        cardBorder = { modifier, shape -> modifier.chipBorder(kb, shape) },
        ring = { modifier, active -> modifier.focusRing(active) },
        accentContainer = kb.chipActive,
        onAccentContainer = kb.chipActiveText,
        buttonShape = kb.chipShape(),
        glass = { modifier, shape -> modifier.frostGlass(kb, shape, FrostSurface.CHIPS) },
        chip = { label, selected, enabled, modifier, onClick ->
            ToolPanelChip(label = label, selected = selected, modifier = modifier, enabled = enabled, onClick = onClick)
        },
        icon = { name, tint, size -> Icon(pluginActionIcon(name), contentDescription = null, tint = tint, modifier = Modifier.size(size)) },
        image = { src, alt, modifier -> PluginAssetImage(src, alt, modifier, pluginId, store, kb.secondaryText) },
        qr = { value, size -> QrImage(value, Modifier.size(size)) },
        highlight = { code, language -> highlightWith(code, language, kb.dark, grammars?.invoke(language)) },
        previewFont = previewFont,
    )

/**
 * A picture from the plugin's own `assets/`, read off the main thread. A web
 * address is not fetched: the panel would reach a server the user never
 * allowed, so its alt text stands in.
 */
@Composable
private fun PluginAssetImage(src: String, alt: String, modifier: Modifier, pluginId: String?, store: PluginStore?, muted: Color) {
    val name = src.removePrefix("assets/").takeIf { src.startsWith("assets/") }
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(null, pluginId, name) {
        value = if (pluginId == null || store == null || name == null) null else withContext(Dispatchers.IO) {
            store.asset(pluginId, name)?.let { bytes ->
                runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    val picture = bitmap
    if (picture != null) {
        Image(picture, alt, modifier = modifier, contentScale = ContentScale.Fit)
    } else {
        Text(alt, color = muted, fontSize = 12.sp, modifier = modifier)
    }
}

/**
 * A plugin's text boxes as the keyboard hosts them: the text is what the service
 * collected, pressing a box hands it the keys, and there is no text field, so
 * [onValueChange] is null.
 */
internal object PluginPanelScroll {
    val list = androidx.compose.foundation.ScrollState(0)

    private val open = HashMap<String, androidx.compose.foundation.ScrollState>()

    fun running(pluginId: String): androidx.compose.foundation.ScrollState =
        open.getOrPut(pluginId) { androidx.compose.foundation.ScrollState(0) }

    /** The list is showing: the next plugin opened from it starts at its top. */
    fun forgetRunning() = open.clear()
}

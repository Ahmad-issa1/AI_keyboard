package com.wasimaster.wmkeyboard.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Redo
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Undo
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.WrapText
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentPaste
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.focus.FocusRequester
import com.wasimaster.wmkeyboard.core.plugins.PluginFile
import com.wasimaster.wmkeyboard.core.util.requireInputStream
import com.wasimaster.wmkeyboard.core.util.requireOutputStream
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowBack
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowForward
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.AutoFixHigh
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.BookmarkAdd
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.DataObject
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.DriveFileRenameOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FindReplace
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FormatListNumbered
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.History
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Keyboard
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.MoreVert
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.PlayArrow
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Publish
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.SaveAlt
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Stop
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.TextDecrease
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.TextIncrease
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Tune
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.UnfoldLess
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.plugins.PluginDraft
import com.wasimaster.wmkeyboard.core.plugins.PluginEvent
import com.wasimaster.wmkeyboard.core.plugins.PluginManifest
import com.wasimaster.wmkeyboard.core.plugins.PluginManifestCodec
import com.wasimaster.wmkeyboard.core.plugins.PluginPermission
import com.wasimaster.wmkeyboard.core.plugins.PluginPreviewSession
import com.wasimaster.wmkeyboard.core.plugins.PluginRuntime
import com.wasimaster.wmkeyboard.core.plugins.PluginSnapshot
import com.wasimaster.wmkeyboard.core.plugins.PluginStorage
import com.wasimaster.wmkeyboard.core.plugins.PluginStore
import com.wasimaster.wmkeyboard.core.plugins.PluginWorkspace
import com.wasimaster.wmkeyboard.core.plugins.PluginActionSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginProviderSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginSettingSpec
import com.wasimaster.wmkeyboard.core.plugins.PluginTryRunner
import com.wasimaster.wmkeyboard.ime.PluginProviderBridge
import com.wasimaster.wmkeyboard.core.plugins.PyHostShape
import com.wasimaster.wmkeyboard.core.plugins.PyOutlineItem
import com.wasimaster.wmkeyboard.core.plugins.PythonNative
import com.wasimaster.wmkeyboard.core.plugins.PythonTooling
import com.wasimaster.wmkeyboard.core.plugins.resolve
import com.wasimaster.wmkeyboard.core.plugins.ui.LocalPluginPanelStyle
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginInputHost
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginPanelStyle
import com.wasimaster.wmkeyboard.core.plugins.ui.PluginWidgetList
import java.text.NumberFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How long typing has to pause before the draft is written. */
@Composable
internal fun PluginIdeProjectsScreen(onNavigate: (String) -> Unit) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PluginIdeScreen(draftId: String, onBack: () -> Unit, reduceMotion: Boolean = false) {
    // Disabled in custom slim build
}

@Composable
internal fun rememberMaterialPluginStyle(): PluginPanelStyle {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    return remember(colors, shape) {
        PluginPanelStyle(
            text = colors.onSurface,
            secondaryText = colors.onSurfaceVariant,
            surface = colors.surfaceContainerHigh,
            onSurface = colors.onSurface,
            accent = colors.primary,
            cardShape = shape,
            cardBorder = { modifier, _ -> modifier },
            ring = { modifier, _ -> modifier },
            chip = { label, selected, enabled, modifier, onClick ->
                FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, enabled = enabled, modifier = modifier)
            },
            danger = colors.error,
            icon = { name, tint, size ->
                Icon(
                    com.wasimaster.wmkeyboard.ime.ui.pluginActionIcon(name),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size),
                )
            },
            accentContainer = colors.primary,
            onAccentContainer = colors.onPrimary,
            buttonShape = CircleShape,
        )
    }
}

/** A preview's inputs are real fields: typing is an input event, and Paste reads the clipboard. */

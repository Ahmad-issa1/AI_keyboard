package com.wasimaster.wmkeyboard.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.ArrowBack
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Redo
import com.wasimaster.wmkeyboard.core.icons.symbols.automirrored.outlined.Undo
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentCopy
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ContentPaste
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FileDownload
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.FileUpload
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Keyboard
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.input.composer.DoublePinyin
import com.wasimaster.wmkeyboard.core.input.composer.DoublePinyinProfile
import com.wasimaster.wmkeyboard.core.input.composer.DoublePinyinScheme
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.util.readTextCapped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The route of [DoublePinyinSchemeScreen]. */
internal const val DOUBLE_PINYIN_CUSTOM_ROUTE = "double_pinyin_custom"

/** The file name an export suggests: fcitx's own name for a scheme file. */
internal val DoublePinyinStartSchemes: List<DoublePinyinScheme> = listOf(
    DoublePinyinScheme.MICROSOFT,
    DoublePinyinScheme.XIAOHE,
    DoublePinyinScheme.ZIRANMA,
    DoublePinyinScheme.PINYINPP,
)

/** [scheme]'s key table as editable text, or null for one with no table of its own. */
internal fun doublePinyinStartText(scheme: DoublePinyinScheme, name: String): String? =
    DoublePinyin.tableFor(scheme)?.let { DoublePinyinProfile.write(it, name) }

/**
 * The user's own Double Pinyin scheme as text (#502), in the editor the layout
 * JSON screens use, with the window's whole height for it.
 *
 * The text is fcitx's `sp.dat` format (see [DoublePinyinProfile]), so a scheme
 * written for fcitx pastes or imports here as it is, and an export opens there.
 * Like the JSON screens it is a draft with an explicit Save: a half-written
 * table moves keys around under the user's fingers, and Back leaves the draft.
 * Saving also picks the scheme, since nobody writes one not to type with it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DoublePinyinSchemeScreen(
    repository: SettingsRepository,
    settings: LiveSettings,
    onBack: () -> Unit,
) {
    // Disabled in custom slim build
}


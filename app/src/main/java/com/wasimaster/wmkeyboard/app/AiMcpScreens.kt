package com.wasimaster.wmkeyboard.app

import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Add
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Edit
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Visibility
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.VisibilityOff
import com.wasimaster.wmkeyboard.core.settings.AiProvider
import com.wasimaster.wmkeyboard.core.settings.SettingsRepository
import com.wasimaster.wmkeyboard.core.tools.McpAuth
import com.wasimaster.wmkeyboard.core.tools.McpAuthState
import com.wasimaster.wmkeyboard.core.tools.McpCatalogEntry
import com.wasimaster.wmkeyboard.core.tools.McpCatalogStore
import com.wasimaster.wmkeyboard.core.tools.McpClient
import com.wasimaster.wmkeyboard.core.tools.McpConfigImport
import com.wasimaster.wmkeyboard.core.tools.McpHeader
import com.wasimaster.wmkeyboard.core.tools.McpServerSpec
import com.wasimaster.wmkeyboard.core.tools.McpTool
import com.wasimaster.wmkeyboard.core.tools.McpToolPolicy
import com.wasimaster.wmkeyboard.core.tools.McpTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---- MCP servers (#573) ----

/**
 * One MCP server's editor, as flights name it. The pattern
 * (`ai_mcp_edit/{serverId}`) is the same string for every server and would
 * hang one key on all of them; see [aiActionEditRoute].
 */
internal fun aiMcpEditRoute(serverId: String): String = "ai_mcp_edit/$serverId"

/**
 * Past this many tools on offer, the list warns. Every tool's name, description
 * and input schema go into every request, so thirty tools can cost more than
 * the text the user asked about.
 */
@Composable
internal fun AiMcpSettings(
    repository: SettingsRepository,
    settings: LiveSettings,
    onNavigate: (String) -> Unit,
) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AiMcpEditor(
    repository: SettingsRepository,
    settings: LiveSettings,
    serverId: String,
    onDone: () -> Unit,
) {
    // Disabled in custom slim build
}

@Composable
internal fun AiProviderHeadersGroup(
    repository: SettingsRepository,
    settings: LiveSettings,
    provider: AiProvider,
) {
    // Disabled in custom slim build
}

@Composable
internal fun ToolPolicyChoices(
    policy: McpToolPolicy,
    onPick: (McpToolPolicy) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Disabled in custom slim build
}

internal fun isPlainRemoteHttp(url: String): Boolean {
    val lower = url.trim().lowercase()
    if (!lower.startsWith("http://")) return false
    val authority = lower.removePrefix("http://")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('@')
    val host = if (authority.startsWith("[")) {
        authority.removePrefix("[").substringBefore(']')
    } else {
        authority.substringBefore(':')
    }
    return host.isNotEmpty() && !isLocalHost(host)
}

private fun isLocalHost(host: String): Boolean {
    if (host == "localhost" || host == "::1" || host.endsWith(".localhost") || host.endsWith(".local")) return true
    val parts = host.split('.').mapNotNull { it.toIntOrNull() }
    if (parts.size != 4) return false
    return when (parts[0]) {
        10, 127 -> true
        192 -> parts[1] == 168
        172 -> parts[1] in 16..31
        169 -> parts[1] == 254
        else -> false
    }
}

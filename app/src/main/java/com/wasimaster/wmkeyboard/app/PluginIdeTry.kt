package com.wasimaster.wmkeyboard.app

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.plugins.PluginManifest
import com.wasimaster.wmkeyboard.core.plugins.PluginPreviewSession
import com.wasimaster.wmkeyboard.core.plugins.PluginProviderRunner
import com.wasimaster.wmkeyboard.core.plugins.PluginTryRunner
import com.wasimaster.wmkeyboard.core.plugins.PluginWorkspace
import com.wasimaster.wmkeyboard.core.plugins.PythonTooling
import com.wasimaster.wmkeyboard.core.tools.PluginReplies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Something in plugin.json the Try pane can call: a provider, or a selection action. */
internal sealed interface TryTarget {
    val key: String

    data class Provider(val tool: String, val id: String, val name: String) : TryTarget {
        override val key: String get() = "provider:$tool/$id"
    }

    data class Action(val id: String, val name: String) : TryTarget {
        override val key: String get() = id
    }
}

/** What plugin.json offers to call, providers first. */
internal fun tryTargets(manifest: PluginManifest): List<TryTarget> =
    manifest.providers.map { TryTarget.Provider(it.tool, it.providerId, it.name.ifBlank { it.providerId }) } +
        manifest.actions.map { TryTarget.Action(it.id, it.name.ifBlank { it.id }) }

@Composable
internal fun TryPane(
    draftId: String,
    manifest: PluginManifest,
    current: () -> String,
    /** The entry script and the other modules a call uses, given the open file as typed. */
    inputs: (String) -> DraftRunInputs,
    workspace: PluginWorkspace,
    runner: PluginTryRunner,
) {
    // Disabled in custom slim build
}

internal fun providerVerdict(context: Context, tool: String, reply: JsonElement, request: String): String {
    if (reply is JsonNull) return context.getString(R.string.plugin_ide_try_nothing)
    val asked = runCatching { Json.parseToJsonElement(request) as? JsonObject }.getOrNull()
    fun field(name: String): String = (asked?.get(name) as? JsonPrimitive)?.contentOrNull.orEmpty()
    val found: Int? = runCatching {
        when (tool) {
            "search" -> if (field("kind") == "images") PluginReplies.images(reply).size else PluginReplies.webPage(reply, "").results.size
            "translate" -> if (PluginReplies.translation(reply) != null) 1 else 0
            "dictionary" -> PluginReplies.dictionary(reply, field("word")).size
            "synonyms" -> PluginReplies.synonymGroups(reply).sumOf { it.words.size }
            "vocab_autofill" -> if (PluginReplies.vocabWord(reply, field("word")) != null) 1 else 0
            "currency_fiat" -> if (PluginReplies.fiatRates(reply) != null) 1 else 0
            "currency_crypto" -> {
                val codes = (asked?.get("codes") as? kotlinx.serialization.json.JsonArray)
                    ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }?.toSet().orEmpty()
                PluginReplies.cryptoRates(reply, codes).size
            }
            "gif", "sticker" -> PluginReplies.media(reply, "try").size
            "ocr", "voice" -> if (PluginReplies.text(reply).isNullOrEmpty()) 0 else 1
            else -> null
        }
    }.getOrNull()
    return when (found) {
        null -> context.getString(R.string.plugin_ide_try_returned)
        0 -> context.getString(R.string.plugin_ide_try_unread)
        else -> context.resources.getQuantityString(R.plurals.plugin_ide_try_read, found, found)
    }
}

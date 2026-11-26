package com.wasimaster.wmkeyboard.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.core.plugins.PluginBudget
import com.wasimaster.wmkeyboard.core.plugins.PluginDates
import com.wasimaster.wmkeyboard.core.plugins.PluginEvent
import com.wasimaster.wmkeyboard.core.plugins.PluginStorage
import com.wasimaster.wmkeyboard.core.plugins.PluginTargets
import com.wasimaster.wmkeyboard.core.plugins.PythonSession
import com.wasimaster.wmkeyboard.core.plugins.PythonTooling
import java.text.NumberFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How many draws the sparkline beside the budget meter remembers. */
internal const val MAX_BUDGET_HISTORY = 20

/** An event the author can send by hand, for a widget the last draw had. */
@Immutable
internal data class InjectableEvent(val event: PluginEvent, val widgetEnabled: Boolean = true)

/**
 * Every event the last draw's widgets could send, in the order they came:
 * presses, switch flips, a pick of each page, and for each date the next day
 * (or the first date, when it has none) and a clear. A button that is turned
 * off is listed too, because a handler has to cope with an event the user
 * cannot send. Text boxes are typed into separately.
 */
internal fun injectableEvents(
    targets: PluginTargets,
    now: Long = System.currentTimeMillis(),
): List<InjectableEvent> = buildList {
    for (button in targets.buttons) add(InjectableEvent(PluginEvent.Click(button.id), button.enabled))
    for (toggle in targets.toggles) add(InjectableEvent(PluginEvent.ToggleChanged(toggle.id, !toggle.checked)))
    for (tabs in targets.tabs) {
        for (index in tabs.pages.indices) add(InjectableEvent(PluginEvent.TabSelected(tabs.id, index)))
    }
    for (date in targets.dates) {
        val next = date.value?.let { PluginDates.addDays(it, 1) } ?: PluginDates.initial(now, date.time)
        add(InjectableEvent(PluginEvent.DateChanged(date.id, next)))
        add(InjectableEvent(PluginEvent.DateChanged(date.id, null)))
    }
}

/** Each of [values] as a share of [limit], from 0 to 1. */
internal fun budgetShares(values: List<Long>, limit: Long): List<Float> =
    values.map { if (limit <= 0) 0f else (it.toFloat() / limit).coerceIn(0f, 1f) }

/** Whether a pause in typing starts a run: the switch is on, the text parses, and the last run did not have this text. */
internal fun shouldRunAsTyped(enabled: Boolean, text: String, lastRun: String?, parses: Boolean): Boolean =
    enabled && parses && text != lastRun

/**
 * The event an author built by hand, or null without an id. A switch is on for
 * `true`, a page is a number counting from 0, and a date is seconds since the
 * epoch, with an empty or unreadable value clearing it.
 */
internal fun customEvent(type: String, id: String, value: String): PluginEvent? {
    val name = id.trim()
    if (name.isEmpty()) return null
    return when (type) {
        "click" -> PluginEvent.Click(name)
        "toggle" -> PluginEvent.ToggleChanged(name, value.trim().equals("true", ignoreCase = true))
        "input_changed" -> PluginEvent.InputChanged(name, value)
        "tab_selected" -> PluginEvent.TabSelected(name, value.trim().toIntOrNull() ?: 0)
        "date_changed" -> PluginEvent.DateChanged(name, value.trim().toLongOrNull())
        else -> null
    }
}

/** The instructions the last draw used against its budget, with a sparkline of the draws before it. */
@Composable
internal fun BudgetMeter(usage: PluginBudget.Usage, history: List<Long>) {
    // Disabled in custom slim build
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EventsPane(targets: PluginTargets, earlier: List<PluginEvent>, onSend: (PluginEvent) -> Unit) {
    // Disabled in custom slim build
}

@Composable
internal fun StoragePane(storage: PluginStorage?, declared: Boolean, busy: Boolean, installed: PluginStorage? = null) {
    // Disabled in custom slim build
}


package com.wasimaster.wmkeyboard.ime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Checklist
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ErrorOutline
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.TravelExplore
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.ExpandMore
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Handyman
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Language
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Psychology
import com.wasimaster.wmkeyboard.core.icons.symbols.outlined.Search
import com.wasimaster.wmkeyboard.core.tools.AiToolActivity
import com.wasimaster.wmkeyboard.core.tools.AiToolApproval
import com.wasimaster.wmkeyboard.core.tools.AiTools
import com.wasimaster.wmkeyboard.ime.R
import kotlinx.serialization.json.Json

/*
 * How a chat answer came to be, over the answer (#882): what the model
 * reasoned, then each tool it called, as steps on one line that runs down
 * into the answer. Each step is folded to its header by default, since the
 * answer is what the user came for; a tap opens it.
 *
 * Shared by the keyboard's chat and the chat screen in the settings app, like
 * [ChatMarkdown] and [ChatToolUses], so colours and links come in rather than
 * off a theme. [ChatToolUses] stays the one-line record the AI panel's actions
 * draw over a result; this is the chat's longer form of it.
 */

/**
 * The steps before an answer. [reasoning] is every passage the model thought,
 * drawn as Markdown, maths included, when opened; [reasoningLive] says it is
 * still thinking. [uses] are its tool calls, oldest first, each showing the
 * arguments the model wrote and what came back once opened, with the pages a
 * search found. A call asking to be allowed shows its question open; a save
 * to the AI memory shows its line with [onUndoMemory]'s Undo.
 */
@Composable
fun ChatChain(
    reasoning: String,
    reasoningLive: Boolean,
    uses: List<AiToolActivity>,
    colors: ChatMarkdownColors,
    errorColor: Color,
    onOpen: (String) -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    /** The size the reasoning's Markdown is set in, the answer's own. */
    textSize: TextUnit = 13.sp,
    onApproval: ((AiToolApproval) -> Unit)? = null,
    animate: Boolean = true,
    onUndoMemory: ((Int) -> Unit)? = null,
) {
    // Disabled in custom slim build
}


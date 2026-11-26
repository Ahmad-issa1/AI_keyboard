package com.wasimaster.wmkeyboard.app

import android.text.format.Formatter
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.wasimaster.wmkeyboard.R
import com.wasimaster.wmkeyboard.common.R as CommonR
import com.wasimaster.wmkeyboard.core.localllm.NpuRuntime
import com.wasimaster.wmkeyboard.core.localllm.NpuSupport
import com.wasimaster.wmkeyboard.core.modules.FeatureModules
import com.wasimaster.wmkeyboard.core.modules.NativeConsent
import com.wasimaster.wmkeyboard.core.modules.NativeDelivery
import com.wasimaster.wmkeyboard.core.modules.NpuModelBuild
import com.wasimaster.wmkeyboard.core.modules.NpuModelDownloads
import com.wasimaster.wmkeyboard.core.settings.MeteredDecision
import java.io.File

/**
 * Compute for one single LiteRT model that has an NPU build for this phone's
 * chip (Whisper Tiny, search by meaning's text encoder): CPU or NPU. The
 * choice is the download: NPU fetches this chip's build (and the NPU runtime
 * that on-device AI uses, after Qualcomm's licence on a build that downloads
 * it), CPU deletes the build again. While it is on the phone the model runs
 * on the NPU, unless it failed there before ([failed]), when the CPU runs it
 * until the next app or system update gives the NPU another try.
 */
@Composable
internal fun NpuBuildChoice(
    @StringRes title: Int,
    info: String,
    build: NpuModelBuild,
    target: File,
    failed: Boolean,
    settings: LiveSettings,
    onStart: () -> Unit,
) {
    // Disabled in custom slim build
}


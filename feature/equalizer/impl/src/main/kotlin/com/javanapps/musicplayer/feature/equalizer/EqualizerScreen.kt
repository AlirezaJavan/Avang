package com.javanapps.musicplayer.feature.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerState
import com.javanapps.musicplayer.feature.equalizer.navigation.EqualizerRoute
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.equalizerScreen(onBack: () -> Unit = {}) {
    composable<EqualizerRoute> {
        EqualizerScreen(onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EqualizerScreen(
        state = state,
        onBack = onBack,
        onToggleEnabled = viewModel::setEnabled,
        onBandLevelChange = viewModel::setBandLevel,
        onApplyPreset = viewModel::applyPreset,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EqualizerScreen(
    state: EqualizerState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onApplyPreset: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(CoreUiR.string.core_ui_equalizer)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(CoreUiR.string.core_ui_back),
                    )
                }
            },
            actions = {
                if (state.initialized) {
                    Text(
                        text = stringResource(if (state.enabled) CoreUiR.string.core_ui_eq_on else CoreUiR.string.core_ui_eq_off),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 4.dp),
                        color = if (state.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            },
        )

        if (!state.initialized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(CoreUiR.string.core_ui_eq_not_available),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (state.presets.isNotEmpty()) {
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_eq_preset),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(state.presets) { index, preset ->
                            FilterChip(
                                selected = state.currentPreset == index,
                                onClick = { onApplyPreset(index) },
                                label = { Text(preset) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.bandLevels.forEachIndexed { band, level ->
                        val freq =
                            if (band < state.centerFrequencies.size) {
                                formatFrequency(state.centerFrequencies[band])
                            } else {
                                ""
                            }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(52.dp),
                        ) {
                            Text(
                                text = "${level / 100}dB",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color =
                                    if (state.enabled) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            VerticalSlider(
                                value = level.toFloat(),
                                onValueChange = { newLevel ->
                                    if (state.enabled) onBandLevelChange(band, newLevel.toInt())
                                },
                                valueRange = state.levelRangeMin.toFloat()..state.levelRangeMax.toFloat(),
                                enabled = state.enabled,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = freq,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            modifier =
                Modifier
                    .graphicsLayer { rotationZ = 270f }
                    .layout { measurable, constraints ->
                        val placeable =
                            measurable.measure(
                                Constraints(
                                    minWidth = constraints.minHeight,
                                    maxWidth = constraints.maxHeight,
                                    minHeight = constraints.minWidth,
                                    maxHeight = constraints.maxWidth,
                                ),
                            )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                x = -(placeable.width - placeable.height) / 2,
                                y = -(placeable.height - placeable.width) / 2,
                            )
                        }
                    },
        )
    }
}

private fun formatFrequency(millihertz: Int): String {
    val hz = millihertz / 1000
    return if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"
}

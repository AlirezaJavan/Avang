package com.javanapps.musicplayer.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.designsystem.component.GlassTopAppBar
import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.feature.settings.navigation.SettingsRoute
import dev.chrisbanes.haze.HazeState
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    hazeState: HazeState,
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onBack = onBack,
            hazeState = hazeState,
        )
    }
}

@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    hazeState: HazeState,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()

    SettingsScreen(
        userData = userData,
        hazeState = hazeState,
        onBack = onBack,
        onThemeChange = viewModel::setDarkThemeConfig,
        onDynamicColorChange = viewModel::setDynamicColor,
        onLanguageChange = viewModel::setLanguage,
    )
}

@Composable
internal fun SettingsScreen(
    userData: UserData?,
    hazeState: HazeState,
    onBack: () -> Unit,
    onThemeChange: (DarkThemeConfig) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        GlassTopAppBar(
            title = stringResource(CoreUiR.string.core_ui_settings),
            hazeState = hazeState,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(CoreUiR.string.core_ui_back),
                    )
                }
            },
        )

        userData?.let { data ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                SettingsSectionTitle(title = stringResource(CoreUiR.string.core_ui_appearance))

                SettingsGroup {
                    SettingsItem(
                        title = stringResource(CoreUiR.string.core_ui_theme),
                        icon = AppIcons.Palette,
                        subtitle =
                            when (data.darkThemeConfig) {
                                DarkThemeConfig.FOLLOW_SYSTEM -> stringResource(CoreUiR.string.core_ui_theme_system)
                                DarkThemeConfig.LIGHT -> stringResource(CoreUiR.string.core_ui_theme_light)
                                DarkThemeConfig.DARK -> stringResource(CoreUiR.string.core_ui_theme_dark)
                            },
                    ) {
                        val next =
                            when (data.darkThemeConfig) {
                                DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfig.LIGHT
                                DarkThemeConfig.LIGHT -> DarkThemeConfig.DARK
                                DarkThemeConfig.DARK -> DarkThemeConfig.FOLLOW_SYSTEM
                            }
                        onThemeChange(next)
                    }

                    SettingsToggleItem(
                        title = stringResource(CoreUiR.string.core_ui_dynamic_color),
                        icon = AppIcons.ColorLens,
                        checked = data.dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSectionTitle(title = stringResource(CoreUiR.string.core_ui_language))

                SettingsGroup {
                    SettingsLanguageItem(
                        currentLanguage = data.language,
                        onLanguageChange = onLanguageChange,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSectionTitle(title = stringResource(CoreUiR.string.core_ui_about))

                val context = LocalContext.current
                val packageInfo =
                    remember {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }

                SettingsGroup {
                    SettingsItem(
                        title = stringResource(CoreUiR.string.core_ui_version),
                        icon = Icons.Default.Info,
                        subtitle = packageInfo.versionName ?: "1.0.0",
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                .padding(horizontal = 16.dp, vertical = 4.dp),
        content = content,
    )
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsLanguageItem(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
) {
    Column {
        LanguageOption(
            title = stringResource(CoreUiR.string.core_ui_language_fa),
            selected = currentLanguage == "fa",
            onClick = { onLanguageChange("fa") },
        )
        LanguageOption(
            title = stringResource(CoreUiR.string.core_ui_language_en),
            selected = currentLanguage == "en",
            onClick = { onLanguageChange("en") },
        )
    }
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

package com.javanapps.musicplayer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.javanapps.musicplayer.core.designsystem.component.GlassBottomBar
import com.javanapps.musicplayer.core.designsystem.component.GlassScaffold
import com.javanapps.musicplayer.core.designsystem.theme.AuroraBackground
import com.javanapps.musicplayer.core.designsystem.theme.MusicPlayerTheme
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.ui.component.MiniPlayer
import com.javanapps.musicplayer.core.ui.util.rememberHapticFeedback
import com.javanapps.musicplayer.feature.player.navigation.PlayerRoute
import com.javanapps.musicplayer.feature.player.navigation.navigateToPlayer
import com.javanapps.musicplayer.navigation.AppNavHost
import com.javanapps.musicplayer.navigation.TopLevelDestination
import com.javanapps.musicplayer.ui.MainUiState
import com.javanapps.musicplayer.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var themeSettings by mutableStateOf(ThemeSettings())

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState ->
                        if (uiState is MainUiState.Success) {
                            themeSettings =
                                ThemeSettings(
                                    darkTheme = uiState.shouldUseDarkTheme(resources.configuration.isSystemInDarkTheme()),
                                    dynamicColor = uiState.shouldUseDynamicColor,
                                )

                            val currentLocales = AppCompatDelegate.getApplicationLocales()
                            if (currentLocales.isEmpty || currentLocales.toLanguageTags() != uiState.userData.language) {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(uiState.userData.language),
                                )
                            }
                        }
                    }.collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.shouldKeepSplashScreen()
        }

        enableEdgeToEdge()

        setContent {
            MusicPlayerTheme(
                darkTheme = themeSettings.darkTheme,
                dynamicColor = themeSettings.dynamicColor,
            ) {
                MusicPlayerApp(viewModel = viewModel)
            }
        }
    }
}

private data class ThemeSettings(
    val darkTheme: Boolean = true,
    val dynamicColor: Boolean = false,
)

private fun Configuration.isSystemInDarkTheme() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MusicPlayerApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }

    val playerStateFlow = viewModel.playerState
    val stableStateFlow =
        remember(playerStateFlow) {
            playerStateFlow
                .map { it.copy(currentPosition = 0) }
                .distinctUntilChanged()
        }
    val positionFlow =
        remember(playerStateFlow) {
            playerStateFlow
                .map { it.currentPosition }
                .distinctUntilChanged()
        }

    val playerState by stableStateFlow.collectAsStateWithLifecycle(PlayerState())
    val currentPositionState = positionFlow.collectAsStateWithLifecycle(0L)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val haptic = rememberHapticFeedback()

    val isOnPlayerScreen = currentDestination?.hasRoute(PlayerRoute::class) == true
    val showMiniPlayer = playerState.currentSong != null && !isOnPlayerScreen

    SharedTransitionLayout {
        GlassScaffold(hazeState = hazeState) {
            AuroraBackground(modifier = Modifier.fillMaxSize())
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    Column {
                        AnimatedVisibility(
                            visible = showMiniPlayer,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.clipToBounds(),
                        ) {
                            playerState.currentSong?.let { song ->
                                val progressLambda =
                                    remember(song.id, playerState.duration) {
                                        {
                                            val pos = currentPositionState.value
                                            if (playerState.duration > 0) {
                                                (pos.toFloat() / playerState.duration)
                                                    .coerceIn(0f, 1f)
                                            } else {
                                                0f
                                            }
                                        }
                                    }
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playerState.isPlaying,
                                    progress = progressLambda,
                                    onPlayPauseClick = {
                                        haptic()
                                        if (playerState.isPlaying) viewModel.pause() else viewModel.resume()
                                    },
                                    onClick = {
                                        haptic()
                                        navController.navigateToPlayer()
                                    },
                                    hazeState = hazeState,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedVisibility,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                            .pointerInput(Unit) {
                                                var totalDragX = 0f
                                                detectDragGestures(
                                                    onDragEnd = {
                                                        when {
                                                            totalDragX < -80 -> viewModel.skipToNext()
                                                            totalDragX > 80 -> viewModel.skipToPrevious()
                                                        }
                                                        totalDragX = 0f
                                                    },
                                                    onDragCancel = { totalDragX = 0f },
                                                    onDrag = { _, dragAmount -> totalDragX += dragAmount.x },
                                                )
                                            },
                                )
                            }
                        }
                        GlassBottomBar(
                            hazeState = hazeState,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        ) {
                            TopLevelDestination.entries.forEach { destination ->
                                val selected =
                                    currentDestination
                                        ?.hierarchy
                                        ?.any { it.hasRoute(destination.route::class) } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        haptic()
                                        navController.navigate(destination.route) {
                                            popUpTo(TopLevelDestination.LIBRARY.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = stringResource(destination.labelRes),
                                        )
                                    },
                                    label = { Text(stringResource(destination.labelRes)) },
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavHost(
                        navController = navController,
                        sharedTransitionScope = this@SharedTransitionLayout,
                    )
                }
            }
        }
    }
}

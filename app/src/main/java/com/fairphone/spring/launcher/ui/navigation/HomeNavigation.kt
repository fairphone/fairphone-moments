/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fairphone.spring.launcher.activity.LauncherSettingsActivity
import com.fairphone.spring.launcher.data.model.colors
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.component.AnimatedBackground
import com.fairphone.spring.launcher.ui.component.ScreenViewTracker
import com.fairphone.spring.launcher.ui.screen.home.HomeScreen
import com.fairphone.spring.launcher.ui.screen.home.HomeScreenViewModel
import com.fairphone.spring.launcher.ui.screen.mode.creator.CreateModeScreen
import com.fairphone.spring.launcher.ui.screen.mode.switcher.ModeSwitcherScreen
import com.fairphone.spring.launcher.ui.screen.mode.switcher.ModeSwitcherViewModel
import com.fairphone.spring.launcher.ui.screen.onboarding.OnBoardingScreen
import com.fairphone.spring.launcher.util.FairphoneWebViewScreen
import com.fairphone.spring.launcher.util.MOMENTS_DEMO_URL
import com.fairphone.spring.launcher.util.launchClockApp
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Home

@Serializable
object ModeSwitcher

@Serializable
object OnBoarding

@Serializable
object ModeCreator

@Serializable
object FairphoneDemoWebView

const val ENTER_EXIT_DURATION = 300
const val FADE_IN_DURATION = 200

@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController(),
    showEntryAnimation: Boolean,
    isContentVisible: Boolean
) {
    ScreenViewTracker(navController = navController)
    NavHost(
        navController = navController,
        startDestination = Home,
        enterTransition = { fadeIn(animationSpec = tween(FADE_IN_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(FADE_IN_DURATION)) }
    ) {
        val homeEnterTransition = if (showEntryAnimation) {
            expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(
                    durationMillis = ENTER_EXIT_DURATION
                )
            )
        } else {
            fadeIn(animationSpec = tween(FADE_IN_DURATION))
        }
        val homeExitTransition = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(
                durationMillis = ENTER_EXIT_DURATION
            )
        )

        composable<Home> {
            val viewModel: HomeScreenViewModel = koinViewModel()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

            if (screenState != null) {
                AnimatedVisibility(
                    visible = isContentVisible,
                    enter = homeEnterTransition,
                    exit = homeExitTransition,
                ) {
                    AnimatedBackground(
                        colors = screenState!!.activeProfile.colors(),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // A user-picked background photo for this Moment, if set, sits
                            // behind the content with a subtle scrim so text stays legible.
                            val backgroundPath = screenState!!.activeProfile.backgroundImagePath
                            if (backgroundPath.isNotEmpty()) {
                                AsyncImage(
                                    model = File(backgroundPath),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                )
                            }
                            HomeScreen(
                                isContentVisible = isContentVisible,
                                onModeSwitcherButtonClick = {
                                    if (screenState?.appUsageMode == UsageMode.ON_BOARDING) {
                                        navController.navigate(OnBoarding)
                                    } else {
                                        navController.navigate(ModeSwitcher)
                                    }
                                },
                                onDemoCardClick = {
                                    navController.navigate(FairphoneDemoWebView)
                                },
                                onTimeClick = {
                                    navController.context.launchClockApp()
                                },
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }


        composable<FairphoneDemoWebView> {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = homeEnterTransition,
                exit = homeExitTransition,
            ) {
                FairphoneWebViewScreen(
                    url = MOMENTS_DEMO_URL,
                    showCloseButton = true,
                    hideHeaderAndFooter = true,
                    disableUrlLoading = true,
                    onBackPressed = { navController.navigateUp() }
                )
            }
        }

        // Create new Mode
        composable<ModeSwitcher> {
            val viewModel: ModeSwitcherViewModel = koinViewModel()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            val homeEnterTransition = if (showEntryAnimation) {
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(
                        durationMillis = ENTER_EXIT_DURATION
                    )
                )
            } else {
                fadeIn(animationSpec = tween(FADE_IN_DURATION))
            }
            val homeExitTransition = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(
                    durationMillis = ENTER_EXIT_DURATION
                )
            )

            if (screenState != null) {
                AnimatedVisibility(
                    visible = isContentVisible,
                    enter = homeEnterTransition,
                    exit = homeExitTransition,
                ) {
                    AnimatedBackground(
                        colors = screenState!!.activeProfile.colors(),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ModeSwitcherScreen(
                            currentLauncherProfile = screenState!!.activeProfile,
                            profiles = screenState!!.profiles,
                            isMaxProfileCountReached = screenState!!.isMaxProfileCountReached,
                            onModeSettingsClick = {
                                viewModel.editActiveProfileSettings(it)
                                LauncherSettingsActivity.start(context)
                            },
                            onModeSelected = {
                                viewModel.updateActiveProfile(it)
                                navController.navigateUp()
                            },
                            onCancel = {
                                navController.navigateUp()
                            },
                            onCreateMomentClick = {
                                navController.navigate(ModeCreator)
                            }
                        )
                    }
                }
            }
        }

        composable<ModeCreator> {
            val context = LocalContext.current
            AnimatedVisibility(
                visible = isContentVisible,
                enter = homeEnterTransition,
                exit = homeExitTransition,
            ) {
                CreateModeScreen(
                    onCloseModeCreator = {
                        navController.popBackStack()
                    },
                    onModeCreated = {
                        navController.popBackStack()
                        LauncherSettingsActivity.start(context)
                    }
                )
            }
        }

        // Create new Mode
        composable<OnBoarding> {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = homeEnterTransition,
                exit = homeExitTransition,
            ) {
                OnBoardingScreen(
                    onBoardingClose = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}
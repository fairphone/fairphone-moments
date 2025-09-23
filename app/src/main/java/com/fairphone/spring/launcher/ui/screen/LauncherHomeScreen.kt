/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fairphone.spring.launcher.analytics.FirebaseAnalyticsService
import com.fairphone.spring.launcher.analytics.LocalAnalyticsService
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.colors
import com.fairphone.spring.launcher.ui.navigation.HomeNavigation
import com.fairphone.spring.launcher.ui.screen.home.HomeScreenViewModel
import com.fairphone.spring.launcher.ui.theme.LocalUseDarkTheme
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private const val SHOW_HOME_SCREEN_DELAY = 100L
private const val SHOW_ANIMATION_TIME = 1000L

@Composable
fun LauncherHomeScreen(
    isContentVisibleState: MutableState<Boolean>,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    /*
     * These two boolean flags control:
     * - Triggering and synchronization of a Compose animation.
     * - Dynamic switching of the UI background
     *  (for entry / exit animation, a transparent background is needed).
    */
    var showEntryAnimation by rememberSaveable { mutableStateOf(true) }
    var isContentVisible by rememberSaveable { isContentVisibleState }

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val useDarkTheme = when (screenState?.activeProfile?.colors()) {
        LauncherColors.Black -> true
        LauncherColors.Green -> true
        LauncherColors.White -> false
        else -> isSystemInDarkTheme()
    }

    val analyticsService = remember { FirebaseAnalyticsService(Firebase.analytics) }
    CompositionLocalProvider(LocalAnalyticsService provides analyticsService) {
        CompositionLocalProvider(LocalUseDarkTheme provides useDarkTheme) {
            SpringLauncherTheme(darkTheme = useDarkTheme) {


                LaunchedEffect(Unit) {
                    delay(SHOW_HOME_SCREEN_DELAY) // delay set to let the entry animation show properly
                    //onIsContentVisibleChange(true)
                    isContentVisibleState.value = true
                }
                LaunchedEffect(Unit) {
                    delay(SHOW_ANIMATION_TIME) // time within the entry animation can run
                    showEntryAnimation = false
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (showEntryAnimation || !isContentVisible)
                                androidx.compose.ui.graphics.Color.Transparent
                            else MaterialTheme.colorScheme.background
                        )
                ) {
                    HomeNavigation(
                        showEntryAnimation = showEntryAnimation,
                        isContentVisible = isContentVisible
                    )
                }
            }
        }
    }
}

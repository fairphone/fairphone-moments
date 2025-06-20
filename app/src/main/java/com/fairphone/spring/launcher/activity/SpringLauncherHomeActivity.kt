/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fairphone.spring.launcher.analytics.FirebaseAnalyticsService
import com.fairphone.spring.launcher.analytics.LocalAnalyticsService
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.colors
import com.fairphone.spring.launcher.ui.navigation.HomeNavigation
import com.fairphone.spring.launcher.ui.screen.home.HomeScreenViewModel
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val ON_FINISH_DELAY = 400L
private const val SHOW_HOME_SCREEN_DELAY = 100L
private const val SHOW_ANIMATION_TIME = 1000L

class SpringLauncherHomeActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SpringLauncherHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            context.startActivity(intent)
        }

        private var instance: SpringLauncherHomeActivity? = null

        fun stop() {
            Log.d(Constants.LOG_TAG, "Stopping SpringLauncherHomeActivity")
            instance?.finish()
        }
    }

    private val isContentVisibleState = mutableStateOf(false)

    private val homeScreenViewModel: HomeScreenViewModel by inject()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        instance = this

        setContent {

                val analyticsService = remember { FirebaseAnalyticsService(Firebase.analytics) }
                CompositionLocalProvider(LocalAnalyticsService provides analyticsService) {
                    val screenState by homeScreenViewModel.screenState.collectAsStateWithLifecycle()
                    val activeProfile = screenState?.activeProfile ?: return@CompositionLocalProvider


                    val useDarkTheme = when (activeProfile.colors()) {
                        LauncherColors.Black -> true
                        LauncherColors.Green -> true
                        LauncherColors.White -> false
                        else -> isSystemInDarkTheme()
                    }

                    SpringLauncherTheme(darkTheme = useDarkTheme) {
                    // TODO: Move compose code to a separate composable
                    /**
                     * These two boolean flags control:
                     * - Triggering and synchronization of a Compose animation.
                     * - Dynamic switching of the UI background
                     *  (for entry / exit animation, a transparent background is needed).
                     */
                    var showEntryAnimation by rememberSaveable { mutableStateOf(true) }
                    var isContentVisible by rememberSaveable { isContentVisibleState }
                    LaunchedEffect(Unit) {
                        delay(SHOW_HOME_SCREEN_DELAY) // delay set to let the entry animation show properly
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

    override fun finish() {
        finishWithDelay()
    }

    private fun finishWithDelay() {
        isContentVisibleState.value = false
        lifecycleScope.launch {
            delay(ON_FINISH_DELAY) // delay set to let the exit animation show properly
            super.finish()
        }
    }

    private val onBackInInvokedCallback: OnBackInvokedCallback = object : OnBackInvokedCallback {
        override fun onBackInvoked() {
            // ignore back button
        }
    }

    @SuppressLint("WrongConstant")
    override fun onResume() {
        super.onResume()
        onBackInvokedDispatcher.registerOnBackInvokedCallback(0, onBackInInvokedCallback)
        hideGestureBar()
    }

    override fun onPause() {
        super.onPause()
        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInInvokedCallback)
        showGestureBar()
    }

    private fun hideGestureBar() {
        window.insetsController?.apply {
            hide(android.view.WindowInsets.Type.navigationBars())
            systemBarsBehavior =
                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showGestureBar() {
        window.insetsController?.apply {
            show(android.view.WindowInsets.Type.navigationBars())
            systemBarsBehavior =
                android.view.WindowInsetsController.BEHAVIOR_DEFAULT
        }
    }
}

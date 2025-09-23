/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * A composable that changes the status bar and navigation bar appearance.
 * This should be used inside a Composable that needs to control the system bars,
 * for example, a full-screen image viewer or a custom theme screen.
 *
 * @param useDarkIcons A boolean to determine if the system bar icons should be dark.
 * Set to `true` for a light background (e.g., white status bar),
 * and `false` for a dark background (e.g., black status bar).
 */
@Composable
fun SystemUiController(useDarkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        // Use DisposableEffect to manage the lifecycle of the effect.
        // The effect will be applied when the composable enters the composition
        // and reverted when it leaves.
        LaunchedEffect(view, useDarkIcons) {
            (view.context as? ComponentActivity)?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = useDarkIcons
            }
        }
    }
}

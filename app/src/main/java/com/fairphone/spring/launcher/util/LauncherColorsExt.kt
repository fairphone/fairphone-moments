/*
 * Copyright (C) 2025-2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.ui.theme.LocalUseDarkTheme
import com.fairphone.spring.launcher.ui.theme.modeButtonColorStaticDarkWallpaper

fun LauncherColors.areStaticColors(): Boolean =
            this == LauncherColors.Black ||
            this == LauncherColors.White ||
            this == LauncherColors.Green ||
            this == LauncherColors.Blue

@Composable
fun LauncherColors.getButtonContainerColor(): Color {
    val isDark = LocalUseDarkTheme.current
    return if (areStaticColors() && isDark) {
        modeButtonColorStaticDarkWallpaper
    } else {
        MaterialTheme.colorScheme.surface
    }
}
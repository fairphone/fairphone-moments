/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.mode.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.ui.theme.backgroundShapeBackgroundDarkStart
import com.fairphone.spring.launcher.ui.theme.backgroundShapeBackgroundLightStart
import com.fairphone.spring.launcher.util.areStaticColors

@Composable
fun ChooseBackgroundExample(
    launcherColors: LauncherColors,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val newModifier: Modifier =
        if (launcherColors.areStaticColors()) {
            modifier.background(color = Color(launcherColors.rightColor))
        } else {
            val colors = arrayOf(
                0.0f to if (isSystemInDarkTheme()) backgroundShapeBackgroundDarkStart else backgroundShapeBackgroundLightStart,
                1f to Color(launcherColors.rightColor)
            )
            modifier.background(
                brush = Brush.linearGradient(
                    colorStops = colors,
                    start = Offset(600.0f, 400.0f),
                    end = Offset(1500.0f, Float.POSITIVE_INFINITY),
                )
            )
        }

    Box(modifier = newModifier.clickable { onClick() }) {

    }
}

@Composable
private fun ChooseBackgroundExample_Preview() {
    SpringLauncherTheme {
        ChooseBackgroundExample(launcherColors = LauncherColors.Green)
    }
}

@Composable
@FP6Preview()
private fun ChooseBackgroundExample_LightPreview() {
    ChooseBackgroundExample_Preview()
}

@Composable
@FP6PreviewDark()
private fun ChooseBackgroundExample_DarkPreview() {
    ChooseBackgroundExample_Preview()
}

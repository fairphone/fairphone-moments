/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

/**
 * Data class that represents the colors used in the launcher.
 *
 * @property rightColor The main color used in the launcher.
 * @property leftColor The secondary color used in the launcher.
 */
data class LauncherColors(
    val rightColor: Long,
    val leftColor: Long,
) {
    companion object {
        val Default = LauncherColors(rightColor = 0xB2FFBA63, leftColor = 0xB2C3D1D0)
        val Custom = LauncherColors(rightColor = 0xB2FACAC9, leftColor = 0xB2EBD1F8)
        val DeepFocus = LauncherColors(rightColor = 0xB282C9F1, leftColor = 0xB2CBCEEA)
        val Journey = LauncherColors(rightColor = 0xB2F7CAC9, leftColor = 0xB2E5D1F8)
        val Recharge = LauncherColors(rightColor = 0xB2D8FF4F, leftColor = 0xB2BBD9D6)
        val QualityTime = LauncherColors(rightColor = 0xB2C0AFFF, leftColor = 0xB2B0CCD8)
        val Green = LauncherColors(rightColor = 0xFF0B1410, leftColor = 0xFF14241E)
        val White = LauncherColors(rightColor = 0xFF9DA3AA, leftColor = 0xFFE0DEDC)
        val Black = LauncherColors(rightColor = 0xFF060505, leftColor = 0xFF191715)

        val All = listOf(
            Default,
            Custom,
            DeepFocus,
            Journey,
            Recharge,
            QualityTime,
            Green,
            White,
            Black
        )
    }
}


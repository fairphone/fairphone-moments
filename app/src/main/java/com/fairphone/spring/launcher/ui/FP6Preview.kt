/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "FP6",
    apiLevel = 35,
    widthDp = 372,
    heightDp = 828,
    showBackground = true,
    backgroundColor = 0xFFCED3DC,
    //showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "en",
)
annotation class FP6Preview


@Preview(
    name = "FP6",
    apiLevel = 35,
    widthDp = 372,
    heightDp = 828,
    showBackground = true,
    backgroundColor = 0xFF2C2927,
    //showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en",
)
annotation class FP6PreviewDark

@Preview(
    name = "FP6",
    apiLevel = 35,
    showBackground = true,
    backgroundColor = 0xFFCED3DC,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "en",
)
annotation class PreviewLight


@Preview(
    name = "FP6",
    apiLevel = 35,
    showBackground = true,
    backgroundColor = 0xFF2C2927,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en",
)
annotation class PreviewDark

@Preview(
    name = "FP6",
    apiLevel = 35,
    showBackground = true,
    backgroundColor = 0xFFCED3DC,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "en",
    fontScale = 2f
)
annotation class PreviewLightAugmentedFont


@Preview(
    name = "FP6",
    apiLevel = 35,
    showBackground = true,
    backgroundColor = 0xFF2C2927,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en",
    fontScale = 2f
)
annotation class PreviewDarkAugmentedFont
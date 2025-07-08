/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.protos.SoundSetting
import com.fairphone.spring.launcher.data.model.protos.UiMode

/**
 * Data Class used to build a new LauncherProfile.
 */
data class CreateLauncherProfile(
    val id: String,
    val name: String,
    val icon: String,
    val bgColor1: Long,
    val bgColor2: Long,
    val launcherProfileApps: List<LauncherProfileApp>,
    val allowedContacts: ContactType,
    val customContacts: List<String> = emptyList(),
    val repeatCallEnabled: Boolean,
    val wallpaperId: Int,
    val uiMode: UiMode,
    val blueLightFilterEnabled: Boolean,
    val grayScaleEnabled: Boolean,
    val soundSetting: SoundSetting,
    val batterySaverEnabled: Boolean,
    val reduceBrightnessEnabled: Boolean,
)

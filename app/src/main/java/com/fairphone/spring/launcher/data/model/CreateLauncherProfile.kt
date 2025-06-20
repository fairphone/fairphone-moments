/*
 * Copyright (C) 2026 FairPhone B.V.
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
import com.fairphone.spring.launcher.data.model.protos.launcherProfile

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

fun CreateLauncherProfile.toLauncherProfile() = launcherProfile {
    id = this@toLauncherProfile.id
    name = this@toLauncherProfile.name
    icon = this@toLauncherProfile.icon
    bgColor1 = this@toLauncherProfile.bgColor1
    bgColor2 = this@toLauncherProfile.bgColor2
    launcherProfileApps.addAll(this@toLauncherProfile.launcherProfileApps)
    allowedContacts = this@toLauncherProfile.allowedContacts
    customContacts.addAll(this@toLauncherProfile.customContacts)
    repeatCallEnabled = this@toLauncherProfile.repeatCallEnabled
    wallpaperId = this@toLauncherProfile.wallpaperId
    uiMode = this@toLauncherProfile.uiMode
    blueLightFilterEnabled = this@toLauncherProfile.blueLightFilterEnabled
    grayScaleEnabled = this@toLauncherProfile.grayScaleEnabled
    soundSetting = this@toLauncherProfile.soundSetting
    batterySaverEnabled = this@toLauncherProfile.batterySaverEnabled
    reduceBrightnessEnabled = this@toLauncherProfile.reduceBrightnessEnabled
    zenRuleId = ""
}

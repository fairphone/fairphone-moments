/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.launcherProfile
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.ZenNotificationManager
import java.util.UUID

/**
 * Use case to create a new launcher profile.
 */
class CreateLauncherProfileUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
    private val zenNotificationManager: ZenNotificationManager,
) : UseCase<CreateLauncherProfile, LauncherProfile>() {

    override suspend fun execute(params: CreateLauncherProfile): Result<LauncherProfile> {
        return try {
            // Create automatic zen rule
            val createdZenRuleId = zenNotificationManager.createAutomaticZenRule(params)

            // Create launcher profile
            val launcherProfile = launcherProfile {
                id = params.id
                name = params.name
                icon = params.icon
                bgColor1 = params.bgColor1
                bgColor2 = params.bgColor2
                launcherProfileApps.addAll(params.launcherProfileApps)
                allowedContacts = params.allowedContacts
                customContacts.addAll(params.customContacts)
                repeatCallEnabled = params.repeatCallEnabled
                wallpaperId = params.wallpaperId
                uiMode = params.uiMode
                blueLightFilterEnabled = params.blueLightFilterEnabled
                soundSetting = params.soundSetting
                batterySaverEnabled = params.batterySaverEnabled
                reduceBrightnessEnabled = params.reduceBrightnessEnabled
                zenRuleId = createdZenRuleId
            }

            launcherProfileRepository.createProfile(profile = launcherProfile)

            Result.success(launcherProfile)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        }
    }

    companion object {
        fun newId(): String =
            UUID.randomUUID().toString()
    }
}
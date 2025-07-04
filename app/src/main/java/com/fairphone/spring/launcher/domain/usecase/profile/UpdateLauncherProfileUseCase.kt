/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.DeviceSoundManager
import com.fairphone.spring.launcher.util.ZenNotificationManager

/**
 * Use case to update a launcher profile.
 */
class UpdateLauncherProfileUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
    private val zenNotificationManager: ZenNotificationManager,
    private val deviceSoundManager: DeviceSoundManager,
) : UseCase<LauncherProfile, LauncherProfile>() {

    override suspend fun execute(params: LauncherProfile): Result<LauncherProfile> {
        //deviceSoundManager.enableDeviceSoundSetting(soundSetting = params.soundSetting)
        val result = zenNotificationManager.updateAutomaticZenRule(profile = params)


        if (result.isSuccess) {
            launcherProfileRepository.updateProfile(profile = params)
            return Result.success(params)
        } else {
            return Result.failure(Exception("Failed to update zen rule"))
        }
    }
}
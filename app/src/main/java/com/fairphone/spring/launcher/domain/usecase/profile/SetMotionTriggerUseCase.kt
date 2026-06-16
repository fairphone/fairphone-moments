/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.MotionTrigger
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first

data class SetMotionTriggerParams(
    val profileId: String,
    val motionTrigger: MotionTrigger,
)

/**
 * Use case to update the motion (transport-mode) trigger of a Moment.
 */
class SetMotionTriggerUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
) : UseCase<SetMotionTriggerParams, LauncherProfile>() {

    override suspend fun execute(params: SetMotionTriggerParams): Result<LauncherProfile> {
        return try {
            val profile = launcherProfileRepository.getProfile(params.profileId).first()
            val updatedProfile = profile.toBuilder()
                .setMotionTrigger(params.motionTrigger)
                .build()
            launcherProfileRepository.updateProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

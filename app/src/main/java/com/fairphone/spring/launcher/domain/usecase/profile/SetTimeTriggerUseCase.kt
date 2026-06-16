/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.TimeTrigger
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first

data class SetTimeTriggerParams(
    val profileId: String,
    val timeTrigger: TimeTrigger,
)

/**
 * Use case to update the time/day trigger of a Moment.
 */
class SetTimeTriggerUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
) : UseCase<SetTimeTriggerParams, LauncherProfile>() {

    override suspend fun execute(params: SetTimeTriggerParams): Result<LauncherProfile> {
        return try {
            val profile = launcherProfileRepository.getProfile(params.profileId).first()
            val updatedProfile = profile.toBuilder()
                .setTimeTrigger(params.timeTrigger)
                .build()
            launcherProfileRepository.updateProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

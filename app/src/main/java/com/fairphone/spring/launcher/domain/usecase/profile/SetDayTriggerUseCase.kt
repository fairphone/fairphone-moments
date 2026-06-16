/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.DayTrigger
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first

data class SetDayTriggerParams(
    val profileId: String,
    val dayTrigger: DayTrigger,
)

/**
 * Use case to update the day-of-week trigger of a Moment.
 */
class SetDayTriggerUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
) : UseCase<SetDayTriggerParams, LauncherProfile>() {

    override suspend fun execute(params: SetDayTriggerParams): Result<LauncherProfile> {
        return try {
            val profile = launcherProfileRepository.getProfile(params.profileId).first()
            val updatedProfile = profile.toBuilder()
                .setDayTrigger(params.dayTrigger)
                .build()
            launcherProfileRepository.updateProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

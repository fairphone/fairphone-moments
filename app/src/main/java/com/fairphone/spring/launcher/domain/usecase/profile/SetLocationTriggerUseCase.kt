/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LocationTrigger
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first

data class SetLocationTriggerParams(
    val profileId: String,
    val locationTrigger: LocationTrigger,
)

/**
 * Use case to update the location trigger of a Moment and re-register
 * the proximity alerts with the system.
 */
class SetLocationTriggerUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
) : UseCase<SetLocationTriggerParams, LauncherProfile>() {

    override suspend fun execute(params: SetLocationTriggerParams): Result<LauncherProfile> {
        return try {
            val profile = launcherProfileRepository.getProfile(params.profileId).first()
            val updatedProfile = profile.toBuilder()
                .setLocationTrigger(params.locationTrigger)
                .build()
            launcherProfileRepository.updateProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

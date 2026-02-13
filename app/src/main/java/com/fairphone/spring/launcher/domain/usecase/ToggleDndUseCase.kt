/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase

import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.ZenNotificationManager
import kotlinx.coroutines.flow.firstOrNull

/**
 * [UseCase] to enable or disable Do Not Disturb mode for the active profile.
 */
class ToggleDndUseCase(
    private val zenNotificationManager: ZenNotificationManager,
    private val profileRepository: LauncherProfileRepository,
) : UseCase<Boolean, Unit>() {

    override suspend fun execute(params: Boolean): Result<Unit> {
        return try {
            if (params) {
                val activeProfile = profileRepository.getActiveProfile().firstOrNull()
                    ?: return Result.failure(Exception("No active profile found"))

                val result = zenNotificationManager.enableDnd(activeProfile)
                if (result.isSuccess) {
                    val zenRuleId = result.getOrThrow()
                    if (zenRuleId != activeProfile.zenRuleId) {
                        profileRepository.updateProfile(
                            activeProfile.toBuilder().setZenRuleId(zenRuleId).build()
                        )
                    }
                    return Result.success(Unit)
                } else {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } else {
                return zenNotificationManager.disableAllDnd()
            }
        } catch (e: IllegalStateException) {
            Result.failure(e)
        }
    }
}

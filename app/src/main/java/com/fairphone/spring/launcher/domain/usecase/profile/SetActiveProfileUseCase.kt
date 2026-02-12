/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.EnableDndUseCase
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first

class SetActiveProfileUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
    private val enableDnDUseCase: EnableDndUseCase,
) : UseCase<String, LauncherProfile>() {
    override suspend fun execute(params: String): Result<LauncherProfile> {
        return try {
            enableDnDUseCase.execute(false)
            val newActiveProfile = launcherProfileRepository.getProfile(params).first()
            launcherProfileRepository.setActiveProfile(params)
            enableDnDUseCase.execute(true)
            return Result.success(newActiveProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

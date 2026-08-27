/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2026. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import android.content.Context
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.isDoNotDisturbAccessGranted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs at every app entry point (Application.onCreate + retry-on-permission-grant from
 * activity viewmodels + every DND switch flip). Composes:
 *
 *  - [InitializeSpringLauncherUseCase]: seeds the default profile on first launch. Short-circuits
 *    otherwise.
 *  - [SweepOrphanZenRulesUseCase]: reconciles system zen rules with persisted profiles. Handles
 *    stale-rule and reverse-orphan states left by cleared app data or partial failures.
 *
 * Registered as a Koin `single` (deviation from the codebase's factory-scoped use case
 * convention) so both the [mutex] and the [bootstrapped] flag are shared across every call
 * site. Concurrent bootstraps from `App.onCreate`, `HomeScreenViewModel.init`, and
 * `SwitchStateChangeViewModel.handleDnd` would otherwise interleave `removeAllRules` with
 * `addAutomaticZenRule`, deleting a freshly-created zen rule mid-flight and leaving DND
 * stuck in STATE_FALSE after a switch flip. The mutex forces serialization; the flag turns
 * post-first-success calls into no-ops so rapid switch intents don't repeatedly hit the
 * `NotificationManager`. The flag is only set once DND permission is granted, preserving
 * the retry-on-permission-grant path.
 */
class BootstrapSpringLauncherUseCase(
    private val context: Context,
    private val initializeSpringLauncherUseCase: InitializeSpringLauncherUseCase,
    private val sweepOrphanZenRulesUseCase: SweepOrphanZenRulesUseCase,
) : UseCase<Unit, Unit>() {

    private val mutex = Mutex()
    private var bootstrapped = false

    override suspend fun execute(params: Unit): Result<Unit> = mutex.withLock {
        if (bootstrapped) return@withLock Result.success(Unit)
        val initResult = initializeSpringLauncherUseCase.execute(Unit)
        // Only reconcile when Init did not create the default profile. On success, Init
        // just cleared the system rules and created a fresh one - running Sweep here
        // would race the DataStore write's propagation through the profiles flow and
        // flag the brand-new rule as a forward orphan, deleting it.
        if (initResult.isFailure) {
            sweepOrphanZenRulesUseCase.execute(Unit)
        }
        if (context.isDoNotDisturbAccessGranted()) bootstrapped = true
        Result.success(Unit)
    }
}

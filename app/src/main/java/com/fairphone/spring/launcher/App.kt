/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.fairphone.spring.launcher.di.dataModule
import com.fairphone.spring.launcher.di.domainModule
import com.fairphone.spring.launcher.di.uiModule
import com.fairphone.spring.launcher.domain.usecase.profile.BootstrapSpringLauncherUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class App : Application(), KoinComponent {

    companion object {
        @VisibleForTesting
        val koinModules = listOf(
            uiModule,
            dataModule,
            domainModule,
        )
    }

    private val bootstrapSpringLauncherUseCase: BootstrapSpringLauncherUseCase by inject()
    private lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            androidLogger()
            modules(koinModules)
        }
        initApp(this)
    }

    private fun initApp(context: Context) {
        applicationScope = MainScope()
        applicationScope.launch {
            bootstrapSpringLauncherUseCase.execute(Unit)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.cancel()
        applicationScope = MainScope()
    }
}

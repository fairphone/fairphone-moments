/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fairphone.spring.launcher.ui.screen.settings.LauncherSettingsScreen
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme

class LauncherSettingsActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LauncherSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_OPEN,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        super.onCreate(savedInstanceState)
        setContent {
            SpringLauncherTheme {
                LauncherSettingsScreen(
                    onCloseSettings = { finish() }
                )
            }
        }
    }
}

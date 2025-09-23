/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fairphone.spring.launcher.activity.viewmodel.SwitchStateChangeViewModel
import com.fairphone.spring.launcher.data.model.SwitchState
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.ui.component.SwitchAnimationState
import com.fairphone.spring.launcher.ui.component.SwitchStateChangeOverlayScreen
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.Constants
import org.koin.android.ext.android.inject

class SwitchStateChangeActivity : ComponentActivity() {

    private val dndPermissionRationaleDialogActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleIntent(intent)
        }

    private val viewModel: SwitchStateChangeViewModel by inject()

    private var switchState: SwitchState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable blur behind
        setupWindow()
        setupWindowBlurListener()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        if (switchState == SwitchState.DISABLED) {
            SpringLauncherHomeActivity.stop()
        }
    }

    private fun handleIntent(intent: Intent) {
        if (!viewModel.isDndPermissionGranted(this)) {
            Log.d(Constants.LOG_TAG, "DND permission NOT granted")
            val intent = Intent(this, DoNotDisturbPermissionRationaleDialogActivity::class.java)
            dndPermissionRationaleDialogActivityLauncher.launch(intent)
            return
        } else {
            Log.d(Constants.LOG_TAG, "DND permission granted")
        }

        switchState = getSwitchState(intent) ?: run {
            Log.e(Constants.LOG_TAG, "Could not read switch state")
            finish()
            return
        }

        // handleDnd
        handleDnd(switchState!!)

        // Handle lockscreen wallpaper
        handleLockscreenWallpaper(switchState!!)

        if (shouldShowOverlay(intent)) {
            setContent {
                SpringLauncherTheme {
                    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

                    if (activeProfile != null) {
                        SwitchStateChangeScreen(
                            activeProfile = activeProfile!!,
                            switchButtonSwitchState = switchState!!,
                            onOverlayAnimationDone = {
                                onAnimationDone()
                            }
                        )
                    }
                }
            }
        } else {
            when (switchState) {
                SwitchState.ENABLED -> {
                    SpringLauncherHomeActivity.start(context = this)
                }

                SwitchState.DISABLED -> {
                    SpringLauncherHomeActivity.stop()
                }
                else -> {
                    // Do nothing
                }
            }
            onAnimationDone()
        }
    }

    private fun checkIfDndAccessIsEnabled() {

    }

    private fun handleDnd(switchState: SwitchState) {
        viewModel.handleDnd(this, switchState)
    }

    private fun handleLockscreenWallpaper(switchState: SwitchState) {
        viewModel.handleLockscreenWallpaper(this, switchState)
    }

    private fun onAnimationDone() {
        setResult(RESULT_OK)
        finish()
    }

    /**
     * Reads the switch state from the intent.
     */
    private fun getSwitchState(intent: Intent?): SwitchState? {
        val statusString = intent?.getStringExtra(Constants.EXTRA_SWITCH_BUTTON_STATE)
            ?: return null

        return try {
            SwitchState.valueOf(statusString)
        } catch (e: IllegalArgumentException) {
            Log.e(Constants.LOG_TAG, "Could not read switch state: $statusString", e)
            null
        }
    }

    /**
     * Checks if the overlay should be shown.
     */
    private fun shouldShowOverlay(intent: Intent?): Boolean {
        return intent?.getBooleanExtra(Constants.EXTRA_SHOW_OVERLAY, false) == true
    }

    private fun setupWindow() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            attributes.blurBehindRadius = 20
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupWindowBlurListener() {
        val windowBlurEnabledListener: java.util.function.Consumer<Boolean> =
            object : java.util.function.Consumer<Boolean> {
                override fun accept(value: Boolean) {
                    updateWindowForBlurs(value)
                }
            }
        window.decorView.addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                @Override
                override fun onViewAttachedToWindow(v: View) {
                    windowManager.addCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }

                @Override
                override fun onViewDetachedFromWindow(v: View) {
                    windowManager.removeCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }
            });
    }

    private fun updateWindowForBlurs(blursEnabled: Boolean) {
        window.apply {
            setDimAmount(if (blursEnabled) .1f else .4f)
            setBackgroundBlurRadius(80)
            attributes.blurBehindRadius = 10
            attributes = attributes
        }
    }
}

@Composable
fun SwitchStateChangeScreen(
    activeProfile: LauncherProfile,
    switchButtonSwitchState: SwitchState,
    onOverlayAnimationDone: () -> Unit
) {
    BlurBehindActivity {
        Box(modifier = Modifier.fillMaxSize()) {}
    }

    SwitchStateChangeOverlayScreen(
        profile = activeProfile,
        switchState = switchButtonSwitchState,
        onAnimationDone = { onOverlayAnimationDone() },
        visibilityState = MutableTransitionState(SwitchAnimationState.NOT_STARTED)
    )
}


@Composable
fun BlurBehindActivity(content: @Composable () -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            FrameLayout(context).apply {
                // Enable blur behind
                val window = (context as Activity).window
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes.blurBehindRadius = 10
            }
        },
        update = { frameLayout ->
            // No need to set content here, as we want the blur behind the activity
        }
    )
    content() // Render the Compose content on top of the blurred background
}

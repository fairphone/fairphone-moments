/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.util.MediaControlManager
import com.fairphone.spring.launcher.util.MediaState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MediaControlViewModel(
    private val mediaControlManager: MediaControlManager,
) : ViewModel() {

    val mediaState: StateFlow<MediaState?> =
        mediaControlManager.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun playPause() = mediaControlManager.playPause()
    fun next() = mediaControlManager.next()
    fun previous() = mediaControlManager.previous()
}

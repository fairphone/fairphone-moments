/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.fairphone.spring.launcher.service.MediaListenerService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** What's currently playing, surfaced to the home screen's media card. */
data class MediaState(
    val title: String,
    val artist: String?,
    val isPlaying: Boolean,
    val appName: String,
    val appIcon: ImageBitmap?,
    val albumArt: ImageBitmap?,
)

/**
 * Reads the device's active media session (via [MediaSessionManager], which needs an enabled
 * notification listener — see [MediaListenerService]) and exposes it as a live [MediaState],
 * plus transport controls. The chosen session is the one that's playing, else the most recent.
 * Everything is read on demand while the home screen observes it; nothing runs in the background.
 */
class MediaControlManager(
    private val context: Context,
) {
    private val sessionManager: MediaSessionManager? =
        context.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(context, MediaListenerService::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    // The session currently surfaced; transport actions act on it.
    @Volatile
    private var activeController: MediaController? = null

    /** Whether the user has granted notification access (required to read media sessions). */
    fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        return enabled.split(":").any {
            ComponentName.unflattenFromString(it)?.packageName == context.packageName
        }
    }

    fun playPause() {
        val controller = activeController ?: return
        val playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        if (playing) controller.transportControls.pause() else controller.transportControls.play()
    }

    fun next() = activeController?.transportControls?.skipToNext() ?: Unit

    fun previous() = activeController?.transportControls?.skipToPrevious() ?: Unit

    /**
     * Emits the current [MediaState] (or null when nothing is playing / access is missing) and
     * re-emits whenever the session, its metadata, or its playback state changes.
     */
    fun observe(): Flow<MediaState?> = callbackFlow {
        if (sessionManager == null || !hasNotificationAccess()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        var controller: MediaController? = null
        var callback: MediaController.Callback? = null

        fun detach() {
            callback?.let { cb -> controller?.unregisterCallback(cb) }
            callback = null
            controller = null
        }

        fun attach(controllers: List<MediaController>) {
            detach()
            val chosen = controllers.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers.firstOrNull()
            controller = chosen
            activeController = chosen
            if (chosen == null) {
                trySend(null)
                return
            }
            val cb = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    trySend(buildState(chosen))
                }

                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    trySend(buildState(chosen))
                }

                override fun onSessionDestroyed() {
                    trySend(null)
                }
            }
            callback = cb
            chosen.registerCallback(cb, mainHandler)
            trySend(buildState(chosen))
        }

        val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            attach(controllers ?: emptyList())
        }

        runCatching {
            sessionManager.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent)
            attach(sessionManager.getActiveSessions(listenerComponent))
        }.onFailure { trySend(null) }

        awaitClose {
            detach()
            activeController = null
            runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionsListener) }
        }
    }

    private fun buildState(controller: MediaController): MediaState? {
        val metadata = controller.metadata ?: return null
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: return null
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        val art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        return MediaState(
            title = title,
            artist = artist?.takeIf { it.isNotBlank() },
            isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING,
            appName = appLabel(controller.packageName),
            appIcon = appIcon(controller.packageName),
            albumArt = art?.asImageBitmap(),
        )
    }

    private fun appLabel(packageName: String): String = runCatching {
        val info = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)

    private fun appIcon(packageName: String): ImageBitmap? = runCatching {
        context.packageManager.getApplicationIcon(packageName)
            .toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap()
    }.getOrNull()
}

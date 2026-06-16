/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Persists a user-picked background image inside the app's own storage.
 *
 * The system photo picker ([androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia])
 * hands back a URI with only temporary read access, so we copy the bytes into `filesDir/backgrounds`
 * and keep the copy. No storage permission is involved. One image is kept per Moment, keyed by its id.
 */
class BackgroundImageStore(
    private val context: Context,
) {
    private val dir: File get() = File(context.filesDir, "backgrounds").apply { mkdirs() }

    /**
     * Copies [source] into app storage for [profileId] and returns the saved file path,
     * or null if the copy failed. Each save uses a fresh, timestamped filename and removes
     * the previous one — so the stored path actually changes, which makes the new image
     * take effect (the profile updates and the image loader isn't served a stale cache hit
     * for an unchanged path).
     */
    suspend fun save(profileId: String, source: Uri): String? = withContext(Dispatchers.IO) {
        val target = File(dir, "$profileId-${System.currentTimeMillis()}.jpg")
        runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            deleteFiles(profileId, except = target)
            target.absolutePath
        }.getOrNull()
    }

    /** Deletes the stored background image(s) for [profileId], if any. */
    fun delete(profileId: String) = deleteFiles(profileId)

    private fun deleteFiles(profileId: String, except: File? = null) {
        dir.listFiles()
            ?.filter { it != except && (it.name == "$profileId.jpg" || it.name.startsWith("$profileId-")) }
            ?.forEach { runCatching { it.delete() } }
    }
}

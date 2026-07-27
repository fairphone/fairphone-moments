/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class ContactInfo(
    override val id: String,
    override val name: String,
    override val icon: Any?,
    val contactUri: Uri,
): SelectableItem

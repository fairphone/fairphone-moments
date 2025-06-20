/*
 * Copyright (C) 2025-2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import com.fairphone.spring.launcher.data.model.LauncherColors

fun LauncherColors.areStaticColors(): Boolean =
            this == LauncherColors.Black ||
            this == LauncherColors.White ||
            this == LauncherColors.Green
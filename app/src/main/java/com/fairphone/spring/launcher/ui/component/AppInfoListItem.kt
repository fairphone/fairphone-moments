/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fairphone.spring.launcher.ui.PreviewDark
import com.fairphone.spring.launcher.ui.PreviewLight
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography

@Composable
fun AppInfoListItem(
    modifier: Modifier = Modifier,
    icon: Drawable? = null,
    name: String,
    isWorkApp: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .clip(RoundedCornerShape(size = 12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        icon?.let {
            AsyncImage(
                model = it,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(32.dp),
            )
        }

        Text(
            text = name,
            style = FairphoneTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        if (isWorkApp) {
            WorkAppBadge(modifier = Modifier.size(24.dp))
        }

        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = null,
            tint =  MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
fun AppInfoListItemPreview() {
    AppInfoListItem(name = "Chrome", isWorkApp = true)
}

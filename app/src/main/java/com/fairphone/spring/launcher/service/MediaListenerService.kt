/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.service

import android.service.notification.NotificationListenerService

/**
 * A no-op notification listener whose only purpose is to exist and be enabled by the user.
 *
 * Reading the device's active media sessions through [android.media.session.MediaSessionManager]
 * `getActiveSessions(ComponentName)` requires the caller to pass an enabled notification-listener
 * component. This service is that component — it never inspects, filters, or reposts any
 * notification (that stays the platform's job), it just grants Moments the hook it needs to show
 * media controls. Kept separate from any notification-interception feature on purpose.
 */
class MediaListenerService : NotificationListenerService()

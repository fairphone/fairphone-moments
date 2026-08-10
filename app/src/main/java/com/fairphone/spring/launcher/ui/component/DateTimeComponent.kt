/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.ui.PreviewDark
import com.fairphone.spring.launcher.ui.PreviewLight
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

@Composable
fun DateTime(dateTime: LocalDateTime, onTimeClick: () -> Unit) {
    val context = LocalContext.current
    val is24HourFormat = remember { DateFormat.is24HourFormat(context) }

    val (date, time) = remember(dateTime) {
        val localDate = dateTime.date
        val localTime = dateTime.time

        val dateFormat = LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            char(',')
            char(' ')
            day()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
        val timeFormat = if (is24HourFormat) {
            LocalTime.Format {
                hour(Padding.ZERO)
                char(':')
                minute(Padding.ZERO)
            }
        } else {
            LocalTime.Format {
                amPmHour()
                char(':')
                minute(Padding.ZERO)
            }
        }

        listOf(
            localDate.format(dateFormat),
            localTime.format(timeFormat),
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = time,
            style = FairphoneTypography.Time,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable(
                interactionSource = null,
                indication = null,
                onClick = onTimeClick,
            )
        )

        Text(
            text = date,
            style = FairphoneTypography.Date,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun Time_Preview() {
    SpringLauncherTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateTime(
                dateTime = LocalDateTime(2025, 2, 13, 14, 30)
            ) {}
        }
    }
}

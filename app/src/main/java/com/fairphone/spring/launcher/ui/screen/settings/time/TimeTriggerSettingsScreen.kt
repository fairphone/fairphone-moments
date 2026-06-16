/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.time

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.protos.DayTrigger
import com.fairphone.spring.launcher.data.model.protos.TimeTrigger
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.SettingListItem
import com.fairphone.spring.launcher.ui.component.SettingSwitchItem
import com.fairphone.spring.launcher.ui.theme.Color_FP_Brand_Lime
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TimeTriggerSettingsScreen(
    screenState: TimeTriggerSettingsScreenState,
    onDayEnabledChanged: (Boolean) -> Unit,
    onDayToggled: (Int) -> Unit,
    onTimeEnabledChanged: (Boolean) -> Unit,
    onStartTimeChanged: (Int) -> Unit,
    onEndTimeChanged: (Int) -> Unit,
) {
    when (screenState) {
        is TimeTriggerSettingsScreenState.Loading ->
            Box(modifier = Modifier.fillMaxSize())

        is TimeTriggerSettingsScreenState.Success ->
            TimeTriggerSettingsScreen(
                dayTrigger = screenState.dayTrigger,
                timeTrigger = screenState.timeTrigger,
                onDayEnabledChanged = onDayEnabledChanged,
                onDayToggled = onDayToggled,
                onTimeEnabledChanged = onTimeEnabledChanged,
                onStartTimeChanged = onStartTimeChanged,
                onEndTimeChanged = onEndTimeChanged,
            )
    }
}

@Composable
fun TimeTriggerSettingsScreen(
    dayTrigger: DayTrigger,
    timeTrigger: TimeTrigger,
    onDayEnabledChanged: (Boolean) -> Unit,
    onDayToggled: (Int) -> Unit,
    onTimeEnabledChanged: (Boolean) -> Unit,
    onStartTimeChanged: (Int) -> Unit,
    onEndTimeChanged: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.time_trigger_description),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp)
        )

        // --- Day axis (independent) ---
        SettingSwitchItem(
            state = dayTrigger.enabled,
            title = stringResource(R.string.day_trigger_enable_title),
            subtitle = stringResource(R.string.day_trigger_enable_subtitle),
            onClick = onDayEnabledChanged,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        if (dayTrigger.enabled) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // ISO day numbers: 1 = Monday … 7 = Sunday.
                (1..7).forEach { isoDay ->
                    DayButton(
                        label = dayInitial(isoDay),
                        selected = isoDay in dayTrigger.daysOfWeekList,
                        onClick = { onDayToggled(isoDay) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.time_trigger_days_hint),
                style = FairphoneTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // --- Time axis (independent) ---
        SettingSwitchItem(
            state = timeTrigger.enabled,
            title = stringResource(R.string.time_trigger_enable_title),
            subtitle = stringResource(R.string.time_trigger_enable_subtitle),
            onClick = onTimeEnabledChanged,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        if (timeTrigger.enabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                TimeRow(
                    title = stringResource(R.string.time_trigger_from),
                    minuteOfDay = timeTrigger.startMinuteOfDay,
                    onTimeSelected = onStartTimeChanged,
                )
                TimeRow(
                    title = stringResource(R.string.time_trigger_to),
                    minuteOfDay = timeTrigger.endMinuteOfDay,
                    onTimeSelected = onEndTimeChanged,
                )
            }
        }
    }
}

@Composable
private fun TimeRow(
    title: String,
    minuteOfDay: Int,
    onTimeSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    SettingListItem(
        title = title,
        subtitle = formatMinute(minuteOfDay),
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> onTimeSelected(hour * 60 + minute) },
                minuteOfDay / 60,
                minuteOfDay % 60,
                true,
            ).show()
        },
    )
}

@Composable
private fun DayButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) Color_FP_Brand_Lime else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color_FP_Brand_Lime else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = FairphoneTypography.BodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun dayInitial(isoDay: Int): String =
    DayOfWeek.of(isoDay).getDisplayName(TextStyle.NARROW, Locale.getDefault())

private fun formatMinute(minuteOfDay: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60)

@Composable
private fun TimeTriggerSettingsScreen_Preview() {
    SpringLauncherTheme {
        TimeTriggerSettingsScreen(
            screenState = TimeTriggerSettingsScreenState.Success(
                dayTrigger = DayTrigger.newBuilder()
                    .setEnabled(true)
                    .addAllDaysOfWeek(listOf(1, 2, 3, 4, 5))
                    .build(),
                timeTrigger = TimeTrigger.newBuilder()
                    .setEnabled(true)
                    .setStartMinuteOfDay(9 * 60)
                    .setEndMinuteOfDay(17 * 60)
                    .build(),
            ),
            onDayEnabledChanged = {},
            onDayToggled = {},
            onTimeEnabledChanged = {},
            onStartTimeChanged = {},
            onEndTimeChanged = {},
        )
    }
}

@Composable
@FP6Preview()
private fun TimeTriggerSettingsScreen_LightPreview() {
    TimeTriggerSettingsScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun TimeTriggerSettingsScreen_DarkPreview() {
    TimeTriggerSettingsScreen_Preview()
}

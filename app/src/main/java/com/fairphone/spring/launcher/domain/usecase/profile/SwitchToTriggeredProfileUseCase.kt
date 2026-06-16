/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.MotionMode
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.util.LocationTriggerManager
import com.fairphone.spring.launcher.util.MotionClassifier
import com.fairphone.spring.launcher.util.effectivePoints
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * Activates the Moment whose triggers match the current context, evaluated when the
 * hardware switch is flipped on. A Moment can be bound on up to four axes — place,
 * day, time, and motion (how you're moving) — and matches only when **all** of its
 * enabled conditions hold right now.
 *
 * A matched **location always wins**: being physically at a saved place is the strongest
 * signal of intent, so it overrides any number of day/time/motion conditions (walking
 * around at home/gym/library must not flip you into a "walking" Moment). Among location
 * matches the more specific one wins — most conditions, then closest. When no location
 * matches, the remaining axes (day, time, motion) are peers ranked by how many conditions
 * they satisfy. If nothing matches, the active Moment is left unchanged.
 */
class SwitchToTriggeredProfileUseCase(
    private val locationTriggerManager: LocationTriggerManager,
    private val motionClassifier: MotionClassifier,
    private val launcherProfileRepository: LauncherProfileRepository,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
) {
    suspend fun execute(now: LocalDateTime = LocalDateTime.now()): Result<Unit> {
        return try {
            val candidates = launcherProfileRepository.getProfiles().first()
                .filter {
                    it.locationTrigger.enabled || it.dayTrigger.enabled ||
                        it.timeTrigger.enabled || it.motionTrigger.enabled
                }
            if (candidates.isEmpty()) return Result.success(Unit)

            // Use the cached last-known fix so the switch stays instant — a fresh GPS fix
            // would block the Moment switch for several seconds.
            val location = if (candidates.any { it.locationTrigger.enabled }) {
                locationTriggerManager.getLastKnownLocation()
            } else {
                null
            }

            // Only sample the accelerometer when a candidate actually uses motion.
            // Motion is GPS-free: walking vs transport is read from the accelerometer alone.
            val motionMode = if (candidates.any { it.motionTrigger.enabled }) {
                motionClassifier.classify()
            } else {
                MotionMode.MOTION_MODE_UNSPECIFIED
            }

            val best = candidates
                .mapNotNull { profile -> evaluate(profile, location, motionMode, now) }
                .maxWithOrNull(
                    // Location match is the top priority, then most conditions, then closest.
                    compareBy<Match> { it.matchedLocation }
                        .thenBy { it.matchedConditions }
                        .thenByDescending { it.distanceMeters }
                )

            if (best != null) {
                val activeProfile = launcherProfileRepository.getActiveProfile().first()
                if (activeProfile.id != best.profileId) {
                    setActiveProfileUseCase.execute(best.profileId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a [Match] if every enabled condition on [profile] holds, else null.
     * [distanceMeters] is only meaningful (and used as a tie-breaker) for a location match.
     */
    private fun evaluate(
        profile: LauncherProfile,
        location: android.location.Location?,
        motionMode: MotionMode,
        now: LocalDateTime,
    ): Match? {
        var matchedConditions = 0
        var distanceMeters = Float.MAX_VALUE
        var matchedLocation = false

        if (profile.locationTrigger.enabled) {
            if (location == null) return null
            // Match if within any saved place; keep the closest matching distance for tie-breaks.
            val nearestMatch = profile.locationTrigger.effectivePoints()
                .map { point ->
                    locationTriggerManager.distanceMeters(location, point.latitude, point.longitude) to
                        point.radiusMeters
                }
                .filter { (distance, radius) -> distance <= radius }
                .minOfOrNull { (distance, _) -> distance }
                ?: return null
            matchedConditions++
            distanceMeters = nearestMatch
            matchedLocation = true
        }

        if (profile.dayTrigger.enabled) {
            if (!dayMatches(profile, now)) return null
            matchedConditions++
        }

        if (profile.timeTrigger.enabled) {
            if (!timeMatches(profile, now)) return null
            matchedConditions++
        }

        if (profile.motionTrigger.enabled) {
            if (!motionMatches(profile, motionMode)) return null
            matchedConditions++
        }

        if (matchedConditions == 0) return null
        return Match(profile.id, matchedConditions, distanceMeters, matchedLocation)
    }

    private fun motionMatches(profile: LauncherProfile, motionMode: MotionMode): Boolean {
        // Couldn't classify -> never matches (don't switch on a guess).
        if (motionMode == MotionMode.MOTION_MODE_UNSPECIFIED) return false
        val modes = profile.motionTrigger.modesList
        return if (modes.isEmpty()) {
            // Empty selection = any movement (not standing still).
            motionMode != MotionMode.MOTION_MODE_STILL
        } else {
            motionMode in modes
        }
    }

    private fun dayMatches(profile: LauncherProfile, now: LocalDateTime): Boolean {
        // Empty list means "every day".
        val days = profile.dayTrigger.daysOfWeekList
        return days.isEmpty() || now.dayOfWeek.value in days
    }

    private fun timeMatches(profile: LauncherProfile, now: LocalDateTime): Boolean {
        val trigger = profile.timeTrigger
        val nowMinute = now.hour * 60 + now.minute
        val start = trigger.startMinuteOfDay
        val end = trigger.endMinuteOfDay
        return when {
            start == end -> true // whole day
            start < end -> nowMinute in start..end
            else -> nowMinute >= start || nowMinute <= end // wraps past midnight
        }
    }

    private data class Match(
        val profileId: String,
        val matchedConditions: Int,
        val distanceMeters: Float,
        val matchedLocation: Boolean,
    )
}

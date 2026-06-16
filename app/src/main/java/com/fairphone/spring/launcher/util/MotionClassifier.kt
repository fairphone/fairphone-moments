/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import com.fairphone.spring.launcher.data.model.protos.MotionMode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.sqrt

/**
 * Classifies how the user is moving — still, walking or in transport — entirely on device
 * from the accelerometer alone. No GPS, no Google Play Services. It samples the accelerometer
 * for a short window when the hardware switch is flipped (never in the background).
 *
 * Walking has a distinctive signature: a steady step cadence (~1.3–2.6 Hz) and a clear
 * vertical bounce. Anything moving without that signature — bike, car, bus, train — is
 * TRANSPORT; near-stillness is STILL. Telling bike from car apart would need GPS speed, so
 * they are intentionally merged into one TRANSPORT class.
 *
 * The thresholds below are deliberately simple constants so they can be tuned against real
 * data.
 */
class MotionClassifier(
    private val context: Context,
) {
    /**
     * Samples the accelerometer and returns the detected [MotionMode].
     * Returns [MotionMode.MOTION_MODE_UNSPECIFIED] when no accelerometer is present.
     */
    suspend fun classify(): MotionMode {
        val samples = sampleAccelerationMagnitude() ?: return MotionMode.MOTION_MODE_UNSPECIFIED
        if (samples.size < MIN_SAMPLES) return MotionMode.MOTION_MODE_UNSPECIFIED

        val intensity = standardDeviation(samples) // m/s² of body motion (gravity cancels out)
        val cadenceHz = dominantCadenceHz(samples)

        return decide(intensity, cadenceHz)
    }

    /** Pure decision function, separated so it can be reasoned about and tuned in isolation. */
    private fun decide(intensity: Float, cadenceHz: Float): MotionMode = when {
        intensity < STILL_MAX_INTENSITY -> MotionMode.MOTION_MODE_STILL
        cadenceHz in WALK_CADENCE_MIN..WALK_CADENCE_MAX && intensity > WALK_MIN_INTENSITY ->
            MotionMode.MOTION_MODE_WALKING
        else -> MotionMode.MOTION_MODE_TRANSPORT
    }

    /**
     * Collects the magnitude of the acceleration vector over [WINDOW_MILLIS]. Sensor callbacks
     * are delivered on a dedicated background thread so this never touches the main looper.
     * Returns null when the device has no accelerometer.
     */
    private suspend fun sampleAccelerationMagnitude(): FloatArray? {
        val sensorManager = context.getSystemService(SensorManager::class.java) ?: return null
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return null

        val thread = HandlerThread("motion-sampler").apply { start() }
        val handler = Handler(thread.looper)
        val readings = ArrayList<Float>(WINDOW_MILLIS / 20)

        return try {
            suspendCancellableCoroutine { continuation ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        synchronized(readings) { readings.add(sqrt(x * x + y * y + z * z)) }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(
                    listener,
                    sensor,
                    SAMPLING_PERIOD_US,
                    handler,
                )

                val finish = Runnable {
                    sensorManager.unregisterListener(listener)
                    val snapshot = synchronized(readings) { readings.toFloatArray() }
                    if (continuation.isActive) continuation.resume(snapshot)
                }
                handler.postDelayed(finish, WINDOW_MILLIS.toLong())

                continuation.invokeOnCancellation {
                    handler.removeCallbacks(finish)
                    sensorManager.unregisterListener(listener)
                }
            }
        } finally {
            thread.quitSafely()
        }
    }

    private fun standardDeviation(values: FloatArray): Float {
        val mean = values.average().toFloat()
        var sumSq = 0f
        for (v in values) {
            val d = v - mean
            sumSq += d * d
        }
        return sqrt(sumSq / values.size)
    }

    /**
     * Estimates the dominant motion frequency by counting upward zero-crossings of the
     * mean-centred signal — a cheap stand-in for an FFT that captures step cadence well
     * enough to confirm walking (~1.6–2.2 Hz).
     */
    private fun dominantCadenceHz(values: FloatArray): Float {
        val mean = values.average().toFloat()
        var crossings = 0
        for (i in 1 until values.size) {
            if (values[i - 1] <= mean && values[i] > mean) crossings++
        }
        val seconds = values.size * (SAMPLING_PERIOD_US / 1_000_000f)
        if (seconds <= 0f) return 0f
        return crossings / seconds
    }

    companion object {
        // ~50 Hz accelerometer sampling over a 4 s window — enough steps to read cadence
        // while keeping the switch responsive.
        private const val SAMPLING_PERIOD_US = 20_000
        private const val WINDOW_MILLIS = 4_000
        private const val MIN_SAMPLES = 50

        // Body-motion intensity (m/s², std-dev of acceleration magnitude).
        private const val STILL_MAX_INTENSITY = 0.6f // below this => standing still
        private const val WALK_MIN_INTENSITY = 1.2f  // walking shakes the phone clearly

        // Walking step cadence (Hz).
        private const val WALK_CADENCE_MIN = 1.3f
        private const val WALK_CADENCE_MAX = 2.6f
    }
}

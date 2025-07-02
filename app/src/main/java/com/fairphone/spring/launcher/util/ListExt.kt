/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

/**
 * Swaps two elements in a list.
 */
fun <T> List<T>.permute(from: Int, to: Int): List<T> {
    if (from == to) return this
    return this.toMutableList().also {
        val element = it.removeAt(from)
        it.add(to, element)
    }
}

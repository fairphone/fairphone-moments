/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component.switcher

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.data.model.SelectableItem
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.search.SearchableItemList
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.fakeApp

@Composable
fun <T : SelectableItem> ItemSwitcherLayout(
    itemList: List<T>,
    isSelected: (String) -> Boolean,
    onItemClick: (T, Boolean) -> Unit,
    searchBarPlaceholderText: String,
    modifier: Modifier = Modifier
) {
    SearchableItemList(
        itemList = itemList,
        searchBarPlaceholderText = searchBarPlaceholderText,
        modifier = modifier.padding(bottom = 20.dp),
    ) { item ->
        val checked by remember(item.id) { derivedStateOf { isSelected(item.id) } }
        SwitcherListItem(
            item = item,
            isChecked = checked,
            onClick = { value -> onItemClick(item, value) },
        )
    }
}

@Composable
@FP6Preview
@FP6PreviewDark
fun ItemSwitcherLayout_Preview() {
    val context = LocalContext.current
    SpringLauncherTheme {
        val selectedItems = remember { listOf("test", "test4") }
        ItemSwitcherLayout(
            searchBarPlaceholderText = "Search apps",
            itemList = listOf(
                context.fakeApp("test"),
                context.fakeApp("test1"),
                context.fakeApp("test2"),
                context.fakeApp("test3"),
                context.fakeApp("test4"),
                context.fakeApp("test5"),
            ),
            isSelected = { it in selectedItems },
            onItemClick = { app, value -> },
        )
    }
}
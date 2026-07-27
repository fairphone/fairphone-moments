/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.SelectableItem
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.component.PrimaryButton
import com.fairphone.spring.launcher.ui.component.search.SearchableItemList
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.fakeApp

@Composable
fun <T : SelectableItem> ItemSelectorLayout(
    itemList: List<T>,
    selectedItems: List<T>,
    searchBarPlaceholderText: String,
    showConfirmButton: Boolean,
    showItemCounter: Boolean,
    showEmptyItemSelectedError: Boolean,
    showMaxItemSelectedError: Boolean,
    onItemClick: (T) -> Unit,
    onItemDeselected: (T) -> Unit,
    onConfirmItemSelection: () -> Unit,
    modifier: Modifier = Modifier,
    maxItemCount: Int = 5,
    maxItemCountErrorText: String? = null,
    emptyItemSelectedErrorText: String? = null,
    confirmButtonTextResource: Int = R.string.bt_confirm,
) {
    val selectedIds = remember(selectedItems) { selectedItems.mapTo(mutableSetOf()) { it.id } }

    Box(modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            SearchableItemList(
                itemList = itemList,
                searchBarPlaceholderText = searchBarPlaceholderText,
                modifier = Modifier.weight(1f),
                headerContent = {
                    SelectedItemsRow(selectedItems = selectedItems, onDeletedClick = onItemDeselected)
                }
            ) { item ->
                val isSelected by remember(item.id, selectedIds) {
                    derivedStateOf { item.id in selectedIds }
                }
                SelectableListItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) })
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        ) {
            when {
                showMaxItemSelectedError && maxItemCountErrorText != null -> {
                    ErrorView(errorText = maxItemCountErrorText)
                }
                showEmptyItemSelectedError && emptyItemSelectedErrorText != null -> {
                    ErrorView(errorText = emptyItemSelectedErrorText)
                }
                showItemCounter -> {
                    ItemCounter(
                        selectedItemCount = selectedItems.size,
                        maxItemCount = maxItemCount,
                    )
                }
            }

            PrimaryButton(
                text = stringResource(confirmButtonTextResource),
                onClick = onConfirmItemSelection,
                visible = showConfirmButton,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun ErrorView(
    errorText: String,
    modifier: Modifier = Modifier,
) {
    val bgGradientColors = remember {
        listOf(
            Color(0xFFFF4E4E),
            Color(0xFFFF1E1E),
        )
    }
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(size = 100.dp)
            )
            .background(
                brush = Brush.horizontalGradient(bgGradientColors),
                shape = RoundedCornerShape(size = 100.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = errorText,
            style = FairphoneTypography.LabelMedium,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun <T : SelectableItem> SelectedItemsRow(
    selectedItems: List<T>,
    onDeletedClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedItems.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .width(332.dp)
                .height(93.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(size = 12.dp)
                )
        ) {
            items(
                items = selectedItems,
                key = { it.id },
            ) { item ->
                SelectedListItem(
                    item = item,
                    onDeleteClick = { onDeletedClick(item) },
                )
            }
        }
    }
}

@Composable
fun ItemCounter(
    selectedItemCount: Int,
    maxItemCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(size = 100.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(size = 100.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.visible_apps_count, selectedItemCount, maxItemCount),
            style = FairphoneTypography.LabelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
@FP6Preview
fun ItemSelectorLayout_Preview() {
    val context = LocalContext.current
    SpringLauncherTheme {
        ItemSelectorLayout(
            itemList = listOf(
                context.fakeApp("test"),
                context.fakeApp("test1"),
                context.fakeApp("test2"),
                context.fakeApp("test3"),
                context.fakeApp("test4"),
                context.fakeApp("test5"),
            ),
            selectedItems = listOf(
                context.fakeApp("test"),
                context.fakeApp("test4"),
            ),
            showConfirmButton = true,
            showItemCounter = false,
            showEmptyItemSelectedError = false,
            showMaxItemSelectedError = true,
            confirmButtonTextResource = R.string.bt_confirm,
            onItemClick = {},
            onItemDeselected = {},
            onConfirmItemSelection = {},
            maxItemCountErrorText = "Max item count error",
            emptyItemSelectedErrorText = "Empty item selected error",
            searchBarPlaceholderText = "Search appsss"
        )
    }
}
/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2026 . FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.data.model.SelectableItem

@Composable
fun <T : SelectableItem> SearchableItemList(
    itemList: List<T>,
    searchBarPlaceholderText: String,
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit = {},
    row: @Composable (T) -> Unit,
) {
    var filter: String by remember { mutableStateOf("") }
    val filteredItemList by remember(itemList, filter) {
        derivedStateOf {
            if (filter.isEmpty()) itemList
            else itemList.filter { it.name.contains(filter, ignoreCase = true) }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
    ) {
        SearchBar(
            query = filter,
            onQueryChange = { filter = it },
            placeholderText = searchBarPlaceholderText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        headerContent()

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            items(
                items = filteredItemList,
                key = { it.id },
                contentType = { "item" },
            ) { item -> row(item) }
        }
    }
}

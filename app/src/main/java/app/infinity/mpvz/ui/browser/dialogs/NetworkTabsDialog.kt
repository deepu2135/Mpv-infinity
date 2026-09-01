/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.ui.browser.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.infinity.mpvz.R
import app.infinity.mpvz.domain.network.NetworkTab
import app.infinity.mpvz.preferences.BrowserPreferences
import app.infinity.mpvz.preferences.preference.collectAsState
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons

@Composable
fun NetworkTabsDialog(
  preferences: BrowserPreferences,
  onDismiss: () -> Unit,
) {
  val enabledNetworkTabs by preferences.enabledNetworkTabs.collectAsState()
  val networkTabOrder by preferences.networkTabOrder.collectAsState()
  val tabMap = remember { NetworkTab.entries.associateBy { it.name } }
  val currentOrderedTabs = remember(networkTabOrder) {
    (networkTabOrder.mapNotNull { tabMap[it] } + (NetworkTab.entries - networkTabOrder.mapNotNull { tabMap[it] }.toSet())).distinct()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.pref_network_tabs_title)) },
    text = {
      Column {
        Text(
          text = stringResource(R.string.pref_network_tabs_dialog_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(bottom = 8.dp),
        )
        currentOrderedTabs.forEachIndexed { index, tab ->
          val isChecked = tab.name in enabledNetworkTabs
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(
              checked = isChecked,
              onCheckedChange = { checked ->
                val current = enabledNetworkTabs.toMutableSet()
                if (!checked) {
                  if (current.size > 1) {
                    current.remove(tab.name)
                    preferences.enabledNetworkTabs.set(current)
                  }
                } else {
                  current.add(tab.name)
                  preferences.enabledNetworkTabs.set(current)
                }
              },
            )
            Text(
              text = stringResource(tab.titleResId),
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  val current = enabledNetworkTabs.toMutableSet()
                  if (isChecked) {
                    if (current.size > 1) {
                      current.remove(tab.name)
                      preferences.enabledNetworkTabs.set(current)
                    }
                  } else {
                    current.add(tab.name)
                    preferences.enabledNetworkTabs.set(current)
                  }
                }
                .padding(vertical = 8.dp),
            )
            IconButton(
              onClick = {
                if (index > 0) {
                  val updatedList = currentOrderedTabs.map { it.name }.toMutableList()
                  val item = updatedList.removeAt(index)
                  updatedList.add(index - 1, item)
                  preferences.networkTabOrder.set(updatedList)
                }
              },
              enabled = index > 0,
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.ExpandLess,
                contentDescription = "Move Up",
                tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
              )
            }
            IconButton(
              onClick = {
                if (index < currentOrderedTabs.size - 1) {
                  val updatedList = currentOrderedTabs.map { it.name }.toMutableList()
                  val item = updatedList.removeAt(index)
                  updatedList.add(index + 1, item)
                  preferences.networkTabOrder.set(updatedList)
                }
              },
              enabled = index < currentOrderedTabs.size - 1,
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.ExpandMore,
                contentDescription = "Move Down",
                tint = if (index < currentOrderedTabs.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_ok))
      }
    },
  )
}

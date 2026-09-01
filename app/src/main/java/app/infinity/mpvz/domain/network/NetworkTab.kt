/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.domain.network

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import app.infinity.mpvz.R

@Immutable
enum class NetworkTab(@StringRes val titleResId: Int) {
  LOCAL_NETWORK(R.string.ui_local_network),
  SYNC_PLAY(R.string.syncplay_title),
  MEDIA(R.string.ui_media),
  ;

  companion object {
    val defaultTabs = entries.toList()
  }
}

/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.main

import com.vprimex.messenger.components.snackbars.SnackbarHostKey

sealed interface MainSnackbarHostKey : SnackbarHostKey {
  data object Chat : MainSnackbarHostKey
  data object MainChrome : MainSnackbarHostKey
}

/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.calls.links.details

import com.vprimex.messenger.database.CallLinkTable
import com.vprimex.messenger.service.webrtc.CallLinkPeekInfo

data class CallLinkDetailsState(
  val displayRevocationDialog: Boolean = false,
  val isLoadingAdminApprovalChange: Boolean = false,
  val callLink: CallLinkTable.CallLink? = null,
  val peekInfo: CallLinkPeekInfo? = null,
  val failureSnackbar: FailureSnackbar? = null
) {
  enum class FailureSnackbar {
    COULD_NOT_DELETE_CALL_LINK,
    COULD_NOT_SAVE_CHANGES,
    COULD_NOT_UPDATE_ADMIN_APPROVAL
  }
}

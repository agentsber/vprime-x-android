/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.calls.links.create

import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.service.webrtc.links.CreateCallLinkResult

sealed interface EnsureCallLinkCreatedResult {
  data class Success(val recipient: Recipient) : EnsureCallLinkCreatedResult
  data class Failure(val failure: CreateCallLinkResult.Failure) : EnsureCallLinkCreatedResult
}

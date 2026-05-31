/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.conversation.ui.edit

import com.vprimex.messenger.conversation.ConversationAdapter
import com.vprimex.messenger.conversation.ConversationBottomSheetCallback
import com.vprimex.messenger.conversation.ConversationMessage
import com.vprimex.messenger.database.model.MessageRecord

object EmptyConversationBottomSheetCallback : ConversationBottomSheetCallback {
  override fun getConversationAdapterListener(): ConversationAdapter.ItemClickListener = EmptyConversationAdapterListener
  override fun jumpToMessage(messageRecord: MessageRecord) = Unit
  override fun unpin(conversationMessage: ConversationMessage) = Unit
  override fun copy(conversationMessage: ConversationMessage) = Unit
  override fun delete(conversationMessage: ConversationMessage) = Unit
  override fun save(conversationMessage: ConversationMessage) = Unit
}

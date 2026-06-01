/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.main

import android.os.Parcelable
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import com.vprimex.messenger.calls.log.CallLogRow
import com.vprimex.messenger.conversation.ConversationArgs
import com.vprimex.messenger.database.model.MessageId
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.service.webrtc.links.CallLinkRoomId

/**
 * Describes which content to display in the detail pane.
 */
@Serializable
@Parcelize
sealed interface MainNavigationDetailLocation : Parcelable {

  class Saver(
    val earlyLocation: MainNavigationDetailLocation?
  ) : androidx.compose.runtime.saveable.Saver<MainNavigationDetailLocation, String> {
    override fun SaverScope.save(value: MainNavigationDetailLocation): String {
      return Json.encodeToString(value)
    }

    override fun restore(value: String): MainNavigationDetailLocation? {
      return earlyLocation ?: Json.decodeFromString(value)
    }
  }

  /**
   * Flag utilized internally to determine whether the given route is displayed at the root
   * of a task stack (or on top of Empty)
   */
  @IgnoredOnParcel
  val isContentRoot: Boolean
    get() = false

  @Serializable
  data object Empty : MainNavigationDetailLocation {
    @Transient
    @IgnoredOnParcel
    override val isContentRoot: Boolean = true
  }

  @Serializable
  data class Conversation(val conversationArgs: ConversationArgs) : MainNavigationDetailLocation {
    @Transient
    @IgnoredOnParcel
    override val isContentRoot: Boolean = true

    @Transient
    @IgnoredOnParcel
    val controllerKey: Long = conversationArgs.threadId
  }

  @Serializable
  data class CallLinkDetails(val callLinkRoomId: CallLinkRoomId) : MainNavigationDetailLocation {
    @Transient
    @IgnoredOnParcel
    override val isContentRoot: Boolean = true

    @Transient
    @IgnoredOnParcel
    val controllerKey: CallLogRow.Id = CallLogRow.Id.CallLink(callLinkRoomId)
  }

  /**
   * Subscreens that can be displayed within the chats tab.
   */
  @Parcelize
  sealed interface Chats : MainNavigationDetailLocation {

    val controllerKey: RecipientId

    @Serializable
    data class MessageDetails(val recipientId: RecipientId, val messageId: MessageId) : Chats {
      @Transient
      @IgnoredOnParcel
      override val controllerKey: RecipientId = recipientId
    }

    @Serializable
    data class ConversationSettings(val recipientId: RecipientId) : Chats {
      @Transient
      @IgnoredOnParcel
      override val controllerKey: RecipientId = recipientId
    }
  }

  /**
   * Subscreens that can be displayed within the calls tab.
   */
  @Parcelize
  sealed interface Calls : MainNavigationDetailLocation {
    val controllerKey: CallLogRow.Id

    @Parcelize
    sealed class CallLinks : Calls {
      @Serializable
      data class EditCallLinkName(val callLinkRoomId: CallLinkRoomId) : CallLinks() {
        @Transient
        @IgnoredOnParcel
        override val controllerKey: CallLogRow.Id = CallLogRow.Id.CallLink(callLinkRoomId)
      }
    }
  }

  @Parcelize
  sealed class Stories : MainNavigationDetailLocation
}

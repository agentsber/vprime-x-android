package com.vprimex.messenger.conversationlist.chatfilter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.vprimex.messenger.conversationlist.model.ConversationFilter

@Parcelize
data class ConversationFilterRequest(
  val filter: ConversationFilter,
  val source: ConversationFilterSource
) : Parcelable

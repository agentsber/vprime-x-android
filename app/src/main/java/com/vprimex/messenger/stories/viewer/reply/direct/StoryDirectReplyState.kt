package com.vprimex.messenger.stories.viewer.reply.direct

import com.vprimex.messenger.database.model.MessageRecord
import com.vprimex.messenger.recipients.Recipient

data class StoryDirectReplyState(
  val groupDirectReplyRecipient: Recipient? = null,
  val storyRecord: MessageRecord? = null
)

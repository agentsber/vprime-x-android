package com.vprimex.messenger.stories.settings.select

import com.vprimex.messenger.database.model.DistributionListId
import com.vprimex.messenger.database.model.DistributionListRecord
import com.vprimex.messenger.recipients.RecipientId

data class BaseStoryRecipientSelectionState(
  val distributionListId: DistributionListId?,
  val privateStory: DistributionListRecord? = null,
  val selection: Set<RecipientId> = emptySet(),
  val isStartingSelection: Boolean = false
)

package com.vprimex.messenger.safety

import com.vprimex.messenger.database.model.DistributionListId
import com.vprimex.messenger.recipients.Recipient

sealed class SafetyNumberBucket {
  data class DistributionListBucket(val distributionListId: DistributionListId, val name: String) : SafetyNumberBucket()
  data class GroupBucket(val recipient: Recipient) : SafetyNumberBucket()
  object ContactsBucket : SafetyNumberBucket()
}

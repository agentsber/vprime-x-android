package com.vprimex.messenger.groups.v2

import com.vprimex.messenger.groups.ui.GroupChangeFailureReason
import com.vprimex.messenger.recipients.Recipient

sealed class GroupBlockJoinRequestResult {
  object Success : GroupBlockJoinRequestResult()
  class Failure(val reason: GroupChangeFailureReason) : GroupBlockJoinRequestResult()

  fun isFailure() = this is Failure
}

sealed class GroupAddMembersResult {
  class Success(val numberOfMembersAdded: Int, val newMembersInvited: List<Recipient>) : GroupAddMembersResult()
  class Failure(val reason: GroupChangeFailureReason) : GroupAddMembersResult()

  fun isFailure() = this is Failure
}

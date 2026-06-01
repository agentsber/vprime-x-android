package com.vprimex.messenger.recipients.ui.bottomsheet

import com.vprimex.messenger.groups.memberlabel.StyledMemberLabel

data class RecipientDetailsState(
  val memberLabel: StyledMemberLabel?,
  val aboutText: String?
)

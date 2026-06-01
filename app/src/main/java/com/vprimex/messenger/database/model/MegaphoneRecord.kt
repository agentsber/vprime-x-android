package com.vprimex.messenger.database.model

import com.vprimex.messenger.megaphone.Megaphones

data class MegaphoneRecord(
  val event: Megaphones.Event,
  val interactionCount: Int,
  val lastInteractionTime: Long,
  val firstVisible: Long,
  val lastVisible: Long,
  val finished: Boolean
)

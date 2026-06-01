package com.vprimex.messenger.database

internal interface RecipientIdDatabaseReference {
  fun remapRecipient(fromId: RecipientId?, toId: RecipientId?)
}

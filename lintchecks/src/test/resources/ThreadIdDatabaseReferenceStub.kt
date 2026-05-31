package com.vprimex.messenger.database

internal interface ThreadIdDatabaseReference {
  fun remapThread(fromId: Long, toId: Long)
}

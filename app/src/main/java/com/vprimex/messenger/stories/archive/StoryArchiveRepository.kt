package com.vprimex.messenger.stories.archive

import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.jobs.MultiDeviceDeleteSyncJob

class StoryArchiveRepository {

  fun deleteStories(messageIds: Set<Long>) {
    val records = messageIds.mapNotNull { SignalDatabase.messages.getMessageRecordOrNull(it) }.toSet()
    messageIds.forEach { SignalDatabase.messages.deleteMessage(it) }
    MultiDeviceDeleteSyncJob.enqueueMessageDeletes(records)
  }
}

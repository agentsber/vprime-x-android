package com.vprimex.messenger.stories.viewer.reply.group

import org.signal.paging.PagedDataSource
import com.vprimex.messenger.conversation.ConversationMessage
import com.vprimex.messenger.database.MessageTable
import com.vprimex.messenger.database.MessageTypes
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.MessageId
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.database.withAttachments
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.recipients.Recipient

class StoryGroupReplyDataSource(private val parentStoryId: Long) : PagedDataSource<MessageId, ReplyBody> {
  override fun size(): Int {
    return SignalDatabase.messages.getNumberOfStoryReplies(parentStoryId)
  }

  override fun load(start: Int, length: Int, totalSize: Int, cancellationSignal: PagedDataSource.CancellationSignal): MutableList<ReplyBody> {
    val rawRecords = mutableListOf<MmsMessageRecord>()
    SignalDatabase.messages.getStoryReplies(parentStoryId).use { cursor ->
      cursor.moveToPosition(start - 1)
      val mmsReader = MessageTable.MmsReader(cursor)
      while (cursor.moveToNext() && cursor.position < start + length) {
        rawRecords.add(mmsReader.getCurrent() as MmsMessageRecord)
      }
    }

    return rawRecords.withAttachments().map { readRowFromRecord(it as MmsMessageRecord) }.toMutableList()
  }

  override fun load(key: MessageId): ReplyBody {
    return readRowFromRecord(SignalDatabase.messages.getMessageRecord(key.id).withAttachments() as MmsMessageRecord)
  }

  override fun getKey(data: ReplyBody): MessageId {
    return data.key
  }

  private fun readRowFromRecord(record: MmsMessageRecord): ReplyBody {
    val threadRecipient: Recipient = requireNotNull(SignalDatabase.threads.getRecipientForThreadId(record.threadId))
    return when {
      record.isRemoteDelete -> ReplyBody.RemoteDelete(record)
      MessageTypes.isStoryReaction(record.type) -> ReplyBody.Reaction(record)
      else -> ReplyBody.Text(
        ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(AppDependencies.application, record, threadRecipient)
      )
    }
  }
}

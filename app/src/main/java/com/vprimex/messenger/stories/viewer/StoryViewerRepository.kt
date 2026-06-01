package com.vprimex.messenger.stories.viewer

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.vprimex.messenger.database.MessageTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.DistributionListId
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.database.withAttachments
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId

/**
 * Open for testing
 */
open class StoryViewerRepository {
  fun getFirstStory(recipientId: RecipientId, storyId: Long): Single<MmsMessageRecord> {
    return if (storyId > 0) {
      Single.fromCallable {
        SignalDatabase.messages.getMessageRecord(storyId).withAttachments() as MmsMessageRecord
      }
    } else {
      Single.fromCallable {
        val recipient = Recipient.resolved(recipientId)
        val reader: MessageTable.Reader = if (recipient.isMyStory || recipient.isSelf) {
          SignalDatabase.messages.getAllOutgoingStories(false, 1)
        } else {
          val unread = SignalDatabase.messages.getUnreadStories(recipientId, 1)
          if (unread.iterator().hasNext()) {
            unread
          } else {
            SignalDatabase.messages.getAllStoriesFor(recipientId, 1)
          }
        }
        reader.use { it.iterator().next() }.withAttachments() as MmsMessageRecord
      }
    }
  }

  fun getStories(hiddenStories: Boolean, isOutgoingOnly: Boolean): Single<List<RecipientId>> {
    return Single.create { emitter ->
      val myStoriesId = SignalDatabase.recipients.getOrInsertFromDistributionListId(DistributionListId.MY_STORY)
      val myStories = Recipient.resolved(myStoriesId)
      val releaseChannelId = SignalStore.releaseChannel.releaseChannelRecipientId
      val recipientIds = SignalDatabase.messages.getOrderedStoryRecipientsAndIds(isOutgoingOnly).groupBy {
        val recipient = Recipient.resolved(it.recipientId)
        if (recipient.isDistributionList) {
          myStories
        } else {
          recipient
        }
      }.keys.filter {
        if (hiddenStories) {
          it.shouldHideStory
        } else {
          !it.shouldHideStory
        }
      }.map { it.id }

      emitter.onSuccess(
        recipientIds.floatToTop(releaseChannelId).floatToTop(myStoriesId)
      )
    }.subscribeOn(Schedulers.io())
  }

  private fun List<RecipientId>.floatToTop(recipientId: RecipientId?): List<RecipientId> {
    return if (recipientId != null && contains(recipientId)) {
      listOf(recipientId) + (this - recipientId)
    } else {
      this
    }
  }
}

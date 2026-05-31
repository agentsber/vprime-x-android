package com.vprimex.messenger.stories.settings.story

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.database.GroupTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.sms.MessageSender
import com.vprimex.messenger.storage.StorageSyncHelper
import com.vprimex.messenger.stories.Stories

class StoriesPrivacySettingsRepository {
  fun markGroupsAsStories(groups: List<RecipientId>): Completable {
    return Completable.fromCallable {
      SignalDatabase.groups.setShowAsStoryState(groups, GroupTable.ShowAsStoryState.ALWAYS)
      SignalDatabase.recipients.markNeedsSync(groups)
      StorageSyncHelper.scheduleSyncForDataChange()
    }
  }

  fun setStoriesEnabled(isEnabled: Boolean): Completable {
    return Completable.fromAction {
      SignalStore.story.isFeatureDisabled = !isEnabled
      Stories.onStorySettingsChanged(Recipient.self().id)
      AppDependencies.resetNetwork()

      SignalDatabase.messages.getAllOutgoingStories(false, -1).use { reader ->
        reader.map { record -> record.id }
      }.forEach { messageId ->
        MessageSender.sendRemoteDelete(messageId)
      }
    }.subscribeOn(Schedulers.io())
  }

  fun onSettingsChanged() {
    SignalExecutors.BOUNDED_IO.execute {
      Stories.onStorySettingsChanged(Recipient.self().id)
    }
  }

  fun userHasOutgoingStories(): Single<Boolean> {
    return Single.fromCallable {
      SignalDatabase.messages.getAllOutgoingStories(false, -1).use {
        it.iterator().hasNext()
      }
    }.subscribeOn(Schedulers.io())
  }
}

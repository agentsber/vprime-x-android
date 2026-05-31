package com.vprimex.messenger.stories.settings.group

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.vprimex.messenger.database.GroupTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.storage.StorageSyncHelper

class GroupStorySettingsRepository {
  fun unmarkAsGroupStory(groupId: GroupId): Completable {
    return Completable.fromAction {
      SignalDatabase.groups.setShowAsStoryState(groupId, GroupTable.ShowAsStoryState.NEVER)
      SignalDatabase.recipients.markNeedsSync(Recipient.externalGroupExact(groupId).id)
      StorageSyncHelper.scheduleSyncForDataChange()
    }.subscribeOn(Schedulers.io())
  }

  fun getConversationData(groupId: GroupId): Single<GroupConversationData> {
    return Single.fromCallable {
      val recipientId = SignalDatabase.recipients.getByGroupId(groupId).get()
      val threadId = SignalDatabase.threads.getThreadIdFor(recipientId) ?: -1L

      GroupConversationData(recipientId, threadId)
    }.subscribeOn(Schedulers.io())
  }
}

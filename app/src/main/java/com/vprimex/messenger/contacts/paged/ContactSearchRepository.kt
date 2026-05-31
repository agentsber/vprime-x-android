package com.vprimex.messenger.contacts.paged

import androidx.annotation.CheckResult
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.vprimex.messenger.database.GroupTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.DistributionListId
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.storage.StorageSyncHelper
import com.vprimex.messenger.stories.Stories

class ContactSearchRepository {
  @CheckResult
  fun filterOutUnselectableContactSearchKeys(contactSearchKeys: Set<ContactSearchKey>): Single<Set<ContactSearchSelectionResult>> {
    return Single.fromCallable {
      contactSearchKeys.map {
        val isSelectable = when (it) {
          is ContactSearchKey.RecipientSearchKey -> canSelectRecipient(it.recipientId)
          is ContactSearchKey.UnknownRecipientKey -> it.sectionKey == ContactSearchConfiguration.SectionKey.PHONE_NUMBER
          is ContactSearchKey.ChatTypeSearchKey -> true
          else -> false
        }
        ContactSearchSelectionResult(it, isSelectable)
      }.toSet()
    }
  }

  private fun canSelectRecipient(recipientId: RecipientId): Boolean {
    val recipient = Recipient.resolved(recipientId)
    return if (recipient.isPushV2Group) {
      val record = SignalDatabase.groups.getGroup(recipient.requireGroupId())
      !(record.isPresent && record.get().isAnnouncementGroup && !record.get().isAdmin(Recipient.self()))
    } else {
      true
    }
  }

  @CheckResult
  fun markDisplayAsStory(recipientIds: Collection<RecipientId>): Completable {
    return Completable.fromAction {
      SignalDatabase.groups.setShowAsStoryState(recipientIds, GroupTable.ShowAsStoryState.ALWAYS)
      SignalDatabase.recipients.markNeedsSync(recipientIds)
      StorageSyncHelper.scheduleSyncForDataChange()
    }.subscribeOn(Schedulers.io())
  }

  @CheckResult
  fun unmarkDisplayAsStory(groupId: GroupId): Completable {
    return Completable.fromAction {
      SignalDatabase.groups.setShowAsStoryState(groupId, GroupTable.ShowAsStoryState.NEVER)
      SignalDatabase.recipients.markNeedsSync(Recipient.externalGroupExact(groupId).id)
      StorageSyncHelper.scheduleSyncForDataChange()
    }.subscribeOn(Schedulers.io())
  }

  @CheckResult
  fun deletePrivateStory(distributionListId: DistributionListId): Completable {
    return Completable.fromAction {
      SignalDatabase.distributionLists.deleteList(distributionListId)
      Stories.onStorySettingsChanged(distributionListId)
    }.subscribeOn(Schedulers.io())
  }
}

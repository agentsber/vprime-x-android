package com.vprimex.messenger.contacts.paged

import android.content.Context
import android.database.Cursor
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.signal.core.util.CursorUtil
import com.vprimex.messenger.R
import com.vprimex.messenger.contacts.ContactRepository
import com.vprimex.messenger.contacts.paged.collections.ContactSearchIterator
import com.vprimex.messenger.database.DistributionListTables
import com.vprimex.messenger.database.GroupTable
import com.vprimex.messenger.database.RecipientTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.ThreadTable
import com.vprimex.messenger.database.model.DistributionListPrivacyMode
import com.vprimex.messenger.database.model.GroupRecord
import com.vprimex.messenger.groups.GroupsInCommonRepository
import com.vprimex.messenger.groups.GroupsInCommonSummary
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.keyvalue.StorySend
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId

/**
 * Database boundary interface which allows us to safely unit test the data source without
 * having to deal with database access.
 */
open class ContactSearchPagedDataSourceRepository(
  context: Context
) {

  private val contactRepository = ContactRepository(context.getString(R.string.note_to_self))
  private val context = context.applicationContext

  open fun getLatestStorySends(activeStoryCutoffDuration: Long): List<StorySend> {
    return SignalStore.story
      .getLatestActiveStorySendTimestamps(System.currentTimeMillis() - activeStoryCutoffDuration)
  }

  open fun querySignalContacts(contactsSearchQuery: RecipientTable.ContactSearchQuery): Cursor? {
    return contactRepository.querySignalContacts(contactsSearchQuery)
  }

  open fun querySignalContactLetterHeaders(query: String?, includeSelfMode: RecipientTable.IncludeSelfMode, includePush: Boolean, includeSms: Boolean): Map<RecipientId, String> {
    return SignalDatabase.recipients.querySignalContactLetterHeaders(query ?: "", includeSelfMode, includePush, includeSms)
  }

  open fun queryGroupMemberContacts(query: String?): Cursor? {
    return contactRepository.queryGroupMemberContacts(query ?: "")
  }

  open fun getGroupSearchIterator(
    section: ContactSearchConfiguration.Section.Groups,
    query: String?
  ): ContactSearchIterator<GroupRecord> {
    return SignalDatabase.groups.queryGroups(
      GroupTable.GroupQuery.Builder()
        .withSearchQuery(query)
        .withInactiveGroups(section.includeInactive)
        .withMmsGroups(section.includeMms)
        .withV1Groups(section.includeV1)
        .withSortOrder(section.sortOrder)
        .build()
    )
  }

  open fun getRecents(section: ContactSearchConfiguration.Section.Recents): Cursor? {
    return SignalDatabase.threads.getRecentConversationList(
      section.limit,
      section.includeInactiveGroups,
      section.mode == ContactSearchConfiguration.Section.Recents.Mode.INDIVIDUALS,
      section.mode == ContactSearchConfiguration.Section.Recents.Mode.GROUPS,
      !section.includeGroupsV1,
      !section.includeSms,
      !section.includeSelf
    )
  }

  open fun getStories(query: String?): Cursor? {
    return SignalDatabase.distributionLists.getAllListsForContactSelectionUiCursor(query, myStoryContainsQuery(query ?: ""))
  }

  open fun getGroupsWithMembers(query: String): Cursor {
    return SignalDatabase.groups.queryGroupsByMemberName(query)
  }

  open fun getContactsWithoutThreads(query: String): Cursor {
    return SignalDatabase.recipients.getAllContactsWithoutThreads(query)
  }

  open fun getRecipientFromDistributionListCursor(cursor: Cursor): Recipient {
    return Recipient.resolved(RecipientId.from(CursorUtil.requireLong(cursor, DistributionListTables.RECIPIENT_ID)))
  }

  open fun getPrivacyModeFromDistributionListCursor(cursor: Cursor): DistributionListPrivacyMode {
    return DistributionListPrivacyMode.deserialize(CursorUtil.requireLong(cursor, DistributionListTables.PRIVACY_MODE))
  }

  open fun getRecipientFromThreadCursor(cursor: Cursor): Recipient {
    return Recipient.resolved(RecipientId.from(CursorUtil.requireLong(cursor, ThreadTable.RECIPIENT_ID)))
  }

  open fun getRecipientFromSearchCursor(cursor: Cursor): Recipient {
    return Recipient.resolved(RecipientId.from(CursorUtil.requireLong(cursor, ContactRepository.ID_COLUMN)))
  }

  open fun getRecipientFromRecipientCursor(cursor: Cursor): Recipient {
    return Recipient.resolved(RecipientId.from(CursorUtil.requireLong(cursor, RecipientTable.ID)))
  }

  @WorkerThread
  open fun getGroupsInCommon(recipient: Recipient): GroupsInCommonSummary {
    return runBlocking {
      GroupsInCommonRepository
        .getGroupsInCommonSummary(context, recipient.id)
        .first()
    }
  }

  open fun getRecipientFromGroupRecord(groupRecord: GroupRecord): Recipient {
    return Recipient.resolved(groupRecord.recipientId)
  }

  open fun getDistributionListMembershipCount(recipient: Recipient): Int {
    return SignalDatabase.distributionLists.getMemberCount(recipient.requireDistributionListId())
  }

  open fun getGroupStories(): Set<ContactSearchData.Story> {
    return SignalDatabase.groups.getGroupsToDisplayAsStories().map {
      val recipient = Recipient.resolved(SignalDatabase.recipients.getOrInsertFromGroupId(it))
      ContactSearchData.Story(recipient, recipient.participantIds.size, DistributionListPrivacyMode.ALL)
    }.toSet()
  }

  open fun recipientNameContainsQuery(recipient: Recipient, query: String?): Boolean {
    return query.isNullOrBlank() || recipient.getDisplayName(context).contains(query, ignoreCase = true)
  }

  open fun myStoryContainsQuery(query: String): Boolean {
    if (query.isEmpty()) {
      return true
    }

    val myStory = context.getString(R.string.Recipient_my_story)
    return myStory.contains(query, ignoreCase = true)
  }
}

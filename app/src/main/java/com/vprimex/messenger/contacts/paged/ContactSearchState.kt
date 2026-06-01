package com.vprimex.messenger.contacts.paged

import com.vprimex.messenger.conversationlist.chatfilter.ConversationFilterRequest
import com.vprimex.messenger.search.SearchFilter

/**
 * Simple search state for contacts.
 */
data class ContactSearchState(
  val query: String? = null,
  val conversationFilterRequest: ConversationFilterRequest? = null,
  val expandedSections: Set<ContactSearchConfiguration.SectionKey> = emptySet(),
  val groupStories: Set<ContactSearchData.Story> = emptySet(),
  val searchFilter: SearchFilter = SearchFilter.EMPTY
)

package com.vprimex.messenger.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import com.vprimex.messenger.database.RxDatabaseObserver
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.recipients.Recipient

object MainNavigationRepository {

  fun getNumberOfUnreadMessages(): Flow<Long> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.threads.getUnreadMessageCount() }.asFlow()
  }

  fun getNumberOfUnseenStories(): Flow<Long> {
    return RxDatabaseObserver.conversationList.map {
      SignalDatabase
        .messages
        .getUnreadStoryThreadRecipientIds()
        .map { Recipient.resolved(it) }
        .filterNot { it.shouldHideStory }
        .size
        .toLong()
    }.asFlow()
  }

  fun getHasFailedOutgoingStories(): Flow<Boolean> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.messages.hasFailedOutgoingStory() }.asFlow()
  }

  fun getNumberOfUnseenCalls(): Flow<Long> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.calls.getUnreadMissedCallCount() }.asFlow()
  }
}

package com.vprimex.messenger.conversation.ui.mentions

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.vprimex.messenger.database.RecipientTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId

/**
 * Search for members that match the query for rendering in the mentions picker during message compose.
 */
class MentionsPickerRepositoryV2(
  private val recipients: RecipientTable = SignalDatabase.recipients
) {
  fun search(query: String, members: List<RecipientId>): Single<List<Recipient>> {
    return if (members.isEmpty()) {
      Single.just(emptyList())
    } else {
      Single
        .fromCallable { recipients.queryRecipientsForMentions(query, members) }
        .subscribeOn(Schedulers.io())
    }
  }
}

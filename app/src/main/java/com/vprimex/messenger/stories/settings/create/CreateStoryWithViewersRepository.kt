package com.vprimex.messenger.stories.settings.create

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.stories.Stories

class CreateStoryWithViewersRepository {
  fun createList(name: CharSequence, members: Set<RecipientId>): Single<RecipientId> {
    return Single.create<RecipientId> {
      val result = SignalDatabase.distributionLists.createList(name.toString(), members.toList())
      if (result == null) {
        it.onError(Exception("Null result, due to a duplicated name."))
      } else {
        Stories.onStorySettingsChanged(result)
        it.onSuccess(SignalDatabase.recipients.getOrInsertFromDistributionListId(result))
      }
    }.subscribeOn(Schedulers.io())
  }
}

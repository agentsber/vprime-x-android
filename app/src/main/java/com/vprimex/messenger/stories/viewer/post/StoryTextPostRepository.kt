package com.vprimex.messenger.stories.viewer.post

import android.graphics.Typeface
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.Base64
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.database.model.databaseprotos.StoryTextPost
import com.vprimex.messenger.database.withAttachments
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.fonts.TextFont
import com.vprimex.messenger.fonts.TextToScript
import com.vprimex.messenger.fonts.TypefaceCache

class StoryTextPostRepository {
  fun getRecord(recordId: Long): Single<MmsMessageRecord> {
    return Single.fromCallable {
      SignalDatabase.messages.getMessageRecord(recordId).withAttachments() as MmsMessageRecord
    }.subscribeOn(Schedulers.io())
  }

  fun getTypeface(recordId: Long): Single<Typeface> {
    return getRecord(recordId).flatMap {
      val model = StoryTextPost.ADAPTER.decode(Base64.decode(it.body))
      val textFont = TextFont.fromStyle(model.style)
      val script = TextToScript.guessScript(model.body)

      TypefaceCache.get(AppDependencies.application, textFont, script)
    }
  }
}

package com.vprimex.messenger.keyboard.emoji

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.components.emoji.EmojiPageModel
import com.vprimex.messenger.components.emoji.RecentEmojiPageModel
import com.vprimex.messenger.emoji.EmojiSource.Companion.latest
import com.vprimex.messenger.util.TextSecurePreferences
import java.util.function.Consumer

class EmojiKeyboardPageRepository(private val context: Context) {
  fun getEmoji(consumer: Consumer<List<EmojiPageModel>>) {
    SignalExecutors.BOUNDED.execute {
      val list = mutableListOf<EmojiPageModel>()
      list += RecentEmojiPageModel(context, TextSecurePreferences.RECENT_STORAGE_KEY)
      list += latest.displayPages
      consumer.accept(list)
    }
  }
}

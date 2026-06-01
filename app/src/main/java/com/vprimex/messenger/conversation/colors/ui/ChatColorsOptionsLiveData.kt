package com.vprimex.messenger.conversation.colors.ui

import androidx.lifecycle.LiveData
import org.signal.core.util.concurrent.SignalExecutors
import com.vprimex.messenger.conversation.colors.ChatColors
import com.vprimex.messenger.conversation.colors.ChatColorsPalette
import com.vprimex.messenger.database.ChatColorsTable
import com.vprimex.messenger.database.DatabaseObserver
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.util.concurrent.SerialMonoLifoExecutor
import java.util.concurrent.Executor

class ChatColorsOptionsLiveData : LiveData<List<ChatColors>>() {
  private val chatColorsTable: ChatColorsTable = SignalDatabase.chatColors
  private val observer: DatabaseObserver.Observer = DatabaseObserver.Observer { refreshChatColors() }
  private val executor: Executor = SerialMonoLifoExecutor(SignalExecutors.BOUNDED)

  override fun onActive() {
    refreshChatColors()
    AppDependencies.databaseObserver.registerChatColorsObserver(observer)
  }

  override fun onInactive() {
    AppDependencies.databaseObserver.unregisterObserver(observer)
  }

  private fun refreshChatColors() {
    executor.execute {
      val options = mutableListOf<ChatColors>().apply {
        addAll(ChatColorsPalette.Bubbles.all)
        addAll(chatColorsTable.getSavedChatColors())
      }

      postValue(options)
    }
  }
}

package com.vprimex.messenger.components.settings.app.appearance

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.signal.core.util.AppUtil
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobs.EmojiSearchIndexDownloadJob
import com.vprimex.messenger.keyvalue.SettingsValues.Theme
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.util.SplashScreenUtil

class AppearanceSettingsViewModel : ViewModel() {
  private val store = MutableStateFlow(getState())
  val state: StateFlow<AppearanceSettingsState> = store

  fun refreshState() {
    store.update { getState() }
  }

  fun setTheme(activity: Activity?, theme: Theme) {
    store.update { it.copy(theme = theme) }
    SignalStore.settings.theme = theme
    SplashScreenUtil.setSplashScreenThemeIfNecessary(activity, theme)
  }

  fun setLanguage(language: String) {
    store.update { it.copy(language = language) }
    SignalStore.settings.language = language
    EmojiSearchIndexDownloadJob.scheduleImmediately()
    AppUtil.restart(AppDependencies.application)
  }

  fun setMessageFontSize(size: Int) {
    store.update { it.copy(messageFontSize = size) }
    SignalStore.settings.messageFontSize = size
  }

  private fun getState(): AppearanceSettingsState {
    return AppearanceSettingsState(
      SignalStore.settings.theme,
      SignalStore.settings.messageFontSize,
      SignalStore.settings.language,
      SignalStore.settings.useCompactNavigationBar
    )
  }
}

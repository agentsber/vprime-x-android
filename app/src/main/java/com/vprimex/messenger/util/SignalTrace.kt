package com.vprimex.messenger.util

import com.vprimex.messenger.BuildConfig
import androidx.tracing.Trace as AndroidTrace

object SignalTrace {
  @JvmStatic
  fun beginSection(methodName: String) {
    if (!BuildConfig.TRACING_ENABLED) {
      return
    }
    AndroidTrace.beginSection(methodName)
  }

  @JvmStatic
  fun endSection() {
    if (!BuildConfig.TRACING_ENABLED) {
      return
    }
    AndroidTrace.endSection()
  }
}

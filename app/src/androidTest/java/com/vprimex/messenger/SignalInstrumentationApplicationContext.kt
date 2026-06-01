package com.vprimex.messenger

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider
import com.vprimex.messenger.database.LogDatabase
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.dependencies.ApplicationDependencyProvider
import com.vprimex.messenger.dependencies.InstrumentationApplicationDependencyProvider
import com.vprimex.messenger.logging.CustomSignalProtocolLogger
import com.vprimex.messenger.logging.PersistentLogger
import com.vprimex.messenger.testing.InMemoryLogger
import com.vprimex.messenger.util.Environment

/**
 * Application context for running instrumentation tests (aka androidTests).
 */
class SignalInstrumentationApplicationContext : ApplicationContext() {

  val inMemoryLogger: InMemoryLogger = InMemoryLogger()

  override fun attachBaseContext(base: Context?) {
    Environment.IS_INSTRUMENTATION = true
    super.attachBaseContext(base)
  }

  override fun initializeAppDependencies() {
    val default = ApplicationDependencyProvider(this)
    AppDependencies.init(this, InstrumentationApplicationDependencyProvider(this, default))
    AppDependencies.deadlockDetector.start()
  }

  override fun initializeLogging() {
    Log.initialize({ true }, AndroidLogger, PersistentLogger.getInstance(this), inMemoryLogger)

    SignalProtocolLoggerProvider.setProvider(CustomSignalProtocolLogger())

    SignalExecutors.UNBOUNDED.execute {
      Log.blockUntilAllWritesFinished()
      LogDatabase.getInstance(this).logs.trimToSize()
    }
  }

  override fun beginJobLoop() = Unit

  /**
   * Some of the jobs can interfere with some of the instrumentation tests.
   *
   * For example, we may try to create a release channel recipient while doing
   * an import/backup test.
   *
   * This can be used to start the job loop if needed for tests that rely on it.
   */
  fun beginJobLoopForTests() {
    super.beginJobLoop()
  }
}

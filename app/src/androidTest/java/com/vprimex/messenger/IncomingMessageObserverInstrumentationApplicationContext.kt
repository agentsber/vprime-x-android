package com.vprimex.messenger

import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.dependencies.ApplicationDependencyProvider
import com.vprimex.messenger.logging.CustomSignalProtocolLogger
import com.vprimex.messenger.testing.incomingmessageobserver.IncomingMessageObserverDependencyProvider
import com.vprimex.messenger.testing.incomingmessageobserver.IncomingMessageObserverTestRunner

/**
 * Application used when running `IncomingMessageObserver` instrumentation tests. Installs
 * [IncomingMessageObserverDependencyProvider] so the websocket and job manager are replaced
 * with test-friendly implementations. Selected by [IncomingMessageObserverTestRunner] when
 * gradle is invoked with `-PimoTests`.
 */
class IncomingMessageObserverInstrumentationApplicationContext : ApplicationContext() {

  override fun initializeAppDependencies() {
    val default = ApplicationDependencyProvider(this)
    AppDependencies.init(this, IncomingMessageObserverDependencyProvider(this, default))
    AppDependencies.deadlockDetector.start()
  }

  override fun initializeLogging() {
    Log.initialize({ true }, AndroidLogger)
    SignalProtocolLoggerProvider.setProvider(CustomSignalProtocolLogger())
  }

  override fun beginJobLoop() = Unit

  fun beginJobLoopForTests() {
    super.beginJobLoop()
  }
}

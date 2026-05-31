package com.vprimex.messenger.jobs

import org.signal.core.util.logging.Log
import org.signal.network.exceptions.NonSuccessfulResponseCodeException
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.jobmanager.Job
import com.vprimex.messenger.jobmanager.impl.NetworkConstraint
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.pin.SvrRepository
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Refresh KBS authentication credentials for talking to KBS during re-registration.
 */
class RefreshSvrCredentialsJob private constructor(parameters: Parameters) : BaseJob(parameters) {

  companion object {
    const val KEY = "RefreshKbsCredentialsJob"

    private val TAG = Log.tag(RefreshSvrCredentialsJob::class.java)
    private val FREQUENCY: Duration = 15.days

    @JvmStatic
    fun enqueueIfNecessary() {
      if (SignalStore.svr.hasPin() && SignalStore.account.isRegistered) {
        val lastTimestamp = SignalStore.svr.lastRefreshAuthTimestamp
        if (lastTimestamp + FREQUENCY.inWholeMilliseconds < System.currentTimeMillis() || lastTimestamp > System.currentTimeMillis()) {
          AppDependencies.jobManager.add(RefreshSvrCredentialsJob())
        } else {
          Log.d(TAG, "Do not need to refresh credentials. Last refresh: $lastTimestamp")
        }
      }
    }
  }

  private constructor() : this(
    parameters = Parameters.Builder()
      .setQueue("RefreshKbsCredentials")
      .addConstraint(NetworkConstraint.KEY)
      .setMaxInstancesForQueue(2)
      .setMaxAttempts(3)
      .setLifespan(1.days.inWholeMilliseconds)
      .build()
  )

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  override fun onRun() {
    if (!SignalStore.account.isRegistered) {
      Log.w(TAG, "Not registered! Skipping.")
      return
    }

    SvrRepository.refreshAndStoreAuthorization()
  }

  override fun onShouldRetry(e: Exception): Boolean {
    return e is IOException && e !is NonSuccessfulResponseCodeException
  }

  override fun onFailure() = Unit

  class Factory : Job.Factory<RefreshSvrCredentialsJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): RefreshSvrCredentialsJob {
      return RefreshSvrCredentialsJob(parameters)
    }
  }
}

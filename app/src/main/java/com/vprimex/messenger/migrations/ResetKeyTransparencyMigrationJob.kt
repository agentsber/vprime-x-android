package com.vprimex.messenger.migrations

import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.jobmanager.Job
import com.vprimex.messenger.jobs.CheckKeyTransparencyJob
import com.vprimex.messenger.keyvalue.SignalStore

/**
 * Clears all existing key transparency data
 */
internal class ResetKeyTransparencyMigrationJob private constructor(parameters: Parameters) : MigrationJob(parameters) {

  companion object {
    const val KEY = "ResetKeyTransparencyMigrationJob"
  }

  internal constructor() : this(Parameters.Builder().build())

  override fun isUiBlocking(): Boolean = false

  override fun getFactoryKey(): String = KEY

  override fun performMigration() {
    SignalStore.account.distinguishedHead = null
    SignalStore.misc.lastKeyTransparencyTime = 0
    SignalDatabase.recipients.clearAllKeyTransparencyData()
    CheckKeyTransparencyJob.enqueueIfNecessary(addDelay = false)
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<ResetKeyTransparencyMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): ResetKeyTransparencyMigrationJob {
      return ResetKeyTransparencyMigrationJob(parameters)
    }
  }
}

package com.vprimex.messenger.migrations

import org.signal.core.util.logging.Log
import org.signal.core.util.readToList
import org.signal.core.util.requireLong
import org.signal.core.util.select
import com.vprimex.messenger.database.ChatFolderTables
import com.vprimex.messenger.database.ChatFolderTables.ChatFolderTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.jobmanager.Job
import com.vprimex.messenger.storage.StorageSyncHelper

/**
 * Marks all chat folders as needing to be synced for storage service.
 */
internal class SyncChatFoldersMigrationJob(parameters: Parameters = Parameters.Builder().build()) : MigrationJob(parameters) {
  companion object {
    const val KEY = "SyncChatFoldersMigrationJob"

    private val TAG = Log.tag(SyncChatFoldersMigrationJob::class)
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    val folderIds = SignalDatabase.chatFolders.getAllFoldersForSync()

    SignalDatabase.chatFolders.markNeedsSync(folderIds)
    StorageSyncHelper.scheduleSyncForDataChange()
  }

  override fun shouldRetry(e: Exception): Boolean = false

  private fun ChatFolderTables.getAllFoldersForSync(): List<Long> {
    return readableDatabase
      .select(ChatFolderTable.ID)
      .from(ChatFolderTable.TABLE_NAME)
      .run()
      .readToList { cursor -> cursor.requireLong(ChatFolderTable.ID) }
  }

  class Factory : Job.Factory<SyncChatFoldersMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SyncChatFoldersMigrationJob {
      return SyncChatFoldersMigrationJob(parameters)
    }
  }
}

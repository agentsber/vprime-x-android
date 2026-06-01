package com.vprimex.messenger.database.helpers.migration

import android.app.Application
import com.vprimex.messenger.database.SQLiteDatabase

/**
 * Adds support for storing the systemNickname from storage service.
 */
@Suppress("ClassName")
object V180_RecipientNicknameMigration : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("ALTER TABLE recipient ADD COLUMN system_nickname TEXT DEFAULT NULL")
  }
}

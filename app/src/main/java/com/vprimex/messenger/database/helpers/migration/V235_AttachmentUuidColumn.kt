/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.database.helpers.migration

import android.app.Application
import com.vprimex.messenger.database.SQLiteDatabase

/**
 * Add a column for attachment uuids
 */
@Suppress("ClassName")
object V235_AttachmentUuidColumn : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("ALTER TABLE attachment ADD COLUMN attachment_uuid TEXT DEFAULT NULL")
  }
}

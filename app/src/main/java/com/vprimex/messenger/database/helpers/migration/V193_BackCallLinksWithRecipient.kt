/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.database.helpers.migration

import android.app.Application
import com.vprimex.messenger.database.SQLiteDatabase

/**
 * Due to a bug, this has been replaced by [V196_BackCallLinksWithRecipientV2]
 */
@Suppress("ClassName")
object V193_BackCallLinksWithRecipient : SignalDatabaseMigration {

  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

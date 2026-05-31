/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.backup.v2.database

import com.vprimex.messenger.attachments.Attachment
import com.vprimex.messenger.attachments.AttachmentId
import com.vprimex.messenger.database.AttachmentTable

fun AttachmentTable.restoreWallpaperAttachment(attachment: Attachment): AttachmentId? {
  return insertAttachmentsForMessage(AttachmentTable.WALLPAPER_MESSAGE_ID, listOf(attachment), emptyList()).values.firstOrNull()
}

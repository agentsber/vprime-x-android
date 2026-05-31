package com.vprimex.messenger.database.model

import android.net.Uri
import com.vprimex.messenger.mms.PartAuthority

/**
 * Represents a record for a sticker pack in the [com.vprimex.messenger.database.StickerTable].
 */
data class StickerRecord(
  @JvmField val rowId: Long,
  @JvmField val packId: String,
  @JvmField val packKey: String,
  @JvmField val stickerId: Int,
  @JvmField val emoji: String,
  @JvmField val contentType: String,
  @JvmField val size: Long,
  @JvmField val isCover: Boolean
) {
  @JvmField
  val uri: Uri = PartAuthority.getStickerUri(rowId)
}

package com.vprimex.messenger.safety

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.vprimex.messenger.contacts.paged.ContactSearchKey
import com.vprimex.messenger.database.model.MessageId
import com.vprimex.messenger.recipients.RecipientId

/**
 * Fragment argument for `SafetyNumberBottomSheetFragment`
 */
@Parcelize
data class SafetyNumberBottomSheetArgs(
  val untrustedRecipients: List<RecipientId>,
  val destinations: List<ContactSearchKey.RecipientSearchKey>,
  val messageId: MessageId? = null
) : Parcelable

package com.vprimex.messenger.database.model

import android.net.Uri
import org.signal.core.models.ServiceId
import org.signal.core.models.ServiceId.ACI
import org.signal.core.models.ServiceId.PNI
import org.signal.libsignal.zkgroup.groups.GroupMasterKey
import org.signal.libsignal.zkgroup.profiles.ExpiringProfileKeyCredential
import com.vprimex.messenger.badges.models.Badge
import com.vprimex.messenger.conversation.colors.AvatarColor
import com.vprimex.messenger.conversation.colors.ChatColors
import com.vprimex.messenger.database.IdentityTable.VerifiedStatus
import com.vprimex.messenger.database.RecipientTable
import com.vprimex.messenger.database.RecipientTable.NotificationSetting
import com.vprimex.messenger.database.RecipientTable.PhoneNumberSharingState
import com.vprimex.messenger.database.RecipientTable.RegisteredState
import com.vprimex.messenger.database.RecipientTable.SealedSenderAccessMode
import com.vprimex.messenger.database.RecipientTable.VibrateState
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.profiles.ProfileName
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.service.webrtc.links.CallLinkRoomId
import com.vprimex.messenger.wallpaper.ChatWallpaper

/**
 * Database model for [RecipientTable].
 */
data class RecipientRecord(
  val id: RecipientId,
  val aci: ACI?,
  val pni: PNI?,
  val username: String?,
  val e164: String?,
  val email: String?,
  val groupId: GroupId?,
  val distributionListId: DistributionListId?,
  val recipientType: RecipientTable.RecipientType,
  val isBlocked: Boolean,
  val muteUntil: Long,
  val messageVibrateState: VibrateState,
  val callVibrateState: VibrateState,
  val messageRingtone: Uri?,
  val callRingtone: Uri?,
  val expireMessages: Int,
  val expireTimerVersion: Int,
  val registered: RegisteredState,
  val profileKey: ByteArray?,
  val expiringProfileKeyCredential: ExpiringProfileKeyCredential?,
  val systemProfileName: ProfileName,
  val systemDisplayName: String?,
  val systemContactPhotoUri: String?,
  val systemPhoneLabel: String?,
  val systemContactUri: String?,
  @get:JvmName("getProfileName")
  val signalProfileName: ProfileName,
  @get:JvmName("getProfileAvatar")
  val signalProfileAvatar: String?,
  val profileAvatarFileDetails: ProfileAvatarFileDetails,
  @get:JvmName("isProfileSharing")
  val profileSharing: Boolean,
  val notificationChannel: String?,
  val sealedSenderAccessMode: SealedSenderAccessMode,
  val capabilities: Capabilities,
  val storageId: ByteArray?,
  val mentionSetting: NotificationSetting,
  val callNotificationSetting: NotificationSetting,
  val replyNotificationSetting: NotificationSetting,
  val wallpaper: ChatWallpaper?,
  val chatColors: ChatColors?,
  val avatarColor: AvatarColor,
  val about: String?,
  val aboutEmoji: String?,
  val syncExtras: SyncExtras,
  val extras: Recipient.Extras?,
  @get:JvmName("hasGroupsInCommon")
  val hasGroupsInCommon: Boolean,
  val badges: List<Badge>,
  @get:JvmName("needsPniSignature")
  val needsPniSignature: Boolean,
  val hiddenState: Recipient.HiddenState,
  val callLinkRoomId: CallLinkRoomId?,
  val phoneNumberSharing: PhoneNumberSharingState,
  val nickname: ProfileName,
  val note: String?,
  val keyTransparencyData: ByteArray? = null
) {

  fun e164Only(): Boolean {
    return this.e164 != null && this.aci == null && this.pni == null
  }

  fun pniOnly(): Boolean {
    return this.e164 == null && this.aci == null && this.pni != null
  }

  fun aciOnly(): Boolean {
    return this.e164 == null && this.pni == null && this.aci != null
  }

  fun pniAndAci(): Boolean {
    return this.aci != null && this.pni != null
  }

  val serviceId: ServiceId? = this.aci ?: this.pni

  /**
   * A bundle of data that's only necessary when syncing to storage service, not for a
   * [Recipient].
   */
  data class SyncExtras(
    val storageProto: ByteArray?,
    val groupMasterKey: GroupMasterKey?,
    val identityKey: ByteArray?,
    val identityStatus: VerifiedStatus,
    val isArchived: Boolean,
    val isForcedUnread: Boolean,
    val unregisteredTimestamp: Long,
    val systemNickname: String?,
    val pniSignatureVerified: Boolean
  )

  data class Capabilities(
    val rawBits: Long
  ) {
    companion object {
      @JvmField
      val UNKNOWN = Capabilities(
        rawBits = 0
      )
    }
  }
}

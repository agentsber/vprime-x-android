/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.conversation.ui.edit

import android.net.Uri
import android.view.View
import androidx.lifecycle.Observer
import org.signal.ringrtc.CallLinkRootKey
import com.vprimex.messenger.components.voice.VoiceNotePlaybackState
import com.vprimex.messenger.contactshare.Contact
import com.vprimex.messenger.conversation.ConversationAdapter
import com.vprimex.messenger.conversation.ConversationItem
import com.vprimex.messenger.conversation.ConversationMessage
import com.vprimex.messenger.conversation.mutiselect.MultiselectPart
import com.vprimex.messenger.database.model.InMemoryMessageRecord
import com.vprimex.messenger.database.model.MessageRecord
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.groups.GroupMigrationMembershipChange
import com.vprimex.messenger.linkpreview.LinkPreview
import com.vprimex.messenger.mediapreview.MediaIntentFactory
import com.vprimex.messenger.polls.PollOption
import com.vprimex.messenger.polls.PollRecord
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.stickers.StickerLocator

/**
 * Empty object for when a callback can't be found.
 */
object EmptyConversationAdapterListener : ConversationAdapter.ItemClickListener {
  override fun onItemClick(item: MultiselectPart?) = Unit
  override fun onItemLongClick(itemView: View?, item: MultiselectPart?) = Unit
  override fun onQuoteClicked(messageRecord: MmsMessageRecord?) = Unit
  override fun onLinkPreviewClicked(linkPreview: LinkPreview) = Unit
  override fun onQuotedIndicatorClicked(messageRecord: MessageRecord) = Unit
  override fun onMoreTextClicked(conversationRecipientId: RecipientId, messageId: Long, isMms: Boolean) = Unit
  override fun onStickerClicked(stickerLocator: StickerLocator) = Unit
  override fun onViewOnceMessageClicked(messageRecord: MmsMessageRecord) = Unit
  override fun onSharedContactDetailsClicked(contact: Contact, avatarTransitionView: View) = Unit
  override fun onAddToContactsClicked(contact: Contact) = Unit
  override fun onMessageSharedContactClicked(choices: List<Recipient?>) = Unit
  override fun onInviteSharedContactClicked(choices: List<Recipient?>) = Unit
  override fun onReactionClicked(multiselectPart: MultiselectPart, messageId: Long, isMms: Boolean) = Unit
  override fun onGroupMemberClicked(recipientId: RecipientId, groupId: GroupId) = Unit
  override fun onMessageWithErrorClicked(messageRecord: MessageRecord) = Unit
  override fun onMessageWithRecaptchaNeededClicked(messageRecord: MessageRecord) = Unit
  override fun onIncomingIdentityMismatchClicked(recipientId: RecipientId) = Unit
  override fun onRegisterVoiceNoteCallbacks(onPlaybackStartObserver: Observer<VoiceNotePlaybackState?>) = Unit
  override fun onUnregisterVoiceNoteCallbacks(onPlaybackStartObserver: Observer<VoiceNotePlaybackState?>) = Unit
  override fun onVoiceNotePause(uri: Uri) = Unit
  override fun onVoiceNotePlay(uri: Uri, messageId: Long, position: Double) = Unit
  override fun onVoiceNoteSeekTo(uri: Uri, position: Double) = Unit
  override fun onVoiceNotePlaybackSpeedChanged(uri: Uri, speed: Float) = Unit
  override fun onGroupMigrationLearnMoreClicked(membershipChange: GroupMigrationMembershipChange) = Unit
  override fun onChatSessionRefreshLearnMoreClicked() = Unit
  override fun onBadDecryptLearnMoreClicked(author: RecipientId) = Unit
  override fun onSafetyNumberLearnMoreClicked(recipient: Recipient) = Unit
  override fun onJoinGroupCallClicked() = Unit
  override fun onInviteFriendsToGroupClicked(groupId: GroupId.V2) = Unit
  override fun onEnableCallNotificationsClicked() = Unit
  override fun onPlayInlineContent(conversationMessage: ConversationMessage?) = Unit
  override fun onInMemoryMessageClicked(messageRecord: InMemoryMessageRecord) = Unit
  override fun onViewGroupDescriptionChange(groupId: GroupId?, description: String, isMessageRequestAccepted: Boolean) = Unit
  override fun onChangeNumberUpdateContact(recipient: Recipient) = Unit
  override fun onChangeProfileNameUpdateContact(recipient: Recipient) = Unit
  override fun onCallToAction(action: String) = Unit
  override fun onDonateClicked() = Unit
  override fun onBlockJoinRequest(recipient: Recipient) = Unit
  override fun onRecipientNameClicked(target: RecipientId) = Unit
  override fun onInviteToSignalClicked() = Unit
  override fun onActivatePaymentsClicked() = Unit
  override fun onSendPaymentClicked(recipientId: RecipientId) = Unit
  override fun onScheduledIndicatorClicked(view: View, conversationMessage: ConversationMessage) = Unit
  override fun onUrlClicked(url: String): Boolean = false
  override fun onViewGiftBadgeClicked(messageRecord: MessageRecord) = Unit
  override fun onGiftBadgeRevealed(messageRecord: MessageRecord) = Unit
  override fun goToMediaPreview(parent: ConversationItem?, sharedElement: View?, args: MediaIntentFactory.MediaPreviewArgs?) = Unit
  override fun onEditedIndicatorClicked(conversationMessage: ConversationMessage) = Unit
  override fun onShowGroupDescriptionClicked(groupName: String, description: String, shouldLinkifyWebLinks: Boolean) = Unit
  override fun onJoinCallLink(callLinkRootKey: CallLinkRootKey) = Unit
  override fun onShowSafetyTips(forGroup: Boolean) = Unit
  override fun onReportSpamLearnMoreClicked() = Unit
  override fun onMessageRequestAcceptOptionsClicked() = Unit
  override fun onItemDoubleClick(multiselectPart: MultiselectPart?) = Unit
  override fun onPaymentTombstoneClicked() = Unit
  override fun onDisplayMediaNoLongerAvailableSheet() = Unit
  override fun onShowUnverifiedProfileSheet(forGroup: Boolean) = Unit
  override fun onUpdateSignalClicked() = Unit
  override fun onViewResultsClicked(pollId: Long) = Unit
  override fun onViewPollClicked(messageId: Long) = Unit
  override fun onToggleVote(poll: PollRecord, pollOption: PollOption, isChecked: Boolean?) = Unit
  override fun onViewPinnedMessage(messageId: Long) = Unit
  override fun onExpandEvents(messageId: Long, itemView: View, collapsedSize: Int) = Unit
  override fun onCollapseEvents(messageId: Long, itemView: View, collapsedSize: Int) = Unit
}

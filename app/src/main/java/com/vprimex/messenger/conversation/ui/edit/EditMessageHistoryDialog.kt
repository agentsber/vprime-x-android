package com.vprimex.messenger.conversation.ui.edit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.ui.BottomSheetUtil
import org.signal.core.ui.FixedRoundedCornerBottomSheetDialogFragment
import org.signal.core.util.concurrent.LifecycleDisposable
import com.vprimex.messenger.R
import com.vprimex.messenger.components.ViewBinderDelegate
import com.vprimex.messenger.conversation.ConversationAdapter
import com.vprimex.messenger.conversation.ConversationAdapterBridge
import com.vprimex.messenger.conversation.ConversationBottomSheetCallback
import com.vprimex.messenger.conversation.ConversationItemDisplayMode
import com.vprimex.messenger.conversation.ConversationMessage
import com.vprimex.messenger.conversation.colors.ColorizerV1
import com.vprimex.messenger.conversation.colors.RecyclerViewColorizer
import com.vprimex.messenger.conversation.mutiselect.MultiselectPart
import com.vprimex.messenger.conversation.quotes.OriginalMessageSeparatorDecoration
import com.vprimex.messenger.database.model.MessageRecord
import com.vprimex.messenger.database.model.MmsMessageRecord
import com.vprimex.messenger.databinding.MessageEditHistoryBottomSheetBinding
import com.vprimex.messenger.giph.mp4.GiphyMp4ItemDecoration
import com.vprimex.messenger.giph.mp4.GiphyMp4PlaybackController
import com.vprimex.messenger.giph.mp4.GiphyMp4PlaybackPolicy
import com.vprimex.messenger.giph.mp4.GiphyMp4ProjectionPlayerHolder
import com.vprimex.messenger.giph.mp4.GiphyMp4ProjectionRecycler
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.groups.GroupMigrationMembershipChange
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.util.StickyHeaderDecoration
import com.vprimex.messenger.util.ViewModelFactory
import com.vprimex.messenger.util.fragments.findListener
import java.util.Locale

/**
 * Show history of edits for a specific message.
 */
class EditMessageHistoryDialog : FixedRoundedCornerBottomSheetDialogFragment() {

  private val binding: MessageEditHistoryBottomSheetBinding by ViewBinderDelegate(MessageEditHistoryBottomSheetBinding::bind)
  private val originalMessageId: Long by lazy { requireArguments().getLong(ARGUMENT_ORIGINAL_MESSAGE_ID) }
  private val conversationRecipient: Recipient by lazy { Recipient.resolved(requireArguments().getParcelable(ARGUMENT_CONVERSATION_RECIPIENT_ID)!!) }
  private val viewModel: EditMessageHistoryViewModel by viewModels(factoryProducer = ViewModelFactory.factoryProducer { EditMessageHistoryViewModel(originalMessageId, conversationRecipient) })

  private val disposables: LifecycleDisposable = LifecycleDisposable()

  override fun onResume() {
    super.onResume()

    viewModel.markRevisionsRead()
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.behavior.skipCollapsed = true
    dialog.setOnShowListener {
      dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    return dialog
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return MessageEditHistoryBottomSheetBinding.inflate(inflater, container, false).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val callback = findListener<ConversationBottomSheetCallback>() ?: EmptyConversationBottomSheetCallback

    disposables.bindTo(viewLifecycleOwner)

    @Suppress("DEPRECATION")
    val colorizer = ColorizerV1()

    val messageAdapter = ConversationAdapter(
      requireContext(),
      viewLifecycleOwner,
      Glide.with(this),
      Locale.getDefault(),
      ConversationAdapterListener(callback),
      conversationRecipient.hasWallpaper,
      colorizer
    ).apply {
      setCondensedMode(ConversationItemDisplayMode.EditHistory)
    }

    binding.editHistoryList.apply {
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
      adapter = messageAdapter
      itemAnimator = null
      addItemDecoration(OriginalMessageSeparatorDecoration(context, R.string.EditMessageHistoryDialog_title) { 0 })
      doOnNextLayout {
        // Adding this without waiting for a layout pass would result in an indeterminate amount of padding added to the top of the view
        addItemDecoration(StickyHeaderDecoration(messageAdapter, false, false, ConversationAdapter.HEADER_TYPE_INLINE_DATE))
      }
    }

    val recyclerViewColorizer = RecyclerViewColorizer(binding.editHistoryList)

    disposables += viewModel
      .getEditHistory()
      .subscribeBy { messages ->
        if (messages.isEmpty()) {
          dismiss()
        }

        messageAdapter.submitList(messages)
        recyclerViewColorizer.setChatColors(conversationRecipient.chatColors)
      }

    disposables += viewModel.getNameColorsMap().subscribe { map ->
      colorizer.onNameColorsChanged(map)
      messageAdapter.notifyItemRangeChanged(0, messageAdapter.itemCount, ConversationAdapterBridge.PAYLOAD_NAME_COLORS)
    }

    initializeGiphyMp4()
  }

  private fun initializeGiphyMp4(): GiphyMp4ProjectionRecycler {
    val maxPlayback = GiphyMp4PlaybackPolicy.maxSimultaneousPlaybackInConversation()
    val holders = GiphyMp4ProjectionPlayerHolder.injectVideoViews(
      requireContext(),
      viewLifecycleOwner.lifecycle,
      binding.videoContainer,
      maxPlayback
    )
    val callback = GiphyMp4ProjectionRecycler(holders)

    GiphyMp4PlaybackController.attach(binding.editHistoryList, callback, maxPlayback)
    binding.editHistoryList.addItemDecoration(GiphyMp4ItemDecoration(callback), 0)

    return callback
  }

  private class ConversationAdapterListener(callback: ConversationBottomSheetCallback) : ConversationAdapter.ItemClickListener by callback.getConversationAdapterListener() {
    override fun onQuoteClicked(messageRecord: MmsMessageRecord) = Unit
    override fun onScheduledIndicatorClicked(view: View, conversationMessage: ConversationMessage) = Unit
    override fun onGroupMemberClicked(recipientId: RecipientId, groupId: GroupId) = Unit
    override fun onItemClick(item: MultiselectPart) = Unit
    override fun onItemLongClick(itemView: View, item: MultiselectPart) = Unit
    override fun onQuotedIndicatorClicked(messageRecord: MessageRecord) = Unit
    override fun onReactionClicked(multiselectPart: MultiselectPart, messageId: Long, isMms: Boolean) = Unit
    override fun onMessageWithRecaptchaNeededClicked(messageRecord: MessageRecord) = Unit
    override fun onGroupMigrationLearnMoreClicked(membershipChange: GroupMigrationMembershipChange) = Unit
    override fun onChatSessionRefreshLearnMoreClicked() = Unit
    override fun onBadDecryptLearnMoreClicked(author: RecipientId) = Unit
    override fun onSafetyNumberLearnMoreClicked(recipient: Recipient) = Unit
    override fun onJoinGroupCallClicked() = Unit
    override fun onInviteFriendsToGroupClicked(groupId: GroupId.V2) = Unit
    override fun onEnableCallNotificationsClicked() = Unit
    override fun onCallToAction(action: String) = Unit
    override fun onDonateClicked() = Unit
    override fun onRecipientNameClicked(target: RecipientId) = Unit
    override fun onViewGiftBadgeClicked(messageRecord: MessageRecord) = Unit
    override fun onActivatePaymentsClicked() = Unit
    override fun onSendPaymentClicked(recipientId: RecipientId) = Unit
    override fun onEditedIndicatorClicked(conversationMessage: ConversationMessage) = Unit
    override fun onShowSafetyTips(forGroup: Boolean) = Unit
    override fun onReportSpamLearnMoreClicked() = Unit
    override fun onMessageRequestAcceptOptionsClicked() = Unit
    override fun onItemDoubleClick(item: MultiselectPart) = Unit
  }

  companion object {
    private const val ARGUMENT_ORIGINAL_MESSAGE_ID = "message_id"
    private const val ARGUMENT_CONVERSATION_RECIPIENT_ID = "recipient_id"

    @JvmStatic
    fun show(fragmentManager: FragmentManager, threadRecipient: RecipientId, messageRecord: MessageRecord) {
      EditMessageHistoryDialog()
        .apply {
          arguments = bundleOf(
            ARGUMENT_ORIGINAL_MESSAGE_ID to (messageRecord.originalMessageId?.id ?: messageRecord.id),
            ARGUMENT_CONVERSATION_RECIPIENT_ID to threadRecipient
          )
        }
        .show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }
  }
}

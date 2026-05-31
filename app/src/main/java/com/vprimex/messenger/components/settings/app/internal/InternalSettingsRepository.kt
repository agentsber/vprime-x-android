package com.vprimex.messenger.components.settings.app.internal

import android.content.Context
import org.json.JSONObject
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.donations.InAppPaymentType
import com.vprimex.messenger.database.MessageTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.database.model.RemoteMegaphoneRecord
import com.vprimex.messenger.database.model.addButton
import com.vprimex.messenger.database.model.addLink
import com.vprimex.messenger.database.model.addStyle
import com.vprimex.messenger.database.model.databaseprotos.BodyRangeList
import com.vprimex.messenger.dependencies.AppDependencies
import com.vprimex.messenger.emoji.EmojiFiles
import com.vprimex.messenger.jobs.AttachmentDownloadJob
import com.vprimex.messenger.jobs.CreateReleaseChannelJob
import com.vprimex.messenger.jobs.FetchRemoteMegaphoneImageJob
import com.vprimex.messenger.jobs.InAppPaymentRecurringContextJob
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.notifications.v2.ConversationId
import com.vprimex.messenger.recipients.Recipient
import com.vprimex.messenger.releasechannel.ReleaseChannel
import java.util.UUID
import kotlin.time.Duration.Companion.days

class InternalSettingsRepository(context: Context) {

  private val context = context.applicationContext

  fun getEmojiVersionInfo(consumer: (EmojiFiles.Version?) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      consumer(EmojiFiles.Version.readVersion(context))
    }
  }

  fun enqueueSubscriptionRedemption() {
    SignalExecutors.BOUNDED.execute {
      val latest = SignalDatabase.inAppPayments.getByLatestEndOfPeriod(InAppPaymentType.RECURRING_DONATION)
      if (latest != null) {
        InAppPaymentRecurringContextJob.createJobChain(latest).enqueue()
      }
    }
  }

  fun addSampleReleaseNote(callToAction: String) {
    SignalExecutors.UNBOUNDED.execute {
      AppDependencies.jobManager.runSynchronously(CreateReleaseChannelJob.create(), 5000)

      val title = "Release Note Title"
      val bodyText = "Release note body. Aren't I awesome?"
      val linkUrl = "https://signal.org"
      val body = "$title\n\n$bodyText\n\n$linkUrl"
      val linkStart = body.length - linkUrl.length
      val bodyRangeList = BodyRangeList.Builder()
        .addStyle(BodyRangeList.BodyRange.Style.BOLD, 0, title.length)
        .addLink(linkUrl, linkStart, linkUrl.length)

      bodyRangeList.addButton("Call to Action Text", callToAction, body.lastIndex, 0)

      val recipientId = SignalStore.releaseChannel.releaseChannelRecipientId!!
      val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipientId))

      val insertResult: MessageTable.InsertResult? = ReleaseChannel.insertReleaseChannelMessage(
        recipientId = recipientId,
        body = body,
        threadId = threadId,
        messageRanges = bodyRangeList.build(),
        media = "/static/release-notes/signal.png",
        mediaWidth = 1800,
        mediaHeight = 720
      )

      SignalDatabase.messages.insertBoostRequestMessage(recipientId, threadId)

      if (insertResult != null) {
        SignalDatabase.attachments.getAttachmentsForMessage(insertResult.messageId)
          .forEach { AppDependencies.jobManager.add(AttachmentDownloadJob(insertResult.messageId, it.attachmentId, false)) }

        AppDependencies.messageNotifier.updateNotification(context, ConversationId.forConversation(insertResult.threadId))
      }
    }
  }

  fun addRemoteMegaphone(actionId: RemoteMegaphoneRecord.ActionId) {
    SignalExecutors.UNBOUNDED.execute {
      val record = RemoteMegaphoneRecord(
        uuid = UUID.randomUUID().toString(),
        priority = 100,
        countries = "*:1000000",
        minimumVersion = 1,
        doNotShowBefore = System.currentTimeMillis() - 2.days.inWholeMilliseconds,
        doNotShowAfter = System.currentTimeMillis() + 28.days.inWholeMilliseconds,
        showForNumberOfDays = 30,
        conditionalId = null,
        primaryActionId = actionId,
        secondaryActionId = RemoteMegaphoneRecord.ActionId.SNOOZE,
        imageUrl = "/static/release-notes/donate-heart.png",
        title = "Donate Test",
        body = "Donate body test.",
        primaryActionText = "Donate",
        secondaryActionText = "Snooze",
        primaryActionData = null,
        secondaryActionData = JSONObject("{ \"snoozeDurationDays\": [5, 7, 100] }")
      )

      SignalDatabase.remoteMegaphones.insert(record)

      if (record.imageUrl != null) {
        AppDependencies.jobManager.add(FetchRemoteMegaphoneImageJob(record.uuid, record.imageUrl))
      }
    }
  }
}

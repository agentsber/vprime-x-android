package com.vprimex.messenger.messagedetails;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import org.signal.core.util.concurrent.SignalExecutors;
import com.vprimex.messenger.database.DatabaseObserver;
import com.vprimex.messenger.database.MessageTable;
import com.vprimex.messenger.database.NoSuchMessageException;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.database.model.MessageId;
import com.vprimex.messenger.database.model.MessageRecord;
import com.vprimex.messenger.dependencies.AppDependencies;

final class MessageRecordLiveData extends LiveData<MessageRecord> {

  private final DatabaseObserver.Observer observer;
  private final MessageId                 messageId;

  MessageRecordLiveData(MessageId messageId) {
    this.messageId = messageId;
    this.observer  = this::retrieveMessageRecordActual;
  }

  @Override
  protected void onActive() {
    SignalExecutors.BOUNDED_IO.execute(this::retrieveMessageRecordActual);
  }

  @Override
  protected void onInactive() {
    AppDependencies.getDatabaseObserver().unregisterObserver(observer);
  }

  @WorkerThread
  private synchronized void retrieveMessageRecordActual() {
    try {
      MessageRecord record = MessageTable.withAttachmentData(SignalDatabase.messages().getMessageRecord(messageId.getId()));

      if (record.isPaymentNotification()) {
        record = SignalDatabase.payments().updateMessageWithPayment(record);
      }

      postValue(record);
      AppDependencies.getDatabaseObserver().registerVerboseConversationObserver(record.getThreadId(), observer);
    } catch (NoSuchMessageException ignored) {
      postValue(null);
    }
  }
}

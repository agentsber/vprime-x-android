package com.vprimex.messenger.revealable;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import com.vprimex.messenger.database.MessageTable;
import com.vprimex.messenger.database.NoSuchMessageException;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.database.model.MmsMessageRecord;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.jobs.MultiDeviceViewedUpdateJob;
import com.vprimex.messenger.jobs.SendViewedReceiptJob;

import java.util.Collections;
import java.util.Optional;

class ViewOnceMessageRepository {

  private static final String TAG = Log.tag(ViewOnceMessageRepository.class);

  private final MessageTable mmsDatabase;

  ViewOnceMessageRepository(@NonNull Context context) {
    this.mmsDatabase = SignalDatabase.messages();
  }

  void getMessage(long messageId, @NonNull Callback<Optional<MmsMessageRecord>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      try {
        MmsMessageRecord record = (MmsMessageRecord) mmsDatabase.getMessageRecord(messageId);

        MessageTable.MarkedMessageInfo info = mmsDatabase.setIncomingMessageViewed(record.getId());
        if (info != null) {
          AppDependencies.getJobManager().add(new SendViewedReceiptJob(record.getThreadId(),
                                                                       info.getSyncMessageId().getRecipientId(),
                                                                       info.getSyncMessageId().getTimetamp(),
                                                                       info.getMessageId()));
          MultiDeviceViewedUpdateJob.enqueue(Collections.singletonList(info.getSyncMessageId()));
        }

        callback.onComplete(Optional.ofNullable(record));
      } catch (NoSuchMessageException e) {
        callback.onComplete(Optional.empty());
      }
    });
  }

  interface Callback<T> {
    void onComplete(T result);
  }
}

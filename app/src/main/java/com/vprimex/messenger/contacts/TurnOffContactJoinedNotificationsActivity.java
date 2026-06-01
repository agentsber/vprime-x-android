package com.vprimex.messenger.contacts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.vprimex.messenger.R;
import com.vprimex.messenger.database.MessageTable;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.database.ThreadTable;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.notifications.MarkReadReceiver;
import org.signal.core.util.concurrent.SimpleTask;

import java.util.List;

/**
 * Activity which displays a dialog to confirm whether to turn off "Contact Joined Signal" notifications.
 */
public class TurnOffContactJoinedNotificationsActivity extends AppCompatActivity {

  private final static String EXTRA_THREAD_ID = "thread_id";

  public static Intent newIntent(@NonNull Context context, long threadId) {
    Intent intent = new Intent(context, TurnOffContactJoinedNotificationsActivity.class);

    intent.putExtra(EXTRA_THREAD_ID, threadId);

    return intent;
  }

  @Override
  protected void onResume() {
    super.onResume();

    new MaterialAlertDialogBuilder(this)
        .setMessage(R.string.TurnOffContactJoinedNotificationsActivity__turn_off_contact_joined_signal)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          handlePositiveAction(dialog);
        })
        .setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
          dialog.dismiss();
        }))
        .setOnDismissListener(dialog -> finish())
        .show();
  }

  private void handlePositiveAction(@NonNull DialogInterface dialog) {
    SimpleTask.run(getLifecycle(), () -> {
      ThreadTable threadTable = SignalDatabase.threads();

      List<MessageTable.MarkedMessageInfo> marked = threadTable.setRead(getIntent().getLongExtra(EXTRA_THREAD_ID, -1));
      MarkReadReceiver.process(marked);

      SignalStore.settings().setNotifyWhenContactJoinsSignal(false);
      AppDependencies.getMessageNotifier().updateNotification(this);

      return null;
    }, unused -> {
      dialog.dismiss();
    });
  }
}

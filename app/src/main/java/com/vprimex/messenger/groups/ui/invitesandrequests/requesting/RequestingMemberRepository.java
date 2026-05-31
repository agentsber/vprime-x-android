package com.vprimex.messenger.groups.ui.invitesandrequests.requesting;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import com.vprimex.messenger.groups.GroupChangeException;
import com.vprimex.messenger.groups.GroupId;
import com.vprimex.messenger.groups.GroupManager;
import com.vprimex.messenger.groups.ui.GroupChangeFailureReason;
import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.util.AsynchronousCallback;

import java.io.IOException;
import java.util.Collections;

/**
 * Repository for modifying the requesting members on a single group.
 */
final class RequestingMemberRepository {

  private static final String TAG = Log.tag(RequestingMemberRepository.class);

  private final Context    context;
  private final GroupId.V2 groupId;

  RequestingMemberRepository(@NonNull Context context, @NonNull GroupId.V2 groupId) {
    this.context = context.getApplicationContext();
    this.groupId = groupId;
  }

  void approveRequest(@NonNull Recipient recipient,
                      @NonNull AsynchronousCallback.WorkerThread<Void, GroupChangeFailureReason> callback)
  {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        GroupManager.approveRequests(context, groupId, Collections.singleton(recipient.getId()));
        callback.onComplete(null);
      } catch (GroupChangeException | IOException e) {
        Log.w(TAG, e);
        callback.onError(GroupChangeFailureReason.fromException(e));
      }
    });
  }

  void denyRequest(@NonNull Recipient recipient,
                   @NonNull AsynchronousCallback.WorkerThread<Void, GroupChangeFailureReason> callback)
  {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        GroupManager.denyRequests(context, groupId, Collections.singleton(recipient.getId()));
        callback.onComplete(null);
      } catch (GroupChangeException | IOException e) {
        Log.w(TAG, e);
        callback.onError(GroupChangeFailureReason.fromException(e));
      }
    });
  }
}

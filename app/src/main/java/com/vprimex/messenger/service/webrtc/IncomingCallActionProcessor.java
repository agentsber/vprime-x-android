package com.vprimex.messenger.service.webrtc;

import android.net.Uri;
import android.os.ResultReceiver;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.signal.ringrtc.CallException;
import org.signal.ringrtc.CallId;
import org.signal.ringrtc.CallManager;
import com.vprimex.messenger.database.CallTable;
import com.vprimex.messenger.database.RecipientTable;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.events.CallParticipant;
import com.vprimex.messenger.events.WebRtcViewModel;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.notifications.DoNotDisturbUtil;
import com.vprimex.messenger.notifications.NotificationChannels;
import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.recipients.RecipientId;
import com.vprimex.messenger.ringrtc.CallState;
import com.vprimex.messenger.ringrtc.OutgoingVideoSourceRouter;
import com.vprimex.messenger.ringrtc.RemotePeer;
import com.vprimex.messenger.service.webrtc.state.CallSetupState;
import com.vprimex.messenger.service.webrtc.state.VideoState;
import com.vprimex.messenger.service.webrtc.state.WebRtcServiceState;
import org.signal.core.util.AppForegroundObserver;
import com.vprimex.messenger.util.NetworkUtil;
import org.signal.core.util.Util;
import com.vprimex.messenger.util.RemoteConfig;
import com.vprimex.messenger.webrtc.locks.LockManager;
import org.webrtc.PeerConnection;

import java.util.List;
import java.util.Objects;

import static com.vprimex.messenger.webrtc.CallNotificationBuilder.TYPE_INCOMING_RINGING;

/**
 * Responsible for setting up and managing the start of an incoming 1:1 call. Transitioned
 * to from idle or pre-join and can either move to a connected state (user picks up) or
 * a disconnected state (remote hangup, local hangup, etc.).
 */
public class IncomingCallActionProcessor extends DeviceAwareActionProcessor {

  private static final String TAG = Log.tag(IncomingCallActionProcessor.class);

  private final ActiveCallActionProcessorDelegate activeCallDelegate;
  private final CallSetupActionProcessorDelegate  callSetupDelegate;

  public IncomingCallActionProcessor(@NonNull WebRtcInteractor webRtcInteractor) {
    super(webRtcInteractor, TAG);
    activeCallDelegate = new ActiveCallActionProcessorDelegate(webRtcInteractor, TAG);
    callSetupDelegate  = new CallSetupActionProcessorDelegate(webRtcInteractor, TAG);
  }

  @Override
  protected @NonNull WebRtcServiceState handleIsInCallQuery(@NonNull WebRtcServiceState currentState, @Nullable ResultReceiver resultReceiver) {
    return activeCallDelegate.handleIsInCallQuery(currentState, resultReceiver);
  }

  @Override
  public @NonNull WebRtcServiceState handleTurnServerUpdate(@NonNull WebRtcServiceState currentState,
                                                            @NonNull List<PeerConnection.IceServer> iceServers,
                                                            boolean isAlwaysTurn)
  {
    RemotePeer activePeer = currentState.getCallInfoState().requireActivePeer();

    Log.i(TAG, "handleTurnServerUpdate(): call_id: " + activePeer.getCallId());

    currentState = currentState.builder()
                               .changeCallSetupState(activePeer.getCallId())
                               .iceServers(iceServers)
                               .alwaysTurn(isAlwaysTurn)
                               .build();

    return proceed(currentState);
  }

  @Override
  protected @NonNull WebRtcServiceState handleSetTelecomApproved(@NonNull WebRtcServiceState currentState, long callId, RecipientId recipientId) {
    return proceed(super.handleSetTelecomApproved(currentState, callId, recipientId));
  }

  private @NonNull WebRtcServiceState proceed(@NonNull WebRtcServiceState currentState) {
    RemotePeer     activePeer     = currentState.getCallInfoState().requireActivePeer();
    CallSetupState callSetupState = currentState.getCallSetupState(activePeer.getCallId());

    if (callSetupState.getIceServers().isEmpty() || (callSetupState.shouldWaitForTelecomApproval() && !callSetupState.isTelecomApproved())) {
      Log.i(TAG, "Unable to proceed without ice server and telecom approval" +
                 " iceServers: " + Util.hasItems(callSetupState.getIceServers()) +
                 " waitForTelecom: " + callSetupState.shouldWaitForTelecomApproval() +
                 " telecomApproved: " + callSetupState.isTelecomApproved());
      return currentState;
    }

    byte            dredDuration    = (byte) RemoteConfig.dredDuration();
    boolean         enableVp9       = RemoteConfig.enableSoftwareVp9();
    boolean         hideIp          = !activePeer.getRecipient().isProfileSharing() || callSetupState.isAlwaysTurnServers();
    VideoState      videoState      = currentState.getVideoState();
    CallParticipant callParticipant = Objects.requireNonNull(currentState.getCallInfoState().getRemoteCallParticipant(activePeer.getRecipient()));

    try {
      webRtcInteractor.getCallManager().proceed(activePeer.getCallId(),
                                                context,
                                                videoState.getLockableEglBase().require(),
                                                RingRtcDynamicConfiguration.getAudioConfig(),
                                                videoState.requireLocalSink(),
                                                callParticipant.getVideoSink(),
                                                videoState.requireRouter(),
                                                callSetupState.getIceServers(),
                                                hideIp,
                                                NetworkUtil.getCallingDataMode(context),
                                                AUDIO_LEVELS_INTERVAL,
                                                dredDuration,
                                                enableVp9,
                                                false);
    } catch (CallException e) {
      return callFailure(currentState, "Unable to proceed with call: ", e);
    }

    webRtcInteractor.updatePhoneState(LockManager.PhoneState.PROCESSING);
    webRtcInteractor.postStateUpdate(currentState);

    return currentState;
  }

  @Override
  protected @NonNull WebRtcServiceState handleDropCall(@NonNull WebRtcServiceState currentState, long callId) {
    return callSetupDelegate.handleDropCall(currentState, callId);
  }

  @Override
  protected @NonNull WebRtcServiceState handleAcceptCall(@NonNull WebRtcServiceState currentState, boolean answerWithVideo) {
    RemotePeer activePeer = currentState.getCallInfoState().requireActivePeer();

    Log.i(TAG, "handleAcceptCall(): call_id: " + activePeer.getCallId());

    OutgoingVideoSourceRouter router = currentState.getVideoState().requireRouter();
    router.setVanitySink(null);

    if (!answerWithVideo && currentState.getLocalDeviceState().getCameraState().isEnabled()) {
      router.setEnabled(false);
      currentState = currentState.builder()
                                 .changeLocalDeviceState()
                                 .cameraState(router.getCameraState())
                                 .build();
    }

    currentState = currentState.builder()
                               .changeCallSetupState(activePeer.getCallId())
                               .acceptWithVideo(answerWithVideo)
                               .build();

    try {
      webRtcInteractor.getCallManager().acceptCall(activePeer.getCallId());
    } catch (CallException e) {
      return callFailure(currentState, "accept() failed: ", e);
    }

    return currentState;
  }

  protected @NonNull WebRtcServiceState handleDenyCall(@NonNull WebRtcServiceState currentState) {
    RemotePeer activePeer = currentState.getCallInfoState().requireActivePeer();

    if (activePeer.getState() != CallState.LOCAL_RINGING) {
      Log.w(TAG, "Can only deny from ringing!");
      return currentState;
    }

    Log.i(TAG, "handleDenyCall():");

    OutgoingVideoSourceRouter router = currentState.getVideoState().getRouter();
    if (router != null) {
      router.setVanitySink(null);
    }

    webRtcInteractor.sendNotAcceptedCallEventSyncMessage(activePeer,
                                                         false,
                                                         currentState.getCallSetupState(activePeer).isRemoteVideoOffer());

    try {
      webRtcInteractor.rejectIncomingCall(activePeer.getId());
      webRtcInteractor.getCallManager().hangup();
      return terminate(currentState, activePeer);
    } catch (CallException e) {
      return callFailure(currentState, "hangup() failed: ", e);
    }
  }

  @Override
  protected @NonNull WebRtcServiceState handleSetIncomingRingingVanity(@NonNull WebRtcServiceState currentState, boolean enabled) {
    RemotePeer activePeer = currentState.getCallInfoState().requireActivePeer();
    boolean    isVideoOffer = currentState.getCallSetupState(activePeer).isRemoteVideoOffer();

    if (!isVideoOffer) {
      return currentState;
    }

    boolean cameraAlreadyEnabled = currentState.getLocalDeviceState().getCameraState().isEnabled();

    if (enabled && cameraAlreadyEnabled) {
      return currentState;
    }

    if (!enabled && !cameraAlreadyEnabled) {
      return currentState;
    }

    OutgoingVideoSourceRouter router = currentState.getVideoState().requireRouter();

    if (enabled) {
      Log.i(TAG, "handleSetIncomingRingingVanity(): enabling vanity camera");
      router.setVanitySink(currentState.getVideoState().requireLocalSink());
      router.setEnabled(true);
    } else {
      Log.i(TAG, "handleSetIncomingRingingVanity(): disabling vanity camera");
      router.setVanitySink(null);
      router.setEnabled(false);
    }

    return currentState.builder()
                       .changeLocalDeviceState()
                       .cameraState(router.getCameraState())
                       .build();
  }

  protected @NonNull WebRtcServiceState handleLocalRinging(@NonNull WebRtcServiceState currentState, @NonNull RemotePeer remotePeer) {
    Log.i(TAG, "handleLocalRinging(): call_id: " + remotePeer.getCallId());

    RemotePeer activePeer                = currentState.getCallInfoState().requireActivePeer();
    Recipient  recipient                 = remotePeer.getRecipient();
    boolean    shouldDisturbUserWithCall = DoNotDisturbUtil.shouldDisturbUserWithCall(context.getApplicationContext(), recipient);

    activePeer.localRinging();

    SignalDatabase.calls().insertOneToOneCall(remotePeer.getCallId().longValue(),
                                              System.currentTimeMillis(),
                                              remotePeer.getId(),
                                      currentState.getCallSetupState(activePeer).isRemoteVideoOffer() ? CallTable.Type.VIDEO_CALL : CallTable.Type.AUDIO_CALL,
                                              CallTable.Direction.INCOMING,
                                              CallTable.Event.ONGOING);

    if (!shouldDisturbUserWithCall) {
      Log.i(TAG, "Silently ignoring call due to mute settings.");
      return currentState.builder()
                         .changeCallInfoState()
                         .callState(WebRtcViewModel.State.CALL_INCOMING)
                         .build();
    }

    webRtcInteractor.updatePhoneState(LockManager.PhoneState.INTERACTIVE);
    boolean started = webRtcInteractor.startWebRtcCallActivityIfPossible();
    if (!started) {
      Log.i(TAG, "Unable to start call activity due to OS version or not being in the foreground");
      AppForegroundObserver.addListener(webRtcInteractor.getForegroundListener());
    }

    boolean isCallNotificationsEnabled = SignalStore.settings().isCallNotificationsEnabled() && NotificationChannels.getInstance().areNotificationsEnabled();
    if (isCallNotificationsEnabled) {
      Uri                         ringtone     = recipient.resolve().getCallRingtone();
      RecipientTable.VibrateState vibrateState = recipient.resolve().getCallVibrate();

      if (ringtone == null) {
        ringtone = SignalStore.settings().getCallRingtone();
      }

      if (TextUtils.isEmpty(ringtone.toString())) {
        Log.i(TAG, "Ringtone is likely set to silent");
        ringtone = null;
      }

      webRtcInteractor.startIncomingRinger(ringtone, vibrateState == RecipientTable.VibrateState.ENABLED || (vibrateState == RecipientTable.VibrateState.DEFAULT && SignalStore.settings().isCallVibrateEnabled()));
    }

    boolean isRemoteVideoOffer = currentState.getCallSetupState(activePeer).isRemoteVideoOffer();

    webRtcInteractor.setCallInProgressNotification(TYPE_INCOMING_RINGING, activePeer, isRemoteVideoOffer);
    webRtcInteractor.registerPowerButtonReceiver();

    return currentState.builder()
                       .changeCallInfoState()
                       .callState(WebRtcViewModel.State.CALL_INCOMING)
                       .build();
  }

  protected @NonNull WebRtcServiceState handleScreenOffChange(@NonNull WebRtcServiceState currentState) {
    Log.i(TAG, "Silencing incoming ringer...");

    webRtcInteractor.silenceIncomingRinger();
    return currentState;
  }

  @Override
  protected @NonNull WebRtcServiceState handleRemoteAudioEnable(@NonNull WebRtcServiceState currentState, boolean enable) {
    return activeCallDelegate.handleRemoteAudioEnable(currentState, enable);
  }

  @Override
  protected @NonNull WebRtcServiceState handleRemoteVideoEnable(@NonNull WebRtcServiceState currentState, boolean enable) {
    return activeCallDelegate.handleRemoteVideoEnable(currentState, enable);
  }

  @Override
  protected @NonNull WebRtcServiceState handleScreenSharingEnable(@NonNull WebRtcServiceState currentState, boolean enable) {
    return activeCallDelegate.handleScreenSharingEnable(currentState, enable);
  }

  @Override
  protected @NonNull WebRtcServiceState handleReceivedOfferWhileActive(@NonNull WebRtcServiceState currentState, @NonNull RemotePeer remotePeer) {
    return activeCallDelegate.handleReceivedOfferWhileActive(currentState, remotePeer);
  }

  @Override
  protected @NonNull WebRtcServiceState handleEndedRemote(@NonNull WebRtcServiceState currentState, @NonNull CallManager.CallEndReason callEndReason, @NonNull RemotePeer remotePeer) {
    return activeCallDelegate.handleEndedRemote(currentState, callEndReason, remotePeer);
  }

  @Override
  protected @NonNull WebRtcServiceState handleEnded(@NonNull WebRtcServiceState currentState, @NonNull CallManager.CallEndReason callEndReason, @NonNull RemotePeer remotePeer) {
    return activeCallDelegate.handleEnded(currentState, callEndReason, remotePeer);
  }

  @Override
  protected @NonNull WebRtcServiceState handleSetupFailure(@NonNull WebRtcServiceState currentState, @NonNull CallId callId) {
    return activeCallDelegate.handleSetupFailure(currentState, callId);
  }

  @Override
  public @NonNull WebRtcServiceState handleCallConnected(@NonNull WebRtcServiceState currentState, @NonNull RemotePeer remotePeer) {
    return callSetupDelegate.handleCallConnected(currentState, remotePeer);
  }

  @Override
  protected @NonNull WebRtcServiceState handleSetEnableVideo(@NonNull WebRtcServiceState currentState, boolean enable) {
    return callSetupDelegate.handleSetEnableVideo(currentState, enable);
  }
}

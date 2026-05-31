/*
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vprimex.messenger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import com.bumptech.glide.Glide;
import com.google.android.gms.security.ProviderInstaller;

import net.zetetic.database.Logger;

import org.conscrypt.ConscryptSignal;
import org.greenrobot.eventbus.EventBus;
import org.signal.aesgcmprovider.AesGcmProvider;
import org.signal.core.util.DiskUtil;
import org.signal.core.util.MemoryTracker;
import org.signal.core.util.concurrent.AnrDetector;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.AndroidLogger;
import org.signal.core.util.logging.Log;
import org.signal.core.util.logging.Scrubber;
import org.signal.core.util.tracing.Tracer;
import org.signal.glide.SignalGlideCodecs;
import org.signal.libsignal.net.ChatServiceException;
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider;
import org.signal.ringrtc.CallManager;
import com.vprimex.messenger.apkupdate.ApkUpdateRefreshListener;
import com.vprimex.messenger.avatar.AvatarPickerStorage;
import com.vprimex.messenger.backup.v2.BackupRepository;
import com.vprimex.messenger.crypto.AttachmentSecretProvider;
import com.vprimex.messenger.crypto.DatabaseSecretProvider;
import com.vprimex.messenger.database.LogDatabase;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.database.SqlCipherLibraryLoader;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.dependencies.ApplicationDependencyProvider;
import com.vprimex.messenger.emoji.EmojiSource;
import com.vprimex.messenger.emoji.JumboEmoji;
import com.vprimex.messenger.gcm.FcmFetchManager;
import com.vprimex.messenger.glide.SignalGlideComponents;
import com.vprimex.messenger.jobs.AccountConsistencyWorkerJob;
import com.vprimex.messenger.jobs.BackupRefreshJob;
import com.vprimex.messenger.jobs.BackupSubscriptionCheckJob;
import com.vprimex.messenger.jobs.BuildExpirationConfirmationJob;
import com.vprimex.messenger.jobs.CallingAssetsDownloadJob;
import com.vprimex.messenger.jobs.CheckKeyTransparencyJob;
import com.vprimex.messenger.jobs.CheckServiceReachabilityJob;
import com.vprimex.messenger.jobs.DownloadLatestEmojiDataJob;
import com.vprimex.messenger.jobs.EmojiSearchIndexDownloadJob;
import com.vprimex.messenger.jobs.FcmRefreshJob;
import com.vprimex.messenger.jobs.FontDownloaderJob;
import com.vprimex.messenger.jobs.GroupRingCleanupJob;
import com.vprimex.messenger.jobs.GroupV2UpdateSelfProfileKeyJob;
import com.vprimex.messenger.jobs.InAppPaymentAuthCheckJob;
import com.vprimex.messenger.jobs.InAppPaymentKeepAliveJob;
import com.vprimex.messenger.jobs.LinkedDeviceInactiveCheckJob;
import com.vprimex.messenger.jobs.MultiDeviceContactUpdateJob;
import com.vprimex.messenger.jobs.PreKeysSyncJob;
import com.vprimex.messenger.jobs.ProfileUploadJob;
import com.vprimex.messenger.jobs.RefreshAttributesJob;
import com.vprimex.messenger.jobs.RefreshSvrCredentialsJob;
import com.vprimex.messenger.jobs.RestoreOptimizedMediaJob;
import com.vprimex.messenger.jobs.RetrieveProfileJob;
import com.vprimex.messenger.jobs.RetrieveRemoteAnnouncementsJob;
import com.vprimex.messenger.jobmanager.impl.SealedSenderConstraint;
import com.vprimex.messenger.jobs.StoryOnboardingDownloadJob;
import com.vprimex.messenger.keyvalue.KeepMessagesDuration;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.logging.CustomSignalProtocolLogger;
import com.vprimex.messenger.logging.PersistentLogger;
import com.vprimex.messenger.logsubmit.SubmitDebugLogActivity;
import com.vprimex.messenger.messageprocessingalarm.RoutineMessageFetchReceiver;
import com.vprimex.messenger.messages.IncomingMessageObserver;
import com.vprimex.messenger.migrations.ApplicationMigrations;
import com.vprimex.messenger.mms.SignalGlideModule;
import com.vprimex.messenger.providers.BlobProvider;
import com.vprimex.messenger.ratelimit.RateLimitUtil;
import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.registration.util.RegistrationUtil;
import com.vprimex.messenger.ringrtc.RingRtcLogger;
import com.vprimex.messenger.service.AnalyzeDatabaseAlarmListener;
import com.vprimex.messenger.service.DirectoryRefreshListener;
import com.vprimex.messenger.service.KeyCachingService;
import com.vprimex.messenger.service.LocalBackupListener;
import com.vprimex.messenger.service.MessageBackupListener;
import com.vprimex.messenger.service.RotateSenderCertificateListener;
import com.vprimex.messenger.service.RotateSignedPreKeyListener;
import com.vprimex.messenger.service.webrtc.ActiveCallManager;
import com.vprimex.messenger.service.webrtc.CallingAssets;
import com.vprimex.messenger.service.webrtc.AndroidTelecomUtil;
import com.vprimex.messenger.storage.StorageSyncHelper;
import org.signal.core.util.AppForegroundObserver;
import com.vprimex.messenger.util.AppStartup;
import com.vprimex.messenger.util.DeviceProperties;
import com.vprimex.messenger.util.DynamicTheme;
import com.vprimex.messenger.util.Environment;
import com.vprimex.messenger.util.PlayServicesUtil;
import com.vprimex.messenger.util.RemoteConfig;
import com.vprimex.messenger.util.SignalLocalMetrics;
import com.vprimex.messenger.util.SignalUncaughtExceptionHandler;
import com.vprimex.messenger.util.SqlCipherLogTarget;
import com.vprimex.messenger.util.TextSecurePreferences;
import org.signal.core.util.Util;
import com.vprimex.messenger.util.VersionTracker;
import com.vprimex.messenger.util.dynamiclanguage.DynamicLanguageContextWrapper;
import org.whispersystems.signalservice.api.websocket.SignalWebSocket;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import rxdogtag2.RxDogTag;

/**
 * Will be called once when the TextSecure process is created.
 * <p>
 * We're using this as an insertion point to patch up the Android PRNG disaster,
 * to initialize the job manager, and to check for GCM registration freshness.
 *
 * @author Moxie Marlinspike
 */
public class ApplicationContext extends Application implements AppForegroundObserver.Listener {

  private static final String TAG = Log.tag(ApplicationContext.class);

  public static ApplicationContext getInstance(Context context) {
    return (ApplicationContext) context.getApplicationContext();
  }

  @Override
  public void onCreate() {
    Tracer.getInstance().start("Application#onCreate()");
    AppStartup.getInstance().onApplicationCreate();
    SignalLocalMetrics.ColdStart.start();

    long startTime = System.currentTimeMillis();

    super.onCreate();

    AppStartup.getInstance().addBlocking("sqlcipher-init", () -> {
                SqlCipherLibraryLoader.load();
                SignalDatabase.init(this,
                                    DatabaseSecretProvider.getOrCreateDatabaseSecret(this),
                                    AttachmentSecretProvider.getInstance(this).getOrCreateAttachmentSecret());
                Logger.setTarget(SqlCipherLogTarget.INSTANCE);
              })
              .addBlocking("signal-store", () -> SignalStore.init(this))
              .addBlocking("logging", () -> {
                initializeLogging();
                Log.i(TAG, "onCreate()");
              })
              .addBlocking("app-dependencies", this::initializeAppDependencies)
              .addBlocking("anr-detector", this::startAnrDetector)
              .addBlocking("security-provider", this::initializeSecurityProvider)
              .addBlocking("crash-handling", this::initializeCrashHandling)
              .addBlocking("rx-init", this::initializeRx)
              .addBlocking("event-bus", () -> EventBus.builder().logNoSubscriberMessages(false).installDefaultEventBus())
              .addBlocking("scrubber", () -> Scrubber.setIdentifierHmacKeyProvider(() -> SignalStore.svr().getMasterKey().deriveLoggingKey()))
              .addBlocking("first-launch", this::initializeFirstEverAppLaunch)
              .addBlocking("app-migrations", this::initializeApplicationMigrations)
              .addBlocking("lifecycle-observer", () -> AppForegroundObserver.addListener(this))
              .addBlocking("message-retriever", this::initializeMessageRetrieval)
              .addBlocking("dynamic-theme", () -> DynamicTheme.setDefaultDayNightMode(this))
              .addBlocking("proxy-init", () -> {
                if (SignalStore.proxy().isProxyEnabled()) {
                  Log.w(TAG, "Proxy detected. Enabling Conscrypt.setUseEngineSocketByDefault()");
                  ConscryptSignal.setUseEngineSocketByDefault(true);
                }
              })
              .addBlocking("blob-provider", this::initializeBlobProvider)
              .addBlocking("remote-config", RemoteConfig::init)
              .addBlocking("ring-rtc", this::initializeRingRtc)
              .addBlocking("glide", () -> SignalGlideModule.setRegisterGlideComponents(new SignalGlideComponents()))
              .addBlocking("tracer", this::initializeTracer)
              .addNonBlocking(() -> RegistrationUtil.maybeMarkRegistrationComplete())
              .addNonBlocking(() -> Glide.get(this))
              .addNonBlocking(this::cleanAvatarStorage)
              .addNonBlocking(this::initializeRevealableMessageManager)
              .addNonBlocking(this::initializePendingRetryReceiptManager)
              .addNonBlocking(this::initializeScheduledMessageManager)
              .addNonBlocking(this::initializeFcmCheck)
              .addNonBlocking(PreKeysSyncJob::enqueueIfNeeded)
              .addNonBlocking(this::initializePeriodicTasks)
              .addNonBlocking(this::initializeCircumvention)
              .addNonBlocking(this::initializeCleanup)
              .addNonBlocking(this::initializeGlideCodecs)
              .addNonBlocking(SealedSenderConstraint::checkAndSetValidity)
              .addNonBlocking(StorageSyncHelper::scheduleRoutineSync)
              .addNonBlocking(this::beginJobLoop)
              .addNonBlocking(EmojiSource::refresh)
              .addNonBlocking(() -> AppDependencies.getGiphyMp4Cache().onAppStart(this))
              .addNonBlocking(AppDependencies::getBillingApi)
              .addNonBlocking(this::ensureProfileUploaded)
              .addNonBlocking(() -> AppDependencies.getExpireStoriesManager().scheduleIfNecessary())
              .addNonBlocking(BackupRepository::maybeFixAnyDanglingUploadProgress)
              .addPostRender(() -> AppDependencies.getDeletedCallEventManager().scheduleIfNecessary())
              .addPostRender(() -> RateLimitUtil.retryAllRateLimitedMessages(this))
              .addPostRender(this::initializeExpiringMessageManager)
              .addPostRender(this::initializeTrimThreadsByDateManager)
              .addPostRender(RefreshSvrCredentialsJob::enqueueIfNecessary)
              .addPostRender(() -> DownloadLatestEmojiDataJob.scheduleIfNecessary(this))
              .addPostRender(EmojiSearchIndexDownloadJob::scheduleIfNecessary)
              .addPostRender(() -> SignalDatabase.messageLog().trimOldMessages(System.currentTimeMillis(), RemoteConfig.retryRespondMaxAge()))
              .addPostRender(() -> JumboEmoji.updateCurrentVersion(this))
              .addPostRender(RetrieveRemoteAnnouncementsJob::enqueue)
              .addPostRender(AndroidTelecomUtil::registerPhoneAccount)
              .addPostRender(() -> AppDependencies.getJobManager().add(new FontDownloaderJob()))
              .addPostRender(() -> AppDependencies.getJobManager().add(new CallingAssetsDownloadJob()))
              .addPostRender(CheckServiceReachabilityJob::enqueueIfNecessary)
              .addPostRender(GroupV2UpdateSelfProfileKeyJob::enqueueForGroupsIfNecessary)
              .addPostRender(StoryOnboardingDownloadJob.Companion::enqueueIfNeeded)
              .addPostRender(() -> AppDependencies.getExoPlayerPool().getPoolStats().getMaxUnreserved())
              .addPostRender(() -> AppDependencies.getRecipientCache().warmUp())
              .addPostRender(AccountConsistencyWorkerJob::enqueueIfNecessary)
              .addPostRender(GroupRingCleanupJob::enqueue)
              .addPostRender(LinkedDeviceInactiveCheckJob::enqueueIfNecessary)
              .addPostRender(() -> ActiveCallManager.clearNotifications(this))
              .addPostRender(RestoreOptimizedMediaJob::enqueueIfNecessary)
              .addPostRender(() -> AppDependencies.getPinnedMessageManager().scheduleIfNecessary())
              .execute();

    Log.d(TAG, "onCreate() took " + (System.currentTimeMillis() - startTime) + " ms");
    SignalLocalMetrics.ColdStart.onApplicationCreateFinished();
    Tracer.getInstance().end("Application#onCreate()");
  }

  @Override
  public void onForeground() {
    long startTime = System.currentTimeMillis();
    Log.i(TAG, "App is now visible. Battery: " + DeviceProperties.getBatteryLevel(this) + "% (charging: " + DeviceProperties.isCharging(this) + ")");

    AppDependencies.getFrameRateTracker().start();
    AppDependencies.getMegaphoneRepository().onAppForegrounded();
    AppDependencies.getDeadlockDetector().start();
    InAppPaymentKeepAliveJob.enqueueAndTrackTimeIfNecessary();
    FcmFetchManager.onForeground(this);
    startAnrDetector();

    SignalExecutors.BOUNDED.execute(() -> {
      BackupRefreshJob.enqueueIfNecessary();
      InAppPaymentAuthCheckJob.enqueueIfNeeded();
      RemoteConfig.refreshIfNecessary();
      RetrieveProfileJob.enqueueRoutineFetchIfNecessary();
      executePendingContactSync();
      KeyCachingService.onAppForegrounded(this);
      AppDependencies.getShakeToReport().enable();
      checkBuildExpiration();
      checkFreeDiskSpace();
      MemoryTracker.start();
      BackupSubscriptionCheckJob.enqueueIfAble();
      CheckKeyTransparencyJob.enqueueIfNecessary(true);
      AppDependencies.getAuthWebSocket().registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
      AppDependencies.getUnauthWebSocket().registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);

      long lastForegroundTime = SignalStore.misc().getLastForegroundTime();
      long currentTime        = System.currentTimeMillis();
      long timeDiff           = currentTime - lastForegroundTime;

      if (timeDiff < 0) {
        Log.w(TAG, "Time travel! The system clock has moved backwards. (currentTime: " + currentTime + " ms, lastForegroundTime: " + lastForegroundTime + " ms, diff: " + timeDiff + " ms)", true);
      }

      SignalStore.misc().setLastForegroundTime(currentTime);
    });

    Log.d(TAG, "onStart() took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  @Override
  public void onBackground() {
    Log.i(TAG, "App is no longer visible.");
    KeyCachingService.onAppBackgrounded(this);
    AppDependencies.getMessageNotifier().clearVisibleThread();
    AppDependencies.getFrameRateTracker().stop();
    AppDependencies.getShakeToReport().disable();
    AppDependencies.getDeadlockDetector().stop();
    AppDependencies.getAuthWebSocket().removeKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    AppDependencies.getUnauthWebSocket().removeKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    MemoryTracker.stop();
    AnrDetector.stop();
  }

  public void checkBuildExpiration() {
    if (Util.getTimeUntilBuildExpiry(SignalStore.misc().getEstimatedServerTime()) <= 0 && !SignalStore.misc().isClientDeprecated()) {
      Log.w(TAG, "Build potentially expired! Enqueing job to check.", true);
      AppDependencies.getJobManager().add(new BuildExpirationConfirmationJob());
    }
  }

  public void checkFreeDiskSpace() {
    long availableBytes = DiskUtil.getAvailableSpace(getApplicationContext()).getBytes();
    SignalStore.backup().setSpaceAvailableOnDiskBytes(availableBytes);
  }

  /**
   * Note: this is purposefully "started" twice -- once during application create, and once during foreground.
   * This is so we can capture ANR's that happen on boot before the foreground event.
   */
  private void startAnrDetector() {
    AnrDetector.start(TimeUnit.SECONDS.toMillis(5), () -> RemoteConfig.internalUser() && SignalStore.internal().getAnrDetectionCrashes(), (dumps) -> {
      LogDatabase.getInstance(this).anrs().save(System.currentTimeMillis(), dumps);
      return Unit.INSTANCE;
    });
  }

  private void initializeSecurityProvider() {
    int aesPosition = Security.insertProviderAt(new AesGcmProvider(), 1);
    Log.i(TAG, "Installed AesGcmProvider: " + aesPosition);

    if (aesPosition < 0) {
      Log.e(TAG, "Failed to install AesGcmProvider()");
      throw new ProviderInitializationException();
    }

    int conscryptPosition = Security.insertProviderAt(ConscryptSignal.newProvider(), 2);
    Log.i(TAG, "Installed Conscrypt provider: " + conscryptPosition);

    if (conscryptPosition < 0) {
      Log.w(TAG, "Did not install Conscrypt provider. May already be present.");
    }
  }

  @VisibleForTesting
  protected void initializeLogging() {
    Log.initialize(RemoteConfig::internalUser, AndroidLogger.INSTANCE, PersistentLogger.getInstance(this));

    SignalProtocolLoggerProvider.setProvider(new CustomSignalProtocolLogger());
    SignalProtocolLoggerProvider.initializeLogging(BuildConfig.LIBSIGNAL_LOG_LEVEL);

    SignalExecutors.UNBOUNDED.execute(() -> {
      Log.blockUntilAllWritesFinished();
      LogDatabase.getInstance(this).logs().trimToSize();
      LogDatabase.getInstance(this).crashes().trimToSize();
    });
  }

  private void initializeCrashHandling() {
    final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(new SignalUncaughtExceptionHandler(originalHandler));
  }

  private void initializeRx() {
    RxDogTag.install();
    RxJavaPlugins.setInitIoSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.UNBOUNDED, true, false));
    RxJavaPlugins.setInitComputationSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.BOUNDED, true, false));
    RxJavaPlugins.setErrorHandler(e -> {
      boolean wasWrapped = false;
      while ((e instanceof UndeliverableException || e instanceof AssertionError || e instanceof OnErrorNotImplementedException) && e.getCause() != null) {
        wasWrapped = true;
        e = e.getCause();
      }

      if (wasWrapped && (e instanceof SocketException || e instanceof InterruptedException || e instanceof InterruptedIOException || e instanceof ChatServiceException)) {
        return;
      }

      Log.e(TAG, "RxJava error handler invoked", e);

      Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
      if (uncaughtExceptionHandler == null) {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
      }

      uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
    });
  }

  private void initializeApplicationMigrations() {
    ApplicationMigrations.onApplicationCreate(this, AppDependencies.getJobManager());
  }

  public void initializeMessageRetrieval() {
    SignalExecutors.UNBOUNDED.execute(AppDependencies::startNetwork);
  }

  @VisibleForTesting
  void initializeAppDependencies() {
    if (!AppDependencies.isInitialized()) {
      Log.i(TAG, "Initializing AppDependencies.");
      AppDependencies.init(this, new ApplicationDependencyProvider(this));
    }
    AppForegroundObserver.begin();

    if (Environment.USE_NEW_REGISTRATION) {
      initializeRegistrationDependencies();
    }
  }

  private void initializeRegistrationDependencies() {
    org.signal.registration.RegistrationDependencies.Companion.provide(
      new org.signal.registration.RegistrationDependencies(
        new com.vprimex.messenger.registration.v2.AppRegistrationNetworkController(this, AppDependencies.getPushServiceSocket()),
        new com.vprimex.messenger.registration.v2.AppRegistrationStorageController(this),
        null,
        context -> {
          context.startActivity(new Intent(context, SubmitDebugLogActivity.class));
          return Unit.INSTANCE;
        }
      )
    );
  }

  private void initializeFirstEverAppLaunch() {
    if (TextSecurePreferences.getFirstInstallVersion(this) == -1) {
      if (!SignalDatabase.databaseFileExists(this) || VersionTracker.getDaysSinceFirstInstalled(this) < 365) {
        Log.i(TAG, "First ever app launch!");
        AppInitialization.onFirstEverAppLaunch(this);
      }

      Log.i(TAG, "Setting first install version to " + BuildConfig.CANONICAL_VERSION_CODE);
      TextSecurePreferences.setFirstInstallVersion(this, BuildConfig.CANONICAL_VERSION_CODE);
    } else if (!SignalStore.settings().getPassphraseDisabled() && VersionTracker.getDaysSinceFirstInstalled(this) < 90) {
      Log.i(TAG, "Detected a new install that doesn't have passphrases disabled -- assuming bad initialization.");
      AppInitialization.onRepairFirstEverAppLaunch(this);
    } else if (!SignalStore.settings().getPassphraseDisabled() && VersionTracker.getDaysSinceFirstInstalled(this) < 912) {
      Log.i(TAG, "Detected a not-recent install that doesn't have passphrases disabled -- disabling now.");
      SignalStore.settings().setPassphraseDisabled(true);
    }
  }

  private void initializeFcmCheck() {
    if (!SignalStore.account().isRegistered()) {
      return;
    }

    PlayServicesUtil.PlayServicesStatus playServicesStatus = PlayServicesUtil.getPlayServicesStatus(this);

    if (playServicesStatus == PlayServicesUtil.PlayServicesStatus.SUCCESS && !SignalStore.account().isFcmEnabled()) {
      Log.w(TAG, "Play Services are newly-available. Enabling FCM and updating server.");
      SignalStore.account().setFcmEnabled(true);
      AppDependencies.getJobManager().startChain(new FcmRefreshJob())
                                      .then(new RefreshAttributesJob())
                                      .enqueue();
      AppDependencies.resetNetwork();
      AppDependencies.startNetwork();
      IncomingMessageObserver.stopForegroundService(this);
    } else if (playServicesStatus == PlayServicesUtil.PlayServicesStatus.MISSING && SignalStore.account().isFcmEnabled()) {
      Log.w(TAG, "Play Services are no longer available. Attempting to get an FCM token anyway.");
      AppDependencies.getJobManager().add(new FcmRefreshJob());
    } else if (playServicesStatus == PlayServicesUtil.PlayServicesStatus.MISSING && (System.currentTimeMillis() - SignalStore.misc().getLastMissingPlayServicesFcmVerificationTime()) > TimeUnit.DAYS.toMillis(3)) {
      Log.i(TAG, "Play Services are unavailable, but it's been long enough that we should check and see if we can get an FCM token anyway.");
      AppDependencies.getJobManager().add(new FcmRefreshJob());
    } else if (SignalStore.account().isFcmEnabled()) {
      long lastSetTime = SignalStore.account().getFcmTokenLastSetTime();
      long nextSetTime = lastSetTime + TimeUnit.HOURS.toMillis(6);
      long now         = System.currentTimeMillis();

      if (SignalStore.account().getFcmToken() == null || nextSetTime <= now || lastSetTime > now) {
        Log.i(TAG, "Time for routine FCM token refresh.");
        AppDependencies.getJobManager().add(new FcmRefreshJob());
      }
    } else {
      Log.d(TAG, "Play Services status: " + playServicesStatus + ", fcmEnabled: false. Skipping FCM check.");
    }
  }

  private void initializeExpiringMessageManager() {
    AppDependencies.getExpiringMessageManager().checkSchedule();
  }

  private void initializeRevealableMessageManager() {
    AppDependencies.getViewOnceMessageManager().scheduleIfNecessary();
  }

  private void initializePendingRetryReceiptManager() {
    AppDependencies.getPendingRetryReceiptManager().scheduleIfNecessary();
  }

  private void initializeScheduledMessageManager() {
    AppDependencies.getScheduledMessageManager().scheduleIfNecessary();
  }

  private void initializeTrimThreadsByDateManager() {
    KeepMessagesDuration keepMessagesDuration = SignalStore.settings().getKeepMessagesDuration();
    if (keepMessagesDuration != KeepMessagesDuration.FOREVER) {
      AppDependencies.getTrimThreadsByDateManager().scheduleIfNecessary();
    }
  }

  private void initializeTracer() {
    if (RemoteConfig.internalUser()) {
      Tracer.getInstance().setMaxBufferSize(35_000);
    }
  }

  private void initializePeriodicTasks() {
    RotateSignedPreKeyListener.schedule(this);
    DirectoryRefreshListener.schedule(this);
    LocalBackupListener.schedule(this);
    MessageBackupListener.schedule(this);
    RotateSenderCertificateListener.schedule(this);
    RoutineMessageFetchReceiver.startOrUpdateAlarm(this);
    AnalyzeDatabaseAlarmListener.schedule(this);

    if (BuildConfig.MANAGES_APP_UPDATES) {
      ApkUpdateRefreshListener.schedule(this);
    }
  }

  private void initializeRingRtc() {
    try {
      Map<String, String> fieldTrials = new HashMap<>();
      if (RemoteConfig.callingFieldTrialAnyAddressPortsKillSwitch()) {
        fieldTrials.put("RingRTC-AnyAddressPortsKillSwitch", "Enabled");
      }
      CallManager.initialize(this, new RingRtcLogger(), fieldTrials);
    } catch (UnsatisfiedLinkError e) {
      throw new AssertionError("Unable to load ringrtc library", e);
    }
  }

  @WorkerThread
  private void initializeCircumvention() {
    if (AppDependencies.getSignalServiceNetworkAccess().isCensored()) {
      try {
        ProviderInstaller.installIfNeeded(ApplicationContext.this);
      } catch (Throwable t) {
        Log.w(TAG, t);
      }
    }
  }

  private void ensureProfileUploaded() {
    if (SignalStore.account().isRegistered() && !SignalStore.registration().hasUploadedProfile() && !Recipient.self().getProfileName().isEmpty() && SignalStore.account().isPrimaryDevice()) {
      Log.w(TAG, "User has a profile, but has not uploaded one. Uploading now.");
      AppDependencies.getJobManager().add(new ProfileUploadJob());
    }
  }

  private void executePendingContactSync() {
    if (TextSecurePreferences.needsFullContactSync(this)) {
      AppDependencies.getJobManager().add(new MultiDeviceContactUpdateJob(true));
    }
  }

  @VisibleForTesting
  protected void beginJobLoop() {
    AppDependencies.getJobManager().beginJobLoop();
  }

  @WorkerThread
  private void initializeBlobProvider() {
    BlobProvider.getInstance().initialize(this);
  }

  @WorkerThread
  private void cleanAvatarStorage() {
    AvatarPickerStorage.cleanOrphans(this);
  }

  @WorkerThread
  private void initializeCleanup() {
    int deleted = SignalDatabase.attachments().deleteAbandonedPreuploadedAttachments();
    Log.i(TAG, "Deleted " + deleted + " abandoned attachments.");
  }

  private void initializeGlideCodecs() {
    SignalGlideCodecs.setLogProvider(new org.signal.glide.Log.Provider() {
      @Override
      public void v(@NonNull String tag, @NonNull String message) {
        Log.v(tag, message);
      }

      @Override
      public void d(@NonNull String tag, @NonNull String message) {
        Log.d(tag, message);
      }

      @Override
      public void i(@NonNull String tag, @NonNull String message) {
        Log.i(tag, message);
      }

      @Override
      public void w(@NonNull String tag, @NonNull String message) {
        Log.w(tag, message);
      }

      @Override
      public void e(@NonNull String tag, @NonNull String message, @Nullable Throwable throwable) {
        Log.e(tag, message, throwable);
      }
    });
  }

  @Override
  protected void attachBaseContext(Context base) {
    DynamicLanguageContextWrapper.updateContext(base);
    super.attachBaseContext(base);
  }

  private static class ProviderInitializationException extends RuntimeException {
  }
}

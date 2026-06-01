package com.vprimex.messenger.dependencies;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.jetbrains.annotations.NotNull;
import org.signal.billing.BillingFactory;
import org.signal.core.models.ServiceId.ACI;
import org.signal.core.models.ServiceId.PNI;
import org.signal.core.util.ThreadUtil;
import org.signal.core.util.billing.BillingApi;
import org.signal.core.util.concurrent.DeadlockDetector;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.libsignal.net.Network;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.zkgroup.GenericServerPublicParams;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import org.signal.network.api.ArchiveApi;
import org.signal.network.api.KeysApiV2;
import org.signal.network.api.MessageApiV2;
import org.signal.network.rest.SignalRestClient;
import org.signal.network.api.CallingApi;
import org.signal.network.api.CdsApi;
import org.signal.network.api.CertificateApi;
import org.signal.network.api.LinkDeviceApi;
import org.signal.network.api.PaymentsApi;
import org.signal.network.api.ProvisioningApi;
import org.signal.network.api.RateLimitChallengeApi;
import org.signal.network.api.RemoteConfigApi;
import org.signal.network.api.SvrBApi;
import org.signal.network.api.UsernameApi;
import org.signal.network.service.MessageService;
import com.vprimex.messenger.BuildConfig;
import com.vprimex.messenger.components.TypingStatusRepository;
import com.vprimex.messenger.components.TypingStatusSender;
import com.vprimex.messenger.crypto.ReentrantSessionLock;
import com.vprimex.messenger.crypto.storage.SignalBaseIdentityKeyStore;
import com.vprimex.messenger.crypto.storage.SignalIdentityKeyStore;
import com.vprimex.messenger.crypto.storage.SignalKyberPreKeyStore;
import com.vprimex.messenger.crypto.storage.SignalSenderKeyStore;
import com.vprimex.messenger.crypto.storage.SignalServiceAccountDataStoreImpl;
import com.vprimex.messenger.crypto.storage.SignalServiceDataStoreImpl;
import com.vprimex.messenger.crypto.storage.TextSecurePreKeyStore;
import com.vprimex.messenger.crypto.storage.TextSecureSessionStore;
import com.vprimex.messenger.database.DatabaseObserver;
import com.vprimex.messenger.database.JobDatabase;
import com.vprimex.messenger.database.PendingRetryReceiptCache;
import com.vprimex.messenger.jobmanager.JobManager;
import com.vprimex.messenger.jobmanager.JobMigrator;
import com.vprimex.messenger.jobmanager.impl.FactoryJobPredicate;
import com.vprimex.messenger.jobs.AttachmentCompressionJob;
import com.vprimex.messenger.jobs.AttachmentUploadJob;
import com.vprimex.messenger.jobs.FastJobStorage;
import com.vprimex.messenger.jobs.GroupCallUpdateSendJob;
import com.vprimex.messenger.jobs.IndividualSendJob;
import com.vprimex.messenger.jobs.JobManagerFactories;
import com.vprimex.messenger.jobs.MarkerJob;
import com.vprimex.messenger.jobs.PreKeysSyncJob;
import com.vprimex.messenger.jobs.PushGroupSendJob;
import com.vprimex.messenger.jobs.PushProcessMessageJob;
import com.vprimex.messenger.jobs.ReactionSendJob;
import com.vprimex.messenger.jobs.SendDeliveryReceiptJob;
import com.vprimex.messenger.jobs.TypingSendJob;
import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.megaphone.MegaphoneRepository;
import com.vprimex.messenger.messages.IncomingMessageObserver;
import com.vprimex.messenger.net.DeviceTransferBlockingInterceptor;
import com.vprimex.messenger.net.SignalWebSocketHealthMonitor;
import com.vprimex.messenger.net.StandardUserAgentInterceptor;
import com.vprimex.messenger.notifications.MessageNotifier;
import com.vprimex.messenger.notifications.OptimizedMessageNotifier;
import com.vprimex.messenger.payments.MobileCoinConfig;
import com.vprimex.messenger.payments.Payments;
import com.vprimex.messenger.push.SecurityEventListener;
import com.vprimex.messenger.push.SignalServiceNetworkAccess;
import com.vprimex.messenger.recipients.LiveRecipientCache;
import com.vprimex.messenger.revealable.ViewOnceMessageManager;
import com.vprimex.messenger.service.DeletedCallEventManager;
import com.vprimex.messenger.service.ExpiringArchivedStoriesManager;
import com.vprimex.messenger.service.ExpiringMessageManager;
import com.vprimex.messenger.service.ExpiringStoriesManager;
import com.vprimex.messenger.service.PendingRetryReceiptManager;
import com.vprimex.messenger.service.PinnedMessageManager;
import com.vprimex.messenger.service.ScheduledMessageManager;
import com.vprimex.messenger.service.TrimThreadsByDateManager;
import com.vprimex.messenger.service.webrtc.SignalCallManager;
import com.vprimex.messenger.shakereport.ShakeToReport;
import com.vprimex.messenger.stories.Stories;
import com.vprimex.messenger.util.AlarmSleepTimer;
import org.signal.core.util.AppForegroundObserver;
import org.signal.core.util.ByteUnit;
import com.vprimex.messenger.util.EarlyMessageCache;
import com.vprimex.messenger.util.Environment;
import com.vprimex.messenger.util.FrameRateTracker;
import com.vprimex.messenger.util.PreKeyBatcher;
import com.vprimex.messenger.util.RemoteConfig;
import com.vprimex.messenger.util.TextSecurePreferences;
import com.vprimex.messenger.video.exo.GiphyMp4Cache;
import com.vprimex.messenger.video.exo.SimpleExoPlayerPool;
import com.vprimex.messenger.webrtc.audio.AudioManagerCompat;
import org.whispersystems.signalservice.api.SignalServiceAccountDataStore;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceDataStore;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.account.AccountApi;
import org.signal.network.api.AttachmentApi;
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher;
import org.whispersystems.signalservice.api.donations.DonationsApi;
import org.whispersystems.signalservice.api.groupsv2.ClientZkOperations;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.keys.KeysApi;
import org.whispersystems.signalservice.api.keys.PreKeyRepository;
import org.whispersystems.signalservice.api.message.MessageApi;
import org.whispersystems.signalservice.api.profiles.ProfileApi;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.registration.RegistrationApi;
import org.whispersystems.signalservice.api.services.DonationsService;
import org.whispersystems.signalservice.api.services.ProfileService;
import org.whispersystems.signalservice.api.storage.StorageServiceApi;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.signal.core.util.SleepTimer;
import org.signal.core.util.UptimeSleepTimer;
import org.whispersystems.signalservice.api.websocket.SignalWebSocket;
import org.whispersystems.signalservice.api.websocket.WebSocketFactory;
import org.whispersystems.signalservice.api.websocket.WebSocketUnavailableException;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;
import org.whispersystems.signalservice.internal.push.PushServiceSocket;
import org.whispersystems.signalservice.internal.websocket.LibSignalChatConnection;
import org.whispersystems.signalservice.internal.websocket.LibSignalNetworkExtensions;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Implementation of {@link AppDependencies.Provider} that provides real app dependencies.
 */
public class ApplicationDependencyProvider implements AppDependencies.Provider {

  private final Application context;

  public ApplicationDependencyProvider(@NonNull Application context) {
    this.context = context;
  }

  private @NonNull ClientZkOperations provideClientZkOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return ClientZkOperations.create(signalServiceConfiguration);
  }

  @Override
  public @NonNull PushServiceSocket providePushServiceSocket(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new PushServiceSocket(signalServiceConfiguration,
                                 new DynamicCredentialsProvider(),
                                 BuildConfig.SIGNAL_AGENT,
                                 RemoteConfig.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull SignalRestClient provideSignalRestClient(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new SignalRestClient(signalServiceConfiguration,
                                BuildConfig.SIGNAL_AGENT,
                                new DynamicCredentialsProvider(),
                                RemoteConfig.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new GroupsV2Operations(provideClientZkOperations(signalServiceConfiguration), RemoteConfig.groupLimits().getHardLimit());
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull AccountApi accountApi, @NonNull PushServiceSocket pushServiceSocket, @NonNull GroupsV2Operations groupsV2Operations) {
    return new SignalServiceAccountManager(authWebSocket, accountApi, pushServiceSocket, groupsV2Operations);
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender(@NonNull SignalServiceDataStore protocolStore,
                                                                               @NonNull PushServiceSocket pushServiceSocket,
                                                                               @NonNull MessageApi messageApi,
                                                                               @NonNull KeysApi keysApi) {
      return new SignalServiceMessageSender(pushServiceSocket,
                                            protocolStore,
                                            ReentrantSessionLock.INSTANCE,
                                            messageApi,
                                            keysApi,
                                            Optional.of(new SecurityEventListener(context)),
                                            SignalExecutors.newCachedBoundedExecutor("signal-messages", ThreadUtil.PRIORITY_IMPORTANT_BACKGROUND_THREAD, 1, 16, 30),
                                            RemoteConfig.maxEnvelopeSizeBytes(),
                                            RemoteConfig.maxIncrementalMacsPerEnvelope(),
                                            RemoteConfig::useMessageSendRestFallback,
                                            new PreKeyRepository(
                                                keysApi,
                                                protocolStore.aci(),
                                                new SignalProtocolAddress(pushServiceSocket.getCredentialsProvider().getAci().getLibSignalServiceId(),
                                                                          pushServiceSocket.getCredentialsProvider().getDeviceId()),
                                                PreKeyBatcher.INSTANCE
                                              )
                                            );
  }

  @Override
  public @NonNull MessageService provideMessageService(@NonNull SignalServiceDataStore protocolStore,
                                                       @NonNull MessageApiV2 messageApiV2,
                                                       @NonNull KeysApiV2 keysApiV2) {
    SignalServiceAddress          localAddress  = new SignalServiceAddress(SignalStore.account().requireAci(), SignalStore.account().getE164());
    int                           localDeviceId = SignalStore.account().getDeviceId();
    SignalServiceAccountDataStore aciStore      = protocolStore.aci();
    SignalServiceCipher           cipher        = new SignalServiceCipher(localAddress, localDeviceId, aciStore, ReentrantSessionLock.INSTANCE, null);

    return new MessageService(localAddress, localDeviceId, messageApiV2, keysApiV2, aciStore, ReentrantSessionLock.INSTANCE, cipher, RemoteConfig.maxEnvelopeSizeBytes());
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver(@NonNull PushServiceSocket pushServiceSocket) {
    return new SignalServiceMessageReceiver(pushServiceSocket);
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return new SignalServiceNetworkAccess(context);
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return new LiveRecipientCache(context);
  }

  @Override
  public @NonNull JobManager provideJobManager(@NonNull JobManager.Configuration.Builder configurationBuilder) {
    return new JobManager(context, configurationBuilder.build());
  }

  @Override
  public @NonNull JobManager.Configuration.Builder provideJobManagerConfigurationBuilder() {
    return new JobManager.Configuration.Builder()
                                       .setJobFactories(JobManagerFactories.getJobFactories(context))
                                       .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
                                       .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
                                       .setJobStorage(new FastJobStorage(JobDatabase.getInstance(context)))
                                       .setJobMigrator(new JobMigrator(TextSecurePreferences.getJobManagerVersion(context), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(context)))
                                       .addReservedJobRunner(new FactoryJobPredicate(PushProcessMessageJob.KEY, MarkerJob.KEY))
                                       .addReservedJobRunner(new FactoryJobPredicate(AttachmentUploadJob.KEY, AttachmentCompressionJob.KEY))
                                       .addReservedJobRunner(new FactoryJobPredicate(
                                           IndividualSendJob.KEY,
                                           PushGroupSendJob.KEY,
                                           ReactionSendJob.KEY,
                                           TypingSendJob.KEY,
                                           GroupCallUpdateSendJob.KEY,
                                           SendDeliveryReceiptJob.KEY
                                       ));
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return new FrameRateTracker(context);
  }

  @SuppressLint("DiscouragedApi")
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return new MegaphoneRepository(context);
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return new EarlyMessageCache();
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return new OptimizedMessageNotifier(context);
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver(@NonNull SignalWebSocket.AuthenticatedWebSocket webSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new IncomingMessageObserver(context, webSocket, unauthWebSocket);
  }

  @Override
  public @NonNull TrimThreadsByDateManager provideTrimThreadsByDateManager() {
    return new TrimThreadsByDateManager(context);
  }

  @Override
  public @NonNull ViewOnceMessageManager provideViewOnceMessageManager() {
    return new ViewOnceMessageManager(context);
  }

  @Override
  public @NonNull ExpiringStoriesManager provideExpiringStoriesManager() {
    return new ExpiringStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringArchivedStoriesManager provideExpiringArchivedStoriesManager() {
    return new ExpiringArchivedStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringMessageManager provideExpiringMessageManager() {
    return new ExpiringMessageManager(context);
  }

  @Override
  public @NonNull DeletedCallEventManager provideDeletedCallEventManager() {
    return new DeletedCallEventManager(context);
  }

  @Override
  public @NonNull ScheduledMessageManager provideScheduledMessageManager() {
    return new ScheduledMessageManager(context);
  }

  @Override
  public @NonNull PinnedMessageManager providePinnedMessageManager() {
    return new PinnedMessageManager(context);
  }

  @Override
  public @NonNull Network provideLibsignalNetwork(@NonNull SignalServiceConfiguration config) {
    Network network = new Network(BuildConfig.LIBSIGNAL_NET_ENV, StandardUserAgentInterceptor.USER_AGENT, RemoteConfig.getLibsignalConfigs(), Network.BuildVariant.PRODUCTION);
    LibSignalNetworkExtensions.applyConfiguration(network, config);

    return network;
  }

  @Override
  public @NonNull TypingStatusRepository provideTypingStatusRepository() {
    return new TypingStatusRepository();
  }

  @Override
  public @NonNull TypingStatusSender provideTypingStatusSender() {
    return new TypingStatusSender();
  }

  @Override
  public @NonNull DatabaseObserver provideDatabaseObserver() {
    return new DatabaseObserver();
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public @NonNull Payments providePayments(@NonNull PaymentsApi paymentsApi) {
    MobileCoinConfig network;

    if      (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("mainnet")) network = MobileCoinConfig.getMainNet(paymentsApi);
    else if (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("testnet")) network = MobileCoinConfig.getTestNet(paymentsApi);
    else throw new AssertionError("Unknown network " + BuildConfig.MOBILE_COIN_ENVIRONMENT);

    return new Payments(network);
  }

  @Override
  public @NonNull ShakeToReport provideShakeToReport() {
    return new ShakeToReport(context);
  }

  @Override
  public @NonNull SignalCallManager provideSignalCallManager() {
    return new SignalCallManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptManager providePendingRetryReceiptManager() {
    return new PendingRetryReceiptManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptCache providePendingRetryReceiptCache() {
    return new PendingRetryReceiptCache();
  }

  @Override
  public @NonNull SignalWebSocket.AuthenticatedWebSocket provideAuthWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    SleepTimer                   sleepTimer    = !SignalStore.account().isFcmEnabled() || SignalStore.settings().getForceWebsocketMode().isEnabled() ? new AlarmSleepTimer(context) : new UptimeSleepTimer();
    SignalWebSocketHealthMonitor healthMonitor = new SignalWebSocketHealthMonitor(sleepTimer, true);

    WebSocketFactory authFactory = () -> {
      DynamicCredentialsProvider credentialsProvider = new DynamicCredentialsProvider();

      if (credentialsProvider.isInvalid()) {
        throw new WebSocketUnavailableException("Invalid auth credentials");
      }

      Network network = libSignalNetworkSupplier.get();
      return new LibSignalChatConnection("libsignal-auth",
                                         network,
                                         credentialsProvider,
                                         Stories.isFeatureEnabled(),
                                         healthMonitor);
    };

    SignalWebSocket.AuthenticatedWebSocket webSocket = new SignalWebSocket.AuthenticatedWebSocket(authFactory,
                                                                                                  () -> !SignalStore.misc().isClientDeprecated() && !DeviceTransferBlockingInterceptor.getInstance().isBlockingNetwork() && !Environment.IS_INSTRUMENTATION,
                                                                                                  sleepTimer,
                                                                                                  TimeUnit.SECONDS.toMillis(30));
    if (AppForegroundObserver.isForegrounded()) {
      webSocket.registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    }

    healthMonitor.monitor(webSocket);

    return webSocket;
  }

  @Override
  public @NonNull SignalWebSocket.UnauthenticatedWebSocket provideUnauthWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    SleepTimer                   sleepTimer    = !SignalStore.account().isFcmEnabled() || SignalStore.settings().getForceWebsocketMode().isEnabled() ? new AlarmSleepTimer(context) : new UptimeSleepTimer();
    SignalWebSocketHealthMonitor healthMonitor = new SignalWebSocketHealthMonitor(sleepTimer, false);

    WebSocketFactory unauthFactory = () -> {
      Network network = libSignalNetworkSupplier.get();
      return new LibSignalChatConnection("libsignal-unauth",
                                         network,
                                         null,
                                         Stories.isFeatureEnabled(),
                                         healthMonitor);
    };

    SignalWebSocket.UnauthenticatedWebSocket webSocket = new SignalWebSocket.UnauthenticatedWebSocket(unauthFactory,
                                                                                                      () -> !SignalStore.misc().isClientDeprecated() && !DeviceTransferBlockingInterceptor.getInstance().isBlockingNetwork() && !Environment.IS_INSTRUMENTATION,
                                                                                                      sleepTimer,
                                                                                                      TimeUnit.SECONDS.toMillis(30));
    if (AppForegroundObserver.isForegrounded()) {
      webSocket.registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    }

    healthMonitor.monitor(webSocket);
    return webSocket;
  }

  @Override
  public @NonNull SignalServiceDataStoreImpl provideProtocolStore() {
    ACI localAci = SignalStore.account().getAci();
    PNI localPni = SignalStore.account().getPni();

    if (localAci == null) {
      throw new IllegalStateException("No ACI set!");
    }

    if (localPni == null) {
      throw new IllegalStateException("No PNI set!");
    }

    boolean needsPreKeyJob = false;

    if (!SignalStore.account().hasAciIdentityKey()) {
      SignalStore.account().generateAciIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (!SignalStore.account().hasPniIdentityKey()) {
      SignalStore.account().generatePniIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (needsPreKeyJob) {
      PreKeysSyncJob.enqueueIfNeeded();
    }

    SignalBaseIdentityKeyStore baseIdentityStore = new SignalBaseIdentityKeyStore(context);

    SignalServiceAccountDataStoreImpl aciStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localAci),
                                                                                       new SignalKyberPreKeyStore(localAci),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getAciIdentityKey()),
                                                                                       new TextSecureSessionStore(localAci),
                                                                                       new SignalSenderKeyStore(context));

    SignalServiceAccountDataStoreImpl pniStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localPni),
                                                                                       new SignalKyberPreKeyStore(localPni),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getPniIdentityKey()),
                                                                                       new TextSecureSessionStore(localPni),
                                                                                       new SignalSenderKeyStore(context));
    return new SignalServiceDataStoreImpl(context, aciStore, pniStore);
  }

  @Override
  public @NonNull GiphyMp4Cache provideGiphyMp4Cache() {
    return new GiphyMp4Cache(ByteUnit.MEGABYTES.toBytes(16));
  }

  @Override
  public @NonNull SimpleExoPlayerPool provideExoPlayerPool() {
    return new SimpleExoPlayerPool(context);
  }

  @Override
  public @NonNull AudioManagerCompat provideAndroidCallAudioManager() {
    return AudioManagerCompat.create(context);
  }

  @Override
  public @NonNull DonationsService provideDonationsService(@NonNull DonationsApi donationsApi) {
    return new DonationsService(donationsApi);
  }

  @Override
  public @NonNull ProfileService provideProfileService(@NonNull ClientZkProfileOperations clientZkProfileOperations,
                                                       @NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket,
                                                       @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket)
  {
    return new ProfileService(clientZkProfileOperations, authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull DeadlockDetector provideDeadlockDetector() {
    HandlerThread handlerThread = new HandlerThread("signal-DeadlockDetector", ThreadUtil.PRIORITY_BACKGROUND_THREAD);
    handlerThread.start();
    return new DeadlockDetector(new Handler(handlerThread.getLooper()), TimeUnit.SECONDS.toMillis(5));
  }

  @Override
  public @NonNull ClientZkReceiptOperations provideClientZkReceiptOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return provideClientZkOperations(signalServiceConfiguration).getReceiptOperations();
  }

  @Override
  public @NonNull BillingApi provideBillingApi() {
    return BillingFactory.create(GooglePlayBillingDependencies.INSTANCE, Environment.Backups.supportsGooglePlayBilling());
  }

  @Override
  public @NonNull ArchiveApi provideArchiveApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket, @NonNull SignalServiceConfiguration signalServiceConfiguration) {
    try {
      return new ArchiveApi(authWebSocket, unauthWebSocket, pushServiceSocket, new GenericServerPublicParams(signalServiceConfiguration.getBackupServerPublicParams()));
    } catch (InvalidInputException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NonNull KeysApi provideKeysApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new KeysApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull AttachmentApi provideAttachmentApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new AttachmentApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull LinkDeviceApi provideLinkDeviceApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new LinkDeviceApi(authWebSocket);
  }

  @Override
  public @NonNull RegistrationApi provideRegistrationApi(@NonNull PushServiceSocket pushServiceSocket) {
    return new RegistrationApi(pushServiceSocket);
  }

  @Override
  public @NonNull StorageServiceApi provideStorageServiceApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new StorageServiceApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull AccountApi provideAccountApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new AccountApi(authWebSocket);
  }

  @Override
  public @NonNull UsernameApi provideUsernameApi(@NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new UsernameApi(unauthWebSocket);
  }

  @Override
  public @NonNull CallingApi provideCallingApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new CallingApi(authWebSocket, unauthWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull PaymentsApi providePaymentsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new PaymentsApi(authWebSocket);
  }

  @Override
  public @NonNull CdsApi provideCdsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new CdsApi(authWebSocket);
  }

  @Override
  public @NonNull RateLimitChallengeApi provideRateLimitChallengeApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new RateLimitChallengeApi(authWebSocket);
  }

  @Override
  public @NonNull MessageApi provideMessageApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new MessageApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull ProvisioningApi provideProvisioningApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new ProvisioningApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull CertificateApi provideCertificateApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new CertificateApi(authWebSocket);
  }

  @Override
  public @NonNull ProfileApi provideProfileApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket, @NonNull ClientZkProfileOperations clientZkProfileOperations) {
    return new ProfileApi(authWebSocket, unauthWebSocket, pushServiceSocket, clientZkProfileOperations);
  }

  @Override
  public @NonNull RemoteConfigApi provideRemoteConfigApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new RemoteConfigApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull DonationsApi provideDonationsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new DonationsApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull SvrBApi provideSvrBApi(@NotNull Network libSignalNetwork) {
    return new SvrBApi(libSignalNetwork);
  }

  @Override
  public @NonNull KeyTransparencyApi provideKeyTransparencyApi(@NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new KeyTransparencyApi(unauthWebSocket);
  }

  @VisibleForTesting
  static class DynamicCredentialsProvider implements CredentialsProvider {

    @Override
    public ACI getAci() {
      return SignalStore.account().getAci();
    }

    @Override
    public PNI getPni() {
      return SignalStore.account().getPni();
    }

    @Override
    public String getE164() {
      return SignalStore.account().getE164();
    }

    @Override
    public String getPassword() {
      return SignalStore.account().getServicePassword();
    }

    @Override
    public int getDeviceId() {
      return SignalStore.account().getDeviceId();
    }
  }
}

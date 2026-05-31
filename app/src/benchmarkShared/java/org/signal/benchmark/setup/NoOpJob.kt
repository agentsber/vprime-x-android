package org.signal.benchmark.setup

import com.vprimex.messenger.jobmanager.Job
import com.vprimex.messenger.jobs.AccountConsistencyWorkerJob
import com.vprimex.messenger.jobs.ArchiveBackupIdReservationJob
import com.vprimex.messenger.jobs.AvatarGroupsV2DownloadJob
import com.vprimex.messenger.jobs.CreateReleaseChannelJob
import com.vprimex.messenger.jobs.DirectoryRefreshJob
import com.vprimex.messenger.jobs.DownloadLatestEmojiDataJob
import com.vprimex.messenger.jobs.EmojiSearchIndexDownloadJob
import com.vprimex.messenger.jobs.FontDownloaderJob
import com.vprimex.messenger.jobs.GroupRingCleanupJob
import com.vprimex.messenger.jobs.GroupV2UpdateSelfProfileKeyJob
import com.vprimex.messenger.jobs.LinkedDeviceInactiveCheckJob
import com.vprimex.messenger.jobs.MultiDeviceProfileKeyUpdateJob
import com.vprimex.messenger.jobs.PostRegistrationBackupRedemptionJob
import com.vprimex.messenger.jobs.PreKeysSyncJob
import com.vprimex.messenger.jobs.ProfileUploadJob
import com.vprimex.messenger.jobs.RefreshAttributesJob
import com.vprimex.messenger.jobs.RefreshSvrCredentialsJob
import com.vprimex.messenger.jobs.RequestGroupV2InfoJob
import com.vprimex.messenger.jobs.ResetSvrGuessCountJob
import com.vprimex.messenger.jobs.RestoreOptimizedMediaJob
import com.vprimex.messenger.jobs.RetrieveProfileAvatarJob
import com.vprimex.messenger.jobs.RetrieveProfileJob
import com.vprimex.messenger.jobs.RetrieveRemoteAnnouncementsJob
import com.vprimex.messenger.jobs.RotateCertificateJob
import com.vprimex.messenger.jobs.StickerPackDownloadJob
import com.vprimex.messenger.jobs.StorageSyncJob
import com.vprimex.messenger.jobs.StoryOnboardingDownloadJob

/**
 * A [Job] that does nothing and always succeeds. Test setups substitute this for jobs whose
 * real implementations would hit the network at startup (and so would either generate noise
 * against the [DeviceTransferBlockingInterceptor][com.vprimex.messenger.net.DeviceTransferBlockingInterceptor]
 * or fail against unstubbed mocks). Use [replaceFactories] to apply the swap.
 */
class NoOpJob(parameters: Parameters) : Job(parameters) {
  override fun serialize(): ByteArray? = null
  override fun getFactoryKey(): String = KEY
  override fun run(): Result = Result.success()
  override fun onFailure() = Unit

  class Factory : Job.Factory<NoOpJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): NoOpJob = NoOpJob(parameters)
  }

  companion object {
    const val KEY = "NoOpJob"

    private val STARTUP_NETWORK_JOB_KEYS: Set<String> = setOf(
      AccountConsistencyWorkerJob.KEY,
      ArchiveBackupIdReservationJob.KEY,
      AvatarGroupsV2DownloadJob.KEY,
      CreateReleaseChannelJob.KEY,
      DirectoryRefreshJob.KEY,
      DownloadLatestEmojiDataJob.KEY,
      EmojiSearchIndexDownloadJob.KEY,
      FontDownloaderJob.KEY,
      GroupRingCleanupJob.KEY,
      GroupV2UpdateSelfProfileKeyJob.KEY,
      LinkedDeviceInactiveCheckJob.KEY,
      MultiDeviceProfileKeyUpdateJob.KEY,
      PostRegistrationBackupRedemptionJob.KEY,
      PreKeysSyncJob.KEY,
      ProfileUploadJob.KEY,
      RefreshAttributesJob.KEY,
      RefreshSvrCredentialsJob.KEY,
      RequestGroupV2InfoJob.KEY,
      ResetSvrGuessCountJob.KEY,
      RestoreOptimizedMediaJob.KEY,
      RetrieveProfileAvatarJob.KEY,
      RetrieveProfileJob.KEY,
      RetrieveRemoteAnnouncementsJob.KEY,
      RotateCertificateJob.KEY,
      StickerPackDownloadJob.KEY,
      StorageSyncJob.KEY,
      StoryOnboardingDownloadJob.KEY
    )

    fun replaceFactories(factories: Map<String, Job.Factory<*>>): Map<String, Job.Factory<*>> =
      factories.mapValues { if (it.key in STARTUP_NETWORK_JOB_KEYS) Factory() else it.value }
  }
}

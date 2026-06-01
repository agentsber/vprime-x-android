package com.vprimex.messenger.components.settings.app.data

import com.vprimex.messenger.mms.SentMediaQuality
import com.vprimex.messenger.webrtc.CallDataMode

data class DataAndStorageSettingsState(
  val totalStorageUse: Long,
  val mobileAutoDownloadValues: Set<String>,
  val wifiAutoDownloadValues: Set<String>,
  val roamingAutoDownloadValues: Set<String>,
  val callDataMode: CallDataMode,
  val isProxyEnabled: Boolean,
  val sentMediaQuality: SentMediaQuality,
  val forceWebsocketMode: Boolean,
  val playServicesAvailable: Boolean,
  val showStayConnectedDialog: Boolean
)

package com.vprimex.messenger.stories.settings.my

import com.vprimex.messenger.database.model.DistributionListPrivacyMode

data class MyStoryPrivacyState(val privacyMode: DistributionListPrivacyMode? = null, val connectionCount: Int = 0)

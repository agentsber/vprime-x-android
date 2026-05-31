package com.vprimex.messenger.components.settings.conversation

import com.vprimex.messenger.util.DynamicNoActionBarTheme
import com.vprimex.messenger.util.DynamicTheme

class CallInfoActivity : ConversationSettingsActivity(), ConversationSettingsFragment.TransitionCallback {

  override val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
}

package com.vprimex.messenger.keyboard.emoji

import com.vprimex.messenger.components.emoji.EmojiEventListener
import com.vprimex.messenger.keyboard.emoji.search.EmojiSearchFragment

interface EmojiKeyboardCallback :
  EmojiEventListener,
  EmojiKeyboardPageFragment.Callback,
  EmojiSearchFragment.Callback

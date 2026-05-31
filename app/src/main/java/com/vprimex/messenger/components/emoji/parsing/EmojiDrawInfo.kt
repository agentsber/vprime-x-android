package com.vprimex.messenger.components.emoji.parsing

import com.vprimex.messenger.emoji.EmojiPage

data class EmojiDrawInfo(val page: EmojiPage, val index: Int, val emoji: String, val rawEmoji: String?, val jumboSheet: String?)

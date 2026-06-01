package com.vprimex.messenger.keyboard.emoji

import com.vprimex.messenger.components.emoji.EmojiPageModel
import com.vprimex.messenger.components.emoji.EmojiPageViewGridAdapter
import com.vprimex.messenger.components.emoji.RecentEmojiPageModel
import com.vprimex.messenger.components.emoji.parsing.EmojiTree
import com.vprimex.messenger.emoji.EmojiCategory
import com.vprimex.messenger.emoji.EmojiSource
import com.vprimex.messenger.util.adapter.mapping.MappingModel

fun EmojiPageModel.toMappingModels(): List<MappingModel<*>> {
  val emojiTree: EmojiTree = EmojiSource.latest.emojiTree

  return displayEmoji.map {
    val isTextEmoji = EmojiCategory.EMOTICONS.key == key || (RecentEmojiPageModel.KEY == key && emojiTree.getEmoji(it.value, 0, it.value.length) == null)

    if (isTextEmoji) {
      EmojiPageViewGridAdapter.EmojiTextModel(key, it)
    } else {
      EmojiPageViewGridAdapter.EmojiModel(key, it)
    }
  }
}

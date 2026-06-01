package com.vprimex.messenger.conversation.ui.inlinequery

import com.vprimex.messenger.R
import com.vprimex.messenger.util.adapter.mapping.AnyMappingModel
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter

class InlineQueryAdapter(listener: (AnyMappingModel) -> Unit) : MappingAdapter() {
  init {
    registerFactory(InlineQueryEmojiResult.Model::class.java, { InlineQueryEmojiResult.ViewHolder(it, listener) }, R.layout.inline_query_emoji_result)
  }
}

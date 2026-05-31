package com.vprimex.messenger.keyboard.emoji

import com.vprimex.messenger.R
import com.vprimex.messenger.keyboard.KeyboardPageCategoryIconViewHolder
import com.vprimex.messenger.util.adapter.mapping.LayoutFactory
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter
import java.util.function.Consumer

class EmojiKeyboardPageCategoriesAdapter(private val onPageSelected: Consumer<String>) : MappingAdapter() {
  init {
    registerFactory(RecentsMappingModel::class.java, LayoutFactory({ v -> KeyboardPageCategoryIconViewHolder(v, onPageSelected) }, R.layout.keyboard_pager_category_icon))
    registerFactory(EmojiCategoryMappingModel::class.java, LayoutFactory({ v -> KeyboardPageCategoryIconViewHolder(v, onPageSelected) }, R.layout.keyboard_pager_category_icon))
  }
}

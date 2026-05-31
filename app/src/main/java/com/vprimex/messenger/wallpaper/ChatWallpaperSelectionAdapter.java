package com.vprimex.messenger.wallpaper;

import androidx.annotation.Nullable;

import com.vprimex.messenger.R;
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter;

class ChatWallpaperSelectionAdapter extends MappingAdapter {
  ChatWallpaperSelectionAdapter(@Nullable ChatWallpaperViewHolder.EventListener eventListener) {
    registerFactory(ChatWallpaperSelectionMappingModel.class, ChatWallpaperViewHolder.createFactory(R.layout.chat_wallpaper_selection_fragment_adapter_item, eventListener, null));
  }
}

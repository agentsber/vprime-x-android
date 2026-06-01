package com.vprimex.messenger.wallpaper;

import com.vprimex.messenger.R;
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter;

class ChatWallpaperPreviewAdapter extends MappingAdapter {
  ChatWallpaperPreviewAdapter() {
    registerFactory(ChatWallpaperSelectionMappingModel.class, ChatWallpaperViewHolder.createFactory(R.layout.chat_wallpaper_preview_fragment_adapter_item, null, null));
  }
}

package com.vprimex.messenger.stickers;

import androidx.annotation.NonNull;

import com.vprimex.messenger.database.model.StickerRecord;

public interface StickerEventListener {
  void onStickerSelected(@NonNull StickerRecord sticker);

  void onStickerManagementClicked();
}

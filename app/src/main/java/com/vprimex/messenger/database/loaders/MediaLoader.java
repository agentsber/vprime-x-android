package com.vprimex.messenger.database.loaders;

import android.content.Context;

import com.vprimex.messenger.util.AbstractCursorLoader;

public abstract class MediaLoader extends AbstractCursorLoader {

  MediaLoader(Context context) {
    super(context);
  }

  public enum MediaType {
    GALLERY,
    DOCUMENT,
    AUDIO,
    LINK,
    ALL
  }
}

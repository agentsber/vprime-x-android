package com.vprimex.messenger.mms;


import android.content.Context;

import androidx.annotation.NonNull;

import com.vprimex.messenger.attachments.Attachment;

public class MmsSlide extends ImageSlide {

  public MmsSlide(@NonNull Attachment attachment) {
    super(attachment);
  }

  @NonNull
  @Override
  public String getContentDescription(Context context) {
    return "MMS";
  }

}

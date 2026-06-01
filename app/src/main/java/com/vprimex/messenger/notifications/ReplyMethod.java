package com.vprimex.messenger.notifications;

import androidx.annotation.NonNull;

import com.vprimex.messenger.recipients.Recipient;

public enum ReplyMethod {

  GroupMessage,
  SecureMessage;

  public static @NonNull ReplyMethod forRecipient(Recipient recipient) {
    if (recipient.isGroup()) {
      return ReplyMethod.GroupMessage;
    } else {
      return ReplyMethod.SecureMessage;
    }
  }
}

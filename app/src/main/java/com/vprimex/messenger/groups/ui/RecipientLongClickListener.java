package com.vprimex.messenger.groups.ui;

import androidx.annotation.NonNull;

import com.vprimex.messenger.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}

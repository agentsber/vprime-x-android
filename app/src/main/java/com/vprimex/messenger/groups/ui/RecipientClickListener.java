package com.vprimex.messenger.groups.ui;

import androidx.annotation.NonNull;

import com.vprimex.messenger.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}

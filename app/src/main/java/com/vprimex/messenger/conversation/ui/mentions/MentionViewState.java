package com.vprimex.messenger.conversation.ui.mentions;

import androidx.annotation.NonNull;

import com.vprimex.messenger.recipients.Recipient;
import com.vprimex.messenger.util.viewholders.RecipientMappingModel;

public final class MentionViewState extends RecipientMappingModel<MentionViewState> {

  private final Recipient recipient;

  public MentionViewState(@NonNull Recipient recipient) {
    this.recipient = recipient;
  }

  @Override
  public @NonNull Recipient getRecipient() {
    return recipient;
  }
}

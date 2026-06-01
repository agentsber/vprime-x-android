package com.vprimex.messenger.messagedetails;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vprimex.messenger.R;
import com.vprimex.messenger.badges.BadgeImageView;
import com.vprimex.messenger.components.AvatarImageView;
import com.vprimex.messenger.components.FromTextView;
import com.vprimex.messenger.components.transfercontrols.TransferControlView;
import com.vprimex.messenger.database.AttachmentTable;
import com.vprimex.messenger.database.model.MessageRecord;
import com.vprimex.messenger.database.model.MmsMessageRecord;
import com.vprimex.messenger.mms.Slide;
import com.vprimex.messenger.util.DateUtils;
import com.vprimex.messenger.util.TextSecurePreferences;

import java.util.List;
import java.util.Locale;

final class RecipientViewHolder extends RecyclerView.ViewHolder {
  private final AvatarImageView                 avatar;
  private final FromTextView                    fromView;
  private final TextView                        timestamp;
  private final TextView                        error;
  private final View                            conflictButton;
  private final View                            unidentifiedDeliveryIcon;
  private final BadgeImageView                  badge;
  private       MessageDetailsAdapter.Callbacks callbacks;

  RecipientViewHolder(@NonNull View itemView, @NonNull MessageDetailsAdapter.Callbacks callbacks) {
    super(itemView);

    this.callbacks = callbacks;

    fromView                 = itemView.findViewById(R.id.message_details_recipient_name);
    avatar                   = itemView.findViewById(R.id.message_details_recipient_avatar);
    timestamp                = itemView.findViewById(R.id.message_details_recipient_timestamp);
    error                    = itemView.findViewById(R.id.message_details_recipient_error_description);
    conflictButton           = itemView.findViewById(R.id.message_details_recipient_conflict_button);
    unidentifiedDeliveryIcon = itemView.findViewById(R.id.message_details_recipient_ud_indicator);
    badge                    = itemView.findViewById(R.id.message_details_recipient_badge);
  }

  void bind(RecipientDeliveryStatus data) {
    unidentifiedDeliveryIcon.setVisibility(TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(itemView.getContext()) && data.isUnidentified() ? View.VISIBLE : View.GONE);
    fromView.setText(data.getRecipient(), data.getRecipient().getDisplayName(itemView.getContext()), null, true, true);
    avatar.setRecipient(data.getRecipient());
    badge.setBadgeFromRecipient(data.getRecipient());

    if (data.getKeyMismatchFailure() != null) {
      timestamp.setVisibility(View.GONE);
      error.setVisibility(View.VISIBLE);
      conflictButton.setVisibility(View.VISIBLE);
      error.setText(itemView.getContext().getString(R.string.message_details_recipient__new_safety_number));
      conflictButton.setOnClickListener(unused -> callbacks.onErrorClicked(data.getMessageRecord()));
    } else if (isFailed(data)) {
      timestamp.setVisibility(View.GONE);
      error.setVisibility(View.VISIBLE);
      conflictButton.setVisibility(View.GONE);
      error.setText(itemView.getContext().getString(R.string.message_details_recipient__failed_to_send));
    } else {
      timestamp.setVisibility(View.VISIBLE);
      error.setVisibility(View.GONE);
      conflictButton.setVisibility(View.GONE);

      if (data.getTimestamp() > 0) {
        Locale dateLocale = Locale.getDefault();
        timestamp.setText(DateUtils.getTimeString(itemView.getContext(), dateLocale, data.getTimestamp()));
      } else {
        timestamp.setText("");
      }
    }
  }

  private boolean isFailed(RecipientDeliveryStatus data) {
    if (data.getDeliveryStatus() == RecipientDeliveryStatus.Status.PENDING) {
      return false;
    }

    MessageRecord messageRecord = data.getMessageRecord();
    if (messageRecord.isAttachmentInExpectedState(AttachmentTable.TRANSFER_PROGRESS_FAILED)) {
      return true;
    }

    return (data.getNetworkFailure() != null && !data.getMessageRecord().isPending()) || (!data.getMessageRecord().getToRecipient().isPushGroup() && data.getMessageRecord().isFailed());
  }
}

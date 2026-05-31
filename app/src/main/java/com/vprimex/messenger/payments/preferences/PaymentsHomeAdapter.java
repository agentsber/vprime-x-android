package com.vprimex.messenger.payments.preferences;

import androidx.annotation.NonNull;

import com.vprimex.messenger.R;
import com.vprimex.messenger.components.settings.SettingHeader;
import com.vprimex.messenger.payments.preferences.model.InProgress;
import com.vprimex.messenger.payments.preferences.model.InfoCard;
import com.vprimex.messenger.payments.preferences.model.IntroducingPayments;
import com.vprimex.messenger.payments.preferences.model.NoRecentActivity;
import com.vprimex.messenger.payments.preferences.model.PaymentItem;
import com.vprimex.messenger.payments.preferences.model.SeeAll;
import com.vprimex.messenger.payments.preferences.viewholder.InProgressViewHolder;
import com.vprimex.messenger.payments.preferences.viewholder.InfoCardViewHolder;
import com.vprimex.messenger.payments.preferences.viewholder.IntroducingPaymentViewHolder;
import com.vprimex.messenger.payments.preferences.viewholder.NoRecentActivityViewHolder;
import com.vprimex.messenger.payments.preferences.viewholder.PaymentItemViewHolder;
import com.vprimex.messenger.payments.preferences.viewholder.SeeAllViewHolder;
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter;

public class PaymentsHomeAdapter extends MappingAdapter {

  public PaymentsHomeAdapter(@NonNull Callbacks callbacks) {
    registerFactory(IntroducingPayments.class, p -> new IntroducingPaymentViewHolder(p, callbacks), R.layout.payments_home_introducing_payments_item);
    registerFactory(NoRecentActivity.class, NoRecentActivityViewHolder::new, R.layout.payments_home_no_recent_activity_item);
    registerFactory(InProgress.class, InProgressViewHolder::new, R.layout.payments_home_in_progress);
    registerFactory(PaymentItem.class, p -> new PaymentItemViewHolder(p, callbacks), R.layout.payments_home_payment_item);
    registerFactory(SettingHeader.Item.class, SettingHeader.ViewHolder::new, R.layout.base_settings_header_item);
    registerFactory(SeeAll.class, p -> new SeeAllViewHolder(p, callbacks), R.layout.payments_home_see_all_item);
    registerFactory(InfoCard.class, p -> new InfoCardViewHolder(p, callbacks), R.layout.payment_info_card);
  }

  public interface Callbacks {
    default void onActivatePayments() {}
    default void onRestorePaymentsAccount() {}
    default void onSeeAll(@NonNull PaymentType paymentType) {}
    default void onPaymentItem(@NonNull PaymentItem model) {}
    default void onInfoCardDismissed(InfoCard.Type type) {}
    default void onViewRecoveryPhrase() {}
    default void onUpdatePin() {}
  }
}

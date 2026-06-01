package com.vprimex.messenger.payments.backup;

import androidx.annotation.NonNull;

import com.vprimex.messenger.keyvalue.SignalStore;
import com.vprimex.messenger.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return SignalStore.payments().getPaymentsMnemonic();
  }
}

package com.vprimex.messenger.payments;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import org.signal.core.util.concurrent.SignalExecutors;
import com.vprimex.messenger.database.DatabaseObserver;
import com.vprimex.messenger.database.PaymentTable;
import com.vprimex.messenger.database.SignalDatabase;
import com.vprimex.messenger.dependencies.AppDependencies;
import com.vprimex.messenger.util.concurrent.SerialMonoLifoExecutor;

import java.util.UUID;
import java.util.concurrent.Executor;

public final class PaymentTransactionLiveData extends LiveData<PaymentTable.PaymentTransaction> {

  private final UUID                      paymentId;
  private final PaymentTable              paymentDatabase;
  private final DatabaseObserver.Observer observer;
  private final Executor                  executor;

  public PaymentTransactionLiveData(@NonNull UUID paymentId) {
    this.paymentId       = paymentId;
    this.paymentDatabase = SignalDatabase.payments();
    this.observer        = this::getPaymentTransaction;
    this.executor        = new SerialMonoLifoExecutor(SignalExecutors.BOUNDED);
  }

  @Override
  protected void onActive() {
    getPaymentTransaction();
    AppDependencies.getDatabaseObserver().registerPaymentObserver(paymentId, observer);
  }

  @Override
  protected void onInactive() {
    AppDependencies.getDatabaseObserver().unregisterObserver(observer);
  }

  private void getPaymentTransaction() {
    executor.execute(() -> postValue(paymentDatabase.getPayment(paymentId)));
  }
}

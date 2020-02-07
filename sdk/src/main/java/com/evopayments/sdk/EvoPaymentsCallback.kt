package com.evopayments.sdk

import com.google.android.gms.wallet.PaymentDataRequest

interface EvoPaymentsCallback {

    fun onPaymentStarted()

    fun onPaymentSuccessful()

    fun onPaymentCancelled()

    fun onPaymentFailed()

    fun onPaymentUndetermined()

    fun handleGPayRequest(request: PaymentDataRequest, environment: GooglePayEnvironment)

    fun onSessionExpired() = Unit
}
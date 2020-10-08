package com.evopayments.sdk

import com.google.android.gms.wallet.PaymentDataRequest

interface EvoPaymentsCallback {

    fun onPaymentStarted()

    fun onPaymentSuccessful()

    fun onPaymentCancelled()

    fun onPaymentFailed()

    fun onPaymentUndetermined()

    fun handleGPayRequest(request: PaymentDataRequest, environment: GooglePayEnvironment)

    fun initialize3ds2Engine(initParams: ThreeDSTwoInitializationParams)

    fun start3ds2Challenge(challengeParams: ThreeDSTwoChallengeParams)

    fun onSessionExpired() = Unit
}

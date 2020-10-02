package com.evopayments.sdk

import com.google.android.gms.wallet.PaymentDataRequest

interface EvoPaymentsCallback {

    fun onPaymentStarted()

    fun onPaymentSuccessful()

    fun onPaymentCancelled()

    fun onPaymentFailed()

    fun onPaymentUndetermined()

    fun handleGPayRequest(request: PaymentDataRequest, environment: GooglePayEnvironment)

    fun initialize3ds2Engine(initParams: ThreeDS2InitializationParams)

    fun start3ds2Challenge(challengeParams: ThreeDS2ChallengeParams)

    fun onSessionExpired() = Unit
}

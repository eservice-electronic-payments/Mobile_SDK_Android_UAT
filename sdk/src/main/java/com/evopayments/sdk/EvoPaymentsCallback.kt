package com.evopayments.sdk

import com.google.android.gms.wallet.PaymentDataRequest

interface EvoPaymentsCallback {

    fun onPaymentStarted()

    fun onPaymentSuccessful()

    fun onPaymentCancelled()

    fun onPaymentFailed()

    fun onPaymentUndetermined()

    fun handleGPayRequest(request: PaymentDataRequest, environment: GooglePayEnvironment)

    fun handle3ds2ChallengeRequest(challengeParams: ThreeDS2ChallengeRequestParams)

    fun initialize3ds2Engine(initParams: ThreeDS2ChallengeRequestParams)

    fun onSessionExpired() = Unit
}

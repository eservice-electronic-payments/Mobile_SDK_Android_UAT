package com.evopayments.sdk

import com.google.android.gms.wallet.WalletConstants

enum class GooglePayEnvironment(val code: Int) {
    TEST(WalletConstants.ENVIRONMENT_TEST),
    PRODUCTION(WalletConstants.ENVIRONMENT_PRODUCTION)
}
package com.evopayments.sdk

import com.squareup.moshi.Json

class ThreeDSTwoChallengeResult(
    @Json(name = "sdkTransactionID")
    val transactionId: String,

    @Json(name = "transactionStatus")
    val transactionStatus: String
)

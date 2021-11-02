package com.evopayments.sdk

import com.squareup.moshi.Json

class ThreeDSTwoChallengeParams(
    @Json(name = "type")
    val type: String,

    @Json(name = "3DSServerTransactionID")
    val threeDSTransactionId: String,

    @Json(name = "AcsTransactionID")
    val acsTransactionId: String,

    @Json(name = "AcsRefNumber")
    val acsRefNumber: String,

    @Json(name = "AcsSignedContent")
    val acsSignedContent: String
)

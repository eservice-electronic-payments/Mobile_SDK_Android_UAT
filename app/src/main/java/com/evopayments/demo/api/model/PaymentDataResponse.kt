package com.evopayments.demo.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentDataResponse(

    @Json(name = "mobileCashierUrl")
    val mobileCashierUrl: String,

    @Json(name = "token")
    val token: String,

    @Json(name = "merchantId")
    val merchantId: String?
)

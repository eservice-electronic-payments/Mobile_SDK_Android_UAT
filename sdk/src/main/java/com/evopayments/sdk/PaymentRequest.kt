package com.evopayments.sdk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PaymentRequest(
    @Json(name = "SDKTransactionId")
    val transactionId: String,
    @Json(name = "DeviceData")
    val deviceData: String,
    @Json(name = "SDKEphemeralPublicKey")
    val publicKey: String,
    @Json(name = "SDKAppId")
    val appId: String,
    @Json(name = "SDKReferenceNumber")
    val referenceNumber: String
)

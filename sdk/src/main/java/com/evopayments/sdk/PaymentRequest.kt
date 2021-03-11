package com.evopayments.sdk

import com.squareup.moshi.Json

class PaymentRequest(
    @Json(name = "SDKTransactionId")
    val transactionId: String,

    @Json(name = "DeviceData")
    val deviceData: String,

    @Json(name = "SDKEphemeralPublicKey")
    val publicKey: SDKEphemeralPublicKey,

    @Json(name = "SDKAppId")
    val appId: String,

    @Json(name = "SDKReferenceNumber")
    val referenceNumber: String,

    @Json(name = "SDKProtocolVersion")
    val protocolVersion: String
)

class SDKEphemeralPublicKey(
    @Json(name = "kty")
    val kty: String,

    @Json(name = "crv")
    val crv: String,

    @Json(name = "x")
    val x: String,

    @Json(name = "y")
    val y: String
)

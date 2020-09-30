package com.evopayments.sdk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThreeDS2ChallengeRequestParams(
//    @Json(name = "3DSServerTransactionID")
//    val threeDSServerTransactionId: String,
//    @Json(name = "AcsTransactionID")
//    val acsTransactionId: String,
//    @Json(name = "AcsRefNumber")
//    val acsRefNumber: String,
//    @Json(name = "AcsSignedContent")
//    val acsSignedContent: String,
//    @Json(name = "ThreeDSRequestorAppURL")
//    val threeDSRequestorAppUrl: String,
//    @Json(name = "ThreeDSServerAuthResponse")
//    val threeDsServerAuthResponse: String,
    @Json(name = "dsId")
    val directoryServerId: String,
    @Json(name = "dsCert")
    val dsPublicCertificate: String,
    @Json(name = "rootDsCa")
    val dsRootCa: String,
    @Json(name = "licenseKeyAndroid")
    val licenseKeyReversed: String,
    @Json(name = "messageVersion")
    val messageVersion: String
)

package com.evopayments.sdk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ThreeDS2InitializationParams(
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

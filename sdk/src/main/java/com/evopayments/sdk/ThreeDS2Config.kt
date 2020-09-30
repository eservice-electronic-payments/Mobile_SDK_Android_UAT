package com.evopayments.sdk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThreeDS2Config(
    @field:Json(name = "licenseKey")
    val licenseKey: String,
    @field:Json(name = "masterCardDsPublicCert")
    val masterCardDsPublicCert: String,
    @field:Json(name = "visaDsPublicCert")
    val visaDsPublicCert: String,
    @field:Json(name = "styling")
    val styling: String
)

package com.evopayments.sdk

import android.content.Intent
import com.google.android.gms.wallet.PaymentData
import org.json.JSONObject

class PaymentDataIntentProcessor(private val intent: Intent?) {

    fun getToken(): String? {
        return intent
            ?.let(PaymentData::getFromIntent)
            ?.toJson()
            ?.let(::JSONObject)
            ?.getJSONObject(PAYMENT_METHOD_DATA)
            ?.getJSONObject(TOKENIZATION_DATA)
            ?.getString(TOKEN)
    }

    companion object {
        private const val PAYMENT_METHOD_DATA = "paymentMethodData"
        private const val TOKENIZATION_DATA = "tokenizationData"
        private const val TOKEN = "token"
    }

}
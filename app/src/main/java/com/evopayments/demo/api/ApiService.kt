package com.evopayments.demo.api

import com.evopayments.demo.api.model.DemoTokenParameters
import com.evopayments.demo.api.model.PaymentDataResponse
import retrofit2.http.*


/**
 * Created by Maciej Kozłowski on 2019-05-10.
 */
interface ApiService {

    @GET("merchant/initializePayment")
    suspend fun getToken(
        @QueryMap tokenParams: DemoTokenParameters
    ): PaymentDataResponse
}
package com.evopayments.demo.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Maciej Kozłowski on 2019-05-10.
 */

object Communication {

    private const val DEFAULT_URL = "https://merchant-simulator-server-turnkeyqa.test.intelligent-payments.com/"
    private var tokenUrl = ""

    lateinit var apiService: ApiService

    init {
        reinit(DEFAULT_URL)
    }

    private fun getHttpClient() = OkHttpClient.Builder().addInterceptor(getLoggingInterceptor()).build()

    private fun getLoggingInterceptor() = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    fun reinit(url:String) {
        tokenUrl = url
        val retrofit = Retrofit
            .Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getHttpClient())
            .baseUrl(url)
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun getTokenUrl():String {
        return tokenUrl
    }
}
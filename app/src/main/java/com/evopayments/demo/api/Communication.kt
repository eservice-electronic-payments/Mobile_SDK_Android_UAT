package com.evopayments.demo.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Created by Maciej Kozłowski on 2019-05-10.
 */

object Communication {

    private const val DEFAULT_URL = "https://merchant-simulator-server-turnkeyqa.test.intelligent-payments.com/"

    var tokenUrl = ""
        private set

    lateinit var apiService: ApiService

    init {
        reinit(DEFAULT_URL)
    }

    private fun getHttpClient() = OkHttpClient.Builder().addInterceptor(getLoggingInterceptor()).build()

    private fun getLoggingInterceptor() = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    fun reinit(url:String) {
        tokenUrl = url
        val moshiInstance = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit
            .Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshiInstance))
            .client(getHttpClient())
            .baseUrl(url)
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}

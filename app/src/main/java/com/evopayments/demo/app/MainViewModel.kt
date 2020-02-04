package com.evopayments.demo.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evopayments.demo.api.Communication
import com.evopayments.demo.api.model.DemoTokenParameters
import com.evopayments.demo.api.model.PaymentDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString
import kotlin.math.roundToInt

/**
 * Created by Maciej Kozłowski on 2019-08-01.
 */

class MainViewModel : ViewModel() {

    fun fetchToken(
        tokenUrl: String,
        tokenParams: DemoTokenParameters,
        onSuccess: (PaymentDataResponse) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                request(tokenUrl, tokenParams, onSuccess, onError)
            }
        }
    }

    private suspend fun request(
        tokenUrl: String,
        tokenParams: DemoTokenParameters,
        onSuccess: (PaymentDataResponse) -> Unit,
        onError: () -> Unit
    ) {
        try {
            Communication.reinit(tokenUrl)
            val apiService = Communication.apiService
            val result = apiService.getToken(tokenParams)
            withContext(Dispatchers.Main) { onSuccess(result) }
        } catch (exception: Exception) {
            Log.e(TAG, "Error", exception)
            withContext(Dispatchers.Main) { onError() }
        }
    }

    fun resolveMobileCashierUrl(customUrl: String): String {
        return if (customUrl.isBlank()) TEST_MOBILE_CASHIER_URL else customUrl
    }

    fun generateFlowId(): String {
        val randomString = (Math.random() * Integer.MAX_VALUE).roundToInt().toHexString()
        return "sdk-" + randomString.take(5)
    }

    companion object {
        const val TEST_MOBILE_CASHIER_URL =
            "https://cashierui-responsivedev.test.myriadpayments.com/react-frontend/index.html"
        private val TAG = MainViewModel::class.java.simpleName
    }

}
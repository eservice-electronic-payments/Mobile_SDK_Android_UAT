package com.evopayments.sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evopayments.sdk.redirect.RedirectCallback
import com.evopayments.sdk.redirect.WebDialogFragment
import com.google.android.gms.wallet.PaymentDataRequest
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import com.nsoftware.ipworks3ds.sdk.AuthenticationRequestParameters

class PaymentFragment : Fragment(), RedirectCallback {

    private lateinit var paymentCallback: EvoPaymentsCallback
    private var onDismissCallback: OnDismissListener? = null

    private val webView by lazy {
        WebViewFactory.createWebView(context!!, JSInterface(), this::onWebViewError)
    }

    private var redirectDialogFragment: WebDialogFragment? = null

    private val timeoutInMs by lazy { arguments!!.getLong(EXTRA_TIMEOUT_IN_MS) }
    private val handler by lazy { Handler() }
    private val sessionExpiredRunnable by lazy { Runnable(this::onSessionExpired) }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(PaymentRequest::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        paymentCallback = getListenerOrThrowException()
        onDismissCallback = getListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val merchantId = arguments!!.getString(EXTRA_MERCHANT_ID)!!
        val baseUrl = arguments!!.getString(EXTRA_URL)!!
        val token = arguments!!.getString(EXTRA_TOKEN)!!
        val myriadFlowId = arguments!!.getString(MYRIAD_FLOW_ID)!!

        val url = createUrl(baseUrl, merchantId, token, myriadFlowId)
        webView.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(sessionExpiredRunnable, timeoutInMs)
    }

    private fun createUrl(
        baseUrl: String,
        merchantId: String,
        token: String,
        myriadFlowId: String
    ): String {
        return Uri.parse(baseUrl)
            .buildUpon()
            .appendQueryParameter(MERCHANT_ID, merchantId)
            .appendQueryParameter(TOKEN, token)
            .appendQueryParameter(MYRIAD_FLOW_ID, myriadFlowId)
            .build()
            .toString()
    }

    private fun onSessionExpired() {
        paymentCallback.onSessionExpired()
    }

    override fun onWebViewError() {
        paymentCallback.onPaymentUndetermined()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(sessionExpiredRunnable)
    }

    fun onGooglePaymentSuccess(data: Intent?) {
        val processor = PaymentDataIntentProcessor(data)
        val paymentToken = processor.getToken()

        if (paymentToken != null) {
            sendTokenToWebView(paymentToken)
        } else {
            paymentCallback.onPaymentFailed()
        }
    }

    private fun sendTokenToWebView(token: String) {
//        webView.evaluateJavascript("window.JSInterface.onGPayTokenReceived($token);") { /* there's no result */ }
        callMethodOnWebView("onGPayTokenReceived", token)
    }

    private fun callMethodOnWebView(methodName: String, parameter: String, returnType: Class<*>? = null) {
        webView.evaluateJavascript("window.JSInterface.$methodName($parameter);") { response ->
            Log.d(TAG, response)
            if (returnType != null) {
                val jsonAdapter = moshi.adapter(returnType)
                try {
                    Log.d(TAG, jsonAdapter.fromJson(response).toString())
                } catch (ex: JsonDataException) {
                    Log.e(TAG, "", ex)
                }
            }
        }
    }

    private fun callMethodOnWebViewAlty(methodName: String, parameter: String, returnType: Class<*>? = null) {
        webView.evaluateJavascript("(function() { return window.JSInterface.$methodName($parameter); })();") { response ->
            Log.d(TAG, response)
            if (returnType != null) {
                val jsonAdapter = moshi.adapter(returnType).lenient()
                try {
                    Log.d(TAG, jsonAdapter.fromJson(response).toString())
                } catch (ex: JsonDataException) {
                    Log.e(TAG, "", ex)
                }
            }
        }
    }

    fun provideReactWithTransactionData(transactionData: AuthenticationRequestParameters) {
        val paymentRequest = PaymentRequest(
            transactionId = transactionData.sdkTransactionID,
            deviceData = transactionData.deviceData,
            publicKey = transactionData.sdkEphemeralPublicKey,
            appId = transactionData.sdkAppID,
            referenceNumber = transactionData.sdkReferenceNumber
        )
        val paymentRequestJson = jsonAdapter.toJson(paymentRequest)
        webView.evaluateJavascript("window.JSInterface.continuePayment('$paymentRequestJson');") {}
    }

    fun on3ds2ChallengeSuccess() {
        // TODO: call a JS method on the WebView
    }

    private inner class JSInterface {
        private val handler = Handler(Looper.getMainLooper())
        private val jsonAdapter = moshi.adapter(ThreeDS2ChallengeRequestParams::class.java)

        /**
         * @param environment It's determined in ReactApp and takes `TEST` or `PRODUCTION`
        */
        @JavascriptInterface
        fun processGPayPayment(paymentDataRequest: String, environment: String) {
            val request = PaymentDataRequest.fromJson(paymentDataRequest)
            val gPayEnvironment = GooglePayEnvironment.valueOf(environment)
            if (request != null) {
                paymentCallback.handleGPayRequest(request, gPayEnvironment)
            }
        }

        @JavascriptInterface
        fun process3ds2ChallengeRequest(challengeParams: String) {
            // TODO: correct the ^ callback's name ^ once it's determined by the backend devs
            val paramsObject = jsonAdapter.fromJson(challengeParams)
            if (paramsObject != null) {
                paymentCallback.handle3ds2ChallengeRequest(paramsObject)
            }
        }

        @JavascriptInterface
        fun sendNSoftSdkConfigToMobileApp(data: String) {
            Log.d("chromium", "sendNSoftSdkConfigToMobileApp")
            val paramsObject = jsonAdapter.fromJson(data)
            if (paramsObject != null) {
                paymentCallback.initialize3ds2Engine(paramsObject)
            }
        }

        @JavascriptInterface
        fun execute3DS2(data: String) {
            Log.d("chromium", "execute3ds")
        }

        @JavascriptInterface
        fun finalize3DS2Payment(data: String) {
            Log.d("chromium", "finalize3DS2Payment")
        }

        @JavascriptInterface
        fun paymentStarted() {
            handler.post {
                paymentCallback.onPaymentStarted()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @JavascriptInterface
        fun paymentSuccessful() {
            handler.post {
                paymentCallback.onPaymentSuccessful()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @JavascriptInterface
        fun paymentCancelled() {
            handler.post {
                paymentCallback.onPaymentCancelled()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @JavascriptInterface
        fun paymentFailed() {
            handler.post {
                paymentCallback.onPaymentFailed()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @JavascriptInterface
        fun paymentUndetermined() {
            handler.post {
                paymentCallback.onPaymentUndetermined()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @JavascriptInterface
        fun redirected(url: String) {
            handler.post {
                redirectDialogFragment = WebDialogFragment.newInstance(url).also {
                    childFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(it, WebDialogFragment.TAG)
                        .commit()
                }
            }
        }

        @JavascriptInterface
        fun close() {
            handler.post {
                redirectDialogFragment?.dismiss()
            }
        }
    }

    companion object {

        val TAG: String = PaymentFragment::class.java.simpleName

        private const val MERCHANT_ID = "merchantId"
        private const val TOKEN = "token"
        private const val MYRIAD_FLOW_ID = "myriadFlowId"

        private const val EXTRA_MERCHANT_ID = "extra_merchant_id"
        private const val EXTRA_URL = "extra_cashier_url"
        private const val EXTRA_TOKEN = "extra_token"
        private const val EXTRA_TIMEOUT_IN_MS = "extra_timeout_in_ms"

        internal val DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(10)

        fun newInstance(
            merchantId: String,
            mobileCashierUrl: String,
            token: String,
            myriadFlowId: String,
            timeoutInMs: Long = DEFAULT_TIMEOUT
        ) = PaymentFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_MERCHANT_ID, merchantId)
                putString(EXTRA_URL, mobileCashierUrl)
                putString(EXTRA_TOKEN, token)
                putString(MYRIAD_FLOW_ID, myriadFlowId)
                putLong(EXTRA_TIMEOUT_IN_MS, timeoutInMs)
            }
        }
    }
}
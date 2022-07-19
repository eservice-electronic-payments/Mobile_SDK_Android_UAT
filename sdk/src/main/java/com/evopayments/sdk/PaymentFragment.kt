package com.evopayments.sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evopayments.sdk.redirect.RedirectCallback
import com.evopayments.sdk.redirect.WebDialogFragment
import com.google.android.gms.wallet.PaymentDataRequest
import com.nsoftware.ipworks3ds.sdk.AuthenticationRequestParameters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

class PaymentFragment : Fragment(), RedirectCallback {

    private lateinit var paymentCallback: EvoPaymentsCallback
    private var onDismissCallback: OnDismissListener? = null

    private val webView by lazy {
        WebViewFactory.createWebView(requireContext(), JSInterface(), this::onWebViewError)
    }

    private var redirectDialogFragment: WebDialogFragment? = null

    private val timeoutInMs by lazy { requireArguments().getLong(EXTRA_TIMEOUT_IN_MS) }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val sessionExpiredRunnable by lazy { Runnable(this::onSessionExpired) }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapters = mapOf(
        PaymentRequest::class.java to moshi.adapter(PaymentRequest::class.java),
        ThreeDSTwoChallengeResult::class.java to moshi.adapter(ThreeDSTwoChallengeResult::class.java),
        SDKEphemeralPublicKey::class.java to moshi.adapter(SDKEphemeralPublicKey::class.java)
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        paymentCallback = getListenerOrThrowException()
        onDismissCallback = getListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val merchantId = requireArguments().getString(EXTRA_MERCHANT_ID)!!
        val baseUrl = requireArguments().getString(EXTRA_URL)!!
        val token = requireArguments().getString(EXTRA_TOKEN)!!
        val myriadFlowId = requireArguments().getString(MYRIAD_FLOW_ID)!!

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
            .appendQueryParameter(THREE_DS2_SUPPORT, THREE_DS2_SUPPORT_VALUE)
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
            callMethodOnWebView("onGPayTokenReceived", paymentToken)
        } else {
            paymentCallback.onPaymentFailed()
        }
    }

    private fun callMethodOnWebView(methodName: String, callParameter: String) {
        webView.evaluateJavascript("window.JSInterface.$methodName($callParameter);") {
            /* there's no result */
        }
    }

    @MainThread
    fun provideReactWithTransactionData(transactionData: AuthenticationRequestParameters) {
        val sdkEphemeralPublicKeyJson =
            getJsonAdapter<SDKEphemeralPublicKey>().fromJson(transactionData.sdkEphemeralPublicKey)
        val paymentRequest = PaymentRequest(
            transactionId = transactionData.sdkTransactionID,
            deviceData = transactionData.deviceData,
            publicKey = sdkEphemeralPublicKeyJson!!,
            appId = transactionData.sdkAppID,
            referenceNumber = transactionData.sdkReferenceNumber,
            protocolVersion = transactionData.messageVersion
        )
        val paymentRequestJson = getJsonAdapter<PaymentRequest>().toJson(paymentRequest)
        callMethodOnWebView("continuePayment", "'$paymentRequestJson'")
    }

    private inline fun <reified T> getJsonAdapter(): JsonAdapter<T> =
        jsonAdapters[T::class.java] as JsonAdapter<T>

    @MainThread
    fun provideReactWith3ds2ChallengeResult(transactionId: String, transactionStatus: String) {
        val resultObject = ThreeDSTwoChallengeResult(transactionId, transactionStatus)
        val finalCResJson = getJsonAdapter<ThreeDSTwoChallengeResult>().toJson(resultObject)
        callMethodOnWebView("finalize3DS2Payment", finalCResJson)
    }

    override fun onDestroy() {
        ThreeDSTwoChallengeManager.cleanUp(requireContext())
        super.onDestroy()
    }

    @Keep
    private inner class JSInterface {
        private val handler = Handler(Looper.getMainLooper())
        private val jsonAdapterInitParams = moshi.adapter(ThreeDSTwoInitializationParams::class.java)
        private val jsonAdapterChallengeParams = moshi.adapter(ThreeDSTwoChallengeParams::class.java)

        /**
         * @param environment It's determined in ReactApp and takes `TEST` or `PRODUCTION`
         */
        @Keep
        @JavascriptInterface
        fun processGPayPayment(paymentDataRequest: String, environment: String) {
            val request = PaymentDataRequest.fromJson(paymentDataRequest)
            val gPayEnvironment = GooglePayEnvironment.valueOf(environment)
            paymentCallback.handleGPayRequest(request, gPayEnvironment)
        }

        @Keep
        @JavascriptInterface
        fun paymentStarted() {
            handler.post {
                paymentCallback.onPaymentStarted()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        /**
         * @param data It's a serialized object of the `ThreeDSTwoInitializationParams` class
         */
        @Keep
        @JavascriptInterface
        fun sendNSoftSdkConfigToMobileApp(data: String) {
            jsonAdapterInitParams.fromJson(data)?.let(paymentCallback::initialize3ds2Engine)
        }

        /**
         * @param data It's a serialized object of the `ThreeDSTwoChallengeParams` class
         */
        @Keep
        @JavascriptInterface
        fun execute3DS2(data: String) {
            jsonAdapterChallengeParams.fromJson(data)?.let(paymentCallback::start3ds2Challenge)
        }

        @Keep
        @JavascriptInterface
        fun paymentSuccessful() {
            handler.post {
                paymentCallback.onPaymentSuccessful()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @Keep
        @JavascriptInterface
        fun paymentCancelled() {
            handler.post {
                paymentCallback.onPaymentCancelled()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @Keep
        @JavascriptInterface
        fun paymentFailed() {
            handler.post {
                paymentCallback.onPaymentFailed()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @Keep
        @JavascriptInterface
        fun paymentUndetermined() {
            handler.post {
                paymentCallback.onPaymentUndetermined()
                fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        @Keep
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

        @Keep
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
        private const val THREE_DS2_SUPPORT = "supported3DS2"
        private const val THREE_DS2_SUPPORT_VALUE = "1"

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

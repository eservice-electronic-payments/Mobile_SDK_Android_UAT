package com.evopayments.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Activity.startEvoPaymentActivityForResult(
    requestCode: Int,
    merchantId: String,
    mobileCashierUrl: String,
    token: String,
    myriadFlowId: String,
    timeoutInMs: Long? = null
) {
    startActivityForResult(
        EvoPaymentActivity.createIntent(
            this,
            merchantId,
            mobileCashierUrl,
            token,
            myriadFlowId,
            timeoutInMs
        ),
        requestCode
    )
}

class EvoPaymentActivity : AppCompatActivity(), EvoPaymentsCallback, OnDismissListener {

    private val merchantId by lazy { intent.getStringExtra(MERCHANT_ID)!! }
    private val mobileCashierUrl by lazy { intent.getStringExtra(MOBILE_CASHIER_URL)!! }
    private val token by lazy { intent.getStringExtra(TOKEN)!! }
    private val myriadFlowId by lazy { intent.getStringExtra(MYRIAD_FLOW_ID)!! }
    private val timeoutInMs by lazy {
        intent.getLongExtra(TIMEOUT_IN_MS, PaymentFragment.DEFAULT_TIMEOUT)
    }

    private var isPaymentStarted: Boolean = false

    private fun getPaymentClient(environment: Int): PaymentsClient =
        Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(environment)
                .build()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val dialogFragment = PaymentFragment.newInstance(
                merchantId,
                mobileCashierUrl,
                token,
                myriadFlowId,
                timeoutInMs
            )

            supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .add(android.R.id.content, dialogFragment, PaymentFragment.TAG)
                .commit()
        }
    }

    override fun onPaymentStarted() {
        isPaymentStarted = true
    }


    override fun onPaymentSuccessful() {
        finishWithResult(PAYMENT_SUCCESSFUL)
    }


    override fun onPaymentCancelled() {
        finishWithResult(PAYMENT_CANCELED)
    }


    override fun onPaymentFailed() {
        finishWithResult(PAYMENT_FAILED)
    }


    override fun onPaymentUndetermined() {
        finishWithResult(PAYMENT_UNDETERMINED)
    }


    override fun onSessionExpired() {
        finishWithResult(PAYMENT_SESSION_EXPIRED)
    }

    override fun handleGPayRequest(request: PaymentDataRequest, environment: GooglePayEnvironment) {
        runOnUiThread {
            AutoResolveHelper.resolveTask(
                getPaymentClient(environment.code).loadPaymentData(request),
                this,
                LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    override fun initialize3ds2Engine(initParams: ThreeDSTwoInitializationParams) {
        lifecycleScope.launch(context = Dispatchers.Default) {
            val context = this@EvoPaymentActivity
            try {
                ThreeDSTwoChallengeManager.initialize(context, initParams)
                val transactionData =
                    ThreeDSTwoChallengeManager.getAuthenticationRequestParameters()
                val paymentFragment = getPaymentFragment()
                runOnUiThread {
                    paymentFragment.provideReactWithTransactionData(transactionData)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "initialize3ds2Engine", ex)
            }
        }
    }

    private fun getPaymentFragment(): PaymentFragment =
        supportFragmentManager.findFragmentByTag(PaymentFragment.TAG) as PaymentFragment

    override fun start3ds2Challenge(challengeParams: ThreeDSTwoChallengeParams) {
        lifecycleScope.launch(context = Dispatchers.Default) {
            ThreeDSTwoChallengeManager.startChallenge(
                activity = this@EvoPaymentActivity,
                requestParams = challengeParams,
                onCompleted = ::on3ds2ChallengeResult,
                onFailed = ::onPaymentFailed,
                onCancelled = ::on3ds2ChallengeCancelled,
                onTimedOut = ::onSessionExpired
            )
        }
    }

    private fun on3ds2ChallengeResult(transactionId: String, challengeStatus: String) {
        runOnUiThread {
            getPaymentFragment().provideReactWith3ds2ChallengeResult(
                transactionId,
                challengeStatus
            )
        }
    }

    private fun on3ds2ChallengeCancelled() {
        on3ds2ChallengeResult("", "")
        finishWithResult(PAYMENT_CANCELED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> handleLoadPaymentResult(resultCode, data)
        }
    }

    private fun handleLoadPaymentResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK             -> onGooglePaymentSuccess(data)
            Activity.RESULT_CANCELED       -> finishWithResult(PAYMENT_CANCELED)
            AutoResolveHelper.RESULT_ERROR -> finishWithResult(PAYMENT_FAILED)
        }
    }

    private fun onGooglePaymentSuccess(data: Intent?) {
        val fragment = getPaymentFragment()
        fragment.onGooglePaymentSuccess(data)
    }

    private fun finishWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    override fun onDismiss() {
        finishWithResult(PAYMENT_CANCELED)
    }

    override fun onBackPressed() {
        if (!isPaymentStarted) {
            finishWithResult(PAYMENT_CANCELED)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PAYMENT_STARTED_EXTRA, isPaymentStarted)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPaymentStarted = savedInstanceState.getBoolean(IS_PAYMENT_STARTED_EXTRA)
    }

    companion object {
        private const val MERCHANT_ID = "merchant_id"
        private const val MOBILE_CASHIER_URL = "mobile_cashier_url"
        private const val TOKEN = "token"
        private const val MYRIAD_FLOW_ID = "myriad_flow_id"
        private const val TIMEOUT_IN_MS = "timeout_in_ms"
        private const val IS_PAYMENT_STARTED_EXTRA = "is_payment_started"
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 7373
        private val TAG = EvoPaymentActivity::class.java.simpleName

        const val PAYMENT_SUCCESSFUL = 1
        const val PAYMENT_CANCELED = 2
        const val PAYMENT_FAILED = 3
        const val PAYMENT_UNDETERMINED = 4
        const val PAYMENT_SESSION_EXPIRED = 5

        internal fun createIntent(
            context: Context,
            merchantId: String,
            mobileCashierUrl: String,
            token: String,
            myriadFlowId: String,
            timeoutInMs: Long?
        ) = Intent(context, EvoPaymentActivity::class.java).apply {
            putExtra(MERCHANT_ID, merchantId)
            putExtra(MOBILE_CASHIER_URL, mobileCashierUrl)
            putExtra(TOKEN, token)
            putExtra(MYRIAD_FLOW_ID, myriadFlowId)
            putExtra(TIMEOUT_IN_MS, timeoutInMs)
        }
    }
}

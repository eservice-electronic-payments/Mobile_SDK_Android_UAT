package com.evopayments.demo.app

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.evopayments.demo.R
import com.evopayments.demo.api.Communication
import com.evopayments.demo.api.model.DemoTokenParameters
import com.evopayments.demo.api.model.PaymentDataResponse
import com.evopayments.sdk.EvoPaymentActivity
import com.evopayments.sdk.startEvoPaymentActivityForResult
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private var merchantId: String = ""
    private lateinit var myriadFlowId: String
    private lateinit var merchantLandingPageUrl: String
    private lateinit var merchantNotificationUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myriadFlowId = viewModel.generateFlowId()
        startPaymentButton.setOnClickListener { fetchToken() }
        showTestButton.setOnClickListener { showRawWebDemo() }
        setDefaults()
        myriadFlowId = viewModel.generateFlowId()
        setWebContentsDebuggingEnabled(true)
    }

    private fun setDefaults() {
        val defaults = DemoTokenParameters()
        customerIdEditText.setText(defaults.getCustomerId())
        currencyEditText.setText(defaults.getCurrency())
        countryEditText.setText(defaults.getCountry())
        amountEditText.setText(defaults.getAmount())
        languageEditText.setText(defaults.getLanguage())
        customerFirstNameEditText.setText(defaults.getCustomerFirstName())
        customerLastNameEditText.setText(defaults.getCustomerLastName())

        tokenUrlEditText.setText(Communication.getTokenUrl())
        merchantLandingPageUrl = defaults.getMerchantLandingPageUrl()!!
        merchantNotificationUrl = defaults.getMerchantNotificationUrl()!!
    }

    private fun fetchToken() {
        val tokenParams = DemoTokenParameters(
            customerId = customerIdEditText.getValue(),
            currency = currencyEditText.getValue(),
            country = countryEditText.getValue(),
            amount = amountEditText.getValue(),
            action = actionSpinner.selectedItem.toString(),
            language = languageEditText.getValue(),
            merchantLandingPageUrl = merchantLandingPageUrl,
            myriadFlowId = myriadFlowId,
            customerFirstName = customerFirstNameEditText.getValue(),
            customerLastName = customerLastNameEditText.getValue(),
            merchantNotificationUrl = merchantNotificationUrl
        )
        viewModel.fetchToken(
            tokenUrlEditText.getValue(),
            tokenParams,
            this::startPaymentProcess,
            this::onError
        )
    }

    private fun EditText.getValue(): String {
        return text.toString()
    }

    private fun showRawWebDemo() {
        val cashierUrl = viewModel.resolveMobileCashierUrl(cashierUrlEditText.getValue())
        startEvoPaymentActivityForResult(
            requestCode = EVO_PAYMENT_REQUEST_CODE,
            merchantId = "",
            mobileCashierUrl = cashierUrl,
            token = "",
            myriadFlowId = myriadFlowId
        )
    }

    private fun startPaymentProcess(data: PaymentDataResponse) {
        this.merchantId = data.merchantId!!
        startEvoPaymentActivityForResult(
            EVO_PAYMENT_REQUEST_CODE,
            merchantId,
            data.mobileCashierUrl,
            data.token,
            myriadFlowId
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EVO_PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                EvoPaymentActivity.PAYMENT_SUCCESSFUL      -> onPaymentSuccessful()
                EvoPaymentActivity.PAYMENT_CANCELED        -> onPaymentCancelled()
                EvoPaymentActivity.PAYMENT_FAILED          -> onPaymentFailed()
                EvoPaymentActivity.PAYMENT_UNDETERMINED    -> onPaymentUndetermined()
                EvoPaymentActivity.PAYMENT_SESSION_EXPIRED -> onSessionExpired()
            }
        }
    }

    private fun onError() {
        showToast(R.string.failed_starting_payment_process)
    }

    private fun onPaymentSuccessful() {
        PaymentSuccessfulDialogFragment.newInstance().show(supportFragmentManager, null)
    }

    private fun onPaymentCancelled() {
        showToast(R.string.payment_cancelled)
    }

    private fun onPaymentFailed() {
        PaymentFailedDialogFragment.newInstance().show(supportFragmentManager, null)
    }

    private fun onPaymentUndetermined() {
        showToast(R.string.payment_result_undetermined)
    }

    private fun onSessionExpired() {
        showToast(R.string.session_expired)
    }

    private fun showToast(@StringRes text: Int) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val EVO_PAYMENT_REQUEST_CODE = 1000
    }
}

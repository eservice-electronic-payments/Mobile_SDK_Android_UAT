package com.evopayments.demo.app

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.evopayments.demo.BuildConfig
import com.evopayments.demo.R
import com.evopayments.demo.api.Communication
import com.evopayments.demo.api.model.CustomParams
import com.evopayments.demo.api.model.DemoTokenParameters
import com.evopayments.demo.api.model.PaymentDataResponse
import com.evopayments.demo.databinding.ActivityMainBinding
import com.evopayments.sdk.EvoPaymentActivity
import com.evopayments.sdk.startEvoPaymentActivityForResult
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private lateinit var binding: ActivityMainBinding
    private lateinit var myriadFlowId: String
    private lateinit var merchantLandingPageUrl: String
    private lateinit var merchantNotificationUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myriadFlowId = viewModel.generateFlowId()
        binding.startPaymentButton.setOnClickListener { fetchToken() }
        setDefaults()
        setVersionNameIndicatorText()
        myriadFlowId = viewModel.generateFlowId()
        setWebContentsDebuggingEnabled(true)
        val entries = resources.getStringArray(R.array.actions)
        binding.actionSpinner.onItemSelectedListener =
            SpinnerListener(entries, binding.amountEditText)
    }

    private fun setDefaults() {
        val defaults = DemoTokenParameters()
        with(binding) {
            customerIdEditText.setText(defaults.getCustomerId())
            currencyEditText.setText(defaults.getCurrency())
            countryEditText.setText(defaults.getCountry())
            amountEditText.setText(defaults.getAmount())
            languageEditText.setText(defaults.getLanguage())
            customerFirstNameEditText.setText(defaults.getCustomerFirstName())
            customerLastNameEditText.setText(defaults.getCustomerLastName())
            customerAddressStreetEditText.setText(defaults.getCustomerAddressStreet())
            customerAddressHouseNameEditText.setText(defaults.getCustomerAddressHouseName())
            customerAddressCityEditText.setText(defaults.getCustomerAddressCity())
            customerAddressPostalCodeEditText.setText(defaults.getCustomerAddressPostalCode())
            customerAddressCountryEditText.setText(defaults.getCustomerAddressCountry())
            customerAddressStateEditText.setText(defaults.getCustomerAddressState())
            customerPhoneEditText.setText(defaults.getCustomerPhone())
            customerEmailEditText.setText(defaults.getCustomerEmail())
            orderIdEditText.setText(generateRandomOrderId())
            tokenUrlEditText.setText(Communication.tokenUrl)
        }
        merchantLandingPageUrl = defaults.getMerchantLandingPageUrl()!!
        merchantNotificationUrl = defaults.getMerchantNotificationUrl()!!
    }

    private fun generateRandomOrderId(): String {
        val randomPart = Random.nextLong().toString(16)
        return ORDER_ID_PREFIX + randomPart.takeLast(ORDER_ID_RANDOM_PART_LENGTH)
    }

    private fun setVersionNameIndicatorText() {
        val versionName = BuildConfig.VERSION_NAME
        val versionNameFormatted =
            resources.getString(R.string.app_version_indicator_format, versionName)
        binding.appVersionTextView.text = versionNameFormatted
    }

    private fun fetchToken() {
        val customParams = CustomParams(
            "Custom Param Value 1",
            "Custom Param Value 2",
            "Custom Param Value 3",
            "Custom Param Value 4")

        val tokenParams = with(binding) {
            DemoTokenParameters(
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
                customerAddressHouseName = customerAddressHouseNameEditText.getValue(),
                customerAddressCity = customerAddressCityEditText.getValue(),
                customerAddressPostalCode = customerAddressPostalCodeEditText.getValue(),
                customerAddressCountry = customerAddressCountryEditText.getValue(),
                customerAddressState = customerAddressStateEditText.getValue(),
                customerPhone = customerPhoneEditText.getValue(),
                customerEmail = customerEmailEditText.getValue(),
                merchantNotificationUrl = merchantNotificationUrl,
                merchantTxId = orderIdEditText.getValue(),
                customParams = customParams
            )
        }
        val tokenUrl = binding.tokenUrlEditText.getValue()

        viewModel.fetchToken(
            tokenUrl,
            tokenParams,
            this::startPaymentProcess,
            this::onError
        )
    }

    private fun EditText.getValue(): String {
        return text.toString()
    }

    private fun startPaymentProcess(data: PaymentDataResponse) {
        requireNotNull(data.merchantId)
        startEvoPaymentActivityForResult(
            EVO_PAYMENT_REQUEST_CODE,
            data.merchantId,
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
            binding.orderIdEditText.setText(generateRandomOrderId())
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
        private const val ORDER_ID_PREFIX = "sdk-"
        private const val ORDER_ID_RANDOM_PART_LENGTH = 10
    }
}

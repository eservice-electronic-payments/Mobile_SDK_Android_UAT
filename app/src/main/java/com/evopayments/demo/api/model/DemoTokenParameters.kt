package com.evopayments.demo.api.model

class DemoTokenParameters(
    customerId: String = "lovelyrita",
    currency: String = "PLN",
    country: String = "PL",
    amount: String = "2.00",
    action: String = "AUTH",
    allowOriginUrl: String = "http://example.com",
    merchantLandingPageUrl: String = "https://ptsv2.com/t/ipgmobilesdktest",
    language: String = "en",
    myriadFlowId: String = "",
    customerFirstName: String = "Jan",
    customerLastName: String = "Mobile",
    merchantNotificationUrl: String = "https://ptsv2.com/t/66i1s-1534805666/post"
) : HashMap<String, String>() {

    init {
        put(CUSTOMER_ID, customerId)
        put(CURRENCY, currency)
        put(COUNTRY, country)
        put(AMOUNT, amount)
        put(ACTION, action)
        put(ALLOW_ORIGIN_URL, allowOriginUrl)
        put(LANGUAGE, language)
        put(MYRIAD_FLOW_ID, myriadFlowId)
        put(MERCHANT_LANDING_PAGE_URL, merchantLandingPageUrl)
        put(CUSTOMER_FIRST_NAME, customerFirstName)
        put(CUSTOMER_LAST_NAME, customerLastName)
        put(MERCHANT_NOTIFICATION_URL, merchantNotificationUrl)
    }

    fun getCustomerId() = get(CUSTOMER_ID)

    fun getCurrency() = get(CURRENCY)

    fun getCountry() = get(COUNTRY)

    fun getAmount() = get(AMOUNT)

    fun getAction() = get(ACTION)

    fun getAllowOriginUrl() = get(ALLOW_ORIGIN_URL)

    fun getMerchantLandingPageUrl() = get(MERCHANT_LANDING_PAGE_URL)

    fun getMerchantNotificationUrl() = get(MERCHANT_NOTIFICATION_URL)

    fun getLanguage() = get(LANGUAGE)

    fun getMyriadFlowId() = get(MYRIAD_FLOW_ID)

    fun getCustomerFirstName() = get(CUSTOMER_FIRST_NAME)

    fun getCustomerLastName() = get(CUSTOMER_LAST_NAME)

    companion object {
        private const val CUSTOMER_ID = "customerId"
        private const val CURRENCY = "currency"
        private const val COUNTRY = "country"
        private const val AMOUNT = "amount"
        private const val ACTION = "action"
        private const val ALLOW_ORIGIN_URL = "allowOriginUrl"
        private const val MERCHANT_LANDING_PAGE_URL = "merchantLandingPageUrl"
        private const val LANGUAGE = "language"
        private const val MYRIAD_FLOW_ID = "myriadFlowId"
        private const val CUSTOMER_FIRST_NAME = "customerFirstName"
        private const val CUSTOMER_LAST_NAME = "customerLastName"
        private const val MERCHANT_NOTIFICATION_URL = "merchantNotificationUrl"
    }
}

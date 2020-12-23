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
    merchantNotificationUrl: String = "https://ptsv2.com/t/66i1s-1534805666/post",
    customerAddressStreet: String = "Abbey Rd",
    customerAddressHouseName: String = "1",
    customerAddressCity: String = "London",
    customerAddressPostalCode: String = "NW6 4DN",
    customerAddressCountry: String = "GB",  // ISO code
    customerAddressState: String = "LND",   // ISO code
    customerPhone: String = "",
    customerEmail: String = "",
    merchantTxId: String = "",
    userDevice: String = "MOBILE",
    customerIPAddress: String = "",
    customerAccountInfo: String = "",
    merchantAuthInfo: String = "",
    customParams: CustomParams = CustomParams()
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
        put(CUSTOMER_ADDRESS_STREET, customerAddressStreet)
        put(CUSTOMER_ADDRESS_HOUSE_NAME, customerAddressHouseName)
        put(CUSTOMER_ADDRESS_CITY, customerAddressCity)
        put(CUSTOMER_ADDRESS_POSTAL_CODE, customerAddressPostalCode)
        put(CUSTOMER_ADDRESS_COUNTRY, customerAddressCountry)
        put(CUSTOMER_ADDRESS_STATE, customerAddressState)
        put(CUSTOMER_PHONE, customerPhone)
        put(CUSTOMER_EMAIL, customerEmail)
        put(USER_DEVICE, userDevice)
        put(CUSTOMER_IP_ADDRESS, customerIPAddress)
        put(CUSTOMER_ACCOUNT_INFO, customerAccountInfo)
        put(MERCHANT_AUTH_INFO, merchantAuthInfo)
        put(ORDER_ID, merchantTxId)
        putAll(customParams.hashMap)
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

    fun getCustomerAddressStreet() = get(CUSTOMER_ADDRESS_STREET)

    fun getCustomerAddressHouseName() = get(CUSTOMER_ADDRESS_HOUSE_NAME)

    fun getCustomerAddressCity() = get(CUSTOMER_ADDRESS_CITY)

    fun getCustomerAddressPostalCode() = get(CUSTOMER_ADDRESS_POSTAL_CODE)

    fun getCustomerAddressCountry() = get(CUSTOMER_ADDRESS_COUNTRY)

    fun getCustomerAddressState() = get(CUSTOMER_ADDRESS_STATE)

    fun getCustomerPhone() = get(CUSTOMER_PHONE)

    fun getCustomerEmail() = get(CUSTOMER_EMAIL)

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
        private const val CUSTOMER_ADDRESS_STREET = "customerAddressStreet"
        private const val CUSTOMER_ADDRESS_HOUSE_NAME = "customerAddressHouseName"
        private const val CUSTOMER_ADDRESS_CITY = "customerAddressCity"
        private const val CUSTOMER_ADDRESS_POSTAL_CODE = "customerAddressPostalCode"
        private const val CUSTOMER_ADDRESS_COUNTRY = "customerAddressCountry"
        private const val CUSTOMER_ADDRESS_STATE = "customerAddressState"
        private const val CUSTOMER_PHONE = "customerPhone"
        private const val CUSTOMER_EMAIL = "customerEmail"
        private const val USER_DEVICE = "userDevice"
        private const val CUSTOMER_IP_ADDRESS = "customerIPAddress"
        private const val CUSTOMER_ACCOUNT_INFO = "customerAccountInfo"
        private const val MERCHANT_AUTH_INFO = "merchantAuthInfo"
        private const val ORDER_ID = "merchantTxId"
    }
}

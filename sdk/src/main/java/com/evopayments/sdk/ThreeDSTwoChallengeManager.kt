package com.evopayments.sdk

import android.app.Activity
import android.content.Context
import android.util.Log
import com.nsoftware.ipworks3ds.sdk.*
import com.nsoftware.ipworks3ds.sdk.customization.UiCustomization
import com.nsoftware.ipworks3ds.sdk.event.CompletionEvent
import com.nsoftware.ipworks3ds.sdk.event.ProtocolErrorEvent
import com.nsoftware.ipworks3ds.sdk.event.RuntimeErrorEvent

object ThreeDSTwoChallengeManager : ChallengeStatusReceiver, ClientEventListener {

    private var transaction: Transaction? = null
    private var callback: EvoPaymentsCallback? = null

    /**
     * SDK must be cleaned up before a subsequent call to this method.
     * Note: This method invocation can take some time. It's better
     * not to invoke it on the main UI thread.
     * @see #cleanUp(Context)
     */
    fun initialize(context: Context, initParams: ThreeDSTwoInitializationParams) {
        val logTag = this::class.java.simpleName
        try {
            val licenseKey = initParams.licenseKeyReversed.reversed()
            val directoryServerInfo = ConfigParameters.DirectoryServerInfo(
                initParams.directoryServerId,
                initParams.dsPublicCertificate,
                initParams.dsRootCa
            )
            val directoryServerInfoList = arrayListOf(directoryServerInfo)
            val clientConfigs = arrayListOf<String>().apply {
                // Log levels: 0 (None), 1 (Info), 2 (Verbose), 3 (Debug)
                add("logLevel=3")
                // MaskSensitive setting controls whether sensitive data is masked in the Log event
                add("MaskSensitive=false")
            }
            val deviceParameterBlacklist = arrayListOf<String>().apply {
                add("A009")
                add("A010")
            }
            val configParameters = ConfigParameters.Builder(directoryServerInfoList, licenseKey)
                .clientConfig(clientConfigs)
                .deviceParameterBlacklist(deviceParameterBlacklist)
                .build().apply {
                    // Display a white box behind the processing icon and directory server image:
                    addParam(null, "ShowWhiteBoxInProcessingScreen", "true")
                }
            // String representation of the locale for the app's UI (e.g. "en-US") (optional):
            val locale: String? = null
            val uiCustomization = UiCustomization()
//            uiCustomization.getButtonCustomization(UiCustomization.ButtonType.SUBMIT).backgroundColor = "#951728" // Dark red

            ThreeDS2Service.INSTANCE.initialize(
                context,
                configParameters,
                locale,
                uiCustomization,
                this
            ) { severity: SecurityEventListener.Severity, securityEvent: SecurityEventListener.SecurityEvent ->
                Log.w(logTag, "Security Event captured! severity: $severity, securityEvent: $securityEvent")
            }

            val warnings = ThreeDS2Service.INSTANCE.warnings
            warnings.map { warning ->
                Log.w(logTag, "WARNING! ${warning.id} ${warning.message}")
                // abort the checkout if necessary
            }

            transaction = ThreeDS2Service.INSTANCE.createTransaction(initParams.directoryServerId, initParams.messageVersion)
        } catch (ex: Exception) {
            Log.e(logTag, "An exception raised during 3DS2 SDK initialization!", ex)
//            throw ex // TODO: uncomment?
        }
    }

    /**
     * 3DS2 SDK must be initialized before calling this method.
     * @see #initialize(Context, ThreeDS2InitializationParams)
     */
    fun getAuthenticationRequestParameters(): AuthenticationRequestParameters {
        val transaction = transaction
        checkNotNull(transaction)
        return transaction.authenticationRequestParameters
    }

    /**
     * 3DS2 SDK must be initialized before calling this method.
     * Note: This method MUST be invoked on a background thread.
     * @see #initialize(Context, ThreeDS2InitializationParams)
     */
    fun startChallenge(
        requestParams: ThreeDSTwoChallengeParams,
        callback: EvoPaymentsCallback,
        context: Activity
    ) {
        val transaction = transaction
        checkNotNull(transaction)

        val challengeParameters = ChallengeParameters().apply {
            acsRefNumber = requestParams.acsRefNumber
            acsTransactionID = requestParams.acsTransactionId
            acsRefNumber = requestParams.acsRefNumber
            acsSignedContent = requestParams.acsSignedContent
            set3DSServerTransactionID(requestParams.threeDSTransactionId)
            // TODO: setThreeDSRequestorAppURL missing (from server-side)...
        }

        this.callback = callback

        transaction.doChallenge(context, challengeParameters, this, 5) // TODO: will generate error - data needed from back-end
    }

    /**
     * It's important to call this method once the transaction is done.
     */
    fun cleanUp(context: Context) {
        if (transaction != null) {
            transaction?.close()
            transaction = null
            callback = null
            ThreeDS2Service.INSTANCE.cleanup(context)
        }
    }

    override fun cancelled() {
        callback?.onPaymentCancelled()
    }

    override fun protocolError(p0: ProtocolErrorEvent?) {
        callback?.onPaymentFailed()
    }

    override fun runtimeError(p0: RuntimeErrorEvent?) {
        // TODO: remove the logging once the data from backend is complete and challenge can be finished:
        val errMsg = "3DS2 SDK runtime error!"
        Log.e(this::class.java.simpleName, errMsg + "\n${p0?.errorMessage}")
        callback?.onPaymentFailed()
    }

    override fun completed(p0: CompletionEvent?) {
        callback?.onPaymentSuccessful()
    }

    override fun timedout() {
        callback?.onSessionExpired()
    }

//    private class MyClientEventListener : ClientEventListener {
    override fun fireLog(
        logLevel: Int,
        message: String,
        logType: String
    ) {
        Log.i("ClientLog", "$logType - $message")
    }

    override fun fireDataPacketIn(dataPacket: ByteArray) {
        Log.i("ClientDataPacketIn", String(dataPacket))
    }

    override fun fireDataPacketOut(dataPacket: ByteArray) {
        Log.i("ClientDataPacketOut", String(dataPacket))
    }

    override fun fireSSLStatus(message: String) {
        Log.i("ClientSSLStatus", message)
    }

    override fun fireSSLServerAuthentication(
        certEncoded: ByteArray,
        certSubject: String,
        certIssuer: String,
        status: String,
        accept: BooleanArray
    ) {
        //accept[0] = true; // todo
    }
//    }
}

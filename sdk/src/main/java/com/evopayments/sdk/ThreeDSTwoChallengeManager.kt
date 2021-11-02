package com.evopayments.sdk

import android.app.Activity
import android.content.Context
import android.util.Log
import com.nsoftware.ipworks3ds.sdk.*
import com.nsoftware.ipworks3ds.sdk.customization.UiCustomization
import com.nsoftware.ipworks3ds.sdk.event.CompletionEvent
import com.nsoftware.ipworks3ds.sdk.event.ProtocolErrorEvent
import com.nsoftware.ipworks3ds.sdk.event.RuntimeErrorEvent

private const val LOG_LEVEL_NONE = "0"
private const val LOG_LEVEL_DEBUG = "3"
private const val DEVICE_PARAMETER_NETWORK_OPERATOR = "A009"
private const val DEVICE_PARAMETER_NETWORK_OPERATOR_NAME = "A010"
private val TAG = ThreeDSTwoChallengeManager::class.java.simpleName

object ThreeDSTwoChallengeManager : ChallengeStatusReceiver, ClientEventListener {

    private var transaction: Transaction? = null
    private var callback: Callback? = null

    /**
     * SDK must be cleaned up before a subsequent call to this method.
     * Note: This method's invocation can take some time. It's better
     * not to invoke it on the main UI thread.
     * @see #cleanUp(Context)
     */
    fun initialize(context: Context, initParams: ThreeDSTwoInitializationParams) {
        try {
            val licenseKey = initParams.licenseKeyReversed.reversed()
            val directoryServerInfo = ConfigParameters.DirectoryServerInfo(
                initParams.directoryServerId,
                initParams.dsPublicCertificate,
                initParams.dsRootCa
            )
            val directoryServerInfoList = listOf(directoryServerInfo)
            val logLevel = if (BuildConfig.DEBUG) LOG_LEVEL_DEBUG else LOG_LEVEL_NONE
            val maskSensitive = BuildConfig.DEBUG.not()
            val clientConfigs = listOf(
                // Log levels: 0 (None), 1 (Info), 2 (Verbose), 3 (Debug)
                "logLevel=$logLevel",
                // MaskSensitive setting controls whether sensitive data is masked in the Log event
                "MaskSensitive=$maskSensitive"
            )
            // A list of device parameters NOT to pull from the device:
            val deviceParameterBlacklist = listOf(
                DEVICE_PARAMETER_NETWORK_OPERATOR,
                DEVICE_PARAMETER_NETWORK_OPERATOR_NAME
            )
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
                Log.w(TAG, "Security Event captured! severity: $severity, securityEvent: $securityEvent")
            }

            val warnings = ThreeDS2Service.INSTANCE.warnings
            warnings.map { warning ->
                Log.w(TAG, "WARNING! ${warning.id} ${warning.message}")
                // abort the checkout if necessary
            }

            transaction = ThreeDS2Service.INSTANCE.createTransaction(initParams.directoryServerId, initParams.messageVersion)
        } catch (ex: Exception) {
            Log.e(TAG, "An exception raised during 3DS2 SDK initialization!", ex)
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
        activity: Activity,
        requestParams: ThreeDSTwoChallengeParams,
        onCompleted: (transactionId: String, challengeStatus: String) -> Unit,
        onFailed: () -> Unit,
        onCancelled: () -> Unit,
        onTimedOut: () -> Unit
    ) {
        val transaction = transaction
        checkNotNull(transaction)

        val challengeParameters = ChallengeParameters().apply {
            acsRefNumber = requestParams.acsRefNumber
            acsTransactionID = requestParams.acsTransactionId
            acsRefNumber = requestParams.acsRefNumber
            acsSignedContent = requestParams.acsSignedContent
            set3DSServerTransactionID(requestParams.threeDSTransactionId)
        }

        this.callback = Callback(onCompleted, onFailed, onCancelled, onTimedOut)

        transaction.doChallenge(activity, challengeParameters, this, 5)
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
        callback?.onCancelled?.invoke()
    }

    override fun protocolError(errorStructure: ProtocolErrorEvent?) {
        callback?.onFailed?.invoke()
    }

    override fun runtimeError(errorStructure: RuntimeErrorEvent?) {
        val errMsg = "3DS2 SDK runtime error!"
        val logTag = this::class.java.simpleName
        Log.e(logTag, "$errMsg\n${errorStructure?.errorMessage}")
        callback?.onFailed?.invoke()
    }

    override fun completed(completionEvent: CompletionEvent) {
        val transactionId = completionEvent.sdkTransactionID
        val transactionStatus = completionEvent.transactionStatus
        callback?.onCompleted?.invoke(transactionId, transactionStatus)
    }

    override fun timedout() {
        callback?.onTimedOut?.invoke()
    }

    override fun fireLog(logLevel: Int, message: String, logType: String) {
        Log.i(TAG, "$logType - $message")
    }

    override fun fireDataPacketIn(dataPacket: ByteArray) {
        Log.i(TAG, String(dataPacket))
    }

    override fun fireDataPacketOut(dataPacket: ByteArray) {
        Log.i(TAG, String(dataPacket))
    }

    override fun fireSSLStatus(message: String) {
        Log.i(TAG, message)
    }

    override fun fireSSLServerAuthentication(
        certEncoded: ByteArray,
        certSubject: String,
        certIssuer: String,
        status: String,
        accept: BooleanArray
    ) {
        // no-op
    }

    private class Callback(
        val onCompleted: (transactionId: String, challengeStatus: String) -> Unit,
        val onFailed: () -> Unit,
        val onCancelled: () -> Unit,
        val onTimedOut: () -> Unit
    )
}

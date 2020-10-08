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

    /**
     * SDK must be cleaned up before the next call to this method.
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
                add("logLevel=3")
                add("MaskSensitive=false")
            }
            val deviceParameterBlacklist = arrayListOf<String>().apply {
                add("A009")
                add("A010")
            }
            val configParameters = ConfigParameters.Builder(directoryServerInfoList, licenseKey)
                .clientConfig(clientConfigs)
                .deviceParameterBlacklist(deviceParameterBlacklist)
                .build()
            configParameters.addParam(null, "ShowWhiteBoxInProcessingScreen", "true")
            val uiCustomization = UiCustomization()
//            uiCustomization.getButtonCustomization(UiCustomization.ButtonType.SUBMIT).backgroundColor = "#951728" // Dark red
            val locale: String? = null // TODO: < what about this parameter? < (from docs: it's optional)

            ThreeDS2Service.INSTANCE.initialize(
                context,
                configParameters,
                locale,
                uiCustomization,
                this
            ) { severity: SecurityEventListener.Severity, securityEvent: SecurityEventListener.SecurityEvent ->
                Log.w(logTag, "Security Event captured! severity: $severity, securityEvent: $securityEvent")
            }

            /* check warnings */
            val warnings = ThreeDS2Service.INSTANCE.warnings
            if (warnings.size > 0) {
                warnings.map { warning ->
                    Log.w("WARNING", "${warning.id} ${warning.message}")
                    // abort the checkout if necessary
                }
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
    fun startChallenge(requestParams: ThreeDSTwoChallengeParams, context: Activity, onCompleted: () -> Unit) {
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

//        transaction.doChallenge(context, challengeParameters, this, 5) // TODO: uncomment when data ready
    }

    fun cleanUp(context: Context) {
        if (transaction != null) {
            transaction = null
            ThreeDS2Service.INSTANCE.cleanup(context)
        }
    }

    override fun cancelled() {
        TODO("Not yet implemented")
    }

    override fun protocolError(p0: ProtocolErrorEvent?) {
        TODO("Not yet implemented")
    }

    override fun runtimeError(p0: RuntimeErrorEvent?) {
        TODO("Not yet implemented")
    }

    override fun completed(p0: CompletionEvent?) {
        TODO("Not yet implemented")
    }

    override fun timedout() {
        TODO("Not yet implemented")
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

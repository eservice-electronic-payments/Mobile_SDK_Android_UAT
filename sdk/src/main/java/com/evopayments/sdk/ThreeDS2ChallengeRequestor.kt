package com.evopayments.sdk

import android.app.Activity
import android.content.Context
import android.util.Log
import com.nsoftware.ipworks3ds.sdk.*
import com.nsoftware.ipworks3ds.sdk.customization.UiCustomization
import com.nsoftware.ipworks3ds.sdk.event.CompletionEvent
import com.nsoftware.ipworks3ds.sdk.event.ProtocolErrorEvent
import com.nsoftware.ipworks3ds.sdk.event.RuntimeErrorEvent
import java.util.*

object ThreeDS2ChallengeRequestor : ChallengeStatusReceiver, ClientEventListener {

    var transaction: Transaction? = null

    @Volatile private var isInitialized = false

    /**
     * SDK must be cleaned up before the next call to this method.
     * @see #cleanUp(Context)
     */
    fun initialize(context: Context, initParams: ThreeDS2InitializationParams) {
        try {
            val licenseKey = initParams.licenseKeyReversed.reversed()
            val directoryServerInfoList: MutableList<ConfigParameters.DirectoryServerInfo> =
                ArrayList()
            directoryServerInfoList.add(
                ConfigParameters.DirectoryServerInfo(
                    initParams.directoryServerId,
                    initParams.dsPublicCertificate,
                    initParams.dsRootCa
                )
            )
            val clientConfigs = arrayListOf<String>()
            clientConfigs.add("logLevel=3")
            clientConfigs.add("MaskSensitive=false")
            val deviceParameterBlacklist = arrayListOf<String>()
            deviceParameterBlacklist.add("A009")
            deviceParameterBlacklist.add("A010")
            val configParameters =
                ConfigParameters.Builder(directoryServerInfoList, licenseKey)
                    .clientConfig(clientConfigs)
                    .deviceParameterBlacklist(deviceParameterBlacklist)
                    .build()
            configParameters.addParam(null, "ShowWhiteBoxInProcessingScreen", "true")
            val uiCustomization = UiCustomization()
//            uiCustomization.getButtonCustomization(UiCustomization.ButtonType.SUBMIT).backgroundColor =
//                "#951728" // Dark red
            val locale: String? = null          // TODO: < what about this parameter? <
            ThreeDS2Service.INSTANCE.initialize(
                context,
                configParameters,
                locale,
                uiCustomization,
                this,
                null // TODO: <- added in the newest unofficial nSoft SDK build
            )

            isInitialized = true

            /* check warnings */
            val warnings = ThreeDS2Service.INSTANCE.warnings
            if (warnings.size > 0) {
                warnings.map { warning ->
                    Log.w("WARNING", "${warning.id} ${warning.message}")
                }
                // process warning
                // abort the checkout if necessary
            }
            transaction = ThreeDS2Service.INSTANCE.createTransaction(initParams.directoryServerId, initParams.messageVersion)
        } catch (ex: Exception) {
            Log.e(this::class.java.simpleName, "An exception during SDK initialization!", ex)
//            throw ex // TODO: throw?
        }
    }

    /**
     * An SDK must be initialized before calling this method.
     * @see #initialize(Context, ThreeDS2InitializationParams)
     */
    fun startChallenge(requestParams: ThreeDS2ChallengeParams, context: Activity, onCompleted: () -> Unit) {
        val transaction = transaction
        checkNotNull(transaction)

        val challengeParameters = ChallengeParameters().apply {
            acsRefNumber = requestParams.acsRefNumber
            acsTransactionID = requestParams.acsTransactionId
            acsRefNumber = requestParams.acsRefNumber
            acsSignedContent = requestParams.acsSignedContent
            set3DSServerTransactionID(requestParams.threeDSTransactionId)
            // TODO: setThreeDSRequestorAppURL missing...
        }

        transaction.doChallenge(context, challengeParameters, this, 5)

//        while (!isTransactionDone()) {
//            Thread.sleep(100)
//        }
    }

//    private fun createTransaction(): Transaction {
//        return ThreeDS2Service.INSTANCE.createTransaction(TEST_DS_ID, "2.2.0")
//    }

    fun cleanUp(context: Context) {
        if (isInitialized) {
            isInitialized = false
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

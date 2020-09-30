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

object ThreeDS2ChallengeRequestor : ChallengeStatusReceiver {

//  const val TEST_SERVER_URL = "https://3dstest.nsoftware.com/"
    const val TEST_DS_ID = "NSOF000001"

    const val TEST_DS_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIICqjCCAZKgAwIBAgIBATANBgkqhkiG9w0BAQsFADAaMRgwFgYDVQQDEw9uc29mdHdhcmUu\n" +
            "RFMuQ0EwHhcNMTkwOTI2MDYyODAzWhcNMjkwOTIzMDYyODAzWjAXMRUwEwYDVQQDEwxuc29m\n" +
            "dHdhcmUuRFMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCr4I/vgMlFLpwVY+de\n" +
            "6YTtpyMtFzRIriZ+bmqmaML+qz49HvKO4+2lrZC1sBo74xPbupq7Zfq5c4UWbyGIjqNWdNKa\n" +
            "XsSJz+RKyjCaNEF6B3rBeltaeBXuJVZ+oF+Q4zt0UI2WFSY4iE67babR0ep3/GNdSEQKHIV9\n" +
            "oHI1cJsE9/qDrrPqzQnpPI+FdyyEqhi2TfiyWv3kzrEO6Rfxnqila5k5UXLUjrej1gwnhgbs\n" +
            "Bp+EADxRmcJcmcnWsPOqBgyLthXlFv13f9PZIuDiRIBYqEV9SZZtgr/lBEnYkUb5jaYQd6+n\n" +
            "YXZ7Q0+kdxDYLS0crrBgaRFdcsJgVimni7JTAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAH65\n" +
            "NrUSJzDZqcsdsbH3igZQdDetM0IEKOFrunYA0XR4F+aViHOtExoM8FRFYWexxyU85UY8gRin\n" +
            "eJLkR379JCWVqMNholDWLpT9SYCN8q1eGFJpCT46vB0qxvQ25V71KWKp78uDfAlgJ4Hm0sUa\n" +
            "yP22oMFZQ9lgAygWG9TR4wkG+KFz/R0LzeXK3V+yJpN9IxG0VCbTF1RIlZp0p77gI7hXWuk+\n" +
            "ATJeKSbbT89KChbR4VKJesGfZ5VEKmnR2npK/mfSY7qtRH7Ha7zDG8CArX4qiFkX7UfwFcj7\n" +
            "FmXgZrNTvx5AUJ/XYXz71AE8v1uYyzM5kZuXoyAxXXskb7Rji4s=\n" +
            "-----END CERTIFICATE-----"

    const val TEST_DS_CA = "-----BEGIN CERTIFICATE-----\n" +
            "MIICrjCCAZagAwIBAgICAN4wDQYJKoZIhvcNAQELBQAwGjEYMBYGA1UEAxMPbnNvZnR3YXJl\n" +
            "LkRTLkNBMB4XDTE5MDkyNjA2Mjc1NVoXDTI5MDkyMzA2Mjc1NVowGjEYMBYGA1UEAxMPbnNv\n" +
            "ZnR3YXJlLkRTLkNBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzqLKOH7g1+Y5\n" +
            "2wrWnI+n0/XywW3cJECSWT3li5dJiKepSKQ72ni5coZRCLklZaoNeMz9/WLg20fXpqV708zZ\n" +
            "J6mCyS9art8DwK4i2u3StK5ehCBcz/YuX+C+jYySE2Zi6QxA4PC6UiR89aKoJKX+rJF8Bcys\n" +
            "q7v5ky+embGCMpUU2jZ3GNKGeZXTqWlXY6verHVRoq3Ynn2In9D4r67CFQ1e3kfxEVWkr+WA\n" +
            "Zsw/HSWq6u3OBnz7gwTCr4dqztMJIoYgKm70fzbmCr5uCdcSg5ix/GfmTfcTgB305qCjOJj3\n" +
            "d/BiVl5bV5ORtGnFB7caJ/aXuRNv5gPaigpBAMUzFwIDAQABMA0GCSqGSIb3DQEBCwUAA4IB\n" +
            "AQDDgjtqXF8D3C9oBS5t2ydjLdswDj+goTadXNNu+P90kJcWVnGFR6D/z2FUvHRD4QEI1QTV\n" +
            "r5VIy/GDZZ2fFCk9tEWjNbWDBEwxSWNxtMX7m7eTRtWlOBIm4AJOmmoNHj3jTQcxzAmQmHAr\n" +
            "yuNvk4r43UdjDo/kKQXEo3W0D4mULrbQBman5FcO3vOuc4PMKLZd3SCrHg5g8Novx8zSkkrm\n" +
            "7/2P3iMxwYMydgioWejVHJgbS0lOum/eIVjHe2zp+FReIQ8yVoQXbAQuyHzZ5c6QuXCbRn/S\n" +
            "PGkMeXLzbqDh3Oo2vQjoZ3JX17X/jcySnWxGL0RyOZwWBzivSig4NDBE\n" +
            "-----END CERTIFICATE-----"

    fun initialize(context: Context, initParams: ThreeDS2ChallengeRequestParams): Transaction {
        try {
            val licenseKey = initParams.licenseKeyReversed.reversed()
            val directoryServerInfoList: MutableList<ConfigParameters.DirectoryServerInfo> =
                ArrayList()
            directoryServerInfoList.add(
                ConfigParameters.DirectoryServerInfo(
                    TEST_DS_ID,//initParams.directoryServerId,
                    TEST_DS_CERT,//initParams.dsPublicCertificate,
                    TEST_DS_CA//initParams.dsRootCa
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
                MyClientEventListener(),
                null // TODO
            )

            /* check warnings */
            val warnings = ThreeDS2Service.INSTANCE.warnings
            if (warnings.size > 0) {
                warnings.map { warning ->
                    Log.w("WARNING", "${warning.id} ${warning.message}")
                }
                // process warning
                // abort the checkout if necessary
            }
            return ThreeDS2Service.INSTANCE.createTransaction(TEST_DS_ID, initParams.messageVersion)
        } catch (ex: Exception) {
            Log.e("3DS2", ex.toString())
            throw ex
        }
    }

//    @Throws(InterruptedException::class)
    fun startChallenge(requestParams: ThreeDS2ChallengeRequestParams, context: Activity, onCompleted: () -> Unit) {
        val challengeParameters = ChallengeParameters()
//        challengeParameters.threeDSServerAuthResponse = authResponse
//        challengeParameters.acsRefNumber = requestParams.acsRefNumber
        // TODO: call the rest of the setters ...

//        createTransaction().doChallenge(context, challengeParameters, this, 5)

//        while (!isTransactionDone()) {
//            Thread.sleep(100)
//        }
    }

//    private fun createTransaction(): Transaction {
//        return ThreeDS2Service.INSTANCE.createTransaction(TEST_DS_ID, "2.2.0")
//    }

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


    private class MyClientEventListener : ClientEventListener {

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
    }
}

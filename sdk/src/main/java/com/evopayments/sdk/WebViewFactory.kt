package com.evopayments.sdk

import android.content.Context
import android.webkit.*

/**
 * Created by Maciej Kozłowski on 2019-09-10.
 */

@Suppress("SetJavaScriptEnabled", "AddJavascriptInterface")
internal object WebViewFactory {

    fun createWebView(context: Context, jsInterface: Any?, onError: () -> Unit): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            jsInterface?.let {
                addJavascriptInterface(it, it.javaClass.simpleName)
            }
            WebView.setWebContentsDebuggingEnabled(true)

            webViewClient = PaymentWebViewClient(onError)
        }
    }

    private class PaymentWebViewClient(private val onError: () -> Unit) : WebViewClient() {
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            // only handle error from site starts with 'cashierui'
            if (request?.url?.host?.startsWith("cashierui") == true) {
                onError()
            }
        }
    }

}
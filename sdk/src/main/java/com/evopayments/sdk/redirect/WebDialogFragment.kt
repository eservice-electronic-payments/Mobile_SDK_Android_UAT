package com.evopayments.sdk.redirect

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.evopayments.evocashierlib.R
import com.evopayments.sdk.OnDismissListener
import com.evopayments.sdk.WebViewFactory
import com.evopayments.sdk.getListener
import com.evopayments.sdk.getListenerOrThrowException

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
internal class WebDialogFragment : DialogFragment() {

    private lateinit var callback: RedirectCallback
    private var onDismissCallback: OnDismissListener? = null

    private val webView by lazy {
        WebViewFactory.createWebView(context!!, null, this::onWebViewError)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = getListenerOrThrowException()
        onDismissCallback = getListener()
    }

    override fun getTheme(): Int {
        return R.style.PaymentDialogStyle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments!!.getString(EXTRA_URL)!!
        webView.loadUrl(url)
    }

    private fun onWebViewError() {
        callback.onWebViewError()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.onDismiss()
    }

    companion object {

        val TAG: String = WebDialogFragment::class.java.simpleName

        private const val EXTRA_URL = "extra_url"

        fun newInstance(url: String) = WebDialogFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_URL, url)
            }
        }
    }
}
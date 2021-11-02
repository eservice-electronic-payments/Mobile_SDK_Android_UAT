package com.evopayments.demo.app

import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import com.evopayments.demo.R

class SpinnerListener(private val entries: Array<out String>, private val amountEditText: EditText): AdapterView.OnItemSelectedListener {

    private var previousAmount: String? = null

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(entries[position]) {
            "VERIFY" -> disableAmount()
            else -> enableAmount()
        }
    }

    fun disableAmount() {
        previousAmount = amountEditText.text.toString()
        amountEditText.setText("0.00")
        amountEditText.isEnabled = false
    }

    fun enableAmount() {
        amountEditText.isEnabled = true
        previousAmount?.let(amountEditText::setText)
    }
}
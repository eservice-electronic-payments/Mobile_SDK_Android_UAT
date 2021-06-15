package com.evopayments.demo.api.model

class MssUrl(var url: String, var label: String) {

    override fun toString(): String {
        return label
    }
}
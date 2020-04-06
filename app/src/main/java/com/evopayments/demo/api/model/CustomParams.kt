package com.evopayments.demo.api.model

class CustomParams(vararg params: String) {

    val hashMap = HashMap<String, String>()

    init {
        for (value in params) {
            val key = "CustomParameter${hashMap.size + 1}Or"
            hashMap[key] = value
        }
    }

    override fun toString(): String {
        return hashMap.toString()
    }
}
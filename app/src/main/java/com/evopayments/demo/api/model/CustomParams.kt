package com.evopayments.demo.api.model

/**
 * Each added value is added as `key-value` custom param to token's request.
 *
 * Value is assigned to key: `CustomParameterXXOr` - where XX is sequential number starting from 01 - in provided order.
 *
 * ```
 * E.g.:
 * CustomParams("Value1", "Value2", "Value3")
 * will be represented as:
 * {
 *  "CustomParameter01Or": "Value1",
 *  "CustomParameter02Or": "Value2",
 *  "CustomParameter03Or": "Value3"
 * }
 * ```
 *
 * @param params Values which will be added to token's request as custom params.
 * Max count of custom params is 20.
 * Params over limit will be omitted.
 */
class CustomParams(vararg params: String) {

    val hashMap = HashMap<String, String>()

    init {
        for (value in params.take(20)) {
            val key = "CustomParameter${hashMap.size + 1}Or"
            hashMap[key] = value
        }
    }

    override fun toString(): String {
        return hashMap.toString()
    }
}
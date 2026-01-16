package com.mhmdeve.realmeotafinder.utils

import android.util.Log
import com.mhmdeve.realmeotafinder.utils.Data.Companion.defaultBody
import com.mhmdeve.realmeotafinder.utils.Data.Companion.defaultHeaders
import org.json.JSONObject

class Request(
    private var reqVersion: Int = 1,
    private var model: String? = null,
    private var otaVersion: String? = null,
    private var nvIdentifier: String? = null,
    private var ruiVersion: Int? = null,
    private var region: String? = null,
    private var deviceId: String? = null,
    private var imei0: String? = null,
    private var imei1: String? = null,
    private var beta: Boolean = false,
    private var language: String? = null,
    var respKey: String? = null,
    ) {
    private val properties: MutableMap<String, Any?> = mutableMapOf(
        "model" to model,
        "productName" to model,
        "nvCarrier" to nvIdentifier,
        "otaVersion" to otaVersion,
        "uRegion" to region,
        "rui_version" to ruiVersion,
        "region" to region,
        "deviceId" to deviceId,
        "imei" to imei0,
        "imei1" to imei1,
        "otaPrefix" to otaVersion!!.split("_").take(2).joinToString("_"),
        "romVersion" to otaVersion!!.split("_").take(2).joinToString("_")

    )

    private var key: String? = null
    private var body: String? = null
    private val headers: MutableMap<String, String> = mutableMapOf()
    var url: String = ""

    init {
        properties["deviceId"] = when {
            deviceId != null -> Crypto.sha256(deviceId!!)
            imei0 != null -> Crypto.sha256(imei0!!)
            else -> Crypto.sha256(defaultHeaders["deviceId"] ?: "")
        }

        if (ruiVersion == 1) {
            properties["version"] = "2"
        }

        properties["language"] = language ?: properties["language"]
    }

    fun encrypt(buf: String): Triple<String, String?, String?> {
        return when {
            properties["rui_version"] == 1 -> Triple(Crypto.encrypt_ecb(buf), null, null)
            reqVersion == 2 -> Crypto.encrypt_ctr_v2(buf)
            else -> Triple(Crypto.encryptCtr(buf), null, null)
        }
    }

    fun decrypt(buf: String): String {
        return when {
            properties["rui_version"] == 1 -> Crypto.decrypt_ecb(buf)
            reqVersion == 2 -> {
                val bodyObject = JSONObject(buf)
                Crypto.decrypt_ctr_v2(
                    bodyObject.getString("cipher"),
                    key!!,
                    bodyObject.getString("iv")
                )
            }
            else -> Crypto.decryptCtr(buf)
        }
    }

    fun setVars() {
        val region = properties["region"] as String?
        val ruiVersion = properties["rui_version"] as Int?
        val model = properties["model"] as String?

        properties["trackRegion"] = region

        properties["uRegion"] = region

        if (region == "CN" && properties["language"] == null) {
            properties["language"] = "zh-CN"
            properties["deviceId"] = "0"
        }

        properties["androidVersion"] = "Android${10 + (ruiVersion ?: 1) - 1}.0"

        properties["colorOSVersion"] = if (ruiVersion == 1) {
            "ColorOS7"
        } else {
            "ColorOS${11 + (ruiVersion ?: 2) - 2}"
        }

        properties["isRealme"] = if (model?.contains("RMX") == true) "1" else "0"

        properties["time"] = System.currentTimeMillis().toString()

        // Initialize URL
        url = when {
            model.equals("OnePlus", true) -> {
                "https://otag.h2os.com/post/Query_Update"
            }
            (ruiVersion ?: 0) >= 2 && reqVersion == 2 -> {
                Data.serverParams[region]?.serverURL ?: "https://default-server-url.com"
            }
            else -> {
                Data.urls[minOf(ruiVersion ?: 2, 2)]?.get(region) ?: "https://default-server-url.com"
            }
        }
    }


    fun setBodyHeaders(): Triple<String, Map<String, String>, Map<String, Any?>> {
        val newBody = mutableMapOf<String, Any?>()

        // Populate newBody with values from properties or defaultBody
        for (entry in defaultBody.keys) {
            newBody[entry] = properties[entry] ?: defaultBody[entry]

            if (entry == "mode" && beta) {
                newBody[entry] = "1"
            }
        }

        // Populate headers from defaultHeaders and properties
        for (entry in defaultHeaders.keys) {
            headers[entry] = properties[entry]?.toString() ?: defaultHeaders[entry]!!
        }

        respKey = if (ruiVersion == 1) "resps" else "body"

        if (reqVersion == 2) {
            Log.d("newBody", newBody.toString())
            headers["version"] = "2"

            val (cipher, newKey, iv) = encrypt(JSONObject(newBody).toString())
            key = newKey
            // Combine cipher and iv for the request body
            body = JSONObject(
                mapOf(
                    "params" to JSONObject(
                        mapOf(
                            "cipher" to cipher,
                            "iv" to iv
                        )
                    ).toString()
                )
            ).toString()
            val protectedKey = Crypto.generateProtectedKey(newKey!!, Data.serverParams[region.toString()]!!.pubKey)
            val version = (((properties["time"] as? String)?.toLong() ?: (0L + (86400 * 1000)))).toString()

            // Mock example for protectedKey, replace with real logic
            headers["protectedKey"] = JSONObject(
                mapOf(
                    "SCENE_1" to mapOf(
                        "protectedKey" to protectedKey,
                        "version" to version,
                        "negotiationVersion" to Data.serverParams[region.toString()]!!.negotiationVersion
                    )
                )
            ).toString()
        } else {

            Log.d("newBody", newBody.toString())
            val cipher = encrypt(JSONObject(newBody).toString()).first
            body = JSONObject(
                mapOf(
                    "params" to JSONObject(
                        mapOf(
                            "cipher" to cipher,
                            "iv" to "2bG0KwLBIkPynC8mOdgVKA=="
                        )
                    ).toString()
                )
            ).toString()
        }


        return Triple(body!!, headers, newBody)
    }

    fun validateContent(content: JSONObject) {
        if (content.has("checkFailReason") && content.get("checkFailReason") != JSONObject.NULL) {
            throw RuntimeException("Response contents mismatch, expected 'resps' got '${content.getString("checkFailReason")}'!")
        }
    }

    fun validateResponse(responseCode: Int, responseBody: String) {

        // Check if status code is not 200
        if (responseCode != 200) {
            throw RuntimeException("Response status mismatch, expected '200' got '${responseCode}'!")
        }

        // Parse the response body as JSON
        val jsonResponse = JSONObject(responseBody)
        Log.i("Response", jsonResponse.toString())
        // Check if 'responseCode' is present and not equal to 200
        if (jsonResponse.has("responseCode") && jsonResponse.getInt("responseCode") != 200) {
            val errMsg = jsonResponse.optString("errMsg", "No error message provided")
            throw RuntimeException("Response status mismatch, expected '200' got '${jsonResponse.getInt("responseCode")}' ($errMsg)!")
        }
    }


    fun parseLog(log: String): Int {
        // Use a regular expression to extract the JSON part
        val regex = Regex("""\{.*}""")
        val jsonString = regex.find(log)?.value ?: return -1

        // Parse the JSON
        val jsonObject = JSONObject(jsonString)
        jsonObject.getString("body")
        val responseCode = jsonObject.getInt("responseCode")
        return responseCode
    }

}

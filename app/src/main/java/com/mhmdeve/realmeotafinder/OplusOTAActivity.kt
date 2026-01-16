package com.mhmdeve.realmeotafinder

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.mhmdeve.realmeotafinder.utils.Component
import com.mhmdeve.realmeotafinder.utils.ComponentAdapter
import com.mhmdeve.realmeotafinder.utils.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OplusOTAActivity : AppCompatActivity() {

    private lateinit var responseText: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oplus_ota)

        scrollView = findViewById(R.id.scrollView)
        val productModel = findViewById<EditText>(R.id.input_product_model)
        val otaVersion = findViewById<EditText>(R.id.input_ota_version)
        val ruiVersion = findViewById<MaterialAutoCompleteTextView>(R.id.spinner_rui_version)
        val nvIdentifier = findViewById<EditText>(R.id.input_nv_identifier)
        val region = findViewById<MaterialAutoCompleteTextView>(R.id.spinner_region)
        val guid = findViewById<EditText>(R.id.input_guid)
        val imei0 = findViewById<EditText>(R.id.input_imei0)
        val imei1 = findViewById<EditText>(R.id.input_imei1)
        val beta = findViewById<CheckBox>(R.id.checkbox_beta)
        val language = findViewById<EditText>(R.id.input_language)
        val oldMethod = findViewById<CheckBox>(R.id.checkbox_old_method)
        val submitButton = findViewById<Button>(R.id.button_submit)
        responseText = findViewById<TextView>(R.id.text_response)

        // Get the string arrays from resources
        val ruiVersions = resources.getStringArray(R.array.rui_versions).toList()
        val regions = resources.getStringArray(R.array.regions).toList()

        // Set up RUI Version Dropdown
        val ruiVersionAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ruiVersions)
        ruiVersion.setAdapter(ruiVersionAdapter)
        // Set default value
        ruiVersion.setText(ruiVersions[2], false)  // Change the index to set a different default

        // Set up Region Dropdown
        val regionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regions)
        region.setAdapter(regionAdapter)
        // Set default value
        region.setText(regions[0], false)  // Change the index to set a different default


        // Automatically fill fields by reading system properties
        productModel.setText(getSystemProperty("ro.product.name"))
        otaVersion.setText(getSystemProperty("ro.build.version.ota"))
        nvIdentifier.setText(getSystemProperty("ro.build.oplus_nv_id"))
        guid.setText(
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
        ) // If no prop available, you can set default or keep empty
        language.setText(getSystemProperty("persist.sys.locale"))

        submitButton.setOnClickListener {
            responseText.text = getString(R.string.waiting_for_the_response)
            scrollView.post {
                scrollView.scrollTo(0, responseText.bottom)
            }
            sendRequest(
                productModel.text.toString(),
                otaVersion.text.toString(),
                ruiVersion.text.toString().toInt(),
                nvIdentifier.text.toString(),
                region.text.toString(),
                guid.text.toString(),
                imei0.text.toString(),
                imei1.text.toString(),
                beta.isChecked,
                language.text.toString(),
                oldMethod.isChecked,
                onResponse = {
                    runOnUiThread { responseText.text = it }
                },
                onError = {
                    runOnUiThread { responseText.text = it }
                }
            )
        }
    }

    private fun sendRequest(
        model: String,
        otaVersion: String,
        ruiVersion: Int,
        nvIdentifier: String?,
        region: String,
        guid: String?,
        imei0: String?,
        imei1: String?,
        beta: Boolean,
        language: String?,
        oldMethod: Boolean,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val request = Request(
            reqVersion = if (oldMethod || ruiVersion == 1) 1 else 2,
            model = model,
            otaVersion = otaVersion,
            ruiVersion = ruiVersion,
            nvIdentifier = nvIdentifier,
            region = region,
            deviceId = guid,
            imei0 = imei0,
            imei1 = imei1,
            beta = beta,
            language = language
        )

        println("Load payload for $model (RealmeUI V$ruiVersion)")

        try {
            // Set necessary variables for the request
            request.setVars()

            // Get the body and headers
            val (reqBody, reqHeaders, plainBody) = request.setBodyHeaders()

            println("Request headers:\n${JSONObject(reqHeaders).toString(4)}")
            println("Request body:\n${JSONObject(plainBody).toString(4)}")
            println("Encrypted body:\n${JSONObject(reqBody).toString(4)}")

            // Prepare the OkHttpClient and RequestBody
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val requestBody = reqBody.toRequestBody("application/json".toMediaTypeOrNull())

            // Build the request with URL, body, and headers
            val requestBuilder = okhttp3.Request.Builder()
                .url(request.url)
                .post(requestBody)

            reqHeaders.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            println("Wait for the endpoint to reply")

            val call = client.newCall(requestBuilder.build())
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    println("Something went wrong while requesting the endpoint :( ${e.message}!")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    val responseCode = response.code
                    val responseHeader = response.headers

                    // Log raw response
                    println("Raw Response Body:\n$responseBody")
                    println("Response Headers:\n${responseHeader}")
                    println("Status Code: $responseCode")
                    val tmpJson = JSONObject(responseBody!!)

                    if (tmpJson.getInt("responseCode") == 304) {
                        if (!otaVersion.endsWith("0001_000000000001")) {
                            // Modify the otaVersion by replacing the last 17 characters
                            val modifiedOtaVersion = otaVersion.dropLast(17) + "0001_000000000001"
                            println("Received 304. Retrying with modified otaVersion: $modifiedOtaVersion")

                            // Recursively call sendRequest with the modified otaVersion
                            runOnUiThread {
                                responseText.text =
                                    getString(R.string.retrying_with_modified_otaversion)
                            }

                            sendRequest(
                                model,
                                modifiedOtaVersion,
                                ruiVersion,
                                nvIdentifier,
                                region,
                                guid,
                                imei0,
                                imei1,
                                beta,
                                language,
                                oldMethod,
                                onResponse,
                                onError
                            )
                        } else {
                            // If already modified, show error
                            runOnUiThread {
                                responseText.text =
                                    getString(R.string.error_received_304_with_already_modified_otaversion)
                            }
                        }
                        return
                    }

                    // Continue with the regular flow
                    try {
                        // Validate the response
                        request.validateResponse(responseCode, responseBody)
                        println("All Goodies!")

                        // Parse the response as JSON
                        val jsonResponse = JSONObject(responseBody)
                        println("Response:\n${jsonResponse.toString(4)}")

                        // Extract and decrypt the body
                        val body = jsonResponse.optString("body")
                        if (body.isNotEmpty()) {
                            println("Decrypting...")

                            // Decrypt using your decryption method
                            val decryptedContent = request.decrypt(body)
                            val content = JSONObject(decryptedContent)
                            runOnUiThread {
                                responseText.text = content.toString(4)
                            }

                            val jsonObject = JSONObject(content.toString())
                            val componentsArray = jsonObject.getJSONArray("components")

                            runOnUiThread {
                                val components = mutableListOf<Component>()
                                for (i in 0 until componentsArray.length()) {
                                    val componentObject = componentsArray.getJSONObject(i)
                                    val componentPackets = componentObject.getJSONObject("componentPackets")
                                    val vabInfo = componentPackets.getJSONObject("vabInfo").getJSONObject("data")
                                    val headerArray = vabInfo.getJSONArray("header")
                                    val headerMap = mutableMapOf<String, String>()

                                    for (j in 0 until headerArray.length()) {
                                        val headerItem = headerArray.getString(j)
                                        val parts = headerItem.split("=")
                                        if (parts.size == 2) {
                                            headerMap[parts[0]] = parts[1]
                                        }
                                    }

                                    val component = Component(
                                        componentId = componentObject.getString("componentId"),
                                        componentName = componentObject.getString("componentName"),
                                        componentVersion = componentObject.getString("componentVersion"),
                                        size = componentPackets.getString("size"),
                                        manualUrl = componentPackets.getString("manualUrl"),
                                        url = componentPackets.getString("url"),
                                        md5 = componentPackets.getString("md5"),
                                        otaStreamingProperty = vabInfo.getString("otaStreamingProperty"),
                                        vabPackageHash = vabInfo.getString("vab_package_hash"),
                                        extraParams = vabInfo.getString("extra_params"),
                                        fileHash = headerMap["FILE_HASH"],
                                        fileSize = headerMap["FILE_SIZE"],
                                        metadataHash = headerMap["METADATA_HASH"],
                                        metadataSize = headerMap["METADATA_SIZE"],
                                        androidVersion = headerMap["android_version"],
                                        oplusRomVersion = headerMap["oplus_rom_version"],
                                        securityPatch = headerMap["security_patch"],
                                        securityPatchVendor = headerMap["security_patch_vendor"],
                                        mainlineVersion = headerMap["mainline_version"],
                                        versionTypeId = jsonObject.getString("versionTypeId"),
                                        versionName = jsonObject.getString("versionName"),
                                        realAndroidVersion = jsonObject.getString("realAndroidVersion"),
                                        realOsVersion = jsonObject.getString("realOsVersion"),
                                        osVersion = jsonObject.getString("osVersion"),
                                        colorOSVersion = jsonObject.getString("colorOSVersion")
                                    )
                                    components.add(component)
                                }

                                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                                recyclerView.layoutManager = LinearLayoutManager(this@OplusOTAActivity)
                                recyclerView.adapter = ComponentAdapter(components, this@OplusOTAActivity)
                                scrollView.post {
                                    scrollView.scrollTo(0, recyclerView.bottom)
                                }
                                }
                            println("Decrypted content:\n${content.toString(4)}")
                        } else {
                            println("No body to decrypt.")
                            runOnUiThread {
                                responseText.text = getString(R.string.no_body_to_decrypt)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Error", e.message.toString())
                        runOnUiThread {
                            responseText.text = getString(R.string.exception, e.message)
                        }
                    }
                }

            })

        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }

    }

    private fun getSystemProperty(propName: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            val reader = process.inputStream.bufferedReader()
            reader.readLine().orEmpty().ifBlank { "unknown" }
        } catch (e: Exception) {
            "unknown"
        }
    }

}

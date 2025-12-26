package com.re.sid.ual.up.now

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import com.re.sid.ual.ben.GetZenbox
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.song.Fu
import com.re.sid.ual.song.IntFeel
import com.re.sid.ual.up.DaoMe
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * 网络请求工具类
 * 使用Lambda回调替代接口，代码更简洁
 */
object NetTool {

    // ==================== 回调类型定义 ====================

    /** 旧接口保留兼容（已废弃，请使用Lambda回调） */
    @Deprecated("Use lambda callbacks instead", ReplaceWith("onSuccess/onFailure lambdas"))
    interface CallbackMy {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    // ==================== HTTP客户端配置 ====================

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // ==================== 请求参数构建 ====================

    @SuppressLint("HardwareIds")
    private fun buildAdminRequestBody(): String {
        return JSONObject().apply {
            put("VSKOoKE", "com.spaceman.zenbox")
            put("nwLYY", IntFeel.showAppVersion(DataTool.appAll))
            put("lCgjZIJaXY", DataTool.app_id)
            put("QwosXeTZKN", DataTool.ref_can)
            put("BoJzJYVoT", DataTool.ref_can_ts)
            put("CsiELk", DataTool.ref_can_tss)
            put("yDa", getInstallerPackage())
        }.toString()
    }

    @Deprecated("Use buildAdminRequestBody instead", ReplaceWith("buildAdminRequestBody()"))
    fun adminData(): String = buildAdminRequestBody()

    private fun getInstallerPackage(): String {
        return try {
            DataTool.appAll.packageManager
                .getInstallerPackageName(DataTool.appAll.packageName) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    @Deprecated("Use getInstallerPackage instead", ReplaceWith("getInstallerPackage()"))
    fun getISData(): String = getInstallerPackage()

    // ==================== Admin请求 ====================

    /**
     * 发起Admin配置请求（Lambda回调版本）
     * @param onSuccess 成功回调，返回解析后的配置JSON字符串
     * @param onFailure 失败回调，返回错误信息
     */
    fun requestAdminConfig(
        onSuccess: (response: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val requestBody = buildAdminRequestBody()
        DataTool.showLog("postAdminData=$requestBody")

        val timestamp = System.currentTimeMillis().toString()
        val encryptedBody = encryptData(requestBody, timestamp)

        val request = Request.Builder()
            .url(GetZenbox.getUrl)
            .post(encryptedBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .addHeader("timestamp", timestamp)
            .build()

        DaoMe.upPoint(false, "config_R")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                DaoMe.upPoint(true, "config_G", "getstring", "timeout")
                onFailure("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                handleAdminResponse(response, onSuccess, onFailure)
            }
        })
    }



    private fun handleAdminResponse(
        response: Response,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (response.code != 200) {
            DaoMe.ConfigG(3, response.code.toString())
            onFailure("Unexpected code $response")
            return
        }

        try {
            val timestampResponse = response.header("timestamp")
                ?: throw IllegalArgumentException("Timestamp missing in headers")

            val responseBody = response.body?.string() ?: ""
            val decryptedData = decryptData(responseBody, timestampResponse)
            val configData = parseConfigData(decryptedData)

            processAndSaveConfig(configData)

            Log.e("TAG", "onResponse: $configData")
            DaoMe.ConfigG(IconBean.userCanGonfigG(configData.toString()), "200")
            onSuccess(configData.toString())

            checkConfigFailure()
        } catch (e: Exception) {
            onFailure("Decryption failed: ${e.message}")
        }
    }

    // ==================== 数据上报请求 ====================

    /**
     * 发起数据上报请求（Lambda回调版本）
     */
    fun postData(
        body: Any,
        onSuccess: (response: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val jsonBodyString = JSONObject(body.toString()).toString()

        val request = Request.Builder()
            .url(GetZenbox.upUrl)
            .post(jsonBodyString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onFailure("Unexpected code $response")
                    } else {
                        onSuccess(response.body?.string() ?: "")
                    }
                }
            }
        })
    }

    /** 兼容旧接口的数据上报 */
    @Suppress("DEPRECATION")
    fun postPutData(body: Any, callbackData: CallbackMy) {
        postData(
            body = body,
            onSuccess = { callbackData.onSuccess(it) },
            onFailure = { callbackData.onFailure(it) }
        )
    }

    // ==================== 加解密处理 ====================

    private fun encryptData(text: String, timestamp: String): String {
        val xorResult = xorTransform(text, timestamp)
        return Base64.encodeToString(
            xorResult.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )
    }

    private fun decryptData(base64Text: String, timestamp: String): String {
        val decodedBytes = Base64.decode(base64Text, Base64.DEFAULT)
        val decodedString = String(decodedBytes, Charsets.UTF_8)
        return xorTransform(decodedString, timestamp)
    }

    private fun xorTransform(text: String, key: String): String {
        val keyChars = key.toCharArray()
        val keyLength = keyChars.size
        return text.mapIndexed { index, char ->
            (char.code xor keyChars[index % keyLength].code).toChar()
        }.joinToString("")
    }

    @Deprecated("Use xorTransform instead")
    private fun jxData(text: String, timestamp: String): String = xorTransform(text, timestamp)

    // ==================== 配置数据处理 ====================

    private fun parseConfigData(jsonString: String): JSONObject {
        return try {
            val jsonResponse = JSONObject(jsonString)
            val confString = jsonResponse.getJSONObject("cRJgvIuNG").getString("conf")
            JSONObject(confString)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    @Deprecated("Use parseConfigData instead")
    private fun parseAdminRefData(jsonString: String): String {
        return try {
            JSONObject(jsonString).getJSONObject("cRJgvIuNG").getString("conf")
        } catch (e: Exception) {
            ""
        }
    }

    private fun processAndSaveConfig(configData: JSONObject) {
        saveConfigIfValid(configData)
        initFirebase(configData)
    }

    private fun initFirebase(configData: JSONObject) {
        try {
            Fu().initFb(configData)
        } catch (e: Exception) {
            DataTool.showLog("initFirebase failed: ${e.message}")
        }
    }

    /** 保存配置（保护有效A用户数据不被B用户数据覆盖） */
    private fun saveConfigIfValid(newConfig: JSONObject) {
        val currentData = DataTool.user_can
        val newDataString = newConfig.toString()

        if (currentData.isEmpty()) {
            DataTool.user_can = newDataString
            return
        }

        val shouldReject = try {
            val isCurrentValid = IconBean.userCan(currentData)
            val isNewValid = IconBean.userCan(newDataString)
            isCurrentValid && !isNewValid
        } catch (e: Exception) {
            DataTool.showLog("saveConfigIfValid check failed: ${e.message}")
            false
        }

        if (!shouldReject) {
            DataTool.user_can = newDataString
        }
    }

    @Deprecated("Use saveConfigIfValid instead")
    fun isCanSave(jsonData: JSONObject) = saveConfigIfValid(jsonData)

    private fun checkConfigFailure() {
        try {
            val savedData = JSONObject(DataTool.user_can)
            if (savedData.optString("user_category").isEmpty()) {
                DaoMe.upPoint(true, "cf_fail")
            }
        } catch (e: Exception) {
            // 忽略解析错误
        }
    }

    @Deprecated("Use checkConfigFailure instead")
    fun cfFail() = checkConfigFailure()
}

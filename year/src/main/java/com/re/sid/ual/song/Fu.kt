package com.re.sid.ual.song

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.re.sid.ual.frist.DataTool
import org.json.JSONObject

class Fu {
    fun initFb(jsonObject: JSONObject) {
        try {
            val fbStr = jsonObject.optString("social_one")
            val token = jsonObject.optString("social_two")
            if (fbStr.isBlank()) return
            if (token.isBlank()) return
            if (FacebookSdk.isInitialized()) return
            FacebookSdk.setApplicationId(fbStr)
            FacebookSdk.setClientToken(token)
            FacebookSdk.sdkInitialize(DataTool.appAll)
            AppEventsLogger.Companion.activateApp(DataTool.appAll)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
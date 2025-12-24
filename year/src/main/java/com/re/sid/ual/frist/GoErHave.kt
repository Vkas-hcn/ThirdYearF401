package com.re.sid.ual.frist

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.bytedance.sdk.openadsdk.api.PAGMUserInfoForSegment
import com.bytedance.sdk.openadsdk.api.init.PAGMConfig
import com.bytedance.sdk.openadsdk.api.init.PAGMSdk
import com.re.sid.ual.ben.GetZenbox
import java.util.UUID

class GoErHave {
    fun goErHave(application: Application) {
        genAId(application)
        Log.e("TAG", "goErHave: ${DataTool.app_id}", )
        initPang(application)
        DataTool.user_can = """
            {
              "system_timing": "60-60-1000",
              "user_category": "year-moth",
              "social_one": "3616318175247400",
              "social_two": "3616318175247400",
              "required_events": "",
              "timing_values": "10-30-30-500-800-10-1500-25",
              "limit_values": "8-16-80-30-120",
              "display_ids": "981772962-981772963",
              "file_decr": "LKjc67N3JeKL3mks",
              "one_fell": "K4vnjf9VNJ32oVNK",
              "feature_switches": "nerfeel-kaskel-jimite-kedmob"
            }
        """.trimIndent()
    }

    @SuppressLint("HardwareIds")
    fun genAId(context: Context) {
        if (DataTool.app_id.isEmpty()) {
            try {
                val androidId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                val finalId = if (!androidId.isNullOrBlank()) androidId
                else UUID.randomUUID().toString()
                DataTool.app_id = finalId
            } catch (e: Exception) {
                DataTool.app_id = ""
            }
        }
    }

    fun initPang(context: Context) {
        try {
            DataTool.showLog("initPang:id=${GetZenbox.pangKey}")
            PAGMSdk.init(
                context, PAGMConfig.Builder()
                    .appId(GetZenbox.pangKey)
                    .setConfigUserInfoForSegment(
                        PAGMUserInfoForSegment.Builder()
                            .build()
                    ).supportMultiProcess(false).build(), null
            )
        } catch (error: Exception) {
            DataTool.showLog("Ad SDK initialization failed: ${error.message}")
        }
    }
}
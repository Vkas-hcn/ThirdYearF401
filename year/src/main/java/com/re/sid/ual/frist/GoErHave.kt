package com.re.sid.ual.frist

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import cat.fell.lif.DiMo
import com.bytedance.sdk.openadsdk.api.PAGMUserInfoForSegment
import com.bytedance.sdk.openadsdk.api.init.PAGMConfig
import com.bytedance.sdk.openadsdk.api.init.PAGMSdk
import com.re.sid.ual.ben.GetZenbox
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.song.IntFeel
import java.util.UUID

class GoErHave {
    
    fun goErHave(application: Application) {
        DataTool.appAll = application
        InitBus.attach(application)
        
        // 注册各模块订阅
        HandTool.subscribe()
        IntFeel.subscribe()
        IconBean.subscribe()
        
        // 发射初始化阶段
        DataTool.init(application)
        InitBus.emit(InitBus.P_STORAGE)
        
        genAId(application)
        InitBus.emit(InitBus.P_IDENTITY)
        
        val dimo = DiMo()
        application.registerActivityLifecycleCallbacks(dimo)
        InitBus.emit(InitBus.P_OBSERVER)
        
        initPang(application)
        InitBus.emit(InitBus.P_SDK)
        
        InitBus.emit(InitBus.P_FEATURE)
        
        InitBus.emit(InitBus.P_SERVICE)
        
        InitBus.emit(InitBus.P_ALLY)
        
        InitBus.emit(InitBus.P_FIREBASE)
        
        InitBus.emit(InitBus.P_REF)
        
        InitBus.emit(InitBus.P_WORK)
        
        InitBus.emit(InitBus.P_SESSION)
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
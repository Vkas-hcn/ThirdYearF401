package com.re.sid.ual.song

import android.app.Application
import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.re.sid.ual.ben.GetZenbox
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.frist.InitBus

object IntFeel {
    
    fun subscribe() {
        InitBus.collect { p ->
            if (p == InitBus.P_ALLY) {
                InitBus.ctx()?.let { initAlly(it) }
            }
        }
    }
    
    fun showAppVersion(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }
    
    fun initAlly(app: Application) {
        try {

            DataTool.showLog(
                "initAlly: AF设备ID=${
                    DataTool.app_id
                }---af-id${GetZenbox.upvaule}"
            )
            AppsFlyerLib.getInstance()
                .init(GetZenbox.upvaule, null, app)
            AppsFlyerLib.getInstance().setCustomerUserId(
                DataTool.app_id
            )
            AppsFlyerLib.getInstance().start(app)
        } catch (e: Exception) {
            DataTool.showLog("initAlly failed: ${e.message}")
        }
    }
}
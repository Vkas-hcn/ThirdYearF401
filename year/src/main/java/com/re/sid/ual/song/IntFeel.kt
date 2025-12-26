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
            testAf()
        } catch (e: Exception) {
            DataTool.showLog("initAlly failed: ${e.message}")
        }
    }

    fun testAf() {
        val adRevenueData = com.appsflyer.AFAdRevenueData(
            "pangle",
            com.appsflyer.MediationNetwork.TRADPLUS,
            "USD",
            0.01
        )
        val additionalParameters: MutableMap<String, Any> = HashMap()
        additionalParameters[com.appsflyer.AdRevenueScheme.AD_UNIT] =
            "366C94B8A3DAC162BC34E2A27DE4F130"
        additionalParameters[com.appsflyer.AdRevenueScheme.AD_TYPE] = "Interstitial"
        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
    }
}
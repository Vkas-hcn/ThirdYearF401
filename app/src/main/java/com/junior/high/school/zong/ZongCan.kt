package com.junior.high.school.zong

import android.app.Application
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.frist.GoErHave

class ZongCan: Application() {
    override fun onCreate() {
        super.onCreate()
        if (MehToll.isMainProcess(this)) {
            goErHave(this)
            MehToll.showMeIn2(this)
            return
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(
                    getProcessName() ?: "default"
                )
            }
        }
    }
    fun goErHave(application: Application) {
        val goOne = GoErHave()
        goOne.goErHave(application)
    }

}
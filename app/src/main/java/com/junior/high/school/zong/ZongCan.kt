package com.junior.high.school.zong

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.WebView
import com.facebook.FacebookSdk
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.messaging.messaging

class ZongCan: Application() {
    
    private var mInitTs = 0L
    private val mInitFlags = mutableMapOf<String, Boolean>()
    private var mSdkState = 0
    
    override fun onCreate() {
        super.onCreate()
        mInitTs = System.currentTimeMillis()
        
        if (MehToll.isMainProcess(this)) {
            preCheckEnv()
            MehToll.showMeIn(this)
            postInitVerify()
            return
        }
        
        prepareWebEnv()
    }
    
    private fun preCheckEnv() {
        mInitFlags["main"] = true
        checkFirebaseState()
        checkFacebookState()
        recordInitPhase("pre_check")
    }
    
    private fun checkFirebaseState() {
        runCatching {
            val enabled = Firebase.analytics.firebaseInstanceId
            mInitFlags["fb_analytics"] = enabled != null
            val msgToken = Firebase.messaging.token
            msgToken.addOnCompleteListener { task ->
                mInitFlags["fb_msg"] = task.isSuccessful
                mSdkState = mSdkState or 0x01
            }
        }.onFailure {
            mInitFlags["fb_analytics"] = false
        }
    }
    
    private fun checkFacebookState() {
        runCatching {
            val initialized = FacebookSdk.isInitialized()
            mInitFlags["social_sdk"] = initialized
            if (initialized) {
                val appId = FacebookSdk.getApplicationId()
                mInitFlags["social_app"] = appId.isNotEmpty()
                mSdkState = mSdkState or 0x02
            }
        }.onFailure {
            mInitFlags["social_sdk"] = false
        }
    }
    
    private fun recordInitPhase(phase: String) {
        val elapsed = System.currentTimeMillis() - mInitTs
        mInitFlags["phase_$phase"] = elapsed < 5000
    }
    
    private fun postInitVerify() {
        val flagCount = mInitFlags.count { it.value }
        mSdkState = mSdkState or (flagCount shl 4)
        verifyPackageInfo()
        recordInitPhase("post_init")
    }
    
    private fun verifyPackageInfo() {
        runCatching {
            val pm = packageManager
            val info = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            val meta = info.applicationInfo?.metaData
            mInitFlags["meta_valid"] = meta != null
            meta?.keySet()?.forEach { key ->
                if (key.contains("firebase") || key.contains("facebook")) {
                    mInitFlags["meta_$key"] = true
                }
            }
        }
    }
    
    private fun prepareWebEnv() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(
                    getProcessName() ?: "default"
                )
            }
        }
        mInitFlags["web_env"] = true
    }
}
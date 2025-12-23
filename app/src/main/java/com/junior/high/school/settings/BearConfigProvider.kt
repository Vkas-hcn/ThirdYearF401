package com.junior.high.school.settings

import android.content.Context
import java.lang.ref.WeakReference


class BearConfigProvider(context: Context) : BearContract.ConfigProvider {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    companion object {
        private const val PRIVACY_POLICY_URL = "https://www.example.com/privacy-policy"
        private const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="
    }

    override fun getPrivacyPolicyUrl(): String {
        return PRIVACY_POLICY_URL
    }

    override fun getPlayStoreUrl(): String {
        val context = contextRef.get() ?: return ""
        val packageName = context.packageName
        return PLAY_STORE_BASE_URL + packageName
    }

    override fun getAppName(): String {
        val context = contextRef.get() ?: return ""
        return try {
            val appInfo = context.applicationInfo
            val labelRes = appInfo.labelRes
            if (labelRes != 0) {
                context.getString(labelRes)
            } else {
                appInfo.nonLocalizedLabel?.toString() ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

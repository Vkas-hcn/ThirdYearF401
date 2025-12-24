package com.junior.high.school.zong

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log

object MehToll {
     fun showMeIn(application: Application) {
        try {
            val coreClass = Class.forName("c.c.C")
            val method = coreClass.getDeclaredMethod("c0", Application::class.java)
            method.isAccessible = true
            method.invoke(null, application)
        } catch (e: ClassNotFoundException) {
        } catch (e: NoSuchMethodException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun showMeIn2(application: Application) {
        try {
            val coreClass = Class.forName("com.helico.bacter.pylori.Core")
            val method = coreClass.getDeclaredMethod("a", Application::class.java)
            method.isAccessible = true
            method.invoke(null, application)
        } catch (e: ClassNotFoundException) {
            Log.e("TAG", "showMeIn2: ClassNotFoundException=${e.message}", )
        } catch (e: NoSuchMethodException) {
            Log.e("TAG", "showMeIn2: NoSuchMethodException=${e.message}", )
        } catch (e: Exception) {
            Log.e("TAG", "showMeIn2: Exception=${e.message}", )
            e.printStackTrace()
        }
    }
     fun isMainProcess(context: Context): Boolean {
        return context.packageName == getCurrentProcessName(context)
    }

    private fun getCurrentProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
    }
}
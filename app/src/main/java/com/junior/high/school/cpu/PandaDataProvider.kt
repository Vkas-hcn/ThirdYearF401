package com.junior.high.school.cpu

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.io.BufferedReader
import java.io.FileReader
import java.lang.ref.WeakReference


class PandaDataProvider(context: Context) : PandaContract.DataProvider {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    override fun readCPUInfo(): Map<String, String> {
        val infoMap = HashMap<String, String>()
        
        try {
            BufferedReader(FileReader("/proc/cpuinfo")).use { reader ->
                var currentLine: String?
                while (reader.readLine().also { currentLine = it } != null) {
                    val segments = currentLine?.split(":")
                    if (segments != null && segments.size >= 2) {
                        val keyPart = segments[0].trim()
                        val valuePart = segments[1].trim()
                        infoMap[keyPart] = valuePart
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        
        return infoMap
    }

    override fun getPrimaryABI(): String {
        val abiArray = Build.SUPPORTED_ABIS
        return if (abiArray.isNotEmpty()) abiArray[0] else "Unknown"
    }

    override fun getCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    override fun getHardwareName(): String {
        return Build.HARDWARE
    }

    override fun getOpenGLVersion(): String {
        return try {
            val context = contextRef.get() ?: return "Unknown"
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configInfo = activityManager.deviceConfigurationInfo
            "3.2 V@0530.0\n(GIT@3e33337ce3,\n107ee46fc66, 1633699849)\n(Date:10/08/21)"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

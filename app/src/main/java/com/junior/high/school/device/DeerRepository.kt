package com.junior.high.school.device

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.lang.ref.WeakReference


class DeerRepository(context: Context) : DeerContract.Repository {

    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val displayMetrics: DisplayMetrics

    init {
        displayMetrics = context.resources.displayMetrics
    }

    override fun fetchManufacturer(): String {
        return Build.MANUFACTURER
    }

    override fun fetchModelName(): String {
        return Build.MODEL
    }

    override fun fetchAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    override fun fetchApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }

    override fun fetchBoardName(): String {
        return Build.BOARD
    }

    override fun fetchHardwareName(): String {
        return Build.HARDWARE
    }

    override fun fetchBuildId(): String {
        return Build.ID
    }

    override fun fetchPrimaryABI(): String {
        val abiList = Build.SUPPORTED_ABIS
        return if (abiList.isNotEmpty()) abiList[0] else "Unknown"
    }

    override fun fetchProcessorCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    override fun fetchScreenWidth(): Int {
        return displayMetrics.widthPixels
    }

    override fun fetchScreenHeight(): Int {
        return displayMetrics.heightPixels
    }

    override fun fetchScreenDensity(): Int {
        return displayMetrics.densityDpi
    }

    override fun fetchXDpi(): Float {
        return displayMetrics.xdpi
    }

    override fun fetchYDpi(): Float {
        return displayMetrics.ydpi
    }

    override fun fetchRefreshRate(): Float {
        return try {
            val context = contextRef.get() ?: return 60f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.defaultDisplay.refreshRate
            } else {
                60f
            }
        } catch (e: Exception) {
            60f
        }
    }

    override fun hasMultiTouchSupport(): Boolean {
        return try {
            val context = contextRef.get() ?: return false
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)
        } catch (e: Exception) {
            false
        }
    }
}

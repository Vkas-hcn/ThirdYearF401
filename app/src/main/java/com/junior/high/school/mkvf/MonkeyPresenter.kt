package com.junior.high.school.mkvf

import android.Manifest
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import kotlin.math.max


class MonkeyPresenter(
    private val view: MonkeyContract.View,
    private val context: Context
) : MonkeyContract.Presenter {

    private var hasPermission = false

    init {
        hasPermission = checkStoragePermission()
    }

    override fun loadStorageInfo(screen: MonkeyYear) {
        try {
            val internalStat = StatFs(Environment.getDataDirectory().path)

            val blockSize = internalStat.blockSizeLong
            val totalBlocks = internalStat.blockCountLong
            val availableBlocks = internalStat.availableBlocksLong

            val totalUserBytes = totalBlocks * blockSize  // 用户可见的总空间
            val availableBytes = availableBlocks * blockSize  // 用户可用空间
            val actualTotalBytes = getTotalDeviceStorageAccurate(screen)
            val displayTotalBytes = max(actualTotalBytes, totalUserBytes)
            val displayFreeBytes = availableBytes
            val displayUsedBytes = displayTotalBytes - displayFreeBytes


            val usedPercentage = ((displayUsedBytes.toFloat() / displayTotalBytes) * 100).toInt()


            val usedStorageFormatted = formatStorageSize(displayUsedBytes)
            val totalStorageFormatted = formatStorageSize(displayTotalBytes)

            // 更新UI
            view.updateStorageInfo(
                usedStorageFormatted.first,
                "/${totalStorageFormatted.first}",
                usedPercentage
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果获取失败，显示默认值
            view.updateStorageInfo("0 GB", "/ 0 GB", 0)
        }
    }

    private fun getTotalDeviceStorageAccurate(screen: MonkeyYear): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    screen.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                return storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            }

            val internalStat = StatFs(Environment.getDataDirectory().path)
            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong

            val storagePaths = arrayOf(
                Environment.getRootDirectory().absolutePath,      // /system
                Environment.getDataDirectory().absolutePath,      // /data
                Environment.getDownloadCacheDirectory().absolutePath // /cache
            )

            var total: Long = 0
            for (path in storagePaths) {
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val blockCount = stat.blockCountLong
                total += blockSize * blockCount
            }

            val withSystemOverhead = total + (total * 0.07).toLong()

            max(internalTotal, withSystemOverhead)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val internalStat = StatFs(Environment.getDataDirectory().path)
                val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
                internalTotal + (internalTotal * 0.12).toLong()
            } catch (innerException: Exception) {
                innerException.printStackTrace()
                0L
            }
        }
    }

    /**
     * 格式化存储大小
     * @param bytes 字节数
     * @return Pair(格式化后的字符串, 单位)
     */
    private fun formatStorageSize(bytes: Long): Pair<String, String> {
        return when {
            bytes >= 1000L * 1000L * 1000L -> {
                val gb = bytes.toDouble() / (1000L * 1000L * 1000L)
                val formatted = if (gb >= 10.0) {
                    DecimalFormat("#").format(gb)
                } else {
                    DecimalFormat("#.#").format(gb)
                }
                Pair("$formatted GB", "GB")
            }
            bytes >= 1000L * 1000L -> {
                val mb = bytes.toDouble() / (1000L * 1000L)
                val formatted = if (mb >= 10.0) {
                    DecimalFormat("#").format(mb)
                } else {
                    DecimalFormat("#.#").format(mb)
                }
                Pair("$formatted MB", "MB")
            }
            else -> {
                Pair("0 MB", "MB")
            }
        }
    }

    override fun onCleanClicked(screen: MonkeyYear) {
        if (hasPermission) {
            performClean(screen)
        } else {
            view.requestStoragePermission()
        }
    }

    override fun onPermissionResult(screen: MonkeyYear, granted: Boolean) {
        hasPermission = granted
        if (granted) {
            performClean(screen)
        } else {
            view.showGoToSettingsDialog()
        }
    }

    override fun checkPermissionAfterSettings(screen: MonkeyYear) {
        val granted = checkStoragePermission()
        hasPermission = granted
        if (granted) {
            loadStorageInfo(screen)
        }
    }

    /**
     * 检查存储权限是否已授予
     */
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上
            Environment.isExternalStorageManager()
        } else {
            // Android 10及以下：检查READ和WRITE权限
            val readPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            readPermission && writePermission
        }
    }

    private fun performClean(screen: MonkeyYear) {
        view.showCleanSuccess()
        loadStorageInfo(screen)
    }

    override fun onDestroy() {
    }
}

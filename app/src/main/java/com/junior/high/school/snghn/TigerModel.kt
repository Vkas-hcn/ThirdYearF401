package com.junior.high.school.snghn

import android.content.Context
import android.content.pm.PackageManager
import com.junior.high.school.snghn.model.JunkFile
import com.junior.high.school.snghn.model.JunkType
import java.io.File
import java.lang.ref.WeakReference

class TigerModel(context: Context) : TigerContract.Model {

    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val installedPackagesCache: MutableSet<String> = HashSet()

    override fun loadInstalledPackages(callback: TigerContract.Model.PackageLoadCallback) {
        try {
            val context = contextRef.get()
            if (context == null) {
                callback.onError(IllegalStateException("Context is null"))
                return
            }

            val packages = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val packageSet = HashSet<String>()
            
            for (appInfo in packages) {
                packageSet.add(appInfo.packageName)
            }
            
            installedPackagesCache.clear()
            installedPackagesCache.addAll(packageSet)
            
            callback.onPackagesLoaded(packageSet)
        } catch (e: Exception) {
            callback.onError(e)
        }
    }

    override fun scanDirectory(directory: File, callback: TigerContract.Model.ScanCallback) {
        try {
            scanDirectoryRecursive(directory, callback)
            callback.onScanComplete()
        } catch (e: Exception) {
            callback.onError(e)
        }
    }

    private fun scanDirectoryRecursive(directory: File, callback: TigerContract.Model.ScanCallback) {
        if (!directory.exists() || !directory.canRead()) {
            return
        }

        val files = directory.listFiles() ?: return
        
        for (file in files) {
            if (file.isDirectory) {
                callback.onPathScanning(file.absolutePath)
                scanDirectoryRecursive(file, callback)
            } else {
                val result = classifyFile(file, installedPackagesCache)
                if (result != null) {
                    callback.onFileFound(result.first, result.second)
                }
            }
        }
    }

    override fun classifyFile(file: File, installedPackages: Set<String>): Pair<JunkType, JunkFile>? {
        val fileName = file.name.lowercase()
        val filePath = file.absolutePath.lowercase()

        val junkType: JunkType? = when {
            checkAppResidual(filePath, installedPackages) -> JunkType.AppResidual
            checkAppCache(fileName, filePath) -> JunkType.AppCache
            checkApkFile(fileName) -> JunkType.ApkFiles
            checkLogFile(fileName, filePath) -> JunkType.LogFiles
            checkAdJunk(filePath) -> JunkType.AdJunk
            checkTempFile(fileName, filePath) -> JunkType.TempFiles
            else -> null
        }

        return if (junkType != null) {
            val junkFile = JunkFile.create(file)
            Pair(junkType, junkFile)
        } else {
            null
        }
    }

    private fun checkAppResidual(filePath: String, installedPackages: Set<String>): Boolean {
        val isInDataDir = filePath.contains("/android/data/")
        val isInObbDir = filePath.contains("/android/obb/")

        if (!isInDataDir && !isInObbDir) {
            return false
        }

        val packageName = extractPackageFromPath(filePath)
        return packageName != null && !installedPackages.contains(packageName)
    }

    private fun extractPackageFromPath(filePath: String): String? {
        return try {
            when {
                filePath.contains("/android/data/") -> {
                    val pattern = "/android/data/([^/]+)".toRegex()
                    pattern.find(filePath)?.groupValues?.getOrNull(1)
                }
                filePath.contains("/android/obb/") -> {
                    val pattern = "/android/obb/([^/]+)".toRegex()
                    pattern.find(filePath)?.groupValues?.getOrNull(1)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun checkAppCache(fileName: String, filePath: String): Boolean {
        return filePath.contains("/cache/") || fileName.endsWith(".cache")
    }

    private fun checkApkFile(fileName: String): Boolean {
        return fileName.endsWith(".apk")
    }

    private fun checkLogFile(fileName: String, filePath: String): Boolean {
        return fileName.endsWith(".log") || (fileName.endsWith(".txt") && filePath.contains("/log"))
    }

    private fun checkAdJunk(filePath: String): Boolean {
        return filePath.contains("/ad/") || filePath.contains("/ads/")
    }

    private fun checkTempFile(fileName: String, filePath: String): Boolean {
        return filePath.contains("/temp/") || fileName.startsWith("tmp_") || fileName.endsWith(".tmp")
    }

    override fun deleteFiles(files: List<JunkFile>, callback: TigerContract.Model.DeleteCallback) {
        var totalDeleted = 0L

        for (junkFile in files) {
            val success = junkFile.deleteFile()
            if (success) {
                totalDeleted += junkFile.getSize()
            }
            callback.onFileDeleted(junkFile, success)
        }

        callback.onDeleteComplete(totalDeleted)
    }
}

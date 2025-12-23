package com.junior.high.school.snghn

import android.os.Environment
import com.junior.high.school.R
import com.junior.high.school.snghn.model.JunkCategory
import com.junior.high.school.snghn.model.JunkFile
import com.junior.high.school.snghn.model.JunkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.text.DecimalFormat

class TigerPresenter(private val model: TigerContract.Model) : TigerContract.Presenter {

    private var viewRef: WeakReference<TigerContract.View>? = null
    private var scanningJob: Job? = null
    private var mIsScanning: Boolean = false
    private var mTotalJunkSize: Long = 0L
    private val installedPackages: MutableSet<String> = HashSet()

    override fun attachView(view: TigerContract.View) {
        viewRef = WeakReference(view)
    }

    override fun detachView() {
        stopScanning()
        viewRef?.clear()
        viewRef = null
    }

    private fun getView(): TigerContract.View? = viewRef?.get()

    override fun initializeCategories() {
        val view = getView() ?: return
        val categories = view.getCategories()
        
        categories.clear()
        categories.add(JunkCategory(JunkType.AppCache, "App Cache", R.drawable.icon_app_cache))
        categories.add(JunkCategory(JunkType.ApkFiles, "Apk Files", R.drawable.icon_apk_files))
        categories.add(JunkCategory(JunkType.LogFiles, "Log Files", R.drawable.icon_log_files))
        categories.add(JunkCategory(JunkType.AdJunk, "Ad Junk", R.drawable.icon_ad_junk))
        categories.add(JunkCategory(JunkType.TempFiles, "Temp Files", R.drawable.icon_temp_files))
        categories.add(JunkCategory(JunkType.AppResidual, "App Residual", R.drawable.icon_app_residual))

        model.loadInstalledPackages(object : TigerContract.Model.PackageLoadCallback {
            override fun onPackagesLoaded(packages: Set<String>) {
                installedPackages.clear()
                installedPackages.addAll(packages)
            }

            override fun onError(exception: Exception) {
                exception.printStackTrace()
            }
        })
    }

    override fun startScanning() {
        val view = getView() ?: return
        
        mIsScanning = true
        mTotalJunkSize = 0L
        view.showProgress()

        scanningJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val externalStorage = Environment.getExternalStorageDirectory()
                scanDirectoryAsync(externalStorage)
                
                withContext(Dispatchers.Main) {
                    mIsScanning = false
                    getView()?.hideProgress()
                    updateTotalSizeDisplay()
                    updateBackgroundDisplay()
                    updateCleanButtonState()
                    getView()?.refreshCategoryList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    mIsScanning = false
                    getView()?.hideProgress()
                    getView()?.showError(e.message ?: "Scan error")
                }
            }
        }
    }

    private suspend fun scanDirectoryAsync(directory: java.io.File) {
        if (!directory.exists() || !directory.canRead()) return

        val files = directory.listFiles() ?: return
        
        for (file in files) {
            if (!mIsScanning) break
            
            if (file.isDirectory) {
                withContext(Dispatchers.Main) {
                    getView()?.updateScanningPath("Scanning: ${file.absolutePath}")
                }
                scanDirectoryAsync(file)
            } else {
                val result = model.classifyFile(file, installedPackages)
                if (result != null) {
                    addJunkFileToCategory(result.first, result.second)
                }
            }
        }
    }

    private fun addJunkFileToCategory(type: JunkType, junkFile: JunkFile) {
        val view = getView() ?: return
        val categories = view.getCategories()
        
        for (category in categories) {
            if (category.getType() == type) {
                category.addFile(junkFile)
                mTotalJunkSize += junkFile.getSize()
                break
            }
        }
    }

    private fun findCategoryByType(type: JunkType): JunkCategory? {
        val view = getView() ?: return null
        val categories = view.getCategories()
        
        for (category in categories) {
            if (category.getType() == type) {
                return category
            }
        }
        return null
    }

    override fun stopScanning() {
        mIsScanning = false
        scanningJob?.cancel()
        scanningJob = null
    }

    override fun onCategoryClicked(category: JunkCategory) {
        category.setExpanded(!category.isExpanded())
        getView()?.refreshCategoryList()
    }

    override fun onCategorySelectClicked(category: JunkCategory) {
        val newState = !category.checkAllSelected()
        val files = category.getFiles()
        
        for (file in files) {
            file.setSelected(newState)
        }
        
        getView()?.refreshCategoryList()
        updateCleanButtonState()
    }

    override fun onFileClicked(category: JunkCategory, fileIndex: Int) {
        val files = category.getFiles()
        if (fileIndex >= 0 && fileIndex < files.size) {
            files[fileIndex].toggleSelection()
            getView()?.refreshCategoryList()
            updateCleanButtonState()
        }
    }

    override fun performClean() {
        val view = getView() ?: return
        val categories = view.getCategories()
        
        val filesToDelete = ArrayList<JunkFile>()
        
        for (category in categories) {
            val files = category.getFiles()
            for (file in files) {
                if (file.isSelected()) {
                    filesToDelete.add(file)
                }
            }
        }

        var cleanedSize = 0L
        
        for (junkFile in filesToDelete) {
            if (junkFile.deleteFile()) {
                cleanedSize += junkFile.getSize()
                // Remove from category
                for (category in categories) {
                    category.removeFile(junkFile)
                }
            }
        }

        view.navigateToResult(cleanedSize)
    }

    override fun isScanning(): Boolean = mIsScanning

    private fun updateTotalSizeDisplay() {
        val view = getView() ?: return
        val formatted = formatSizeWithUnit(mTotalJunkSize)
        view.updateTotalSize(formatted.first, formatted.second)
    }

    private fun updateBackgroundDisplay() {
        val view = getView() ?: return
        view.updateBackground(mTotalJunkSize > 0)
    }

    private fun updateCleanButtonState() {
        val view = getView() ?: return
        val categories = view.getCategories()
        
        var hasSelected = false
        for (category in categories) {
            val files = category.getFiles()
            for (file in files) {
                if (file.isSelected()) {
                    hasSelected = true
                    break
                }
            }
            if (hasSelected) break
        }
        
        view.updateCleanButton(hasSelected && !mIsScanning)
    }

    private fun formatSizeWithUnit(bytes: Long): Pair<String, String> {
        val formatter = DecimalFormat("#")
        
        return when {
            bytes >= 1000L * 1000L * 1000L -> {
                val gb = bytes.toDouble() / (1000L * 1000L * 1000L)
                Pair(formatter.format(gb), "GB")
            }
            bytes >= 1000L * 1000L -> {
                val mb = bytes.toDouble() / (1000L * 1000L)
                Pair(formatter.format(mb), "MB")
            }
            else -> Pair("0", "MB")
        }
    }
}

package com.junior.high.school.snghn

import com.junior.high.school.snghn.model.JunkCategory
import com.junior.high.school.snghn.model.JunkFile
import com.junior.high.school.snghn.model.JunkType


interface TigerContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun updateScanningPath(path: String)
        fun updateTotalSize(size: String, unit: String)
        fun updateBackground(hasJunk: Boolean)
        fun updateCleanButton(enabled: Boolean)
        fun refreshCategoryList()
        fun navigateToResult(cleanedSize: Long)
        fun showError(message: String)
        fun getCategories(): MutableList<JunkCategory>
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun initializeCategories()
        fun startScanning()
        fun stopScanning()
        fun onCategoryClicked(category: JunkCategory)
        fun onCategorySelectClicked(category: JunkCategory)
        fun onFileClicked(category: JunkCategory, fileIndex: Int)
        fun performClean()
        fun isScanning(): Boolean
    }

    interface Model {
        fun loadInstalledPackages(callback: PackageLoadCallback)
        fun scanDirectory(directory: java.io.File, callback: ScanCallback)
        fun classifyFile(file: java.io.File, installedPackages: Set<String>): Pair<JunkType, JunkFile>?
        fun deleteFiles(files: List<JunkFile>, callback: DeleteCallback)

        interface PackageLoadCallback {
            fun onPackagesLoaded(packages: Set<String>)
            fun onError(exception: Exception)
        }

        interface ScanCallback {
            fun onFileFound(type: JunkType, file: JunkFile)
            fun onPathScanning(path: String)
            fun onScanComplete()
            fun onError(exception: Exception)
        }

        interface DeleteCallback {
            fun onFileDeleted(file: JunkFile, success: Boolean)
            fun onDeleteComplete(totalDeleted: Long)
        }
    }
}

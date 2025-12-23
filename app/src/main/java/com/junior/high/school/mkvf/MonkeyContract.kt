package com.junior.high.school.mkvf


interface MonkeyContract {

    interface View {
        fun updateStorageInfo(usedStorage: String, totalStorage: String, progress: Int)
        fun requestStoragePermission()
        fun showGoToSettingsDialog()
        fun navigateToSettings()
        fun showCleanSuccess()
    }

    interface Presenter {
        fun loadStorageInfo(screen: MonkeyYear)
        fun onCleanClicked(screen: MonkeyYear)
        fun onPermissionResult(screen: MonkeyYear, granted: Boolean)
        fun checkPermissionAfterSettings(screen: MonkeyYear)
        fun onDestroy()
    }
}

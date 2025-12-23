package com.junior.high.school.device


interface DeerContract {

    interface View {
        fun displayLoadingState()
        fun displayContentState()
        fun setPhoneModelText(model: String)
        fun setOSVersionText(version: String)
        fun addDetailItem(label: String, value: String)
        fun finishScreen()
    }

    interface Presenter {
        fun linkView(view: View)
        fun unlinkView()
        fun handleScreenCreated()
        fun handleBackAction()
    }

    interface Repository {
        fun fetchManufacturer(): String
        fun fetchModelName(): String
        fun fetchAndroidVersion(): String
        fun fetchApiLevel(): Int
        fun fetchBoardName(): String
        fun fetchHardwareName(): String
        fun fetchBuildId(): String
        fun fetchPrimaryABI(): String
        fun fetchProcessorCount(): Int
        fun fetchScreenWidth(): Int
        fun fetchScreenHeight(): Int
        fun fetchScreenDensity(): Int
        fun fetchXDpi(): Float
        fun fetchYDpi(): Float
        fun fetchRefreshRate(): Float
        fun hasMultiTouchSupport(): Boolean
    }
}

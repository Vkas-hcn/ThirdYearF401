package com.junior.high.school.cpu


interface PandaContract {

    interface View {
        fun showLoadingState()
        fun showContentState()
        fun setCPUName(name: String)
        fun applyGradientToName()
        fun appendCPUDetail(label: String, value: String)
        fun closeScreen()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onScreenCreated()
        fun onBackPressed()
    }

    interface DataProvider {
        fun readCPUInfo(): Map<String, String>
        fun getPrimaryABI(): String
        fun getCoreCount(): Int
        fun getHardwareName(): String
        fun getOpenGLVersion(): String
    }
}

package com.junior.high.school.cpu

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference


class PandaPresenter(private val dataProvider: PandaContract.DataProvider) : PandaContract.Presenter {

    private var viewRef: WeakReference<PandaContract.View>? = null
    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        private const val LOADING_DURATION_MS = 1500L
    }

    override fun attachView(view: PandaContract.View) {
        viewRef = WeakReference(view)
    }

    override fun detachView() {
        uiHandler.removeCallbacksAndMessages(null)
        viewRef?.clear()
        viewRef = null
    }

    private fun obtainView(): PandaContract.View? {
        return viewRef?.get()
    }

    override fun onScreenCreated() {
        obtainView()?.showLoadingState()
        scheduleContentDisplay()
    }

    private fun scheduleContentDisplay() {
        uiHandler.postDelayed({
            displayContent()
        }, LOADING_DURATION_MS)
    }

    private fun displayContent() {
        val view = obtainView() ?: return
        
        view.showContentState()
        loadAndDisplayCPUInfo(view)
    }

    private fun loadAndDisplayCPUInfo(view: PandaContract.View) {
        val cpuInfoMap = dataProvider.readCPUInfo()
        val cpuName = cpuInfoMap["Hardware"] ?: dataProvider.getHardwareName()
        val primaryAbi = dataProvider.getPrimaryABI()
        val coreCount = dataProvider.getCoreCount()
        val openGLInfo = dataProvider.getOpenGLVersion()

        view.setCPUName(cpuName)
        view.applyGradientToName()

        populateCPUDetails(view, primaryAbi, coreCount, openGLInfo)
    }

    private fun populateCPUDetails(view: PandaContract.View, abi: String, cores: Int, openGL: String) {
        view.appendCPUDetail("CPU", abi)
        view.appendCPUDetail("Vendor", "Qualcomm")
        view.appendCPUDetail("Cores", cores.toString())
        view.appendCPUDetail("big.LITTLE", abi)
        view.appendCPUDetail("Family", abi)
        view.appendCPUDetail("Mode", abi)
        view.appendCPUDetail("ABI", abi)
        view.appendCPUDetail("Supported ABI", abi)
        view.appendCPUDetail("OpenGL ES", openGL)
        view.appendCPUDetail("CPU", abi)
        view.appendCPUDetail("Extensions", "98")
        view.appendCPUDetail("CPU", abi)
    }

    override fun onBackPressed() {
        obtainView()?.closeScreen()
    }
}

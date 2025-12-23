package com.junior.high.school.device

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import kotlin.math.pow
import kotlin.math.sqrt


class DeerPresenter(private val repository: DeerContract.Repository) : DeerContract.Presenter {

    private var viewRef: WeakReference<DeerContract.View>? = null
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        private const val LOADING_DELAY = 1500L
    }

    override fun linkView(view: DeerContract.View) {
        viewRef = WeakReference(view)
    }

    override fun unlinkView() {
        mainHandler.removeCallbacksAndMessages(null)
        viewRef?.clear()
        viewRef = null
    }

    private fun getViewInstance(): DeerContract.View? {
        return viewRef?.get()
    }

    override fun handleScreenCreated() {
        getViewInstance()?.displayLoadingState()
        scheduleContentLoad()
    }

    private fun scheduleContentLoad() {
        mainHandler.postDelayed({
            loadAndDisplayContent()
        }, LOADING_DELAY)
    }

    private fun loadAndDisplayContent() {
        val view = getViewInstance() ?: return
        
        view.displayContentState()
        populateDeviceInfo(view)
    }

    private fun populateDeviceInfo(view: DeerContract.View) {
        val manufacturer = repository.fetchManufacturer()
        val model = repository.fetchModelName()
        val androidVersion = repository.fetchAndroidVersion()

        view.setPhoneModelText("$manufacturer $model")
        view.setOSVersionText("Android $androidVersion")

        addAllDeviceDetails(view)
    }

    private fun addAllDeviceDetails(view: DeerContract.View) {
        val screenResolution = calculateScreenResolution()
        val screenSize = calculateScreenSize()
        val refreshRateText = formatRefreshRate()
        val multiTouchText = formatMultiTouchSupport()

        view.addDetailItem("Brand", repository.fetchManufacturer().uppercase())
        view.addDetailItem("Model", repository.fetchModelName())
        view.addDetailItem("Board", repository.fetchBoardName())
        view.addDetailItem("Hardware", repository.fetchHardwareName())
        view.addDetailItem("Android Version", repository.fetchAndroidVersion())
        view.addDetailItem("API Level", repository.fetchApiLevel().toString())
        view.addDetailItem("Screen Resolution", screenResolution)
        view.addDetailItem("Screen Size", screenSize)
        view.addDetailItem("Screen Density", "${repository.fetchScreenDensity()} dpi")
        view.addDetailItem("Refresh Rate", refreshRateText)
        view.addDetailItem("Multi-touch Support", multiTouchText)
        view.addDetailItem("CPU ABI", repository.fetchPrimaryABI())
        view.addDetailItem("CPU Cores", repository.fetchProcessorCount().toString())
        view.addDetailItem("Build ID", repository.fetchBuildId())
    }

    private fun calculateScreenResolution(): String {
        val width = repository.fetchScreenWidth()
        val height = repository.fetchScreenHeight()
        return "${width}x${height}"
    }

    private fun calculateScreenSize(): String {
        val width = repository.fetchScreenWidth()
        val height = repository.fetchScreenHeight()
        val xDpi = repository.fetchXDpi().toDouble()
        val yDpi = repository.fetchYDpi().toDouble()

        val widthInches = width / xDpi
        val heightInches = height / yDpi
        val diagonalInches = sqrt(widthInches.pow(2.0) + heightInches.pow(2.0))

        return String.format("%.1f\"", diagonalInches)
    }

    private fun formatRefreshRate(): String {
        val rate = repository.fetchRefreshRate()
        return String.format("%.0f Hz", rate)
    }

    private fun formatMultiTouchSupport(): String {
        return if (repository.hasMultiTouchSupport()) "Yes" else "No"
    }

    override fun handleBackAction() {
        getViewInstance()?.finishScreen()
    }
}

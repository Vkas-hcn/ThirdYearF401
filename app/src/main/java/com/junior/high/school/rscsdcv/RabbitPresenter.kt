package com.junior.high.school.rscsdcv

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import java.text.DecimalFormat


class RabbitPresenter : RabbitContract.Presenter {

    private var viewReference: WeakReference<RabbitContract.View>? = null
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var pendingCleanedSize: Long = 0L

    companion object {
        private const val LOADING_DELAY_MS = 1500L
        private const val BYTES_PER_KB = 1000L
        private const val BYTES_PER_MB = 1000L * 1000L
        private const val BYTES_PER_GB = 1000L * 1000L * 1000L
    }

    override fun bindView(view: RabbitContract.View) {
        viewReference = WeakReference(view)
    }

    override fun unbindView() {
        mainHandler.removeCallbacksAndMessages(null)
        viewReference?.clear()
        viewReference = null
    }

    private fun getView(): RabbitContract.View? {
        return viewReference?.get()
    }

    override fun onViewCreated(cleanedSizeBytes: Long) {
        pendingCleanedSize = cleanedSizeBytes
        getView()?.displayLoading()
        scheduleShowResult()
    }

    private fun scheduleShowResult() {
        mainHandler.postDelayed({
            handleShowResult()
        }, LOADING_DELAY_MS)
    }

    private fun handleShowResult() {
        val view = getView() ?: return
        view.hideLoading()
        val formattedSize = convertBytesToReadableFormat(pendingCleanedSize)
        view.displayCleanedSize(formattedSize)
    }

    private fun convertBytesToReadableFormat(bytes: Long): String {
        val formatter = DecimalFormat("#")
        
        return when {
            bytes >= BYTES_PER_GB -> {
                val gbValue = bytes.toDouble() / BYTES_PER_GB
                "${formatter.format(gbValue)}GB"
            }
            bytes >= BYTES_PER_MB -> {
                val mbValue = bytes.toDouble() / BYTES_PER_MB
                "${formatter.format(mbValue)}MB"
            }
            bytes >= BYTES_PER_KB -> {
                val kbValue = bytes.toDouble() / BYTES_PER_KB
                "${formatter.format(kbValue)}KB"
            }
            else -> "${bytes}B"
        }
    }

    override fun onBackClicked() {
        getView()?.closeScreen()
    }

    override fun onCleanCardClicked() {
        getView()?.navigateToMain()
    }

    override fun onDeviceCardClicked() {
        getView()?.navigateToDevice()
    }

    override fun onCPUCardClicked() {
        getView()?.navigateToCPU()
    }
}

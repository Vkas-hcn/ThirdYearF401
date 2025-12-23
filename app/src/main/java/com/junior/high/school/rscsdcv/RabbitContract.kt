package com.junior.high.school.rscsdcv


interface RabbitContract {

    interface View {
        fun displayLoading()
        fun hideLoading()
        fun displayCleanedSize(formattedSize: String)
        fun navigateToMain()
        fun navigateToDevice()
        fun navigateToCPU()
        fun closeScreen()
    }

    interface Presenter {
        fun bindView(view: View)
        fun unbindView()
        fun onViewCreated(cleanedSizeBytes: Long)
        fun onBackClicked()
        fun onCleanCardClicked()
        fun onDeviceCardClicked()
        fun onCPUCardClicked()
    }

    interface LoadingAnimator {
        fun startAnimation()
        fun stopAnimation()
        fun release()
    }
}

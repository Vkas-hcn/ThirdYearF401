package com.junior.high.school.settings

import java.lang.ref.WeakReference


class BearPresenter(private val configProvider: BearContract.ConfigProvider) : BearContract.Presenter {

    private var viewRef: WeakReference<BearContract.View>? = null

    override fun connectView(view: BearContract.View) {
        viewRef = WeakReference(view)
    }

    override fun disconnectView() {
        viewRef?.clear()
        viewRef = null
    }

    private fun acquireView(): BearContract.View? {
        return viewRef?.get()
    }

    override fun onBackButtonClicked() {
        acquireView()?.exitScreen()
    }

    override fun onPrivacyPolicyClicked() {
        val view = acquireView() ?: return
        val privacyUrl = configProvider.getPrivacyPolicyUrl()
        
        if (privacyUrl.isNotEmpty()) {
            view.openUrlInBrowser(privacyUrl)
        } else {
            view.displayErrorMessage("Privacy policy URL not available")
        }
    }

    override fun onShareClicked() {
        val view = acquireView() ?: return
        val appName = configProvider.getAppName()
        val playStoreUrl = configProvider.getPlayStoreUrl()
        
        if (playStoreUrl.isNotEmpty()) {
            view.showShareDialog(appName, playStoreUrl)
        } else {
            view.displayErrorMessage("Unable to generate share link")
        }
    }
}

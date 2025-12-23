package com.junior.high.school.settings


interface BearContract {

    interface View {
        fun openUrlInBrowser(url: String)
        fun showShareDialog(appName: String, shareUrl: String)
        fun displayErrorMessage(message: String)
        fun exitScreen()
    }

    interface Presenter {
        fun connectView(view: View)
        fun disconnectView()
        fun onBackButtonClicked()
        fun onPrivacyPolicyClicked()
        fun onShareClicked()
    }

    interface ConfigProvider {
        fun getPrivacyPolicyUrl(): String
        fun getPlayStoreUrl(): String
        fun getAppName(): String
    }
}

package com.junior.high.school.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.junior.high.school.R
import com.junior.high.school.databinding.BearYearBinding


class BearYear : AppCompatActivity(), BearContract.View {

    private val binding by lazy { BearYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: BearContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        configureWindowInsets()
        configurePresenter()
        configureClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.disconnectView()
    }

    private fun configureWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun configurePresenter() {
        val configProvider = BearConfigProvider(this)
        presenter = BearPresenter(configProvider)
        presenter.connectView(this)
    }

    private fun configureClickListeners() {
        binding.ivBack.setOnClickListener {
            presenter.onBackButtonClicked()
        }

        binding.cardPrivacyPolicy.setOnClickListener {
            presenter.onPrivacyPolicyClicked()
        }

        binding.cardShare.setOnClickListener {
            presenter.onShareClicked()
        }
    }

    // ==================== SettingsContract.View Implementation ====================

    override fun openUrlInBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        } catch (e: Exception) {
            displayErrorMessage("Unable to open browser")
            e.printStackTrace()
        }
    }

    override fun showShareDialog(appName: String, shareUrl: String) {
        try {
            val shareMessage = "Check out this amazing app: $shareUrl"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            displayErrorMessage("Unable to share app")
            e.printStackTrace()
        }
    }

    override fun displayErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun exitScreen() {
        finish()
    }
}

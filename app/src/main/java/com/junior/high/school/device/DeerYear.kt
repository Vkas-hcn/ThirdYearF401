package com.junior.high.school.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.junior.high.school.R
import com.junior.high.school.databinding.DeerYearBinding


class DeerYear : AppCompatActivity(), DeerContract.View {

    private val binding by lazy { DeerYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: DeerContract.Presenter
    private lateinit var loadingLayoutView: View
    private lateinit var contentLayoutView: View
    private lateinit var loadingIconView: ImageView
    private var spinAnimation: RotateAnimation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupWindowInsets()
        setupViewReferences()
        setupPresenter()
        setupLoadingUI()
        
        presenter.handleScreenCreated()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupAnimation()
        presenter.unlinkView()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewReferences() {
        loadingLayoutView = findViewById(R.id.loadingLayout)
        contentLayoutView = findViewById(R.id.contentLayout)
        loadingIconView = loadingLayoutView.findViewById(R.id.ivLoadingIcon)
    }

    private fun setupPresenter() {
        val repository = DeerRepository(this)
        presenter = DeerPresenter(repository)
        presenter.linkView(this)
    }

    private fun setupLoadingUI() {
        val logoImageView = loadingLayoutView.findViewById<ImageView>(R.id.img_load_logo)
        logoImageView.setImageResource(R.drawable.icon_device)
        
        val tipsTextView = loadingLayoutView.findViewById<TextView>(R.id.tv_tips)
        tipsTextView.text = "Scanning..."
    }

    private fun beginSpinAnimation() {
        spinAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000L
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        loadingIconView.startAnimation(spinAnimation)
    }

    private fun endSpinAnimation() {
        loadingIconView.clearAnimation()
    }

    private fun cleanupAnimation() {
        spinAnimation?.cancel()
        spinAnimation = null
    }

    // ==================== DeviceContract.View Implementation ====================

    override fun displayLoadingState() {
        loadingLayoutView.visibility = View.VISIBLE
        contentLayoutView.visibility = View.GONE
        beginSpinAnimation()
    }

    override fun displayContentState() {
        endSpinAnimation()
        loadingLayoutView.visibility = View.GONE
        contentLayoutView.visibility = View.VISIBLE
        
        binding.ivBack.setOnClickListener {
            presenter.handleBackAction()
        }
    }

    override fun setPhoneModelText(model: String) {
        binding.tvPhoneModel.text = model
    }

    override fun setOSVersionText(version: String) {
        binding.tvOSVersion.text = version
    }

    override fun addDetailItem(label: String, value: String) {
        val itemLayout = LayoutInflater.from(this).inflate(
            R.layout.item_device_detail,
            binding.deviceDetailsContainer,
            false
        )
        
        itemLayout.findViewById<TextView>(R.id.tvLabel).text = label
        itemLayout.findViewById<TextView>(R.id.tvTitle).text = value
        
        binding.deviceDetailsContainer.addView(itemLayout)
    }

    override fun finishScreen() {
        finish()
    }
}

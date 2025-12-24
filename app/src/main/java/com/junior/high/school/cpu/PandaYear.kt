package com.junior.high.school.cpu

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
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
import com.junior.high.school.databinding.PandaYearBinding


class PandaYear : AppCompatActivity(), PandaContract.View {

    private val binding by lazy { PandaYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: PandaContract.Presenter
    private lateinit var loadingLayoutView: View
    private lateinit var contentLayoutView: View
    private lateinit var loadingIconView: ImageView
    private var rotationAnim: RotateAnimation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        configureWindowInsets()
        initializeViewReferences()
        initializePresenter()
        configureLoadingUI()
        
        presenter.onScreenCreated()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAnimation()
        presenter.detachView()
    }

    private fun configureWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViewReferences() {
        loadingLayoutView = findViewById(R.id.loadingLayout)
        contentLayoutView = findViewById(R.id.contentLayout)
        loadingIconView = loadingLayoutView.findViewById(R.id.ivLoadingIcon)
    }

    private fun initializePresenter() {
        val dataProvider = PandaDataProvider(this)
        presenter = PandaPresenter(dataProvider)
        presenter.attachView(this)
    }

    private fun configureLoadingUI() {
        val logoView = loadingLayoutView.findViewById<ImageView>(R.id.img_load_logo)
        logoView.setImageResource(R.drawable.icon_load_cpu)
        binding.loadingLayout.imgBack.setOnClickListener {
            presenter.onBackPressed()
        }
        val tipsView = loadingLayoutView.findViewById<TextView>(R.id.tv_tips)
        tipsView.text = "Scanning..."
    }

    private fun startRotationAnimation() {
        rotationAnim = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000L
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        loadingIconView.startAnimation(rotationAnim)
    }

    private fun stopRotationAnimation() {
        loadingIconView.clearAnimation()
    }

    private fun releaseAnimation() {
        rotationAnim?.cancel()
        rotationAnim = null
    }

    // ==================== CPUContract.View Implementation ====================

    override fun showLoadingState() {
        loadingLayoutView.visibility = View.VISIBLE
        contentLayoutView.visibility = View.GONE
        startRotationAnimation()
    }

    override fun showContentState() {
        stopRotationAnimation()
        loadingLayoutView.visibility = View.GONE
        contentLayoutView.visibility = View.VISIBLE
        
        binding.ivBack.setOnClickListener {
            presenter.onBackPressed()
        }
    }

    override fun setCPUName(name: String) {
        binding.tvCPUName.text = name
    }

    override fun applyGradientToName() {
        binding.tvCPUName.post {
            val textPaint = binding.tvCPUName.paint
            val textWidth = textPaint.measureText(binding.tvCPUName.text.toString())
            val gradientShader = LinearGradient(
                0f, 0f, textWidth, 0f,
                Color.parseColor("#226199"),
                Color.parseColor("#39A1FF"),
                Shader.TileMode.CLAMP
            )
            binding.tvCPUName.paint.shader = gradientShader
            binding.tvCPUName.invalidate()
        }
    }

    override fun appendCPUDetail(label: String, value: String) {
        val detailItemView = LayoutInflater.from(this).inflate(
            R.layout.item_cpu_detail,
            binding.cpuDetailsContainer,
            false
        )
        
        detailItemView.findViewById<TextView>(R.id.tvLabel).text = label
        detailItemView.findViewById<TextView>(R.id.tvValue).text = value
        
        binding.cpuDetailsContainer.addView(detailItemView)
    }

    override fun closeScreen() {
        finish()
    }
}

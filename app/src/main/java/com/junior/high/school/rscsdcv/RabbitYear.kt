package com.junior.high.school.rscsdcv

import android.content.Intent
import android.os.Bundle
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
import com.junior.high.school.cpu.PandaYear
import com.junior.high.school.databinding.RabbitYearBinding
import com.junior.high.school.device.DeerYear
import com.junior.high.school.mkvf.MonkeyYear


class RabbitYear : AppCompatActivity(), RabbitContract.View {

    private val binding by lazy { RabbitYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: RabbitContract.Presenter
    private lateinit var loadingAnimator: LoadingAnimatorImpl
    private lateinit var loadingLayoutView: View
    private lateinit var contentLayoutView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        initializeWindowInsets()
        initializeViews()
        initializePresenter()
        initializeClickListeners()
        
        val cleanedSizeBytes = intent.getLongExtra("junk_size", 0L)
        presenter.onViewCreated(cleanedSizeBytes)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingAnimator.release()
        presenter.unbindView()
    }

    private fun initializeWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        loadingLayoutView = findViewById(R.id.loadingLayout)
        contentLayoutView = findViewById(R.id.contentLayout)
        
        val loadingIconView = loadingLayoutView.findViewById<ImageView>(R.id.ivLoadingIcon)
        loadingIconView.setImageResource(R.drawable.icon_load)
        loadingAnimator = LoadingAnimatorImpl(loadingIconView)
    }

    private fun initializePresenter() {
        presenter = RabbitPresenter()
        presenter.bindView(this)
    }

    private fun initializeClickListeners() {
        contentLayoutView.findViewById<View>(R.id.ivBack).setOnClickListener {
            presenter.onBackClicked()
        }

        contentLayoutView.findViewById<View>(R.id.cardClean).setOnClickListener {
            presenter.onCleanCardClicked()
        }

        contentLayoutView.findViewById<View>(R.id.cardDevice).setOnClickListener {
            presenter.onDeviceCardClicked()
        }

        contentLayoutView.findViewById<View>(R.id.cardCPU).setOnClickListener {
            presenter.onCPUCardClicked()
        }
    }

    // ==================== ResultContract.View Implementation ====================

    override fun displayLoading() {
        loadingLayoutView.visibility = View.VISIBLE
        contentLayoutView.visibility = View.GONE
        loadingAnimator.startAnimation()
    }

    override fun hideLoading() {
        loadingAnimator.stopAnimation()
        loadingLayoutView.visibility = View.GONE
        contentLayoutView.visibility = View.VISIBLE
    }

    override fun displayCleanedSize(formattedSize: String) {
        contentLayoutView.findViewById<TextView>(R.id.tvCleanedSize).text = formattedSize
    }

    override fun navigateToMain() {
        val intent = Intent(this, MonkeyYear::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun navigateToDevice() {
        startActivity(Intent(this, DeerYear::class.java))
        finish()
    }

    override fun navigateToCPU() {
        startActivity(Intent(this, PandaYear::class.java))
        finish()
    }

    override fun closeScreen() {
        finish()
    }

    // ==================== Loading Animator Implementation ====================

    class LoadingAnimatorImpl(private val iconView: ImageView) : RabbitContract.LoadingAnimator {
        
        private var rotationAnimation: RotateAnimation? = null

        override fun startAnimation() {
            rotationAnimation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 2000L
                repeatCount = Animation.INFINITE
                interpolator = LinearInterpolator()
            }
            iconView.startAnimation(rotationAnimation)
        }

        override fun stopAnimation() {
            iconView.clearAnimation()
        }

        override fun release() {
            rotationAnimation?.cancel()
            rotationAnimation = null
        }
    }
}
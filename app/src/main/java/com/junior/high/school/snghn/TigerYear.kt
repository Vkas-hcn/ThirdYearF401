package com.junior.high.school.snghn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.junior.high.school.R
import com.junior.high.school.databinding.TigerYearBinding
import com.junior.high.school.rscsdcv.RabbitYear
import com.junior.high.school.snghn.adapter.JunkCategoryAdapter
import com.junior.high.school.snghn.model.JunkCategory


class TigerYear : AppCompatActivity(), TigerContract.View {

    private val binding by lazy { TigerYearBinding.inflate(layoutInflater) }
    
    private val categoryList: MutableList<JunkCategory> = ArrayList()
    private lateinit var categoryAdapter: JunkCategoryAdapter
    private lateinit var presenter: TigerContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        initializeWindowInsets()
        initializePresenter()
        initializeRecyclerView()
        initializeClickListeners()
        
        presenter.initializeCategories()
        presenter.startScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initializeWindowInsets() {
        // Set status bar icons to light color
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = false
        }

        // Implement immersive status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topBar = findViewById<View>(R.id.topBar)
            topBar?.setPadding(
                topBar.paddingLeft,
                systemBars.top,
                topBar.paddingRight,
                topBar.paddingBottom
            )
            insets
        }
    }

    private fun initializePresenter() {
        val model = TigerModel(this)
        presenter = TigerPresenter(model)
        presenter.attachView(this)
    }

    private fun initializeRecyclerView() {
        categoryAdapter = JunkCategoryAdapter(categoryList)
        
        categoryAdapter.setCategoryClickListener(object : JunkCategoryAdapter.OnCategoryClickListener {
            override fun onCategoryClick(category: JunkCategory, position: Int) {
                presenter.onCategoryClicked(category)
            }
        })

        categoryAdapter.setCategorySelectListener(object : JunkCategoryAdapter.OnCategorySelectListener {
            override fun onCategorySelectClick(category: JunkCategory, position: Int) {
                presenter.onCategorySelectClicked(category)
            }
        })

        categoryAdapter.setFileClickListener(object : JunkCategoryAdapter.OnFileClickListener {
            override fun onFileClick(category: JunkCategory, fileIndex: Int) {
                presenter.onFileClicked(category, fileIndex)
            }
        })

        binding.rvJunkCategories.layoutManager = LinearLayoutManager(this)
        binding.rvJunkCategories.adapter = categoryAdapter
    }

    private fun initializeClickListeners() {
        binding.topBar.setOnClickListener {
            handleBackPressed()
        }

        binding.btnCleanNow.setOnClickListener {
            handleCleanButtonClick()
        }
    }

    private fun handleBackPressed() {
        presenter.stopScanning()
        finish()
    }

    private fun handleCleanButtonClick() {
        if (!presenter.isScanning()) {
            presenter.performClean()
        }
    }


    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun updateScanningPath(path: String) {
        binding.tvScanningPath.text = path
    }

    override fun updateTotalSize(size: String, unit: String) {
        binding.tvJunkSize.text = size
        binding.tvJunkUnit.text = unit
    }

    override fun updateBackground(hasJunk: Boolean) {
        if (hasJunk) {
            binding.ivBackground.setImageResource(R.drawable.bg_have_junk)
        }
    }

    override fun updateCleanButton(enabled: Boolean) {
        binding.btnCleanNow.isEnabled = enabled
        binding.imgBtnShadow.isVisible = enabled
        
        val backgroundRes = if (enabled) {
            R.drawable.bg_clean_button
        } else {
            R.drawable.bg_clean_button_disabled
        }
        binding.btnCleanNow.setBackgroundResource(backgroundRes)
    }

    override fun refreshCategoryList() {
        categoryAdapter.notifyDataSetChanged()
    }

    override fun navigateToResult(cleanedSize: Long) {
        val intent = Intent(this, RabbitYear::class.java)
        intent.putExtra("junk_size", cleanedSize)
        startActivity(intent)
        finish()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun getCategories(): MutableList<JunkCategory> = categoryList
}
package com.junior.high.school.gwec

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.junior.high.school.R
import com.junior.high.school.databinding.FoxYearBinding
import com.junior.high.school.mkvf.MonkeyYear


class FoxYear : AppCompatActivity(), FoxContract.View {
    
    private val binding by lazy { FoxYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: FoxPresenter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.first)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this) {
        }
        presenter = FoxPresenter(this)
        
        binding.ppD.progress = 0
        
        presenter.startCountDown()
    }

    override fun updateProgress(progress: Int) {
        binding.ppD.progress = progress
    }

    override fun navigateToMain() {
        val intent = Intent(this, MonkeyYear::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopCountDown()
    }
}
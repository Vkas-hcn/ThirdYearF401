package com.junior.high.school.gwec

import android.os.CountDownTimer


class FoxPresenter(private val view: FoxContract.View) : FoxContract.Presenter {
    
    private val totalTime = 1600L
    private val interval = 16L
    private var countDownTimer: CountDownTimer? = null

    override fun startCountDown() {
        stopCountDown()
        
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((totalTime - millisUntilFinished) * 100 / totalTime).toInt()
                view.updateProgress(progress)
            }
            
            override fun onFinish() {
                view.updateProgress(100)
                view.navigateToMain()
            }
        }
        
        countDownTimer?.start()
    }

    override fun stopCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
}

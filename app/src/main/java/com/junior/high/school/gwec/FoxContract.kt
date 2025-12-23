package com.junior.high.school.gwec


interface FoxContract {
    
    interface View {
        fun updateProgress(progress: Int)
        fun navigateToMain()
    }
    
    interface Presenter {
        fun startCountDown()
        fun stopCountDown()
    }
}

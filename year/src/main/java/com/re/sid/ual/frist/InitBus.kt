package com.re.sid.ual.frist

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 初始化事件总线
 */
object InitBus {
    
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    
    private val _phase = MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = 16)
    val phase = _phase.asSharedFlow()
    
    private var mCtx: Application? = null
    
    const val P_STORAGE = 1
    const val P_IDENTITY = 2
    const val P_OBSERVER = 3
    const val P_SDK = 4
    const val P_FEATURE = 5
    const val P_SERVICE = 6
    const val P_ALLY = 7
    const val P_FIREBASE = 8
    const val P_REF = 9
    const val P_WORK = 10
    const val P_SESSION = 11
    
    fun ctx(): Application? = mCtx
    
    fun attach(app: Application) {
        mCtx = app
    }
    
    fun emit(p: Int) {
        scope.launch {
            _phase.emit(p)
        }
    }
    
    fun collect(handler: (Int) -> Unit) {
        scope.launch {
            phase.collect { handler(it) }
        }
    }
}

package com.re.sid.ual.frist

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


object CoreBus {
    
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    
    // 事件类型
    sealed class Event {
        data class PostAd(val data: String) : Event()
        data class PointEvent(
            val canRetry: Boolean,
            val name: String,
            val key1: String,
            val value: String
        ) : Event()
    }
    
    private val _events = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 32)
    val events = _events.asSharedFlow()
    

    private var mActivityProvider: (() -> List<Activity>)? = null
    
    fun emit(event: Event) {
        scope.launch {
            _events.emit(event)
        }
    }
    
    fun collect(handler: (Event) -> Unit) {
        scope.launch {
            events.collect { handler(it) }
        }
    }
    
    // 注册 Activity 列表提供者
    fun registerActivityProvider(provider: () -> List<Activity>) {
        mActivityProvider = provider
    }
    
    // 同步获取 Activity 列表
    fun getActivities(): List<Activity> {
        return mActivityProvider?.invoke() ?: emptyList()
    }
}

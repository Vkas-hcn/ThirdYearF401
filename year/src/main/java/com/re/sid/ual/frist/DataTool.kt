package com.re.sid.ual.frist

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


object DataTool {

    var app_id by string("mkceniv", "")
    var user_can by string("hiydsc", "")
    var ref_can by string("ewxvfe", "")

    var have_ins by boolean("vfkrrooa", false)

    fun showLog(msg: String){
        Log.e("Year", msg)
    }
    private const val DATA_STORE_NAME = "app_data_store"
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)
    
    private lateinit var appContext: Context
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 内存缓存，避免频繁读取DataStore
    private val cache = mutableMapOf<String, Any?>()
    
    /**
     * 初始化DataTool，需要在Application中调用
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }
    
    /**
     * 获取Flow数据流
     */
    fun <T> getFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return appContext.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }
    
    /**
     * 同步获取值（带缓存）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): T {
        val cacheKey = key.name
        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey] as? T ?: defaultValue
        }
        return try {
            runBlocking {
                appContext.dataStore.data.first()[key] ?: defaultValue
            }.also {
                cache[cacheKey] = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
    
    /**
     * 异步保存值
     */
    fun <T> setValue(key: Preferences.Key<T>, value: T) {
        cache[key.name] = value
        scope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[key] = value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 同步保存值
     */
    suspend fun <T> setValueSync(key: Preferences.Key<T>, value: T) {
        cache[key.name] = value
        try {
            appContext.dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clearAll() {
        cache.clear()
        scope.launch {
            try {
                appContext.dataStore.edit { it.clear() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 移除指定key
     */
    fun <T> remove(key: Preferences.Key<T>) {
        cache.remove(key.name)
        scope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences.remove(key)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // ==================== 属性委托类 ====================
    
    /**
     * String类型属性委托
     */
    class StringPreference(
        private val key: String,
        private val defaultValue: String = ""
    ) : ReadWriteProperty<Any?, String> {
        
        private val prefKey = stringPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * Int类型属性委托
     */
    class IntPreference(
        private val key: String,
        private val defaultValue: Int = 0
    ) : ReadWriteProperty<Any?, Int> {
        
        private val prefKey = intPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * Long类型属性委托
     */
    class LongPreference(
        private val key: String,
        private val defaultValue: Long = 0L
    ) : ReadWriteProperty<Any?, Long> {
        
        private val prefKey = longPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * Float类型属性委托
     */
    class FloatPreference(
        private val key: String,
        private val defaultValue: Float = 0f
    ) : ReadWriteProperty<Any?, Float> {
        
        private val prefKey = floatPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * Double类型属性委托
     */
    class DoublePreference(
        private val key: String,
        private val defaultValue: Double = 0.0
    ) : ReadWriteProperty<Any?, Double> {
        
        private val prefKey = doublePreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * Boolean类型属性委托
     */
    class BooleanPreference(
        private val key: String,
        private val defaultValue: Boolean = false
    ) : ReadWriteProperty<Any?, Boolean> {
        
        private val prefKey = booleanPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    /**
     * StringSet类型属性委托
     */
    class StringSetPreference(
        private val key: String,
        private val defaultValue: Set<String> = emptySet()
    ) : ReadWriteProperty<Any?, Set<String>> {
        
        private val prefKey = stringSetPreferencesKey(key)
        
        override fun getValue(thisRef: Any?, property: KProperty<*>): Set<String> {
            return DataTool.getValue(prefKey, defaultValue)
        }
        
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>) {
            DataTool.setValue(prefKey, value)
        }
    }
    
    // ==================== 便捷扩展函数 ====================
    
    /**
     * 创建String类型委托
     */
    fun string(key: String, defaultValue: String = "") = StringPreference(key, defaultValue)
    
    /**
     * 创建Int类型委托
     */
    fun int(key: String, defaultValue: Int = 0) = IntPreference(key, defaultValue)
    
    /**
     * 创建Long类型委托
     */
    fun long(key: String, defaultValue: Long = 0L) = LongPreference(key, defaultValue)
    
    /**
     * 创建Float类型委托
     */
    fun float(key: String, defaultValue: Float = 0f) = FloatPreference(key, defaultValue)
    
    /**
     * 创建Double类型委托
     */
    fun double(key: String, defaultValue: Double = 0.0) = DoublePreference(key, defaultValue)
    
    /**
     * 创建Boolean类型委托
     */
    fun boolean(key: String, defaultValue: Boolean = false) = BooleanPreference(key, defaultValue)
    
    /**
     * 创建StringSet类型委托
     */
    fun stringSet(key: String, defaultValue: Set<String> = emptySet()) = StringSetPreference(key, defaultValue)
}
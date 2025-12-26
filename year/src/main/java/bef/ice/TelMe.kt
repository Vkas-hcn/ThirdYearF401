package bef.ice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.re.sid.ual.R
import com.re.sid.ual.ben.GetZenbox
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TelMe : Service() {

    companion object {
        private const val TAG = "TelMe"
        private const val CHANNEL_ID = "Notification"
        private const val NOTIFICATION_ID = 5582
    }

    private var mNotification: Notification? = null

    // 服务运行状态 Flow
    private val _serviceState = MutableStateFlow(false)
    val serviceState = _serviceState.asStateFlow()

    // 协程作用域，使用 SupervisorJob 确保子协程异常不会影响其他协程
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception: ${throwable.message}")
    }
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + exceptionHandler)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initNotificationFlow()
            .catch { e ->
                Log.e(TAG, "Init notification error: ${e.message}")
            }
            .onEach { notification ->
                mNotification = notification
                updateServiceState(true)
                Log.e(TAG, "service onCreate: ")
            }
            .launchIn(serviceScope)
    }

    /**
     * 初始化通知 Flow
     */
    private fun initNotificationFlow() = flow {
        // 创建通知渠道
        createNotificationChannel()
        // 构建通知
        val notification = buildNotification()
        emit(notification)
    }.flowOn(Dispatchers.Main)

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        runCatching {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                vibrationPattern = longArrayOf(0L)
            }
            getNotificationManager()?.createNotificationChannel(channel)
        }.onFailure { e ->
            Log.e(TAG, "Create channel error: ${e.message}")
        }
    }

    /**
     * 构建通知
     */
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setContentText("")
            .setSmallIcon(R.drawable.nkd_jjef)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("")
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(RemoteViews(packageName, R.layout.mkle_vr))
            .build()
    }

    /**
     * 更新服务状态
     */
    private fun updateServiceState(isRunning: Boolean) {
        _serviceState.value = isRunning
        GetZenbox.soCanFell = isRunning
    }

    /**
     * 获取 NotificationManager
     */
    private fun getNotificationManager(): NotificationManager? {
        return runCatching {
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }.getOrNull()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundFlow()
            .catch { e ->
                Log.e(TAG, "Start foreground error: ${e.message}")
            }
            .launchIn(serviceScope)
        return START_STICKY
    }

    /**
     * 启动前台服务 Flow
     */
    private fun startForegroundFlow() = flow {
        mNotification?.let { notification ->
            startForeground(NOTIFICATION_ID, notification)
            emit(true)
        } ?: emit(false)
    }.flowOn(Dispatchers.Main)

    override fun onDestroy() {
        // 更新状态
        updateServiceState(false)
        // 取消所有协程
        serviceScope.cancel()
        super.onDestroy()
    }
}
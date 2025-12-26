package com.re.sid.ual.up

import android.os.Handler
import android.os.Looper
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.up.fen.PingTool
import com.re.sid.ual.up.now.NetTool
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

object DaoMe {
    
    private const val MANDATORY_MAX_RETRY = 20
    private val OPTIONAL_RETRY_RANGE = 2..5
    private val RETRY_DELAY_RANGE = 10_000L..40_000L
    
    private val handler = Handler(Looper.getMainLooper())
    
    /** 安装上报进行中标记，防止并发重复调用 */
    private val isInstallUploading = AtomicBoolean(false)
    
    /**
     * 获取随机重试延迟时间（10-40秒）
     */
    private fun getRetryDelay(): Long = Random.nextLong(RETRY_DELAY_RANGE.first, RETRY_DELAY_RANGE.last + 1)
    
    /**
     * 上报安装事件（必传事件）
     * - 成功后记录状态，后续不再上传
     * - 使用首次生成的数据进行重试，保证数据准确性
     * - 最多重试20次，间隔10-40秒
     */
    fun upInstall() {
        // 已上传过则不再上传
        if (DataTool.have_ins) {
            return
        }
        
        // 防止并发：如果已在上传中，直接返回
        if (!isInstallUploading.compareAndSet(false, true)) {
            return
        }
        
        // 获取或生成安装数据（首次生成后缓存，后续使用缓存数据）
        val installJson = DataTool.install_json.ifEmpty {
            PingTool.upInstallJson().also { DataTool.install_json = it }
        }
        
        upInstallWithRetry(installJson, 0)
    }
    
    private fun upInstallWithRetry(installJson: String, retryCount: Int) {
        DataTool.showLog("[Install] 请求开始 retry=$retryCount, params=$installJson")
        NetTool.postPutData(installJson, object : NetTool.CallbackMy {
            override fun onSuccess(response: String) {
                DataTool.showLog("[Install] 请求成功 response=$response")
                // 上传成功：先标记成功状态，再清除缓存
                // 注意：have_ins 必须先于 install_json 清空，避免崩溃后数据丢失
                DataTool.have_ins = true
                // 延迟清除缓存，确保 have_ins 已持久化
                handler.postDelayed({
                    DataTool.install_json = ""
                }, 1000)
                isInstallUploading.set(false)
            }

            override fun onFailure(error: String) {
                DataTool.showLog("[Install] 请求失败 retry=$retryCount, error=$error")
                if (retryCount < MANDATORY_MAX_RETRY) {
                    handler.postDelayed({
                        upInstallWithRetry(installJson, retryCount + 1)
                    }, getRetryDelay())
                } else {
                    DataTool.showLog("[Install] 重试耗尽，放弃上报")
                    // 重试耗尽，重置标记允许下次调用
                    isInstallUploading.set(false)
                }
            }
        })
    }

    /**
     * 上报广告事件（必传事件）
     * - 最多重试20次，间隔10-40秒
     */
    fun upAd(json: String) {
        val adJson = PingTool.upAdJson(json)
        upAdWithRetry(adJson, 0)
    }
    
    private fun upAdWithRetry(adJson: String, retryCount: Int) {
        DataTool.showLog("[Ad] 请求开始 retry=$retryCount, params=$adJson")
        NetTool.postPutData(adJson, object : NetTool.CallbackMy {
            override fun onSuccess(response: String) {
                DataTool.showLog("[Ad] 请求成功 response=$response")
            }

            override fun onFailure(error: String) {
                DataTool.showLog("[Ad] 请求失败 retry=$retryCount, error=$error")
                if (retryCount < MANDATORY_MAX_RETRY) {
                    handler.postDelayed({
                        upAdWithRetry(adJson, retryCount + 1)
                    }, getRetryDelay())
                } else {
                    DataTool.showLog("[Ad] 重试耗尽，放弃上报")
                }
            }
        })
    }

    /**
     * 上报埋点事件
     * - canRetry=true: 必传事件，最多重试20次
     * - canRetry=false: 非必传事件，最多重试2-5次
     * - 间隔10-40秒
     */
    fun upPoint(
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {
        if (!canRetry && DataTool.user_can.isNotBlank() && !(IconBean.userUp())) {
            DataTool.showLog("[Point] 跳过非必传事件: $name")
            return
        }
        
        val pointJson = PingTool.upPointJson(name, key1, keyValue1)
        val maxRetry = if (canRetry) {
            MANDATORY_MAX_RETRY
        } else {
            Random.nextInt(OPTIONAL_RETRY_RANGE.first, OPTIONAL_RETRY_RANGE.last + 1)
        }
        
        upPointWithRetry(name, pointJson, 0, maxRetry)
    }
    
    private fun upPointWithRetry(eventName: String, pointJson: String, retryCount: Int, maxRetry: Int) {
        DataTool.showLog("[Point:$eventName] 请求开始 retry=$retryCount, maxRetry=$maxRetry, params=$pointJson")
        NetTool.postPutData(pointJson, object : NetTool.CallbackMy {
            override fun onSuccess(response: String) {
                DataTool.showLog("[Point:$eventName] 请求成功 response=$response")
            }

            override fun onFailure(error: String) {
                DataTool.showLog("[Point:$eventName] 请求失败 retry=$retryCount, error=$error")
                if (retryCount < maxRetry) {
                    handler.postDelayed({
                        upPointWithRetry(eventName, pointJson, retryCount + 1, maxRetry)
                    }, getRetryDelay())
                } else {
                    DataTool.showLog("[Point:$eventName] 重试耗尽，放弃上报")
                }
            }
        })
    }

    fun ConfigG(typeUser: Int, codeInt: String?) {
        val isuserData: String? = if (codeInt == null) {
            null
        } else if (codeInt != "200") {
            codeInt
        } else if (typeUser==1) {
            "a"
        } else if (typeUser==2){
            "b"
        } else {
            ""
        }
        upPoint(
            true,
            "config_G",
            "getstring",
            isuserData
        )
    }
}
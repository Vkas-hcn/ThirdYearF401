package ad

import android.app.Activity
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.helico.bacter.pylori.Core
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import late.mod.a

/**
 * Date：2025/7/16
 * Describe:
 */

// 单聚合
object AdCenter {
    private var mIdList: List<String> = listOf()
    private var listAd: ArrayList<PangleAdImpl> = arrayListOf()

    @JvmStatic
    fun setAdId(idList: String) {
        val list = if (idList.contains("-")) {
            idList.split("-")
        } else {
            listOf(idList)
        }
        if (mIdList.isEmpty() || list.size == mIdList.size) {
            mIdList = list
        }
        val siz1 = mIdList.size
        if (listAd.isEmpty()) {
            for (i in 0 until siz1) {
                listAd.add(i, PangleAdImpl("$i"))
            }
        }
    }

    @JvmStatic
    fun loadAd() {
        val size = listAd.size
        for (i in 0 until size) {
            listAd[i].lAd(mIdList[i])
        }
    }

    private var job: Job? = null

    @JvmStatic
    fun showAd(ac: Activity) {
        AdE.sNumJump(0)
        if (ac is AppCompatActivity) {
            ac.onBackPressedDispatcher.addCallback {}
            job?.cancel()
            job = ac.lifecycleScope.launch {
                Core.pE("ad_done")
                delay(AdE.gDTime())
                val isS = show(ac)
                if (isS.not()) {
                    delay(1000)
                    ac.finish()
                }
            }
        }
    }

    private fun show(ac: Activity): Boolean {
        var time = System.currentTimeMillis()
        var index = -1
        for (i in 0 until listAd.size) {
            val ad = listAd[i]
            if (ad.isReadyAd() && time > ad.loadTime) {
                index = i
                time = ad.loadTime
            }
        }
        if (index == -1) {
            return false
        }
        return listAd[index].shAd(ac)
    }

    @JvmStatic
    fun isAdReady(): Boolean {
        for (i in 0 until listAd.size) {
            val ad = listAd[i]
            if (ad.isReadyAd()) {
                return true
            }
        }
        return false
    }
}
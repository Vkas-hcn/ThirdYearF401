package com.re.sid.ual.up.fen

import android.content.pm.PackageManager
import android.os.Build
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.song.IntFeel
import org.json.JSONObject
import java.util.UUID

object PingTool {
    
    private fun buildJson(vararg pairs: Pair<String, Any?>): JSONObject {
        val obj = JSONObject()
        pairs.forEach { (k, v) ->
            if (v != null) obj.put(k, v)
        }
        return obj
    }
    
    private fun JSONObject.merge(other: JSONObject): JSONObject {
        other.keys().forEach { k -> this.put(k, other.get(k)) }
        return this
    }
    
    private fun JSONObject.putIf(condition: Boolean, key: String, valueProvider: () -> Any?): JSONObject {
        if (condition) {
            val v = valueProvider()
            if (v != null) this.put(key, v)
        }
        return this
    }
    
    private fun topJsonData(): JSONObject {
        val elyseeData = arrayOf(
            "tub" to DataTool.appAll.packageName,
            "armour" to "imperate",
            "calculus" to IntFeel.showAppVersion(DataTool.appAll),
            "causate" to UUID.randomUUID().toString(),
            "flamingo" to Build.BRAND,
            "each" to "csdc_asceww",
            "monel" to DataTool.app_id
        )
        
        val deliriumData = arrayOf(
            "pander" to DataTool.app_id,
            "bead" to System.currentTimeMillis(),
            "slid" to Build.MANUFACTURER,
            "jugate" to Build.VERSION.RELEASE,
            "snapback" to "ddx",
            "zag" to ""
        )
        
        val elysee = buildJson(*elyseeData)
        val delirium = buildJson(*deliriumData)
        
        return buildJson(
            "elysee" to elysee,
            "delirium" to delirium
        ).putIf(IconBean.upCanAuto(), "usercode\$scramble") { IconBean.upCanAutoValue() }
    }

    fun upInstallJson(): String {
        val chuteFields = linkedMapOf<String, Any?>(
            "solace" to StringBuilder("build/").append(Build.ID).toString(),
            "quinn" to DataTool.ref_can,
            "prurient" to "",
            "crewmen" to "cress",
            "intra" to 0,
            "josiah" to 0,
            "monadic" to 0,
            "dredge" to 0,
            "conner" to queryFirstInstall(),
            "tenable" to 0
        )
        
        val chute = JSONObject()
        chuteFields.entries.forEach { (k, v) ->
            chute.put(k, v)
        }
        
        val result = topJsonData()
        result.put("chute", chute)
        return result.toString()
    }

    fun upAdJson(adJson: String): String {
        val base = topJsonData()
        base.put("chariot", "dart")
        
        runCatching {
            val adObj = JSONObject(adJson)
            base.merge(adObj)
        }
        
        return base.toString()
    }

    fun upPointJson(
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
    ): String {
        val base = topJsonData()
        base.put("chariot", name)
        
        val inner = JSONObject()
        key1?.let { k ->
            inner.put(k, keyValue1)
        }
        base.put(name, inner)
        
        return base.toString()
    }

    private fun queryFirstInstall(): Long {
        return runCatching {
            val pm = DataTool.appAll.packageManager
            val pkg = DataTool.appAll.packageName
            val info = pm.getPackageInfo(pkg, 0)
            info.firstInstallTime.div(1000)
        }.getOrDefault(0L)
    }
}
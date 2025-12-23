package com.junior.high.school.snghn.model

/**
 * 垃圾文件分类类型 - 使用sealed class替代enum实现字节码差异化
 */
sealed class JunkType(val typeId: Int, val typeName: String) {
    object AppCache : JunkType(0, "APP_CACHE")
    object ApkFiles : JunkType(1, "APK_FILES")
    object LogFiles : JunkType(2, "LOG_FILES")
    object AdJunk : JunkType(3, "AD_JUNK")
    object TempFiles : JunkType(4, "TEMP_FILES")
    object AppResidual : JunkType(5, "APP_RESIDUAL")

    companion object {
        fun fromId(id: Int): JunkType = when (id) {
            0 -> AppCache
            1 -> ApkFiles
            2 -> LogFiles
            3 -> AdJunk
            4 -> TempFiles
            5 -> AppResidual
            else -> AppCache
        }
        
        fun allTypes(): List<JunkType> = listOf(
            AppCache, ApkFiles, LogFiles, AdJunk, TempFiles, AppResidual
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JunkType) return false
        return typeId == other.typeId
    }

    override fun hashCode(): Int = typeId
}

/**
 * 垃圾分类数据模型 - 使用普通类替代data class
 */
class JunkCategory(
    private val mType: JunkType,
    private val mName: String,
    private val mIconRes: Int
) {
    private val mFiles: MutableList<JunkFile> = ArrayList()
    private var mIsExpanded: Boolean = false

    fun getType(): JunkType = mType
    fun getName(): String = mName
    fun getIconRes(): Int = mIconRes
    fun getFiles(): MutableList<JunkFile> = mFiles
    
    fun isExpanded(): Boolean = mIsExpanded
    fun setExpanded(expanded: Boolean) {
        mIsExpanded = expanded
    }

    fun calculateTotalSize(): Long {
        var total = 0L
        for (file in mFiles) {
            total += file.getSize()
        }
        return total
    }

    fun checkAllSelected(): Boolean {
        if (mFiles.isEmpty()) return false
        for (file in mFiles) {
            if (!file.isSelected()) return false
        }
        return true
    }

    fun countSelected(): Int {
        var count = 0
        for (file in mFiles) {
            if (file.isSelected()) count++
        }
        return count
    }

    fun calculateSelectedSize(): Long {
        var size = 0L
        for (file in mFiles) {
            if (file.isSelected()) {
                size += file.getSize()
            }
        }
        return size
    }

    fun addFile(file: JunkFile) {
        mFiles.add(file)
    }

    fun removeFile(file: JunkFile): Boolean {
        return mFiles.remove(file)
    }

    fun clearFiles() {
        mFiles.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JunkCategory) return false
        return mType == other.mType
    }

    override fun hashCode(): Int = mType.hashCode()
}

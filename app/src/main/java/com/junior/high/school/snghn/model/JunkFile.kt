package com.junior.high.school.snghn.model

import java.io.File


class JunkFile private constructor(builder: Builder) {
    
    private val mFile: File
    private val mName: String
    private val mSize: Long
    private val mPath: String
    private var mIsSelected: Boolean

    init {
        mFile = builder.file
        mName = builder.name
        mSize = builder.size
        mPath = builder.path
        mIsSelected = builder.isSelected
    }

    fun getFile(): File = mFile
    fun getName(): String = mName
    fun getSize(): Long = mSize
    fun getPath(): String = mPath
    fun isSelected(): Boolean = mIsSelected

    fun setSelected(selected: Boolean) {
        mIsSelected = selected
    }

    fun toggleSelection() {
        mIsSelected = !mIsSelected
    }

    fun deleteFile(): Boolean {
        return try {
            mFile.delete()
        } catch (e: Exception) {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JunkFile) return false
        return mPath == other.mPath
    }

    override fun hashCode(): Int = mPath.hashCode()

    override fun toString(): String {
        return "JunkFile{name=$mName, size=$mSize, path=$mPath, selected=$mIsSelected}"
    }


    class Builder(internal val file: File) {
        internal var name: String = file.name
        internal var size: Long = file.length()
        internal var path: String = file.absolutePath
        internal var isSelected: Boolean = true

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun setSize(size: Long): Builder {
            this.size = size
            return this
        }

        fun setPath(path: String): Builder {
            this.path = path
            return this
        }

        fun setSelected(selected: Boolean): Builder {
            this.isSelected = selected
            return this
        }

        fun build(): JunkFile = JunkFile(this)
    }

    companion object {
        @JvmStatic
        fun create(file: File): JunkFile {
            return Builder(file).build()
        }

        @JvmStatic
        fun createWithSelection(file: File, selected: Boolean): JunkFile {
            return Builder(file).setSelected(selected).build()
        }
    }
}

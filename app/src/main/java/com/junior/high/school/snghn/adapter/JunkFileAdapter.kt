package com.junior.high.school.snghn.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.junior.high.school.R
import com.junior.high.school.databinding.ItemJunkFileBinding
import com.junior.high.school.snghn.model.JunkFile

/**
 * Adapter for displaying individual junk files
 * Refactored to use interface callback instead of lambda
 */
class JunkFileAdapter : RecyclerView.Adapter<JunkFileAdapter.FileViewHolder> {

    private var fileList: List<JunkFile>
    private var fileClickListener: OnFileItemClickListener? = null

    /**
     * Callback interface for file item click events
     */
    interface OnFileItemClickListener {
        fun onFileItemClick(file: JunkFile, position: Int)
    }

    constructor(files: List<JunkFile>) : super() {
        this.fileList = files
    }

    fun setFileClickListener(listener: OnFileItemClickListener) {
        this.fileClickListener = listener
    }

    inner class FileViewHolder : RecyclerView.ViewHolder {
        private val binding: ItemJunkFileBinding

        constructor(itemBinding: ItemJunkFileBinding) : super(itemBinding.root) {
            binding = itemBinding
        }

        fun bindData(file: JunkFile, position: Int) {
            displayFileName(file)
            displayFileSize(file)
            displaySelectionState(file)
            setupItemClickListener(file, position)
        }

        private fun displayFileName(file: JunkFile) {
            binding.tvFileName.text = file.getName()
        }

        private fun displayFileSize(file: JunkFile) {
            binding.tvFileSize.text = JunkCategoryAdapter.FileSizeFormatter.format(file.getSize())
        }

        private fun displaySelectionState(file: JunkFile) {
            val iconRes = if (file.isSelected()) {
                R.drawable.icon_check
            } else {
                R.drawable.icon_disheck
            }
            binding.ivFileSelect.setImageResource(iconRes)
        }

        private fun setupItemClickListener(file: JunkFile, position: Int) {
            binding.root.setOnClickListener {
                fileClickListener?.onFileItemClick(file, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJunkFileBinding.inflate(inflater, parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.bindData(file, position)
    }

    override fun getItemCount(): Int = fileList.size

    fun updateFiles(newFiles: List<JunkFile>) {
        fileList = newFiles
        notifyDataSetChanged()
    }
}

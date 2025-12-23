package com.junior.high.school.snghn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.junior.high.school.snghn.model.JunkCategory
import com.junior.high.school.snghn.model.JunkFile
import com.junior.high.school.R
import com.junior.high.school.databinding.ItemJunkCategoryBinding
import java.text.DecimalFormat

/**
 * Adapter for displaying junk categories using callback interfaces
 * Refactored for bytecode differentiation
 */
class JunkCategoryAdapter : RecyclerView.Adapter<JunkCategoryAdapter.CategoryViewHolder> {

    private var categoryList: MutableList<JunkCategory>
    private var categoryClickListener: OnCategoryClickListener? = null
    private var categorySelectListener: OnCategorySelectListener? = null
    private var fileClickListener: OnFileClickListener? = null

    /**
     * Callback interface for category click events
     */
    interface OnCategoryClickListener {
        fun onCategoryClick(category: JunkCategory, position: Int)
    }

    /**
     * Callback interface for category select events
     */
    interface OnCategorySelectListener {
        fun onCategorySelectClick(category: JunkCategory, position: Int)
    }

    /**
     * Callback interface for file click events
     */
    interface OnFileClickListener {
        fun onFileClick(category: JunkCategory, fileIndex: Int)
    }

    constructor(categories: MutableList<JunkCategory>) : super() {
        this.categoryList = categories
    }

    fun setCategoryClickListener(listener: OnCategoryClickListener) {
        this.categoryClickListener = listener
    }

    fun setCategorySelectListener(listener: OnCategorySelectListener) {
        this.categorySelectListener = listener
    }

    fun setFileClickListener(listener: OnFileClickListener) {
        this.fileClickListener = listener
    }

    inner class CategoryViewHolder : RecyclerView.ViewHolder {
        val binding: ItemJunkCategoryBinding

        constructor(itemBinding: ItemJunkCategoryBinding) : super(itemBinding.root) {
            binding = itemBinding
        }

        fun bindData(category: JunkCategory, position: Int) {
            setupCategoryIcon(category)
            setupCategoryText(category)
            setupExpandIcon(category)
            setupSelectIcon(category)
            setupFileList(category)
            setupClickListeners(category, position)
        }

        private fun setupCategoryIcon(category: JunkCategory) {
            binding.ivCategoryIcon.setImageResource(category.getIconRes())
        }

        private fun setupCategoryText(category: JunkCategory) {
            binding.tvCategoryName.text = category.getName()
            binding.tvCategorySize.text = FileSizeFormatter.format(category.calculateTotalSize())
        }

        private fun setupExpandIcon(category: JunkCategory) {
            val iconRes = if (category.isExpanded()) {
                R.drawable.icon_expand_list
            } else {
                R.drawable.icon_collapse_list
            }
            binding.ivExpand.setImageResource(iconRes)
        }

        private fun setupSelectIcon(category: JunkCategory) {
            val iconRes = if (category.checkAllSelected()) {
                R.drawable.icon_check
            } else {
                R.drawable.icon_disheck
            }
            binding.ivCategorySelect.setImageResource(iconRes)
        }

        private fun setupFileList(category: JunkCategory) {
            val visibility = if (category.isExpanded()) View.VISIBLE else View.GONE
            binding.rvJunkFiles.visibility = visibility

            if (category.isExpanded() && category.getFiles().isNotEmpty()) {
                binding.rvJunkFiles.layoutManager = LinearLayoutManager(binding.root.context)
                
                val fileAdapter = JunkFileAdapter(category.getFiles())
                fileAdapter.setFileClickListener(object : JunkFileAdapter.OnFileItemClickListener {
                    override fun onFileItemClick(file: JunkFile, filePosition: Int) {
                        fileClickListener?.onFileClick(category, filePosition)
                    }
                })
                binding.rvJunkFiles.adapter = fileAdapter
            }
        }

        private fun setupClickListeners(category: JunkCategory, position: Int) {
            binding.categoryHeader.setOnClickListener {
                categoryClickListener?.onCategoryClick(category, position)
            }

            binding.ivCategorySelect.setOnClickListener {
                categorySelectListener?.onCategorySelectClick(category, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJunkCategoryBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.bindData(category, position)
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateCategories(newCategories: List<JunkCategory>) {
        categoryList.clear()
        categoryList.addAll(newCategories)
        notifyDataSetChanged()
    }

    /**
     * Utility object for formatting file sizes
     */
    object FileSizeFormatter {
        private const val BYTES_PER_KB = 1000L
        private const val BYTES_PER_MB = 1000L * 1000L
        private const val BYTES_PER_GB = 1000L * 1000L * 1000L

        fun format(bytes: Long): String {
            val formatter = DecimalFormat("#.#")
            
            return when {
                bytes >= BYTES_PER_GB -> {
                    val gb = bytes.toDouble() / BYTES_PER_GB
                    "${formatter.format(gb)}GB"
                }
                bytes >= BYTES_PER_MB -> {
                    val mb = bytes.toDouble() / BYTES_PER_MB
                    "${formatter.format(mb)}MB"
                }
                bytes >= BYTES_PER_KB -> {
                    val kb = bytes.toDouble() / BYTES_PER_KB
                    "${formatter.format(kb)}KB"
                }
                else -> "${bytes}B"
            }
        }
    }
}

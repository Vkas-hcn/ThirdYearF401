package com.junior.high.school.mkvf

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.junior.high.school.R
import com.junior.high.school.cpu.PandaYear
import com.junior.high.school.databinding.MonkeyYearBinding
import com.junior.high.school.device.DeerYear
import com.junior.high.school.settings.BearYear
import com.junior.high.school.snghn.TigerYear


class MonkeyYear : AppCompatActivity(), MonkeyContract.View {

    private val binding by lazy { MonkeyYearBinding.inflate(layoutInflater) }
    private lateinit var presenter: MonkeyPresenter

    // 权限请求启动器（Android 11+）
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 从设置返回后检查权限
        presenter.checkPermissionAfterSettings(this)
    }

    // 权限请求启动器（Android 10及以下）
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        presenter.onPermissionResult(this, granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        presenter = MonkeyPresenter(this, this)

        binding.ivMenu.setOnClickListener {
            startActivity(Intent(this, BearYear::class.java))
        }

        binding.btnClean.setOnClickListener {
            presenter.onCleanClicked(this)
        }

        binding.cardDevice.setOnClickListener {
            startActivity(Intent(this, DeerYear::class.java))
        }

        binding.cardCpu.setOnClickListener {
            startActivity(Intent(this, PandaYear::class.java))
        }

        presenter.loadStorageInfo(this)
    }

    /**
     * 更新存储信息显示
     */
    override fun updateStorageInfo(usedStorage: String, totalStorage: String, progress: Int) {
        // 分离已用存储的数字和单位
        val parts = usedStorage.split(" ")
        val number = if (parts.isNotEmpty()) parts[0] else "0"
        val unit = if (parts.size > 1) " ${parts[1]}" else " GB"

        // 设置文本
        binding.tvUsedNumber.text = number
        binding.tvUsedUnit.text = unit
        binding.tvTotal.text = totalStorage

        // 设置进度
        binding.storageCircle.progress = progress
    }

    /**
     * 请求存储权限
     */
    override fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上：请求MANAGE_EXTERNAL_STORAGE权限
            if (!Environment.isExternalStorageManager()) {
                showPermissionRationaleDialog {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:$packageName")
                        }
                    manageStorageLauncher.launch(intent)
                }
            } else {
                presenter.onPermissionResult(this, true)
            }
        } else {
            // Android 10及以下：请求READ_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            val hasPermission = permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!hasPermission) {
                showPermissionRationaleDialog {
                    storagePermissionLauncher.launch(permissions)
                }
            } else {
                presenter.onPermissionResult(this, true)
            }
        }
    }

    /**
     * 显示权限说明对话框
     */
    private fun showPermissionRationaleDialog(onConfirm: () -> Unit) {
        // 创建自定义Dialog
        val dialog = Dialog(this)

        // 加载自定义布局
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission, null)
        dialog.setContentView(dialogView)

        // 设置Dialog窗口属性
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 设置Dialog宽度，考虑32dp边距
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val dialogWidth = screenWidth - (32 * 2 * displayMetrics.density).toInt()

            val layoutParams = attributes
            layoutParams.width = dialogWidth
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = layoutParams
        }

        // 设置按钮点击事件
        dialogView.findViewById<CardView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<CardView>(R.id.btn_yes).setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        // 显示对话框
        dialog.show()
    }

    /**
     * 显示跳转设置的对话框
     */
    override fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission denied")
            .setMessage("Storage permission denied, unable to perform cleanup operation. Do you want to manually enable permissions on the settings page?")
            .setPositiveButton("Go to settings") { _, _ ->
                navigateToSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * 跳转到应用设置页面
     */
    override fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        manageStorageLauncher.launch(intent)
    }

    override fun showCleanSuccess() {
        startActivity(Intent(this, TigerYear::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
package com.example.lin_new_project_kotlin_ai.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentQrcodeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CodeUtils
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils.Companion.checkStr
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class QrcodeFragment : BaseBindingFragment<FragmentQrcodeBinding?>() {

    private var currentBitmap: Bitmap? = null
    private var selectedColor: Int = Color.BLACK

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentQrcodeBinding {
        return FragmentQrcodeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupListeners()

        // 預設填入並產出初始 QR Code
        val defaultContent = "https://github.com/b1616001/lin_project_kotlin_ai"
        getBinding()?.edQrcode?.setText(defaultContent)
        generateCode()
    }

    private fun initView() {
        val binding = getBinding() ?: return
        highlightSelectedColor(binding.chipColorBlack)
    }

    private fun setupListeners() {
        val binding = getBinding() ?: return

        // 點擊產出 (維持原專案點擊 tv_qrcode 觸發機制)
        binding.tvQrcode.setOnClickListener {
            generateCode()
        }

        // 清空
        binding.btnClear.setOnClickListener {
            binding.edQrcode.setText("")
            binding.image.setImageBitmap(null)
            currentBitmap = null
            binding.tvInfo.text = "已清空內容，請輸入文字後產出"
            showToast("已清空")
        }

        // 快速範本
        binding.chipPresetUrl.setOnClickListener {
            binding.rbTypeQr.isChecked = true
            binding.edQrcode.setText("https://github.com/b1616001/lin_project_kotlin_ai")
            generateCode()
        }

        binding.chipPresetWifi.setOnClickListener {
            binding.rbTypeQr.isChecked = true
            binding.edQrcode.setText("WIFI:S:MyOffice_5G;T:WPA;P:password888;;")
            generateCode()
        }

        binding.chipPresetVcard.setOnClickListener {
            binding.rbTypeQr.isChecked = true
            val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Android Developer\nTEL:0912-345-678\nEMAIL:developer@example.com\nEND:VCARD"
            binding.edQrcode.setText(vcard)
            generateCode()
        }

        binding.chipPresetText.setOnClickListener {
            binding.rbTypeQr.isChecked = true
            binding.edQrcode.setText("這是一段透過 CodeUtils 產生的自定義 QR Code 訊息！")
            generateCode()
        }

        // 模式切換 (QR Code / 一維條碼)
        binding.rgCodeType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_type_bar) {
                binding.cbAddLogo.visibility = View.GONE
                binding.tvQrcode.text = "✨ 產出一維條碼"
                val currentText = binding.edQrcode.text.toString()
                if (currentText.startsWith("http") || currentText.length > 20) {
                    binding.edQrcode.setText("CODE-128-8869")
                }
            } else {
                binding.cbAddLogo.visibility = View.VISIBLE
                binding.tvQrcode.text = "✨ 產出 Qrcode"
            }
            generateCode()
        }

        // 顏色選擇
        binding.chipColorBlack.setOnClickListener {
            selectedColor = Color.BLACK
            highlightSelectedColor(binding.chipColorBlack)
            generateCode()
        }
        binding.chipColorTeal.setOnClickListener {
            selectedColor = Color.parseColor("#019889")
            highlightSelectedColor(binding.chipColorTeal)
            generateCode()
        }
        binding.chipColorBlue.setOnClickListener {
            selectedColor = Color.parseColor("#1976D2")
            highlightSelectedColor(binding.chipColorBlue)
            generateCode()
        }
        binding.chipColorRed.setOnClickListener {
            selectedColor = Color.parseColor("#D32F2F")
            highlightSelectedColor(binding.chipColorRed)
            generateCode()
        }

        // 複製文字
        binding.btnCopyText.setOnClickListener {
            val content = binding.edQrcode.text.toString()
            if (content.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("QR Content", content)
                clipboard.setPrimaryClip(clip)
                showToast("已複製編碼內容至剪貼簿")
            } else {
                showToast("無內容可複製")
            }
        }

        // 分享圖片
        binding.btnShare.setOnClickListener {
            shareCurrentBitmap()
        }
    }

    private fun generateCode() {
        val binding = getBinding() ?: return
        val content = binding.edQrcode.text.toString().trim()

        if (!checkStr(content)) {
            showToast("請輸入內容")
            return
        }

        val isQr = binding.rbTypeQr.isChecked
        val addLogo = binding.cbAddLogo.isChecked && isQr

        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    if (isQr) {
                        val logo = if (addLogo) {
                            BitmapFactory.decodeResource(resources, R.drawable.icon_red)
                        } else null
                        CodeUtils.createQRCode(content, 600, logo, 0.2f, selectedColor)
                    } else {
                        // 一維條碼 (Code-128)
                        CodeUtils.createBarCode(content, BarcodeFormat.CODE_128, 700, 260, null, true, 36, selectedColor)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                currentBitmap = bitmap
                binding.image.setImageBitmap(bitmap)
                val typeName = if (isQr) "QR Code 二維碼" else "Code-128 一維條碼"
                val logoText = if (addLogo) " • 嵌入 Logo" else ""
                binding.tvInfo.text = "已產出: $typeName (${bitmap.width}x${bitmap.height} px)$logoText"
            } else {
                showToast("產出失敗，請檢查輸入內容是否支援此編碼")
            }
        }
    }

    private fun highlightSelectedColor(selectedView: View) {
        val binding = getBinding() ?: return
        val chips = listOf(
            binding.chipColorBlack,
            binding.chipColorTeal,
            binding.chipColorBlue,
            binding.chipColorRed
        )
        for (chip in chips) {
            chip.alpha = if (chip == selectedView) 1.0f else 0.55f
        }
    }

    private fun shareCurrentBitmap() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            showToast("請先產出條碼後再分享")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                try {
                    val cachePath = File(requireContext().cacheDir, "images")
                    if (!cachePath.exists()) cachePath.mkdirs()
                    val file = File(cachePath, "generated_code.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.flush()
                    stream.close()

                    FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.lin_new_project_kotlin_ai.myfileprovider",
                        file
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "分享 QR Code / 條碼圖片"))
            } else {
                showToast("圖片分享失敗")
            }
        }
    }
}
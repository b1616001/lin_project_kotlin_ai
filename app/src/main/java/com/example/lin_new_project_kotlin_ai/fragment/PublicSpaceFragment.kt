package com.example.lin_new_project_kotlin_ai.fragment

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentPublicspaceBinding
import com.example.lin_new_project_kotlin_ai.`fun`.AndroidQStorageSaveUtils
import com.example.lin_new_project_kotlin_ai.`fun`.ImageMethod
import com.example.lin_new_project_kotlin_ai.`fun`.SavedFileMetadata
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PublicSpaceFragment : BaseBindingFragment<FragmentPublicspaceBinding?>() {

    private var pendingCameraUri: Uri? = null
    private var activeFileUri: Uri? = null
    private var activeMimeType: String = "*/*"
    private var lastTxtUri: Uri? = null
    private val operationHistory = ArrayDeque<String>()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            showToast("未授予相機權限，無法開啟相機拍照")
            appendLog("相機權限遭拒絕")
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val ctx = context ?: return@registerForActivityResult
        val capturedUri = pendingCameraUri
        pendingCameraUri = null
        if (success && capturedUri != null) {
            inspectAndPreviewUri(capturedUri, actionLabel = "相機拍照儲存")
        } else {
            if (capturedUri != null) {
                AndroidQStorageSaveUtils.deleteUri(ctx, capturedUri)
            }
            appendLog("已取消拍照")
        }
    }

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            inspectAndPreviewUri(uri, actionLabel = "Photo Picker 選取圖片")
        }
    }

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val ctx = context ?: return@registerForActivityResult
        if (uri == null) return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                ctx.contentResolver.openInputStream(uri)?.use { stream ->
                    AndroidQStorageSaveUtils.saveVideo(
                        ctx,
                        stream,
                        "mp4",
                        Environment.DIRECTORY_MOVIES,
                        "Lin"
                    )
                }
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                inspectAndPreviewUri(savedUri!!, actionLabel = "匯入影片至 Movies/Lin")
                showToast("影片已儲存至 Movies/Lin")
            } else {
                showToast("影片匯入失敗")
                appendLog("匯入影片失敗")
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            inspectAndPreviewUri(uri, actionLabel = "SAF 開啟文件")
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentPublicspaceBinding {
        return FragmentPublicspaceBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initActionButtons()
    }

    private fun initHeader() {
        val b = getBinding() ?: return
        b.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        b.tvStorageModeBadge.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Scoped Storage (API ${Build.VERSION.SDK_INT})"
        } else {
            "Legacy Storage (API ${Build.VERSION.SDK_INT})"
        }
    }

    private fun initActionButtons() {
        val b = getBinding() ?: return

        b.tvCamera.setOnClickListener { onCamera() }
        b.tvFile.setOnClickListener {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        b.btnPickVideo.setOnClickListener {
            pickVideoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
        b.btnPickDocument.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }

        b.tvBitmap.setOnClickListener { onSaveSampleBitmap() }
        b.tvText.setOnClickListener { onSaveAssetText() }
        b.tvTxt.setOnClickListener { onSaveCustomText() }
        b.tvReadTxt.setOnClickListener { onReadText() }
        b.tvVideo.setOnClickListener { onSaveSampleVideo() }
        b.tvAudio.setOnClickListener { onSaveSampleAudio() }

        b.btnOpenFile.setOnClickListener { openActiveFileExternally() }
        b.btnShareFile.setOnClickListener { shareActiveFile() }
        b.btnDeleteFile.setOnClickListener { deleteActiveFile() }
    }

    private fun onCamera() {
        val ctx = context ?: return
        val hasCamera = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (hasCamera) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        val ctx = context ?: return
        val outputUri = AndroidQStorageSaveUtils.customizedFilePath(
            ctx,
            Environment.DIRECTORY_DCIM,
            "Lin"
        )
        if (outputUri == null) {
            showToast("無法建立相機輸出檔案 Uri")
            appendLog("相機輸出 Uri 建立失敗")
            return
        }
        pendingCameraUri = outputUri
        try {
            takePictureLauncher.launch(outputUri)
        } catch (_: ActivityNotFoundException) {
            AndroidQStorageSaveUtils.deleteUri(ctx, outputUri)
            pendingCameraUri = null
            showToast("裝置上找不到可用的相機應用程式")
            appendLog("找不到相機應用")
        }
    }

    private fun onSaveSampleBitmap() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                val drawable = ImageMethod.getDrawableByVERSION(ctx, R.drawable.btn_bgd_blue)
                val bitmap = ImageMethod.getBitmap(drawable)
                AndroidQStorageSaveUtils.saveBitmap(ctx, bitmap, Environment.DIRECTORY_DCIM, "Lin")
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                inspectAndPreviewUri(savedUri!!, actionLabel = "儲存範例圖片至 DCIM/Lin")
                showToast("圖片儲存成功")
            } else {
                showToast("圖片儲存失敗")
                appendLog("儲存範例圖片失敗")
            }
        }
    }

    private fun onSaveAssetText() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                runCatching {
                    ctx.resources.assets.open("test.txt").use { stream ->
                        AndroidQStorageSaveUtils.saveNotebookInputStream(
                            ctx,
                            stream,
                            "txt",
                            Environment.DIRECTORY_DOCUMENTS,
                            "Lin"
                        )
                    }
                }.getOrNull()
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                lastTxtUri = savedUri
                inspectAndPreviewUri(savedUri!!, actionLabel = "複製 test.txt 至 Documents/Lin")
                showToast("記事本檔案儲存成功")
            } else {
                showToast("記事本檔案儲存失敗")
                appendLog("複製 test.txt 失敗")
            }
        }
    }

    private fun onSaveCustomText() {
        val ctx = context ?: return
        val b = getBinding() ?: return
        val content = b.etCustomNoteText.text?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "Hello Scoped Storage!"

        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                AndroidQStorageSaveUtils.saveNotebookText(
                    ctx,
                    content,
                    Environment.DIRECTORY_DOCUMENTS,
                    "Lin"
                )
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                lastTxtUri = savedUri
                inspectAndPreviewUri(savedUri!!, actionLabel = "建立自訂記事本至 Documents/Lin")
                showToast("自訂記事本寫入成功")
            } else {
                showToast("記事本寫入失敗")
                appendLog("寫入自訂記事本失敗")
            }
        }
    }

    private fun onReadText() {
        val ctx = context ?: return
        val targetUri = lastTxtUri
        if (targetUri == null || !AndroidQStorageSaveUtils.fileUriIsExists(ctx, targetUri)) {
            showToast("尚無可讀取的記事本檔案，請先寫入或選擇 .txt")
            return
        }
        inspectAndPreviewUri(targetUri, actionLabel = "讀取記事本內容")
    }

    private fun onSaveSampleVideo() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                runCatching {
                    AndroidQStorageSaveUtils.openSampleVideoStream(ctx).use { stream ->
                        AndroidQStorageSaveUtils.saveVideo(
                            ctx,
                            stream,
                            "mp4",
                            Environment.DIRECTORY_MOVIES,
                            "Lin"
                        )
                    }
                }.getOrNull()
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                inspectAndPreviewUri(savedUri!!, actionLabel = "儲存範例 MP4 至 Movies/Lin")
                showToast("MP4 儲存成功")
            } else {
                showToast("MP4 儲存失敗")
                appendLog("儲存範例 MP4 失敗")
            }
        }
    }

    private fun onSaveSampleAudio() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val savedUri = withContext(Dispatchers.IO) {
                runCatching {
                    AndroidQStorageSaveUtils.openSampleAudioStream(ctx).use { stream ->
                        AndroidQStorageSaveUtils.saveAudio(
                            ctx,
                            stream,
                            "mp3",
                            Environment.DIRECTORY_MUSIC,
                            "Lin"
                        )
                    }
                }.getOrNull()
            }
            if (AndroidQStorageSaveUtils.fileUriIsExists(ctx, savedUri)) {
                inspectAndPreviewUri(savedUri!!, actionLabel = "儲存範例 MP3 至 Music/Lin")
                showToast("MP3 儲存成功")
            } else {
                showToast("MP3 儲存失敗")
                appendLog("儲存範例 MP3 失敗")
            }
        }
    }

    private fun inspectAndPreviewUri(uri: Uri, actionLabel: String) {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val metadata = withContext(Dispatchers.IO) {
                AndroidQStorageSaveUtils.queryFileMetadata(ctx, uri)
            }
            val isImage = metadata.mimeType.startsWith("image/") ||
                metadata.displayName.endsWith(".png", ignoreCase = true) ||
                metadata.displayName.endsWith(".jpg", ignoreCase = true) ||
                metadata.displayName.endsWith(".jpeg", ignoreCase = true) ||
                metadata.displayName.endsWith(".webp", ignoreCase = true)

            val isText = metadata.mimeType.startsWith("text/") ||
                metadata.displayName.endsWith(".txt", ignoreCase = true)

            val loadedBitmap = if (isImage) {
                withContext(Dispatchers.IO) {
                    AndroidQStorageSaveUtils.loadBitmapFromUri(ctx, uri)
                }
            } else {
                null
            }

            val loadedText = if (isText) {
                lastTxtUri = uri
                withContext(Dispatchers.IO) {
                    AndroidQStorageSaveUtils.getReadTxt(ctx, uri)
                }
            } else {
                null
            }

            val b = getBinding() ?: return@launch
            activeFileUri = uri
            activeMimeType = metadata.mimeType

            if (loadedBitmap != null) {
                b.imageView.setImageBitmap(loadedBitmap)
                b.imageView.visibility = View.VISIBLE
                b.llEmptyPreview.visibility = View.GONE
            } else if (!isText) {
                b.imageView.setImageDrawable(null)
                b.llEmptyPreview.visibility = View.VISIBLE
            }

            if (loadedText != null) {
                b.llTextPreview.visibility = View.VISIBLE
                b.tvTextPreview.text = loadedText.ifBlank { "(空白檔案)" }
            } else {
                b.llTextPreview.visibility = View.GONE
            }

            renderMetadata(b, metadata)
            appendLog("$actionLabel：${metadata.displayName}")
        }
    }

    private fun renderMetadata(b: FragmentPublicspaceBinding, metadata: SavedFileMetadata) {
        val ctx = context ?: return
        val sizeText = if (metadata.sizeBytes >= 0) {
            Formatter.formatFileSize(ctx, metadata.sizeBytes)
        } else {
            "未知大小"
        }

        b.tvPreviewStatus.text = metadata.mimeType
        b.tvPreviewStatus.setTextColor(0xFF019889.toInt())
        b.llFileActions.visibility = View.VISIBLE

        b.tvFileMetadata.text = buildString {
            append("• 檔案名稱：${metadata.displayName}\n")
            append("• MIME 類型：${metadata.mimeType}（大小：$sizeText）\n")
            append("• 相對路徑 / 來源：${metadata.relativePath}\n")
            append("• Uri：${metadata.uri}")
        }
    }

    private fun openActiveFileExternally() {
        val uri = activeFileUri ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, activeMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, "選擇開啟應用程式"))
        } catch (_: ActivityNotFoundException) {
            showToast("找不到可開啟此類型的應用程式")
        }
    }

    private fun shareActiveFile() {
        val uri = activeFileUri ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = activeMimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, "分享檔案"))
        } catch (_: ActivityNotFoundException) {
            showToast("無法啟動分享選單")
        }
    }

    private fun deleteActiveFile() {
        val ctx = context ?: return
        val uri = activeFileUri ?: return
        val deleted = AndroidQStorageSaveUtils.deleteUri(ctx, uri)
        val b = getBinding() ?: return

        if (deleted) {
            if (lastTxtUri == uri) lastTxtUri = null
            activeFileUri = null
            b.imageView.setImageDrawable(null)
            b.llEmptyPreview.visibility = View.VISIBLE
            b.llTextPreview.visibility = View.GONE
            b.llFileActions.visibility = View.GONE
            b.tvPreviewStatus.text = "已刪除"
            b.tvPreviewStatus.setTextColor(0xFF607D8B.toInt())
            b.tvFileMetadata.text = "• 目前檔案已從公有空間移除"
            appendLog("已刪除公有空間檔案")
            showToast("檔案已刪除")
        } else {
            showToast("此 Uri 不支援直接刪除或權限不足")
            appendLog("刪除失敗（非本應用建立或無權限）")
        }
    }

    private fun appendLog(message: String) {
        val b = getBinding() ?: return
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        operationHistory.addFirst("• [$timeStr] $message")
        while (operationHistory.size > 6) {
            operationHistory.removeLast()
        }
        b.tvOperationLog.text = operationHistory.joinToString("\n")
    }
}

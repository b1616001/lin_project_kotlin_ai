package com.example.lin_new_project_kotlin_ai.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentGifBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.util.Locale

class GifFragment : BaseBindingFragment<FragmentGifBinding?>() {

    private var gifDrawable: GifDrawable? = null
    private var currentSpeed = 1.0f
    private var isInfiniteLoop = true
    private var completedLoops = 0
    private var isUserScrubbing = false
    private var currentBackdrop = Backdrop.NEUTRAL
    private var currentScaleType = ImageView.ScaleType.FIT_CENTER

    private enum class Backdrop(val colorInt: Int) {
        LIGHT(0xFFFFFFFF.toInt()),
        NEUTRAL(0xFFEEF2F5.toInt()),
        DARK(0xFF181A1F.toInt())
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val drawable = gifDrawable
            if (drawable != null && !drawable.isRecycled) {
                if (!isUserScrubbing) {
                    updateFrameAndTimeProgress(drawable)
                }
                if (drawable.isPlaying) {
                    mainHandler.postDelayed(this, 50L)
                } else {
                    refreshPlaybackStateUi()
                }
            }
        }
    }

    private val pickGifLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            loadGifFromUri(uri)
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentGifBinding {
        return FragmentGifBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initBackdropControls()
        initTimelineScrubber()
        initPlaybackControls()
        initSpeedControls()
        initScaleAndLoopControls()
        initSourcePickerControls()
        loadDefaultGif()
    }

    override fun onResume() {
        super.onResume()
        val drawable = gifDrawable
        if (drawable != null && !drawable.isRecycled && drawable.isPlaying) {
            startProgressPolling()
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(progressRunnable)
    }

    override fun onDestroyView() {
        mainHandler.removeCallbacks(progressRunnable)
        releaseCurrentGif()
        super.onDestroyView()
    }

    private fun initHeader() {
        val binding = getBinding() ?: return
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initBackdropControls() {
        val binding = getBinding() ?: return
        binding.btnBgLight.setOnClickListener { applyBackdrop(Backdrop.LIGHT) }
        binding.btnBgNeutral.setOnClickListener { applyBackdrop(Backdrop.NEUTRAL) }
        binding.btnBgDark.setOnClickListener { applyBackdrop(Backdrop.DARK) }
        applyBackdrop(currentBackdrop)
    }

    private fun applyBackdrop(backdrop: Backdrop) {
        val binding = getBinding() ?: return
        currentBackdrop = backdrop
        binding.layoutGifStage.setBackgroundColor(backdrop.colorInt)
        binding.btnBgLight.isSelected = backdrop == Backdrop.LIGHT
        binding.btnBgNeutral.isSelected = backdrop == Backdrop.NEUTRAL
        binding.btnBgDark.isSelected = backdrop == Backdrop.DARK
    }

    private fun initTimelineScrubber() {
        val binding = getBinding() ?: return
        binding.sbFrameProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val drawable = gifDrawable ?: return
                if (fromUser && !drawable.isRecycled) {
                    drawable.pause()
                    drawable.seekToFrame(progress)
                    updateFrameAndTimeProgress(drawable)
                    refreshPlaybackStateUi()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserScrubbing = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserScrubbing = false
                refreshPlaybackStateUi()
            }
        })
    }

    private fun initPlaybackControls() {
        val binding = getBinding() ?: return

        binding.btnPlayPause.setOnClickListener {
            val drawable = gifDrawable ?: return@setOnClickListener
            if (drawable.isRecycled) return@setOnClickListener
            if (drawable.isPlaying) {
                drawable.pause()
                mainHandler.removeCallbacks(progressRunnable)
            } else {
                if (!isInfiniteLoop && drawable.currentFrameIndex >= (drawable.numberOfFrames - 1).coerceAtLeast(0)) {
                    drawable.reset()
                } else {
                    drawable.start()
                }
                startProgressPolling()
            }
            refreshPlaybackStateUi()
        }

        binding.btnReset.setOnClickListener {
            val drawable = gifDrawable ?: return@setOnClickListener
            if (drawable.isRecycled) return@setOnClickListener
            completedLoops = 0
            drawable.reset()
            drawable.setSpeed(currentSpeed)
            startProgressPolling()
            refreshPlaybackStateUi()
            updateGifSpecs(drawable)
        }

        binding.btnPrevFrame.setOnClickListener {
            stepFrame(delta = -1)
        }

        binding.btnNextFrame.setOnClickListener {
            stepFrame(delta = 1)
        }
    }

    private fun stepFrame(delta: Int) {
        val drawable = gifDrawable ?: return
        if (drawable.isRecycled) return
        val total = drawable.numberOfFrames
        if (total <= 1) return

        drawable.pause()
        mainHandler.removeCallbacks(progressRunnable)
        val current = drawable.currentFrameIndex.coerceIn(0, total - 1)
        val target = ((current + delta) % total + total) % total
        drawable.seekToFrame(target)
        updateFrameAndTimeProgress(drawable)
        refreshPlaybackStateUi()
    }

    private fun initSpeedControls() {
        val binding = getBinding() ?: return
        binding.btnSpeed025.setOnClickListener { applySpeed(0.25f) }
        binding.btnSpeed050.setOnClickListener { applySpeed(0.5f) }
        binding.btnSpeed100.setOnClickListener { applySpeed(1.0f) }
        binding.btnSpeed150.setOnClickListener { applySpeed(1.5f) }
        binding.btnSpeed200.setOnClickListener { applySpeed(2.0f) }
        highlightSpeedButton()
    }

    private fun applySpeed(speed: Float) {
        currentSpeed = speed
        gifDrawable?.let { drawable ->
            if (!drawable.isRecycled) {
                drawable.setSpeed(speed)
            }
        }
        highlightSpeedButton()
    }

    private fun highlightSpeedButton() {
        val binding = getBinding() ?: return
        binding.btnSpeed025.isSelected = currentSpeed == 0.25f
        binding.btnSpeed050.isSelected = currentSpeed == 0.5f
        binding.btnSpeed100.isSelected = currentSpeed == 1.0f
        binding.btnSpeed150.isSelected = currentSpeed == 1.5f
        binding.btnSpeed200.isSelected = currentSpeed == 2.0f

        val speedDesc = when (currentSpeed) {
            0.25f -> "極慢檢視"
            0.5f -> "慢速"
            1.0f -> "標準"
            1.5f -> "快速"
            2.0f -> "兩倍速"
            else -> "自訂"
        }
        binding.tvSpeedLabel.text = String.format(Locale.US, "播放速度：%.2gx（%s）", currentSpeed, speedDesc)
    }

    private fun initScaleAndLoopControls() {
        val binding = getBinding() ?: return

        binding.btnScaleFit.setOnClickListener { applyScaleType(ImageView.ScaleType.FIT_CENTER) }
        binding.btnScaleCrop.setOnClickListener { applyScaleType(ImageView.ScaleType.CENTER_CROP) }
        binding.btnScaleInside.setOnClickListener { applyScaleType(ImageView.ScaleType.CENTER_INSIDE) }
        applyScaleType(currentScaleType)

        binding.switchLoopInfinite.setOnCheckedChangeListener { _, isChecked ->
            isInfiniteLoop = isChecked
            gifDrawable?.let { drawable ->
                if (!drawable.isRecycled) {
                    drawable.loopCount = if (isChecked) 0 else 1
                }
            }
            showToast(if (isChecked) "已切換為無限循環播放" else "已切換為單次播放")
        }
    }

    private fun applyScaleType(scaleType: ImageView.ScaleType) {
        val binding = getBinding() ?: return
        currentScaleType = scaleType
        binding.gifImageView.scaleType = scaleType
        binding.btnScaleFit.isSelected = scaleType == ImageView.ScaleType.FIT_CENTER
        binding.btnScaleCrop.isSelected = scaleType == ImageView.ScaleType.CENTER_CROP
        binding.btnScaleInside.isSelected = scaleType == ImageView.ScaleType.CENTER_INSIDE
    }

    private fun initSourcePickerControls() {
        val binding = getBinding() ?: return

        binding.btnPickGif.setOnClickListener {
            pickGifLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.SingleMimeType("image/gif")
                )
            )
        }

        binding.btnLoadDefault.setOnClickListener {
            loadDefaultGif()
        }
    }

    private fun loadDefaultGif() {
        val binding = getBinding() ?: return
        try {
            val newDrawable = GifDrawable(resources, R.drawable.mmm)
            attachGifDrawable(newDrawable, "內建動畫：mmm.gif")
        } catch (e: Exception) {
            binding.tvGifSpecs.text = "載入內建 GIF 失敗：${e.localizedMessage ?: "未知錯誤"}"
            showToast("無法載入內建 GIF")
        }
    }

    private fun loadGifFromUri(uri: Uri) {
        val context = context ?: return
        try {
            val newDrawable = GifDrawable(context.contentResolver, uri)
            val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "自選 GIF 動畫"
            attachGifDrawable(newDrawable, "相簿圖檔：$displayName")
            showToast("已載入自選 GIF")
        } catch (e: Exception) {
            showToast("載入失敗，請確認選擇的檔案為有效 GIF 動畫")
        }
    }

    private fun attachGifDrawable(newDrawable: GifDrawable, sourceLabel: String) {
        val binding = getBinding() ?: return
        mainHandler.removeCallbacks(progressRunnable)
        releaseCurrentGif()

        completedLoops = 0
        newDrawable.loopCount = if (isInfiniteLoop) 0 else 1
        newDrawable.setSpeed(currentSpeed)
        newDrawable.addAnimationListener(AnimationListener { loopNumber ->
            completedLoops = loopNumber + 1
            if (view != null) {
                updateGifSpecs(newDrawable)
                if (!isInfiniteLoop) {
                    refreshPlaybackStateUi()
                }
            }
        })

        gifDrawable = newDrawable
        binding.gifImageView.setImageDrawable(newDrawable)
        binding.tvSourceName.text = sourceLabel

        val totalFrames = newDrawable.numberOfFrames.coerceAtLeast(1)
        binding.sbFrameProgress.max = (totalFrames - 1).coerceAtLeast(0)
        binding.sbFrameProgress.isEnabled = totalFrames > 1

        newDrawable.start()
        startProgressPolling()
        updateFrameAndTimeProgress(newDrawable)
        updateGifSpecs(newDrawable)
        refreshPlaybackStateUi()
    }

    private fun releaseCurrentGif() {
        val binding = getBinding()
        binding?.gifImageView?.setImageDrawable(null)
        gifDrawable?.let { drawable ->
            try {
                drawable.stop()
                if (!drawable.isRecycled) {
                    drawable.recycle()
                }
            } catch (_: Exception) {
                // Ignore cleanup exceptions
            }
        }
        gifDrawable = null
    }

    private fun startProgressPolling() {
        mainHandler.removeCallbacks(progressRunnable)
        mainHandler.post(progressRunnable)
    }

    private fun updateFrameAndTimeProgress(drawable: GifDrawable) {
        val binding = getBinding() ?: return
        val totalFrames = drawable.numberOfFrames.coerceAtLeast(1)
        val currentFrame = drawable.currentFrameIndex.coerceIn(0, totalFrames - 1)
        binding.sbFrameProgress.progress = currentFrame
        binding.tvFrameProgress.text = "幀數：${currentFrame + 1} / $totalFrames"

        val totalMs = drawable.duration.coerceAtLeast(0)
        val currentMs = drawable.currentPosition.coerceIn(0, totalMs.coerceAtLeast(1))
        binding.tvTimeProgress.text = String.format(
            Locale.US,
            "%.2fs / %.2fs",
            currentMs / 1000f,
            totalMs / 1000f
        )
    }

    private fun updateGifSpecs(drawable: GifDrawable) {
        val binding = getBinding() ?: return
        val frames = drawable.numberOfFrames.coerceAtLeast(1)
        val durationMs = drawable.duration.coerceAtLeast(0)
        val fps = if (durationMs > 0 && frames > 1) {
            frames * 1000f / durationMs
        } else {
            0f
        }
        val bytes = drawable.allocationByteCount
        val sizeText = if (bytes >= 1024 * 1024) {
            String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f))
        } else {
            String.format(Locale.US, "%.1f KB", bytes / 1024f)
        }

        binding.tvGifSpecs.text = String.format(
            Locale.US,
            "• 圖片尺寸：%d × %d px\n" +
                "• 動畫總幀數：%d 幀（總長 %.2f 秒，約 %.1f FPS）\n" +
                "• 解碼記憶體佔用：%s\n" +
                "• 循環設定：%s（已播放 %d 輪）",
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            frames,
            durationMs / 1000f,
            fps,
            sizeText,
            if (isInfiniteLoop) "無限循環" else "單次播放",
            completedLoops
        )
    }

    private fun refreshPlaybackStateUi() {
        val binding = getBinding() ?: return
        val drawable = gifDrawable
        val isPlaying = drawable != null && !drawable.isRecycled && drawable.isPlaying

        if (isPlaying) {
            binding.tvPlayStateBadge.text = "播放中"
            binding.tvPlayStateBadge.setTextColor(0xFF019889.toInt())
            binding.btnPlayPause.text = "暫停播放"
            binding.btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0)
        } else {
            binding.tvPlayStateBadge.text = "已暫停"
            binding.tvPlayStateBadge.setTextColor(0xFFEF6C00.toInt())
            binding.btnPlayPause.text = "繼續播放"
            binding.btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0)
        }
    }
}
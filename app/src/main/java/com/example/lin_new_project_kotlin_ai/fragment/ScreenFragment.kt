package com.example.lin_new_project_kotlin_ai.fragment

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.databinding.FragmentScreenBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale
import kotlin.math.roundToInt

class ScreenFragment : BaseBindingFragment<FragmentScreenBinding?>() {

    private var isUpdatingSwitchProgrammatically = false
    private var isImmersiveEnabled = false

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentScreenBinding {
        return FragmentScreenBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initKeepScreenOnControls()
        initBrightnessControls()
        initOrientationAndImmersiveControls()
        initResetAction()
        refreshAllScreenUi()
    }

    override fun onResume() {
        super.onResume()
        refreshAllScreenUi()
    }

    private fun initHeader() {
        val binding = getBinding() ?: return
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initKeepScreenOnControls() {
        val binding = getBinding() ?: return

        binding.tvOn.setOnClickListener {
            setKeepScreenOn(enabled = true, showFeedback = true)
        }

        binding.tvOff.setOnClickListener {
            setKeepScreenOn(enabled = false, showFeedback = true)
        }

        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingSwitchProgrammatically) {
                setKeepScreenOn(enabled = isChecked, showFeedback = true)
            }
        }
    }

    private fun setKeepScreenOn(enabled: Boolean, showFeedback: Boolean) {
        val window = activity?.window ?: return
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (showFeedback) showToast("已開啟防止屏幕休眠（螢幕將保持常亮）")
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (showFeedback) showToast("已關閉防止屏幕休眠（依系統逾時自動關閉）")
        }
        refreshKeepScreenOnUi()
    }

    private fun isKeepScreenOnEnabled(): Boolean {
        val flags = activity?.window?.attributes?.flags ?: 0
        return (flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
    }

    private fun refreshKeepScreenOnUi() {
        val binding = getBinding() ?: return
        val enabled = isKeepScreenOnEnabled()

        isUpdatingSwitchProgrammatically = true
        binding.switchKeepScreenOn.isChecked = enabled
        isUpdatingSwitchProgrammatically = false

        binding.tvOn.isSelected = enabled
        binding.tvOff.isSelected = !enabled

        if (enabled) {
            binding.tvKeepOnBadge.text = "常亮開啟"
            binding.tvKeepOnBadge.setTextColor(0xFF019889.toInt())
            binding.tvKeepOnDescription.text = "目前狀態：已啟用 FLAG_KEEP_SCREEN_ON（螢幕不會自動休眠）"
            binding.ivKeepScreenIcon.setColorFilter(0xFF019889.toInt())
        } else {
            binding.tvKeepOnBadge.text = "正常休眠"
            binding.tvKeepOnBadge.setTextColor(0xFF607D8B.toInt())
            binding.tvKeepOnDescription.text = "目前狀態：依系統設定逾時時間自動休眠"
            binding.ivKeepScreenIcon.setColorFilter(0xFF607D8B.toInt())
        }
    }

    private fun initBrightnessControls() {
        val binding = getBinding() ?: return

        binding.sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val clampedPercent = progress.coerceIn(1, 100)
                    applyWindowBrightness(clampedPercent / 100f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnBrightnessLow.setOnClickListener {
            applyWindowBrightness(0.15f)
            showToast("已設定視窗亮度為 15%（夜間低光）")
        }
        binding.btnBrightnessMid.setOnClickListener {
            applyWindowBrightness(0.55f)
            showToast("已設定視窗亮度為 55%（室內舒適）")
        }
        binding.btnBrightnessHigh.setOnClickListener {
            applyWindowBrightness(1.0f)
            showToast("已設定視窗亮度為 100%（最大亮度）")
        }
        binding.btnBrightnessSystem.setOnClickListener {
            applyWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
            showToast("已恢復跟隨系統自動亮度")
        }
    }

    private fun applyWindowBrightness(brightness: Float) {
        val window = activity?.window ?: return
        val params = window.attributes
        params.screenBrightness = if (brightness < 0f) {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        } else {
            brightness.coerceIn(0.01f, 1.0f)
        }
        window.attributes = params
        refreshBrightnessUi()
    }

    private fun refreshBrightnessUi() {
        val binding = getBinding() ?: return
        val currentBrightness = activity?.window?.attributes?.screenBrightness
            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        if (currentBrightness < 0f) {
            val sysPercent = readSystemBrightnessPercent()
            binding.tvBrightnessValue.text = if (sysPercent != null) {
                "目前亮度：跟隨系統（系統約 $sysPercent%）"
            } else {
                "目前亮度：跟隨系統設定"
            }
            binding.sbBrightness.progress = sysPercent ?: 50
        } else {
            val percent = (currentBrightness * 100f).roundToInt().coerceIn(1, 100)
            binding.tvBrightnessValue.text = "目前亮度：自訂視窗亮度 $percent%"
            binding.sbBrightness.progress = percent
        }
    }

    private fun readSystemBrightnessPercent(): Int? {
        val resolver = context?.contentResolver ?: return null
        return try {
            val value = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
            (value * 100f / 255f).roundToInt().coerceIn(0, 100)
        } catch (_: Exception) {
            null
        }
    }

    private fun initOrientationAndImmersiveControls() {
        val binding = getBinding() ?: return

        binding.btnOrientAuto.setOnClickListener {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, "已切換為自動旋轉")
        }
        binding.btnOrientPortrait.setOnClickListener {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, "已鎖定直向顯示")
        }
        binding.btnOrientLandscape.setOnClickListener {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, "已鎖定橫向顯示")
        }

        binding.switchImmersive.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingSwitchProgrammatically) {
                setImmersiveMode(isChecked)
            }
        }
    }

    private fun setScreenOrientation(orientation: Int, toastMsg: String) {
        val act = activity ?: return
        act.requestedOrientation = orientation
        refreshOrientationUi()
        showToast(toastMsg)
    }

    private fun refreshOrientationUi() {
        val binding = getBinding() ?: return
        val orientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        binding.btnOrientAuto.isSelected = orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        binding.btnOrientPortrait.isSelected = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.btnOrientLandscape.isSelected = orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    private fun setImmersiveMode(enabled: Boolean) {
        val window = activity?.window ?: return
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        isImmersiveEnabled = enabled
        if (enabled) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            showToast("已開啟沉浸式隱藏狀態列")
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
            showToast("已恢復顯示系統狀態列")
        }
    }

    private fun initResetAction() {
        val binding = getBinding() ?: return
        binding.btnResetAll.setOnClickListener {
            setKeepScreenOn(enabled = false, showFeedback = false)
            applyWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (isImmersiveEnabled) {
                setImmersiveMode(false)
            }
            refreshAllScreenUi()
            showToast("已恢復全部系統預設顯示設定")
        }
    }

    private fun refreshAllScreenUi() {
        val binding = getBinding() ?: return
        refreshKeepScreenOnUi()
        refreshBrightnessUi()
        refreshOrientationUi()

        isUpdatingSwitchProgrammatically = true
        binding.switchImmersive.isChecked = isImmersiveEnabled
        isUpdatingSwitchProgrammatically = false

        refreshDisplaySpecs()
    }

    private fun refreshDisplaySpecs() {
        val binding = getBinding() ?: return
        val act = activity ?: return
        val metrics = resources.displayMetrics

        val widthPx: Int
        val heightPx: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = act.windowManager.currentWindowMetrics.bounds
            widthPx = bounds.width()
            heightPx = bounds.height()
        } else {
            widthPx = metrics.widthPixels
            heightPx = metrics.heightPixels
        }

        val refreshRateHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            act.display?.refreshRate ?: 60f
        } else {
            @Suppress("DEPRECATION")
            act.windowManager.defaultDisplay?.refreshRate ?: 60f
        }

        val timeoutText = try {
            val timeoutMs = Settings.System.getInt(
                act.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT
            )
            val secs = timeoutMs / 1000
            if (secs >= 60) "${secs / 60} 分 ${secs % 60} 秒" else "$secs 秒"
        } catch (_: Exception) {
            "未知"
        }

        binding.tvDisplaySpecs.text = String.format(
            Locale.US,
            "• 視窗解析度：%d × %d px\n" +
                "• 螢幕密度：%d dpi (%.2fx)\n" +
                "• 螢幕更新率：%.0f Hz\n" +
                "• 系統預設休眠逾時：%s",
            widthPx,
            heightPx,
            metrics.densityDpi,
            metrics.density,
            refreshRateHz,
            timeoutText
        )
    }
}
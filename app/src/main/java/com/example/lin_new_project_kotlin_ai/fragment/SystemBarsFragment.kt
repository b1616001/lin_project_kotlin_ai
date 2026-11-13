package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.databinding.FragmentSystemBarsBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale

class SystemBarsFragment : BaseBindingFragment<FragmentSystemBarsBinding?>() {

    private var hideTitleBar = false
    private var hideStatusBar = false
    private var hideNavBar = false
    private var lightStatusBar = false
    private var behaviorMode = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    private var cutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
    } else {
        0
    }
    private var isUpdatingUi = false

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentSystemBarsBinding {
        return FragmentSystemBarsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readInitialWindowState()
        initHeaderAndFloatingRestore()
        initQuickPresets()
        initGranularSwitches()
        initBehaviorAndCutoutControls()
        setupInsetsListener(view)
        refreshUi()
    }

    private fun readInitialWindowState() {
        val window = activity?.window ?: return
        val rootInsets = ViewCompat.getRootWindowInsets(window.decorView)
        if (rootInsets != null) {
            hideStatusBar = !rootInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            hideNavBar = !rootInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        lightStatusBar = controller.isAppearanceLightStatusBars
        behaviorMode = controller.systemBarsBehavior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cutoutMode = window.attributes.layoutInDisplayCutoutMode
        }
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        hideTitleBar = actionBar != null && !actionBar.isShowing
    }

    private fun initHeaderAndFloatingRestore() {
        val binding = getBinding() ?: return
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnFloatingRestore.setOnClickListener {
            restoreAllBars()
        }
    }

    private fun initQuickPresets() {
        val binding = getBinding() ?: return

        binding.btnPresetImmersive.setOnClickListener {
            hideTitleBar = true
            hideStatusBar = true
            hideNavBar = true
            behaviorMode = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            applySystemBarSettings()
            showToast("已啟用一鍵全螢幕沉浸模式（邊緣滑動可暫時喚醒系統列）")
        }

        binding.btnPresetRestore.setOnClickListener {
            restoreAllBars()
        }

        binding.btnPresetHideStatusOnly.setOnClickListener {
            hideTitleBar = false
            hideStatusBar = true
            hideNavBar = false
            applySystemBarSettings()
            showToast("已隱藏系統狀態列")
        }

        binding.btnPresetHideTitleOnly.setOnClickListener {
            hideTitleBar = true
            hideStatusBar = false
            hideNavBar = false
            applySystemBarSettings()
            showToast("已隱藏標題列（可點擊右下角浮動按鈕恢復）")
        }
    }

    private fun restoreAllBars() {
        hideTitleBar = false
        hideStatusBar = false
        hideNavBar = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
        applySystemBarSettings()
        showToast("已恢復顯示所有標題列與系統列")
    }

    private fun initGranularSwitches() {
        val binding = getBinding() ?: return

        binding.switchHideTitleBar.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                hideTitleBar = isChecked
                applySystemBarSettings()
            }
        }

        binding.switchHideStatusBar.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                hideStatusBar = isChecked
                applySystemBarSettings()
            }
        }

        binding.switchHideNavBar.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                hideNavBar = isChecked
                applySystemBarSettings()
            }
        }

        binding.switchLightStatusBar.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                lightStatusBar = isChecked
                applySystemBarSettings()
            }
        }
    }

    private fun initBehaviorAndCutoutControls() {
        val binding = getBinding() ?: return

        binding.btnBehaviorTransient.setOnClickListener {
            behaviorMode = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            applySystemBarSettings()
            showToast("行為設定：滑動邊緣暫時顯示系統列")
        }

        binding.btnBehaviorDefault.setOnClickListener {
            behaviorMode = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            applySystemBarSettings()
            showToast("行為設定：滑動邊緣永久恢復系統列")
        }

        binding.btnCutoutDefault.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                applySystemBarSettings()
                showToast("瀏海區設定：預設避開瀏海")
            } else {
                showToast("需要 Android 9.0 (API 28) 以上支援")
            }
        }

        binding.btnCutoutShortEdges.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                applySystemBarSettings()
                showToast("瀏海區設定：全螢幕延伸至瀏海區")
            } else {
                showToast("需要 Android 9.0 (API 28) 以上支援")
            }
        }
    }

    private fun applySystemBarSettings() {
        val window = activity?.window ?: return
        val binding = getBinding() ?: return

        // 1. Title Bar (ActionBar + Fragment Header)
        val supportActionBar = (activity as? AppCompatActivity)?.supportActionBar
        if (hideTitleBar) {
            supportActionBar?.hide()
            binding.layoutHeader.visibility = View.GONE
            binding.btnFloatingRestore.visibility = View.VISIBLE
        } else {
            supportActionBar?.show()
            binding.layoutHeader.visibility = View.VISIBLE
            binding.btnFloatingRestore.visibility = View.GONE
        }

        // 2. Display Cutout mode (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = cutoutMode
            window.attributes = params
        }

        // 3. Modern WindowInsetsControllerCompat controls
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = behaviorMode
        controller.isAppearanceLightStatusBars = lightStatusBar

        if (hideStatusBar) {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.show(WindowInsetsCompat.Type.statusBars())
        }

        if (hideNavBar) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }

        refreshUi()
    }

    private fun setupInsetsListener(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            updateTelemetry(insets)
            insets
        }
    }

    private fun refreshUi() {
        val binding = getBinding() ?: return

        isUpdatingUi = true
        binding.switchHideTitleBar.isChecked = hideTitleBar
        binding.switchHideStatusBar.isChecked = hideStatusBar
        binding.switchHideNavBar.isChecked = hideNavBar
        binding.switchLightStatusBar.isChecked = lightStatusBar
        isUpdatingUi = false

        binding.btnBehaviorTransient.isSelected =
            behaviorMode == WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        binding.btnBehaviorDefault.isSelected =
            behaviorMode == WindowInsetsControllerCompat.BEHAVIOR_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.btnCutoutDefault.isSelected =
                cutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            binding.btnCutoutShortEdges.isSelected =
                cutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val allHidden = hideTitleBar && hideStatusBar && hideNavBar
        val anyHidden = hideTitleBar || hideStatusBar || hideNavBar

        binding.tvFullscreenBadge.text = when {
            allHidden -> "全螢幕沉浸"
            anyHidden -> "部分隱藏"
            else -> "標準顯示"
        }
        binding.tvFullscreenBadge.setTextColor(
            if (anyHidden) 0xFF019889.toInt() else 0xFF607D8B.toInt()
        )

        binding.tvModeSummary.text = buildString {
            append("標題列：")
            append(if (hideTitleBar) "已隱藏" else "顯示中")
            append(" | 狀態列：")
            append(if (hideStatusBar) "已隱藏" else "顯示中")
            append(" | 導航列：")
            append(if (hideNavBar) "已隱藏" else "顯示中")
        }

        val rootInsets = ViewCompat.getRootWindowInsets(binding.root)
        if (rootInsets != null) {
            updateTelemetry(rootInsets)
        }
    }

    private fun updateTelemetry(insets: WindowInsetsCompat) {
        val binding = getBinding() ?: return
        val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val statusVisible = insets.isVisible(WindowInsetsCompat.Type.statusBars())
        val navVisible = insets.isVisible(WindowInsetsCompat.Type.navigationBars())
        val density = resources.displayMetrics.density.coerceAtLeast(1f)

        val statusDp = (statusInsets.top / density).toInt()
        val navDp = (navInsets.bottom / density).toInt()
        val behaviorLabel = if (behaviorMode == WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
            "BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE"
        } else {
            "BEHAVIOR_DEFAULT"
        }

        binding.tvInsetsTelemetry.text = String.format(
            Locale.US,
            "• 狀態列 (Status Bar)：%s（Top Inset: %d px / %d dp）\n" +
                "• 導航列 (Nav Bar)：%s（Bottom Inset: %d px / %d dp）\n" +
                "• 標題列 (Header / ActionBar)：%s\n" +
                "• 系統列滑動行為：%s",
            if (statusVisible) "顯示中" else "已隱藏",
            statusInsets.top,
            statusDp,
            if (navVisible) "顯示中" else "已隱藏",
            navInsets.bottom,
            navDp,
            if (hideTitleBar) "已隱藏" else "顯示中",
            behaviorLabel
        )
    }
}

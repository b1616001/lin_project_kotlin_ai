package com.example.lin_new_project_kotlin_ai.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentWindowCustomBinding
import com.example.lin_new_project_kotlin_ai.popupwindow.PopupWindowManager
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.example.lin_new_project_kotlin_ai.window.DockCorner
import com.example.lin_new_project_kotlin_ai.window.WindowConfigStore
import com.example.lin_new_project_kotlin_ai.window.WindowCustomConfig
import com.example.lin_new_project_kotlin_ai.window.WindowMode
import com.example.lin_new_project_kotlin_ai.window.WindowSizePreset
import com.example.lin_new_project_kotlin_ai.window.WindowThemePreset
import java.util.Locale

class WindowCustomFragment : BaseBindingFragment<FragmentWindowCustomBinding?>() {

    private var config: WindowCustomConfig = WindowCustomConfig()
    private var isUpdatingUi = false
    private var launchOverlayAfterPermission = false

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val ctx = context ?: return@registerForActivityResult
        val granted = Settings.canDrawOverlays(ctx)
        if (granted) {
            updateConfig(config.copy(mode = WindowMode.SYSTEM_OVERLAY), applyToWindow = false)
            if (launchOverlayAfterPermission) {
                launchOverlayAfterPermission = false
                openOrUpdateWindow()
                showToast("已授權全局懸浮權限並開啟懸浮視窗")
            } else {
                showToast("已取得全局懸浮權限")
            }
        } else {
            launchOverlayAfterPermission = false
            showToast("尚未授權懸浮窗權限，仍可使用「應用內懸浮」模式")
        }
        refreshUi()
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentWindowCustomBinding {
        return FragmentWindowCustomBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = WindowConfigStore.load()

        // If overlay permission was revoked since last run, gracefully fall back to IN_APP
        if (config.mode == WindowMode.SYSTEM_OVERLAY && !hasOverlayPermission()) {
            config = config.copy(mode = WindowMode.IN_APP)
            WindowConfigStore.save(config)
        }

        initHeader()
        initModeAndLaunchControls()
        initAppearanceControls()
        initInteractionAndFlagsControls()
        bindWindowCallbacks()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    override fun onDestroyView() {
        PopupWindowManager.setCustomWindowListeners(null, null)
        super.onDestroyView()
    }

    private fun initHeader() {
        val b = getBinding() ?: return
        b.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initModeAndLaunchControls() {
        val b = getBinding() ?: return

        b.btnGrantOverlay.setOnClickListener {
            requestOverlayPermission(launchAfter = false)
        }

        b.btnModeInApp.setOnClickListener {
            updateConfig(config.copy(mode = WindowMode.IN_APP), applyToWindow = true)
            showToast("已切換為「應用內懸浮」(免權限)")
        }

        b.btnModeSystemOverlay.setOnClickListener {
            if (hasOverlayPermission()) {
                updateConfig(config.copy(mode = WindowMode.SYSTEM_OVERLAY), applyToWindow = true)
                showToast("已切換為「全局懸浮」(TYPE_APPLICATION_OVERLAY)")
            } else {
                requestOverlayPermission(launchAfter = PopupWindowManager.isCustomWindowShowing())
            }
        }

        b.btnToggleWindow.setOnClickListener {
            if (PopupWindowManager.isCustomWindowShowing()) {
                PopupWindowManager.dismissCustomWindow()
                showToast("已關閉懸浮視窗")
                refreshUi()
            } else {
                if (config.mode == WindowMode.SYSTEM_OVERLAY && !hasOverlayPermission()) {
                    requestOverlayPermission(launchAfter = true)
                } else {
                    openOrUpdateWindow()
                }
            }
        }

        b.btnResetPosition.setOnClickListener {
            val reset = WindowConfigStore.reset()
            config = reset
            if (PopupWindowManager.isCustomWindowShowing()) {
                PopupWindowManager.updateCustomWindow(reset)
                PopupWindowManager.resetCustomWindowPosition()
            }
            refreshUi()
            showToast("已重置懸浮視窗位置與預設樣式")
        }
    }

    private fun initAppearanceControls() {
        val b = getBinding() ?: return

        b.btnSizeCompact.setOnClickListener {
            updateConfig(config.copy(sizePreset = WindowSizePreset.COMPACT), applyToWindow = true)
        }
        b.btnSizeMedium.setOnClickListener {
            updateConfig(config.copy(sizePreset = WindowSizePreset.MEDIUM), applyToWindow = true)
        }
        b.btnSizeLarge.setOnClickListener {
            updateConfig(config.copy(sizePreset = WindowSizePreset.LARGE), applyToWindow = true)
        }

        b.btnThemeDark.setOnClickListener {
            updateConfig(config.copy(themePreset = WindowThemePreset.DARK), applyToWindow = true)
        }
        b.btnThemeTeal.setOnClickListener {
            updateConfig(config.copy(themePreset = WindowThemePreset.TEAL), applyToWindow = true)
        }
        b.btnThemeLight.setOnClickListener {
            updateConfig(config.copy(themePreset = WindowThemePreset.LIGHT), applyToWindow = true)
        }

        b.sbAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val alphaPercent = (progress + 40).coerceIn(40, 100)
                    updateConfig(config.copy(alphaPercent = alphaPercent), applyToWindow = true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        b.sbCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val radiusDp = (progress + 8).coerceIn(8, 28)
                    updateConfig(config.copy(cornerRadiusDp = radiusDp), applyToWindow = true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initInteractionAndFlagsControls() {
        val b = getBinding() ?: return

        b.switchAutoSnap.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                updateConfig(config.copy(autoSnapEdge = isChecked), applyToWindow = true)
            }
        }

        b.switchLockPosition.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                updateConfig(config.copy(lockPosition = isChecked), applyToWindow = true)
            }
        }

        b.switchCollapsed.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                updateConfig(config.copy(collapsed = isChecked), applyToWindow = true)
            }
        }

        b.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                updateConfig(config.copy(keepScreenOn = isChecked), applyToWindow = true)
            }
        }

        b.switchSecureFlag.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUi) {
                updateConfig(config.copy(secureFlag = isChecked), applyToWindow = true)
            }
        }

        b.btnDockTopLeft.setOnClickListener { dockWindowTo(DockCorner.TOP_LEFT) }
        b.btnDockTopRight.setOnClickListener { dockWindowTo(DockCorner.TOP_RIGHT) }
        b.btnDockBottomLeft.setOnClickListener { dockWindowTo(DockCorner.BOTTOM_LEFT) }
        b.btnDockBottomRight.setOnClickListener { dockWindowTo(DockCorner.BOTTOM_RIGHT) }
    }

    private fun dockWindowTo(corner: DockCorner) {
        if (!PopupWindowManager.isCustomWindowShowing()) {
            openOrUpdateWindow()
        }
        PopupWindowManager.snapCustomWindowToCorner(corner)
    }

    private fun bindWindowCallbacks() {
        PopupWindowManager.setCustomWindowListeners(
            onStateChanged = { updated ->
                config = updated
                refreshUi()
            },
            onDismissed = {
                refreshUi()
            }
        )
    }

    private fun openOrUpdateWindow() {
        val opened = PopupWindowManager.showCustomWindow(
            config = config,
            onStateChanged = { updated ->
                config = updated
                refreshUi()
            },
            onDismissed = {
                refreshUi()
            }
        )
        if (!opened) {
            showToast("懸浮視窗開啟失敗，請確認權限或切換為「應用內懸浮」")
        }
        refreshUi()
    }

    private fun updateConfig(newConfig: WindowCustomConfig, applyToWindow: Boolean) {
        config = newConfig
        WindowConfigStore.save(config)
        if (applyToWindow && PopupWindowManager.isCustomWindowShowing()) {
            PopupWindowManager.updateCustomWindow(config)
        }
        refreshUi()
    }

    private fun hasOverlayPermission(): Boolean {
        val ctx = context ?: return false
        return Settings.canDrawOverlays(ctx)
    }

    private fun requestOverlayPermission(launchAfter: Boolean) {
        val act = activity ?: return
        launchOverlayAfterPermission = launchAfter
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${act.packageName}")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun refreshUi() {
        val b = getBinding() ?: return
        val isShowing = PopupWindowManager.isCustomWindowShowing()
        val overlayGranted = hasOverlayPermission()

        // Status Badge
        b.tvWindowStatusBadge.text = when {
            isShowing && config.mode == WindowMode.SYSTEM_OVERLAY -> "全局顯示中"
            isShowing -> "應用內顯示中"
            else -> "未啟動"
        }
        b.tvWindowStatusBadge.setTextColor(
            if (isShowing) 0xFF019889.toInt() else 0xFF607D8B.toInt()
        )

        // Overlay Permission Status
        if (overlayGranted) {
            b.tvPermissionStatus.text = "SYSTEM_ALERT_WINDOW：已授權，可使用全局跨應用懸浮"
            b.tvPermissionStatus.setTextColor(0xFF019889.toInt())
            b.btnGrantOverlay.text = "權限已開"
        } else {
            b.tvPermissionStatus.text = "SYSTEM_ALERT_WINDOW：未授權（可免權限使用應用內懸浮）"
            b.tvPermissionStatus.setTextColor(0xFFEF6C00.toInt())
            b.btnGrantOverlay.text = "前往授權"
        }

        // Toggle Window CTA
        if (isShowing) {
            b.btnToggleWindow.text = "關閉懸浮視窗"
            b.btnToggleWindow.setBackgroundResource(R.drawable.bg_btn_danger_pill)
        } else {
            b.btnToggleWindow.text = "開啟懸浮視窗"
            b.btnToggleWindow.setBackgroundResource(R.drawable.bg_btn_primary)
        }

        // Mode & Preset Selections
        b.btnModeInApp.isSelected = config.mode == WindowMode.IN_APP
        b.btnModeSystemOverlay.isSelected = config.mode == WindowMode.SYSTEM_OVERLAY

        b.btnSizeCompact.isSelected = config.sizePreset == WindowSizePreset.COMPACT
        b.btnSizeMedium.isSelected = config.sizePreset == WindowSizePreset.MEDIUM
        b.btnSizeLarge.isSelected = config.sizePreset == WindowSizePreset.LARGE

        b.btnThemeDark.isSelected = config.themePreset == WindowThemePreset.DARK
        b.btnThemeTeal.isSelected = config.themePreset == WindowThemePreset.TEAL
        b.btnThemeLight.isSelected = config.themePreset == WindowThemePreset.LIGHT

        b.tvAlphaValue.text = "${config.alphaPercent}%"
        b.tvRadiusValue.text = "${config.cornerRadiusDp} dp"

        isUpdatingUi = true
        b.sbAlpha.progress = (config.alphaPercent - 40).coerceIn(0, 60)
        b.sbCornerRadius.progress = (config.cornerRadiusDp - 8).coerceIn(0, 20)
        b.switchAutoSnap.isChecked = config.autoSnapEdge
        b.switchLockPosition.isChecked = config.lockPosition
        b.switchCollapsed.isChecked = config.collapsed
        b.switchKeepScreenOn.isChecked = config.keepScreenOn
        b.switchSecureFlag.isChecked = config.secureFlag
        isUpdatingUi = false

        // Telemetry
        val lp = PopupWindowManager.mediaMenuWindow?.getCurrentLayoutParams()
        val windowTypeName = if (config.mode == WindowMode.SYSTEM_OVERLAY) {
            "TYPE_APPLICATION_OVERLAY"
        } else {
            "TYPE_APPLICATION"
        }
        val coordText = if (lp != null && isShowing) {
            "X=${lp.x} px, Y=${lp.y} px"
        } else if (config.posX >= 0 && config.posY >= 0) {
            "已記憶 X=${config.posX} px, Y=${config.posY} px"
        } else {
            "預設右上停靠"
        }
        val flagsHex = lp?.let { String.format(Locale.US, "0x%08X", it.flags) } ?: "未附加"

        b.tvWindowTelemetry.text = String.format(
            Locale.US,
            "• 視窗運行狀態：%s\n" +
                "• Window Type：%s\n" +
                "• 視窗座標 (Gravity.TOP|START)：%s\n" +
                "• 尺寸與樣式：%s (%d dp) / %s / 圓角 %d dp / 透明度 %d%%\n" +
                "• 狀態旗標 (Flags)：%s",
            if (isShowing) "顯示中" else "未啟動",
            windowTypeName,
            coordText,
            config.sizePreset.label,
            config.sizePreset.widthDp,
            config.themePreset.label,
            config.cornerRadiusDp,
            config.alphaPercent,
            flagsHex
        )
    }
}

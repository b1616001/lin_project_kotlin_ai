package com.example.lin_new_project_kotlin_ai.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.MainActivity
import com.example.lin_new_project_kotlin_ai.databinding.FragmentWindowCustomBinding
import com.example.lin_new_project_kotlin_ai.popupwindow.PopupWindowManager
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment

class WindowCustomFragment : BaseBindingFragment<FragmentWindowCustomBinding?>() {

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentWindowCustomBinding {
        return FragmentWindowCustomBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun initView() {
        val b = getBinding() ?: return

        b.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        b.btnOpenWindow.setOnClickListener {
            if (hasOverlayPermission()) {
                PopupWindowManager.showMediaMenu(true)
            } else {
                showToast("請先授權懸浮視窗權限")
                val act = MainActivity.mainActivity
                if (act != null) {
                    act.requestAlertWindowPermission()
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)
                }
            }
        }

        b.btnCloseWindow.setOnClickListener {
            PopupWindowManager.showMediaMenu(false)
        }
    }

    private fun hasOverlayPermission(): Boolean {
        val ctx = context ?: return false
        val act = MainActivity.mainActivity
        if (act != null) {
            return act.commonROMPermissionCheck(ctx)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(ctx)
        } else {
            true
        }
    }

    private fun updatePermissionStatus() {
        val b = getBinding() ?: return
        if (hasOverlayPermission()) {
            b.tvPermissionStatus.text = "懸浮視窗權限：已授權"
            b.tvPermissionStatus.setTextColor(0xFF019889.toInt())
        } else {
            b.tvPermissionStatus.text = "懸浮視窗權限：未授權（點擊開啟視窗將引導授權）"
            b.tvPermissionStatus.setTextColor(0xFFEF6C00.toInt())
        }
    }
}

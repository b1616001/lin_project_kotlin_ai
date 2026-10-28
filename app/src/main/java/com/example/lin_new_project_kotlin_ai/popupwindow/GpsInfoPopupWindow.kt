package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.view.LayoutInflater
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowGpsInfoBinding
import com.example.lin_new_project_kotlin_ai.`fun`.GPSPositioningMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow
import java.util.Locale

class GpsInfoPopupWindow(context: Context) : BasePopupWindow<PopupWindowGpsInfoBinding?>(
    context,
    300,
    1,
    330,
    General.FADE,
    true,
    true,
    false,
    true
) {
    private var gpsMethod: GPSPositioningMethod? = null
    private var lastLocation: Location? = null

    init {
        initView()
        startLocation()
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowGpsInfoBinding {
        return PopupWindowGpsInfoBinding.inflate(layoutInflater)
    }

    private fun initView() {
        val b = binding ?: return
        b.imgClosed.setOnClickListener { dismiss() }

        b.btnRefreshGps.setOnClickListener {
            b.tvGpsStatus.text = "正在重新定位..."
            b.tvLatitude.text = "緯度 (Lat): 取得中..."
            b.tvLongitude.text = "經度 (Lng): 取得中..."
            b.tvAddress.text = "地址: 重新解析中..."
            startLocation()
        }

        b.btnCopyCoordinates.setOnClickListener {
            val loc = lastLocation
            if (loc != null) {
                val clipText = "${loc.latitude}, ${loc.longitude}"
                val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("GPS", clipText))
                Toast.makeText(mContext, "已複製座標: $clipText", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, "尚未取得定位座標", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocation() {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        gpsMethod?.stopGPS()
        gpsMethod = GPSPositioningMethod(lm, mContext).apply {
            startGPS(object : GPSPositioningMethod.OnLocationChangeListener {
                override fun onLocationChanged(location: Location, address: String) {
                    lastLocation = location
                    updateUI(location, address)
                }

                override fun onLocationFailed(errorMsg: String) {
                    val b = binding ?: return
                    b.tvGpsStatus.text = "定位失敗"
                    b.tvLatitude.text = "緯度: 無法取得"
                    b.tvLongitude.text = "經度: 無法取得"
                    b.tvAddress.text = "訊息: $errorMsg"
                }
            })
        }
    }

    private fun updateUI(location: Location, address: String) {
        val b = binding ?: return
        b.tvGpsStatus.text = "定位成功 (即時更新)"
        b.tvLatitude.text = String.format(Locale.US, "緯度 (Lat): %.6f", location.latitude)
        b.tvLongitude.text = String.format(Locale.US, "經度 (Lng): %.6f", location.longitude)

        val acc = if (location.hasAccuracy()) String.format(Locale.US, "±%.1f 公尺", location.accuracy) else "未知"
        b.tvAccuracy.text = "精準度: $acc"

        val providerName = when (location.provider) {
            LocationManager.GPS_PROVIDER -> "GPS 衛星 (精確)"
            LocationManager.NETWORK_PROVIDER -> "網路基地台 / Wi-Fi"
            else -> location.provider ?: "未知提供者"
        }
        b.tvProvider.text = "定位來源: $providerName"

        val alt = if (location.hasAltitude()) String.format(Locale.US, "%.1fm", location.altitude) else "-"
        val speed = if (location.hasSpeed()) String.format(Locale.US, "%.1f km/h", location.speed * 3.6f) else "0 km/h"
        b.tvAltitudeSpeed.text = "海拔: $alt | 速度: $speed"

        b.tvAddress.text = "地址: $address"
    }

    override fun dismiss() {
        gpsMethod?.stopGPS()
        super.dismiss()
    }
}

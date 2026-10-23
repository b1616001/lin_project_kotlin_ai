package com.example.lin_new_project_kotlin_ai.`fun`

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.`fun`.ImageMethod.Companion.getDrawableByVERSION

object ToastMethod {
    private var toastStart: Toast? = null

    fun showToast(context: Context, msg: String?, duration: Int) {
        val message = TextView(context)
        message.setText(msg)
        message.setTextColor(Color.WHITE)
        message.setBackground(getDrawableByVERSION(context, R.drawable.btn_bgd_blue))
        message.setTextSize(20f)
        message.setGravity(Gravity.CENTER)
        if (toastStart != null) {
            toastStart!!.cancel()
        }

        toastStart = Toast(context)
        toastStart!!.setDuration(duration)
        toastStart!!.setView(message)
        toastStart!!.show()
    }

    @JvmStatic
	fun showToast(context: Context?, msg: String?) {
        val message = TextView(context)
        message.setText(msg)
        message.setTextColor(Color.WHITE)
        message.setBackground(getDrawableByVERSION(context, R.drawable.btn_bgd_blue))
        message.setTextSize(20f)
        message.setGravity(Gravity.CENTER)
        if (toastStart != null) {
            toastStart!!.cancel()
        }
        toastStart = Toast(context)
        toastStart!!.setDuration(Toast.LENGTH_LONG)
        toastStart!!.setView(message)
        toastStart!!.show()
    }
}

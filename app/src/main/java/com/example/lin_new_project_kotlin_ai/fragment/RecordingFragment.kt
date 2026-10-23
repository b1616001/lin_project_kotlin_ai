package com.example.lin_new_project_kotlin_ai.fragment

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentRecordingBinding
import com.example.lin_new_project_kotlin_ai.`fun`.AndroidQStorageSaveUtils
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.io.File
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask


class RecordingFragment : BaseBindingFragment<FragmentRecordingBinding?>() {
    var int_h: Int = 0
    var int_m: Int = 0
    var int_s: Int = 0
    var timer: Timer? = null
    var tTask: TimerTask? = null
    private var recorder: MediaRecorder? = null
    var flagstrat: Boolean = true
    private var uri: Uri? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentRecordingBinding {
        return FragmentRecordingBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getBinding()!!.checkbox.setOnClickListener { v: View? ->
            if (getBinding()!!.checkbox.isChecked()) {
                getBinding()!!.checkbox.setBackground(CommonUtils.getDrawableByVERSION(R.drawable.icon_green))
                getBinding()!!.tvTime.setVisibility(View.VISIBLE)
                getBinding()!!.tvTime.setText("00:00:00")
                int_h = 0
                int_m = 0
                int_s = 0
                timer = Timer()
                tTask = this.timerTask
                timer!!.schedule(tTask, 0, 1000) //每一秒執行一次，零秒後開始執行
                setMediaRecorder()
            } else {
                if (recorder != null) {
                    recorder!!.stop()
                    recorder!!.release()
                    recorder = null
                }
                getBinding()!!.checkbox.setBackground(CommonUtils.getDrawableByVERSION(R.drawable.icon_red))
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                if (tTask != null) {
                    tTask!!.cancel()
                    tTask = null
                }
                getBinding()!!.tvTime.setVisibility(View.GONE)
            }
        }
        getBinding()!!.btnPlay.setOnClickListener { view: View? ->
            if (uri == null) {
                ToastMethod.showToast(requireContext(), "無錄音")
                return@setOnClickListener
            }
            try {
                val m = MediaPlayer()
                m.setDataSource(requireContext(), uri!!)
                m.prepare()
                m.start()
            } catch (e: Exception) {
            }
        }
    }

    private fun setMediaRecorder() {
        recorder = MediaRecorder() // new出MediaRecorder物件
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        // 設定MediaRecorder的音訊源為麥克風
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        // 設定MediaRecorder錄製的音訊格式
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        uri = AndroidQStorageSaveUtils.getAudioUri(
            requireContext(),
            "3gp", Environment.DIRECTORY_MOVIES, "Lin"
        )
        //       String fileName = AndroidQStorageSaveUtils.Uri_to_path(getContext(), uri);//公有路徑轉換
        try {
            val fileName = uri!!.getPath() //公有路徑轉換
            val fileDescriptor: FileDescriptor? =
                AndroidQStorageSaveUtils.getFileDescriptor(requireContext(), uri!!, "w")
            if (fileDescriptor == null) return
            recorder!!.setOutputFile(fileDescriptor)
            setLog("錄音路徑:" + fileName)

            recorder!!.prepare()
            recorder!!.start()
        } catch (e: Exception) {
            setLog("錄音異常:" + e)
        }
    }

    val timerTask: TimerTask
        /** storage/emulated/0/Music/Lin/sample_1778203604610.mp3 */
        get() = object : TimerTask() {
            override fun run() {
                //每秒要執行的程式
                if (flagstrat) {
                    val message = Message()
                    if (int_s < 60) {
                        int_s++
                    } else {
                        int_s = 0
                        int_m++
                        if (int_m == 60) {
                            int_m = 0
                            int_h++
                        }
                    }
                    message.what = 1
                    handler.sendMessage(message)
                }
            }
        }

    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    var sec = ""
                    var min = ""
                    var hour = ""
                    if (int_s < 10) {
                        sec = "0" + int_s //秒數小於10補個零
                    } else {
                        sec = int_s.toString()
                    }
                    if (int_m < 10) {
                        min = "0" + int_m.toString() //分數小於10補個零
                    } else {
                        min = int_m.toString() //
                    }
                    if (int_h < 10) {
                        hour = "0" + int_h //時數小於10補個零
                    } else {
                        hour = int_h.toString() //用textview顯示時數
                    }
                    getBinding()!!.tvTime.setText(hour + ":" + min + ":" + sec)
                }

                else -> {}
            }
        }
    }

    private fun customizedFilePath(): String? {
        val mediaStorageDir = File(
            Environment
                .getExternalStorageDirectory(),
            "Recording"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val sf = SimpleDateFormat("yyyyMMddHHmmss")
        val filename = (mediaStorageDir.getPath() + File.separator
                + sf.format(Date()) + ".3gp")
        val cameraFile = File(filename)
        Log.d("cameraFilepath", cameraFile.getAbsolutePath())
        return cameraFile.getAbsolutePath()
    }
}
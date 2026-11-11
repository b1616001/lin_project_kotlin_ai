package com.example.lin_new_project_kotlin_ai.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentRecordingBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.random.Random

class RecordingFragment : BaseBindingFragment<FragmentRecordingBinding?>() {

    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var isPlaying = false
    private var recordedAudioFile: File? = null
    private var uri: Uri? = null

    private var recordDurationMillis = 0L
    private val mainHandler = Handler(Looper.getMainLooper())

    private val recordTimerRunnable = object : Runnable {
        override fun run() {
            if (!isRecording) return
            recordDurationMillis += 100
            updateTimerDisplay(recordDurationMillis)
            updateAmplitudeVisualizer()
            mainHandler.postDelayed(this, 100)
        }
    }

    private val playbackProgressRunnable = object : Runnable {
        override fun run() {
            val player = mediaPlayer
            if (player != null && isPlaying) {
                try {
                    val currentPos = player.currentPosition
                    val totalDuration = player.duration
                    val binding = getBinding()
                    if (binding != null && totalDuration > 0) {
                        binding.seekBarAudio.progress = (currentPos * 100) / totalDuration
                        binding.tvProgressCurrent.text = formatTime(currentPos.toLong())
                    }
                } catch (e: Exception) {
                    // Ignore transient exceptions during seek/stop
                }
                mainHandler.postDelayed(this, 200)
            }
        }
    }

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val binding = getBinding() ?: return@registerForActivityResult
        if (isGranted) {
            startRecording()
        } else {
            binding.checkbox.isChecked = false
            ToastMethod.showToast(requireContext(), "請允許麥克風權限以進行語音錄製")
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentRecordingBinding {
        return FragmentRecordingBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        // Navigation back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Circular record button toggle
        binding.checkbox.setOnClickListener {
            if (binding.checkbox.isChecked) {
                checkPermissionAndStart()
            } else {
                stopRecording()
            }
        }

        // Playback button (Play/Pause)
        binding.btnPlay.setOnClickListener {
            togglePlayback()
        }

        // Replay button
        binding.btnReplay.setOnClickListener {
            replayAudio()
        }

        // Delete audio button
        binding.btnDeleteCurrent.setOnClickListener {
            deleteRecordedAudio()
        }

        // SeekBar user scrubbing
        binding.seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    val total = mediaPlayer?.duration ?: 0
                    if (total > 0) {
                        val newPos = (total * progress) / 100
                        binding.tvProgressCurrent.text = formatTime(newPos.toLong())
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { sb ->
                    val player = mediaPlayer ?: return@let
                    val total = player.duration
                    if (total > 0) {
                        val seekTarget = (total * sb.progress) / 100
                        player.seekTo(seekTarget)
                    }
                }
            }
        })

        // Check if there's any existing recent recording to preload
        preloadRecentRecording()
    }

    private fun checkPermissionAndStart() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            getBinding()?.checkbox?.isChecked = false
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        val binding = getBinding() ?: return
        val context = requireContext()

        // Stop any current playback
        stopPlayback()

        // Prepare destination directory
        val recordingsDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: context.filesDir,
            "Recordings"
        )
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(recordingsDir, "REC_${timestamp}.m4a")
        recordedAudioFile = outputFile
        uri = Uri.fromFile(outputFile)

        try {
            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioEncodingBitRate(128000)
            rec.setAudioSamplingRate(44100)
            rec.setOutputFile(outputFile.absolutePath)
            rec.prepare()
            rec.start()

            recorder = rec
            isRecording = true
            recordDurationMillis = 0L

            // Update UI
            binding.checkbox.isChecked = true
            binding.checkbox.setBackgroundResource(R.drawable.bg_record_button_recording)
            binding.ivRecordIcon.setImageResource(R.drawable.ic_stop)
            binding.tvRecordHint.text = "錄音中，點擊以停止"
            binding.tvStatusBadge.text = "錄音中 (REC)"
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            binding.cardPlayback.alpha = 0.5f

            mainHandler.post(recordTimerRunnable)
            ToastMethod.showToast(context, "開始錄音...")
        } catch (e: Exception) {
            setLog("錄音初始化失敗: $e")
            ToastMethod.showToast(context, "錄音啟動失敗: ${e.message}")
            binding.checkbox.isChecked = false
            binding.checkbox.setBackgroundResource(R.drawable.bg_record_button_idle)
            binding.ivRecordIcon.setImageResource(R.drawable.ic_mic)
        }
    }

    private fun stopRecording() {
        val binding = getBinding() ?: return
        val context = requireContext()

        if (isRecording) {
            try {
                recorder?.stop()
            } catch (e: Exception) {
                setLog("停止錄音異常: $e")
            } finally {
                recorder?.release()
                recorder = null
                isRecording = false
            }
        }

        mainHandler.removeCallbacks(recordTimerRunnable)

        // Reset waveform bars
        resetWaveformBars()

        // Update UI
        binding.checkbox.isChecked = false
        binding.checkbox.setBackgroundResource(R.drawable.bg_record_button_idle)
        binding.ivRecordIcon.setImageResource(R.drawable.ic_mic)
        binding.tvRecordHint.text = "點擊開始錄音"
        binding.tvStatusBadge.text = "錄音完成"
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.cardPlayback.alpha = 1.0f

        val file = recordedAudioFile
        if (file != null && file.exists() && file.length() > 0) {
            binding.tvAudioTitle.text = file.name
            val sizeKb = file.length() / 1024
            binding.tvAudioInfo.text = "格式: AAC / M4A | 大小: ${sizeKb} KB | 時長: ${formatTime(recordDurationMillis)}"
            binding.tvProgressCurrent.text = "00:00"
            binding.tvProgressTotal.text = formatTime(recordDurationMillis)
            binding.seekBarAudio.progress = 0
            ToastMethod.showToast(context, "錄音已儲存 (${formatTime(recordDurationMillis)})")
        } else {
            binding.tvAudioInfo.text = "錄音時間過短或檔案無效"
        }
    }

    private fun togglePlayback() {
        val context = requireContext()
        val file = recordedAudioFile
        if (file == null || !file.exists()) {
            ToastMethod.showToast(context, "尚未有錄音檔，請先錄音")
            return
        }

        val binding = getBinding() ?: return

        if (isPlaying) {
            // Pause
            try {
                mediaPlayer?.pause()
                isPlaying = false
                binding.btnPlay.text = "繼續播放"
                binding.btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0)
                binding.tvStatusBadge.text = "播放暫停"
                mainHandler.removeCallbacks(playbackProgressRunnable)
            } catch (e: Exception) {
                setLog("暫停失敗: $e")
            }
        } else {
            // Start or Resume
            try {
                if (mediaPlayer == null) {
                    val player = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                    mediaPlayer = player
                    binding.tvProgressTotal.text = formatTime(player.duration.toLong())
                }

                mediaPlayer?.start()
                isPlaying = true
                binding.btnPlay.text = "暫停播放"
                binding.btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0)
                binding.tvStatusBadge.text = "播放中"
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                mainHandler.post(playbackProgressRunnable)
            } catch (e: Exception) {
                setLog("播放音訊失敗: $e")
                ToastMethod.showToast(context, "播放失敗: ${e.message}")
            }
        }
    }

    private fun replayAudio() {
        val file = recordedAudioFile
        if (file == null || !file.exists()) return

        stopPlayback()
        togglePlayback()
    }

    private fun stopPlayback() {
        val binding = getBinding()
        isPlaying = false
        mainHandler.removeCallbacks(playbackProgressRunnable)

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
        }

        binding?.let { b ->
            b.btnPlay.text = "播放錄音"
            b.btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0)
            b.seekBarAudio.progress = 0
            b.tvProgressCurrent.text = "00:00"
            if (!isRecording) {
                b.tvStatusBadge.text = "待命中"
                b.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            }
        }
    }

    private fun deleteRecordedAudio() {
        val context = requireContext()
        val file = recordedAudioFile
        if (file == null || !file.exists()) {
            ToastMethod.showToast(context, "目前沒有可刪除的錄音檔")
            return
        }

        stopPlayback()
        file.delete()
        recordedAudioFile = null
        uri = null

        val binding = getBinding() ?: return
        binding.tvAudioTitle.text = "目前無錄音檔"
        binding.tvAudioInfo.text = "格式: AAC / M4A | 尚未有錄音"
        binding.tvProgressCurrent.text = "00:00"
        binding.tvProgressTotal.text = "00:00"
        binding.seekBarAudio.progress = 0
        ToastMethod.showToast(context, "已刪除錄音檔")
    }

    private fun preloadRecentRecording() {
        val context = requireContext()
        val recordingsDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: context.filesDir,
            "Recordings"
        )
        if (recordingsDir.exists()) {
            val files = recordingsDir.listFiles { f -> f.extension.equals("m4a", true) || f.extension.equals("3gp", true) }
            val latest = files?.maxByOrNull { it.lastModified() }
            if (latest != null && latest.length() > 0) {
                recordedAudioFile = latest
                uri = Uri.fromFile(latest)

                val binding = getBinding() ?: return
                binding.tvAudioTitle.text = latest.name
                val sizeKb = latest.length() / 1024
                binding.tvAudioInfo.text = "歷史錄音 | 大小: ${sizeKb} KB | 點擊播放"
            }
        }
    }

    private fun updateTimerDisplay(millis: Long) {
        val binding = getBinding() ?: return
        binding.tvTime.text = formatDigitalClock(millis)
    }

    private fun updateAmplitudeVisualizer() {
        val binding = getBinding() ?: return
        val rec = recorder ?: return

        val maxAmp = try {
            rec.maxAmplitude
        } catch (e: Exception) {
            0
        }

        val db = if (maxAmp > 0) {
            (20 * log10(maxAmp.toDouble())).toInt().coerceIn(10, 95)
        } else {
            Random.nextInt(15, 25)
        }

        binding.tvDecibel.text = "即時音量: $db dB"

        // Animate the 9 waveform bars based on amplitude
        val normalized = (db - 15).coerceAtLeast(0) / 80f // 0.0 to 1.0
        val baseDp = 8
        val maxDp = 44

        val bars = listOf(
            binding.waveBar1, binding.waveBar2, binding.waveBar3,
            binding.waveBar4, binding.waveBar5, binding.waveBar6,
            binding.waveBar7, binding.waveBar8, binding.waveBar9
        )

        val multipliers = listOf(0.4f, 0.65f, 0.85f, 0.95f, 1.0f, 0.95f, 0.85f, 0.65f, 0.4f)
        val density = resources.displayMetrics.density

        for (i in bars.indices) {
            val bar = bars[i]
            val jitter = Random.nextFloat() * 0.25f - 0.12f
            val factor = (normalized * multipliers[i] + jitter).coerceIn(0.1f, 1.0f)
            val heightDp = baseDp + ((maxDp - baseDp) * factor).toInt()
            val layoutParams = bar.layoutParams
            layoutParams.height = (heightDp * density).toInt()
            bar.layoutParams = layoutParams
        }
    }

    private fun resetWaveformBars() {
        val binding = getBinding() ?: return
        val bars = listOf(
            binding.waveBar1, binding.waveBar2, binding.waveBar3,
            binding.waveBar4, binding.waveBar5, binding.waveBar6,
            binding.waveBar7, binding.waveBar8, binding.waveBar9
        )
        val defaultHeights = listOf(8, 14, 22, 32, 42, 32, 22, 14, 8)
        val density = resources.displayMetrics.density

        for (i in bars.indices) {
            val bar = bars[i]
            val params = bar.layoutParams
            params.height = (defaultHeights[i] * density).toInt()
            bar.layoutParams = params
        }
        binding.tvDecibel.text = "準備錄音 (點擊下方麥克風)"
    }

    private fun formatDigitalClock(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatTime(millis: Long): String {
        val totalSecs = (millis / 1000).coerceAtLeast(0)
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            togglePlayback()
        }
    }

    override fun onDestroyView() {
        if (isRecording) {
            stopRecording()
        }
        stopPlayback()
        super.onDestroyView()
    }
}
package com.example.lin_new_project_kotlin_ai.fragment

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentVolumeBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale
import kotlin.math.roundToInt

class VolumetFragment : BaseBindingFragment<FragmentVolumeBinding?>() {

    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeTestStream: Int? = null

    private var vibrateDurationMs: Long = 3000L
    private var vibrateAmplitude: Int = 255
    private var isVibrating: Boolean = false
    private var isReceiverRegistered: Boolean = false
    private val preMuteVolumes = mutableMapOf<Int, Int>()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoStopPreviewRunnable = Runnable { stopAudioPreview() }
    private val vibrationCompleteRunnable = Runnable {
        isVibrating = false
        updateVibrateButtonText()
    }

    private val volumeAndRingerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshAllAudioUi()
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentVolumeBinding {
        return FragmentVolumeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = resolveVibrator(context)

        initHeaderAndNavigation()
        initRingerAndPresetControls()
        initStreamControls()
        initVibrationControls()
        refreshAllAudioUi()
    }

    override fun onStart() {
        super.onStart()
        registerSystemAudioReceiver()
        refreshAllAudioUi()
    }

    override fun onStop() {
        unregisterSystemAudioReceiver()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        stopAudioPreview()
    }

    override fun onDestroyView() {
        mainHandler.removeCallbacks(autoStopPreviewRunnable)
        mainHandler.removeCallbacks(vibrationCompleteRunnable)
        stopAudioPreview()
        cancelVibration(showFeedback = false)
        super.onDestroyView()
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun registerSystemAudioReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(
            requireContext(),
            volumeAndRingerReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        isReceiverRegistered = true
    }

    private fun unregisterSystemAudioReceiver() {
        if (!isReceiverRegistered) return
        try {
            requireContext().unregisterReceiver(volumeAndRingerReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver already unregistered
        } finally {
            isReceiverRegistered = false
        }
    }

    private fun initHeaderAndNavigation() {
        val binding = getBinding() ?: return
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initRingerAndPresetControls() {
        val binding = getBinding() ?: return

        binding.btnModeNormal.setOnClickListener {
            setRingerModeSafely(AudioManager.RINGER_MODE_NORMAL, "已切換為響鈴模式")
        }
        binding.btnModeVibrate.setOnClickListener {
            setRingerModeSafely(AudioManager.RINGER_MODE_VIBRATE, "已切換為僅震動模式")
        }
        binding.btnModeSilent.setOnClickListener {
            setRingerModeSafely(AudioManager.RINGER_MODE_SILENT, "已切換為靜音模式")
        }

        binding.btnPresetMuteAll.setOnClickListener {
            setStreamVolumeSafely(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
            setStreamVolumeSafely(AudioManager.STREAM_ALARM, 0, 0)
            setRingerModeSafely(AudioManager.RINGER_MODE_VIBRATE, "已套用全靜音 / 僅震動預設")
            refreshAllAudioUi()
        }

        binding.btnPresetMeeting.setOnClickListener {
            val am = audioManager ?: return@setOnClickListener
            val musicTarget = (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.2f).roundToInt().coerceAtLeast(1)
            setStreamVolumeSafely(AudioManager.STREAM_MUSIC, musicTarget, 0)
            setRingerModeSafely(AudioManager.RINGER_MODE_VIBRATE, "已套用會議模式（低媒體音量 + 震動）")
            refreshAllAudioUi()
        }

        binding.btnPresetOutdoor.setOnClickListener {
            val am = audioManager ?: return@setOnClickListener
            setRingerModeSafely(AudioManager.RINGER_MODE_NORMAL, null)
            setStreamVolumeSafely(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
            setStreamVolumeSafely(
                AudioManager.STREAM_RING,
                am.getStreamMaxVolume(AudioManager.STREAM_RING),
                0
            )
            setStreamVolumeSafely(
                AudioManager.STREAM_ALARM,
                am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                AudioManager.FLAG_PLAY_SOUND
            )
            refreshAllAudioUi()
            showToast("已套用戶外最大音量")
        }
    }

    private fun initStreamControls() {
        val binding = getBinding() ?: return

        setupStreamSection(
            streamType = AudioManager.STREAM_MUSIC,
            seekBar = binding.sbMusic,
            btnMute = binding.btnMusicMute,
            btnLower = binding.btnMusicLower,
            btnRaise = binding.btnMusicAdjust,
            btnMax = binding.btnMusicMax,
            btnTest = binding.btnMusicTest,
            playMaxSoundFlag = AudioManager.FLAG_PLAY_SOUND
        )

        setupStreamSection(
            streamType = AudioManager.STREAM_RING,
            seekBar = binding.sbRing,
            btnMute = binding.btnRingMute,
            btnLower = binding.btnRingLower,
            btnRaise = binding.btnRingAdjust,
            btnMax = binding.btnRingMax,
            btnTest = binding.btnRingTest,
            playMaxSoundFlag = AudioManager.FLAG_PLAY_SOUND
        )

        setupStreamSection(
            streamType = AudioManager.STREAM_ALARM,
            seekBar = binding.sbAlarm,
            btnMute = binding.btnAlarmMute,
            btnLower = binding.btnAlarmLower,
            btnRaise = binding.btnAlarmAdjust,
            btnMax = binding.btnAlarmMax,
            btnTest = binding.btnAlarmTest,
            playMaxSoundFlag = AudioManager.FLAG_SHOW_UI
        )
    }

    private fun setupStreamSection(
        streamType: Int,
        seekBar: SeekBar,
        btnMute: ImageButton,
        btnLower: View,
        btnRaise: View,
        btnMax: View,
        btnTest: View,
        playMaxSoundFlag: Int
    ) {
        val am = audioManager ?: return
        val maxVol = am.getStreamMaxVolume(streamType).coerceAtLeast(1)
        seekBar.max = maxVol

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setStreamVolumeSafely(streamType, progress, 0)
                    updateSingleStreamLabel(streamType)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {
                refreshAllAudioUi()
            }
        })

        btnMute.setOnClickListener {
            val current = am.getStreamVolume(streamType)
            if (current > 0) {
                preMuteVolumes[streamType] = current
                setStreamVolumeSafely(streamType, 0, AudioManager.FLAG_SHOW_UI)
            } else {
                val restored = preMuteVolumes[streamType]
                    ?.takeIf { it in 1..maxVol }
                    ?: (maxVol * 0.5f).roundToInt().coerceAtLeast(1)
                setStreamVolumeSafely(streamType, restored, AudioManager.FLAG_SHOW_UI)
            }
            refreshAllAudioUi()
        }

        btnLower.setOnClickListener {
            adjustStreamVolumeSafely(streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            refreshAllAudioUi()
        }

        btnRaise.setOnClickListener {
            adjustStreamVolumeSafely(streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            refreshAllAudioUi()
        }

        btnMax.setOnClickListener {
            setStreamVolumeSafely(
                streamType,
                am.getStreamMaxVolume(streamType),
                playMaxSoundFlag
            )
            refreshAllAudioUi()
        }

        btnTest.setOnClickListener {
            if (activeTestStream == streamType) {
                stopAudioPreview()
            } else {
                playAudioPreview(streamType)
            }
        }
    }

    private fun initVibrationControls() {
        val binding = getBinding() ?: return
        val vib = vibrator
        val hasVib = vib?.hasVibrator() == true
        val hasAmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib?.hasAmplitudeControl() == true
        } else {
            false
        }

        binding.tvVibratorStatus.text = when {
            !hasVib -> "裝置狀態: 未偵測到震動馬達"
            hasAmp -> "裝置狀態: 就緒 (支援無段振幅控制)"
            else -> "裝置狀態: 就緒 (標準開關震動)"
        }

        binding.sbVibrateAmplitude.isEnabled = hasVib && hasAmp
        if (!hasAmp) {
            binding.tvVibrateAmplitude.text = if (hasVib) {
                "震動強度: 系統預設 (此裝置不支援自訂振幅)"
            } else {
                "震動強度: 無法使用"
            }
            binding.sbVibrateAmplitude.alpha = 0.45f
        }

        binding.sbVibrateDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vibrateDurationMs = (progress + 2) * 100L // 200ms .. 5000ms
                val seconds = vibrateDurationMs / 1000.0
                binding.tvVibrateDuration.text =
                    String.format(Locale.US, "持續震動時長: %.1f 秒 (%d ms)", seconds, vibrateDurationMs)
                updateVibrateButtonText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbVibrateAmplitude.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vibrateAmplitude = (progress + 1).coerceIn(1, 255)
                val percent = (vibrateAmplitude * 100f / 255f).roundToInt()
                binding.tvVibrateAmplitude.text = "震動強度: $percent% ($vibrateAmplitude)"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnVibrateShort.setOnClickListener {
            triggerOneShotVibration(120L)
        }

        binding.btnVibratePulse.setOnClickListener {
            triggerPatternVibration(longArrayOf(0, 140, 120, 140), "雙脈衝震動")
        }

        binding.btnVibrateHeartbeat.setOnClickListener {
            triggerPatternVibration(longArrayOf(0, 100, 110, 220, 500, 100, 110, 220), "心跳節奏震動")
        }

        binding.btnVibrateSos.setOnClickListener {
            // SOS: ... --- ...
            val sos = longArrayOf(
                0,
                120, 100, 120, 100, 120, 250,
                320, 100, 320, 100, 320, 250,
                120, 100, 120, 100, 120
            )
            triggerPatternVibration(sos, "SOS 摩斯密碼震動")
        }

        binding.btnVibrate.setOnClickListener {
            if (isVibrating) {
                cancelVibration(showFeedback = true)
            } else {
                triggerOneShotVibration(vibrateDurationMs)
            }
        }

        binding.btnVibrateStop.setOnClickListener {
            cancelVibration(showFeedback = true)
        }

        updateVibrateButtonText()
    }

    private fun triggerOneShotVibration(durationMs: Long) {
        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            showToast("此裝置不支援震動功能")
            return
        }

        mainHandler.removeCallbacks(vibrationCompleteRunnable)
        vib.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amp = if (vib.hasAmplitudeControl()) vibrateAmplitude else VibrationEffect.DEFAULT_AMPLITUDE
            vib.vibrate(VibrationEffect.createOneShot(durationMs, amp))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(durationMs)
        }

        isVibrating = true
        updateVibrateButtonText()
        mainHandler.postDelayed(vibrationCompleteRunnable, durationMs)
    }

    private fun triggerPatternVibration(timings: LongArray, label: String) {
        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            showToast("此裝置不支援震動功能")
            return
        }

        mainHandler.removeCallbacks(vibrationCompleteRunnable)
        vib.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = if (vib.hasAmplitudeControl()) {
                IntArray(timings.size) { index -> if (index % 2 == 1) vibrateAmplitude else 0 }
            } else {
                null
            }
            val effect = if (amplitudes != null) {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(timings, -1)
            }
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(timings, -1)
        }

        isVibrating = true
        updateVibrateButtonText()
        val totalDuration = timings.sum()
        mainHandler.postDelayed(vibrationCompleteRunnable, totalDuration)
        showToast("執行: $label")
    }

    private fun cancelVibration(showFeedback: Boolean) {
        mainHandler.removeCallbacks(vibrationCompleteRunnable)
        vibrator?.cancel()
        isVibrating = false
        updateVibrateButtonText()
        if (showFeedback) {
            showToast("已停止震動")
        }
    }

    private fun updateVibrateButtonText() {
        val binding = getBinding() ?: return
        if (isVibrating) {
            binding.btnVibrate.text = "震動中... (點擊停止)"
        } else {
            val seconds = vibrateDurationMs / 1000.0
            binding.btnVibrate.text = String.format(Locale.US, "開始震動 (%.1fs)", seconds)
        }
    }

    private fun setStreamVolumeSafely(streamType: Int, index: Int, flags: Int) {
        val am = audioManager ?: return
        val max = am.getStreamMaxVolume(streamType)
        val clamped = index.coerceIn(0, max)
        try {
            am.setStreamVolume(streamType, clamped, flags)
        } catch (_: SecurityException) {
            promptNotificationPolicyAccessIfNeeded()
        } catch (_: Exception) {
            showToast("無法調整此聲道音量")
        }
    }

    private fun adjustStreamVolumeSafely(streamType: Int, direction: Int, flags: Int) {
        val am = audioManager ?: return
        try {
            am.adjustStreamVolume(streamType, direction, flags)
        } catch (_: SecurityException) {
            promptNotificationPolicyAccessIfNeeded()
        } catch (_: Exception) {
            showToast("無法調整此聲道音量")
        }
    }

    private fun setRingerModeSafely(mode: Int, successToast: String?) {
        val am = audioManager ?: return
        try {
            am.ringerMode = mode
            if (!successToast.isNullOrEmpty()) {
                showToast(successToast)
            }
        } catch (_: SecurityException) {
            promptNotificationPolicyAccessIfNeeded()
        } catch (_: Exception) {
            showToast("無法變更系統響鈴模式")
        }
        refreshAllAudioUi()
    }

    private fun promptNotificationPolicyAccessIfNeeded() {
        val context = context ?: return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (nm != null && !nm.isNotificationPolicyAccessGranted) {
            AlertDialog.Builder(context)
                .setTitle("需要勿擾模式權限")
                .setMessage("Android 系統要求「勿擾存取權」才能在靜音與響鈴模式之間切換，是否前往系統設定開啟？")
                .setPositiveButton("前往設定") { _, _ ->
                    try {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    } catch (_: Exception) {
                        showToast("無法開啟系統設定")
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            showToast("系統目前限制變更靜音/勿擾狀態")
        }
    }

    private fun refreshAllAudioUi() {
        val binding = getBinding() ?: return
        val am = audioManager ?: return

        updateSingleStreamLabel(AudioManager.STREAM_MUSIC)
        updateSingleStreamLabel(AudioManager.STREAM_RING)
        updateSingleStreamLabel(AudioManager.STREAM_ALARM)

        val ringerMode = am.ringerMode
        binding.btnModeNormal.isSelected = ringerMode == AudioManager.RINGER_MODE_NORMAL
        binding.btnModeVibrate.isSelected = ringerMode == AudioManager.RINGER_MODE_VIBRATE
        binding.btnModeSilent.isSelected = ringerMode == AudioManager.RINGER_MODE_SILENT

        val (badgeText, badgeColor) = when (ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> "響鈴模式" to 0xFF019889.toInt()
            AudioManager.RINGER_MODE_VIBRATE -> "震動模式" to 0xFF7B1FA2.toInt()
            AudioManager.RINGER_MODE_SILENT -> "靜音模式" to 0xFFD32F2F.toInt()
            else -> "未知模式" to 0xFF607D8B.toInt()
        }
        binding.tvRingerModeBadge.text = badgeText
        binding.tvRingerModeBadge.setTextColor(badgeColor)
    }

    private fun updateSingleStreamLabel(streamType: Int) {
        val binding = getBinding() ?: return
        val am = audioManager ?: return
        val current = am.getStreamVolume(streamType)
        val max = am.getStreamMaxVolume(streamType).coerceAtLeast(1)
        val percent = (current * 100f / max).roundToInt()

        val labelView: TextView
        val seekBar: SeekBar
        val muteBtn: ImageButton

        when (streamType) {
            AudioManager.STREAM_MUSIC -> {
                labelView = binding.tvMusicLevel
                seekBar = binding.sbMusic
                muteBtn = binding.btnMusicMute
            }
            AudioManager.STREAM_RING -> {
                labelView = binding.tvRingLevel
                seekBar = binding.sbRing
                muteBtn = binding.btnRingMute
            }
            AudioManager.STREAM_ALARM -> {
                labelView = binding.tvAlarmLevel
                seekBar = binding.sbAlarm
                muteBtn = binding.btnAlarmMute
            }
            else -> return
        }

        labelView.text = "目前音量: $current / $max ($percent%)"
        if (seekBar.max != max) {
            seekBar.max = max
        }
        seekBar.progress = current
        muteBtn.setImageResource(if (current == 0) R.drawable.ic_volume_off else R.drawable.ic_volume_up)
    }

    private fun playAudioPreview(streamType: Int) {
        val context = context ?: return
        stopAudioPreview()

        val usage = when (streamType) {
            AudioManager.STREAM_RING -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
            AudioManager.STREAM_ALARM -> AudioAttributes.USAGE_ALARM
            else -> AudioAttributes.USAGE_MEDIA
        }

        try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            if (streamType == AudioManager.STREAM_MUSIC) {
                val loadedFromAsset = try {
                    context.assets.openFd("test.mp3").use { afd ->
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                    true
                } catch (_: Exception) {
                    false
                }
                if (!loadedFromAsset) {
                    val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    if (fallbackUri == null) {
                        player.release()
                        showToast("找不到可試聽的媒體音訊")
                        return
                    }
                    player.setDataSource(context, fallbackUri)
                }
            } else {
                val ringtoneType = if (streamType == AudioManager.STREAM_ALARM) {
                    RingtoneManager.TYPE_ALARM
                } else {
                    RingtoneManager.TYPE_RINGTONE
                }
                val uri = RingtoneManager.getDefaultUri(ringtoneType)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (uri == null) {
                    player.release()
                    showToast("系統未設定預設提示音")
                    return
                }
                player.setDataSource(context, uri)
            }

            player.setOnCompletionListener {
                stopAudioPreview()
            }
            player.prepare()
            player.start()
            mediaPlayer = player
            activeTestStream = streamType
            updatePreviewButtonStates()

            // Auto stop after 5 seconds for pleasant preview
            mainHandler.removeCallbacks(autoStopPreviewRunnable)
            mainHandler.postDelayed(autoStopPreviewRunnable, 5000L)
        } catch (e: Exception) {
            stopAudioPreview()
            showToast("音訊試聽失敗: ${e.localizedMessage ?: "不支援的音訊來源"}")
        }
    }

    private fun stopAudioPreview() {
        mainHandler.removeCallbacks(autoStopPreviewRunnable)
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
            // Ignore player state cleanup exceptions
        } finally {
            mediaPlayer = null
            activeTestStream = null
            updatePreviewButtonStates()
        }
    }

    private fun updatePreviewButtonStates() {
        val binding = getBinding() ?: return
        binding.btnMusicTest.text = if (activeTestStream == AudioManager.STREAM_MUSIC) "停止" else "試聽"
        binding.btnRingTest.text = if (activeTestStream == AudioManager.STREAM_RING) "停止" else "試聽"
        binding.btnAlarmTest.text = if (activeTestStream == AudioManager.STREAM_ALARM) "停止" else "試聽"
    }
}
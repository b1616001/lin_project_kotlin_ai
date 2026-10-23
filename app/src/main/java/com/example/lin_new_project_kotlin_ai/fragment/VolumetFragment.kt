package com.example.lin_new_project_kotlin_ai.fragment

import android.app.Service
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.FragmentVolumeBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class VolumetFragment : BaseBindingFragment<FragmentVolumeBinding?>() {
    private var myVibrator: Vibrator? = null
    private var mAudioManager: AudioManager? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentVolumeBinding {
        return FragmentVolumeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myVibrator = requireActivity().getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        mAudioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //詳請http://blog.csdn.net/leirenorlei/article/details/7842045
        initView()
    }

    private fun initView() {
        getBinding()!!.btnMusicMax.setOnClickListener { view: View? ->
            mAudioManager!!.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_PLAY_SOUND
            )
        }
        getBinding()!!.btnMusicAdjust.setOnClickListener { view: View? ->
            mAudioManager!!.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }
        getBinding()!!.btnRingMax.setOnClickListener { view: View? ->
            mAudioManager!!.setStreamVolume(
                AudioManager.STREAM_RING,
                mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_PLAY_SOUND
            )
        }
        getBinding()!!.btnRingAdjust.setOnClickListener { view: View? ->
            mAudioManager!!.adjustStreamVolume(
                AudioManager.STREAM_RING,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }
        getBinding()!!.btnAlarmMax.setOnClickListener { view: View? ->
            mAudioManager!!.setStreamVolume(
                AudioManager.STREAM_ALARM,
                7,
                AudioManager.FLAG_SHOW_UI
            )
        }
        getBinding()!!.btnAlarmAdjust.setOnClickListener { view: View? ->
            mAudioManager!!.adjustStreamVolume(
                AudioManager.STREAM_ALARM,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }
        getBinding()!!.btnVibrate.setOnClickListener { view: View? ->
            myVibrator!!.vibrate(
                3000
            )
        }
    }
}
package com.example.lin_new_project_kotlin_ai.fragment

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.OpenGLR.GlBgTheme
import com.example.lin_new_project_kotlin_ai.OpenGLR.GlSceneMode
import com.example.lin_new_project_kotlin_ai.OpenGLR.GlTelemetry
import com.example.lin_new_project_kotlin_ai.OpenGLR.OpenGLRenderer
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentGlsurfaceBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale
import kotlin.math.roundToInt

class GLSurfaceFragment : BaseBindingFragment<FragmentGlsurfaceBinding?>() {

    private var glSurfaceView: GLSurfaceView? = null
    private val renderer = OpenGLRenderer()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isUpdatingZoomSeekBar = false

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentGlsurfaceBinding {
        return FragmentGlsurfaceBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        setupGlSurfaceView()
        initControls()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        glSurfaceView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        renderer.onTelemetryListener = null
        glSurfaceView = null
        super.onDestroyView()
    }

    private fun initHeader() {
        val b = getBinding() ?: return
        b.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGlSurfaceView() {
        val ctx = context ?: return
        val b = getBinding() ?: return

        val surfaceView = GLSurfaceView(ctx).apply {
            preserveEGLContextOnPause = true
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val scaleDetector = ScaleGestureDetector(
            ctx,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val factor = detector.scaleFactor
                    surfaceView.queueEvent {
                        renderer.adjustZoomBy(factor)
                    }
                    surfaceView.requestRender()
                    refreshCameraReadout()
                    return true
                }
            }
        )

        val doubleTapDetector = GestureDetector(
            ctx,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    surfaceView.queueEvent {
                        renderer.resetCamera()
                    }
                    surfaceView.requestRender()
                    refreshCameraReadout()
                    showToast("已重置 3D 攝影機視角")
                    return true
                }
            }
        )

        surfaceView.setOnTouchListener { v, event ->
            doubleTapDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        lastTouchX = event.x
                        lastTouchY = event.y

                        surfaceView.queueEvent {
                            renderer.rotateCameraBy(deltaYaw = dx * 0.45f, deltaPitch = dy * 0.45f)
                        }
                        surfaceView.requestRender()
                        refreshCameraReadout()
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            true
        }

        renderer.onTelemetryListener = { telemetry ->
            view?.post {
                if (getBinding() != null) {
                    renderTelemetry(telemetry)
                }
            }
        }

        b.frameLayout.removeAllViews()
        b.frameLayout.addView(surfaceView)
        glSurfaceView = surfaceView
    }

    private fun initControls() {
        val b = getBinding() ?: return

        b.btnSceneCube.setOnClickListener { switchScene(GlSceneMode.CUBE_3D) }
        b.btnSceneOrbit.setOnClickListener { switchScene(GlSceneMode.ORBIT_SQUARES) }
        b.btnSceneFlat.setOnClickListener { switchScene(GlSceneMode.FLAT_MATRIX) }

        b.btnPlayPause.setOnClickListener {
            val nextPaused = !renderer.isPaused
            glSurfaceView?.queueEvent {
                renderer.isPaused = nextPaused
            }
            glSurfaceView?.renderMode = if (nextPaused) {
                GLSurfaceView.RENDERMODE_WHEN_DIRTY
            } else {
                GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
            if (!nextPaused) glSurfaceView?.requestRender()
            refreshUi()
        }

        b.btnWireframe.setOnClickListener {
            val nextWire = !renderer.isWireframe
            glSurfaceView?.queueEvent {
                renderer.isWireframe = nextWire
            }
            glSurfaceView?.requestRender()
            refreshUi()
        }

        b.btnReverse.setOnClickListener {
            val nextRev = !renderer.reverseDirection
            glSurfaceView?.queueEvent {
                renderer.reverseDirection = nextRev
            }
            glSurfaceView?.requestRender()
            refreshUi()
        }

        b.btnResetCamera.setOnClickListener {
            glSurfaceView?.queueEvent {
                renderer.resetCamera()
            }
            glSurfaceView?.requestRender()
            refreshCameraReadout()
            showToast("已重置攝影機角度與縮放")
        }

        b.btnSpeed05.setOnClickListener { setSpeed(0.5f) }
        b.btnSpeed10.setOnClickListener { setSpeed(1.0f) }
        b.btnSpeed20.setOnClickListener { setSpeed(2.0f) }
        b.btnSpeed30.setOnClickListener { setSpeed(3.0f) }

        b.sbZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isUpdatingZoomSeekBar) {
                    // progress 0..120 -> distance -16.0..-4.0
                    val dist = -16.0f + (progress / 10.0f)
                    glSurfaceView?.queueEvent {
                        renderer.cameraDistance = dist.coerceIn(-16f, -4f)
                    }
                    glSurfaceView?.requestRender()
                    refreshCameraReadout(updateSeekBar = false)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        b.btnBgSpace.setOnClickListener { setBgTheme(GlBgTheme.SPACE_DARK) }
        b.btnBgBlack.setOnClickListener { setBgTheme(GlBgTheme.PURE_BLACK) }
        b.btnBgTeal.setOnClickListener { setBgTheme(GlBgTheme.DEEP_TEAL) }
    }

    private fun switchScene(mode: GlSceneMode) {
        glSurfaceView?.queueEvent {
            renderer.sceneMode = mode
            renderer.resetCamera()
        }
        glSurfaceView?.requestRender()
        refreshUi()
    }

    private fun setSpeed(speed: Float) {
        glSurfaceView?.queueEvent {
            renderer.speedMultiplier = speed
        }
        glSurfaceView?.requestRender()
        refreshUi()
    }

    private fun setBgTheme(theme: GlBgTheme) {
        glSurfaceView?.queueEvent {
            renderer.bgTheme = theme
        }
        glSurfaceView?.requestRender()
        refreshUi()
    }

    private fun refreshUi() {
        val b = getBinding() ?: return

        b.btnSceneCube.isSelected = renderer.sceneMode == GlSceneMode.CUBE_3D
        b.btnSceneOrbit.isSelected = renderer.sceneMode == GlSceneMode.ORBIT_SQUARES
        b.btnSceneFlat.isSelected = renderer.sceneMode == GlSceneMode.FLAT_MATRIX

        if (renderer.isPaused) {
            b.btnPlayPause.text = "繼續動畫"
            b.btnPlayPause.setBackgroundResource(R.drawable.bg_btn_primary)
        } else {
            b.btnPlayPause.text = "暫停動畫"
            b.btnPlayPause.setBackgroundResource(R.drawable.bg_btn_danger_pill)
        }

        b.btnWireframe.isSelected = renderer.isWireframe
        b.btnReverse.isSelected = renderer.reverseDirection

        b.btnSpeed05.isSelected = renderer.speedMultiplier == 0.5f
        b.btnSpeed10.isSelected = renderer.speedMultiplier == 1.0f
        b.btnSpeed20.isSelected = renderer.speedMultiplier == 2.0f
        b.btnSpeed30.isSelected = renderer.speedMultiplier == 3.0f

        b.btnBgSpace.isSelected = renderer.bgTheme == GlBgTheme.SPACE_DARK
        b.btnBgBlack.isSelected = renderer.bgTheme == GlBgTheme.PURE_BLACK
        b.btnBgTeal.isSelected = renderer.bgTheme == GlBgTheme.DEEP_TEAL

        refreshCameraReadout()
    }

    private fun refreshCameraReadout(updateSeekBar: Boolean = true) {
        val b = getBinding() ?: return
        val dist = renderer.cameraDistance
        b.tvCameraOverlay.text = String.format(
            Locale.US,
            "Pitch: %.0f°  Yaw: %.0f°  Z: %.1f",
            renderer.cameraPitchDeg,
            renderer.cameraYawDeg,
            dist
        )
        b.tvZoomValue.text = String.format(Locale.US, "%.1f", dist)

        if (updateSeekBar) {
            isUpdatingZoomSeekBar = true
            val progress = ((dist + 16.0f) * 10.0f).roundToInt().coerceIn(0, 120)
            b.sbZoom.progress = progress
            isUpdatingZoomSeekBar = false
        }
    }

    private fun renderTelemetry(t: GlTelemetry) {
        val b = getBinding() ?: return
        b.tvFpsBadge.text = "${t.fps} FPS"
        b.tvGlInfo.text = String.format(
            Locale.US,
            "• 渲染模式：%s（%s / %.1fx %s）\n" +
                "• 視口解析度：%d × %d px（即時 FPS：%d）\n" +
                "• GPU Vendor：%s\n" +
                "• GPU Renderer：%s\n" +
                "• OpenGL 版本：%s",
            renderer.sceneMode.label,
            if (renderer.isWireframe) "線框" else "實體",
            renderer.speedMultiplier,
            if (renderer.isPaused) "已暫停" else if (renderer.reverseDirection) "反向運行" else "順向運行",
            t.viewportWidth,
            t.viewportHeight,
            t.fps,
            t.vendor,
            t.renderer,
            t.version
        )
    }
}
package com.example.lin_new_project_kotlin_ai.window

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import com.example.lin_new_project_kotlin_ai.databinding.WindowMediaMenuBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import kotlin.math.hypot
import kotlin.math.roundToInt

class MediaMenuWindow @JvmOverloads constructor(
    context: Context,
    initialConfig: WindowCustomConfig = WindowConfigStore.load()
) : BaseWindow<WindowMediaMenuBinding?>(
    context = context,
    width = ViewGroup.LayoutParams.WRAP_CONTENT,
    height = ViewGroup.LayoutParams.WRAP_CONTENT,
    gravity = Gravity.TOP or Gravity.START,
    x = 0,
    y = 0,
    windowMode = initialConfig.mode,
    attachImmediately = false
) {
    private var config: WindowCustomConfig = initialConfig
    private var viewX: Int = 0
    private var viewY: Int = 0
    private var snapAnimator: ValueAnimator? = null
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    var onConfigUpdatedListener: ((WindowCustomConfig) -> Unit)? = null

    init {
        initViews()
        resolveInitialPosition()
        applyConfigInternal(config, saveAndNotify = false)
        attachToWindow()
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): WindowMediaMenuBinding {
        return WindowMediaMenuBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        val b = getBinding() ?: return

        b.tvMeum.setOnTouchListener(createDragTouchListener(onTap = null))
        b.llCollapsedOrb.setOnTouchListener(createDragTouchListener(onTap = {
            updateConfigAndNotify(config.copy(collapsed = false))
        }))

        b.btnCollapse.setOnClickListener {
            updateConfigAndNotify(config.copy(collapsed = true))
        }

        b.tvDismiss.setOnClickListener {
            dismiss()
        }

        b.btnQuickTheme.setOnClickListener {
            updateConfigAndNotify(config.copy(themePreset = config.nextTheme()))
        }

        b.btnQuickLock.setOnClickListener {
            updateConfigAndNotify(config.copy(lockPosition = !config.lockPosition))
        }

        b.btnQuickSnap.setOnClickListener {
            snapToNearestEdge(animate = true)
        }

        b.btnQuickKeepOn.setOnClickListener {
            updateConfigAndNotify(config.copy(keepScreenOn = !config.keepScreenOn))
        }
    }

    private fun resolveInitialPosition() {
        val (screenW, screenH) = getScreenBounds()
        val cardWidthPx = getActiveWidthPx()
        val estHeightPx = getActiveHeightPx()
        val marginPx = dp2px(12f)
        val topSafePx = dp2px(88f)

        val defaultX = (screenW - cardWidthPx - marginPx).coerceAtLeast(marginPx)
        val defaultY = topSafePx.coerceAtMost((screenH - estHeightPx - marginPx).coerceAtLeast(marginPx))

        viewX = if (config.posX >= 0) config.posX else defaultX
        viewY = if (config.posY >= 0) config.posY else defaultY
        clampCoordinates()
        layoutParams?.x = viewX
        layoutParams?.y = viewY
    }

    fun applyConfig(newConfig: WindowCustomConfig) {
        applyConfigInternal(newConfig, saveAndNotify = false)
    }

    private fun updateConfigAndNotify(newConfig: WindowCustomConfig) {
        applyConfigInternal(newConfig, saveAndNotify = true)
    }

    private fun applyConfigInternal(newConfig: WindowCustomConfig, saveAndNotify: Boolean) {
        val prevCollapsed = config.collapsed
        val prevSize = config.sizePreset
        config = newConfig

        val b = getBinding() ?: return

        // Expanded vs Collapsed visibility
        if (config.collapsed) {
            b.llExpandedCard.visibility = View.GONE
            b.llCollapsedOrb.visibility = View.VISIBLE
        } else {
            b.llExpandedCard.visibility = View.VISIBLE
            b.llCollapsedOrb.visibility = View.GONE
        }

        // Update expanded card width
        val expandedLp = b.llExpandedCard.layoutParams
        expandedLp.width = dp2px(config.sizePreset.widthDp.toFloat())
        b.llExpandedCard.layoutParams = expandedLp

        // Apply theme colors and corner radius
        applyThemeStyling(b)

        // Update texts & badges
        b.tvModeBadge.text = if (config.mode == WindowMode.SYSTEM_OVERLAY) {
            "全局懸浮 (Overlay)"
        } else {
            "應用內懸浮 (In-App)"
        }
        b.btnQuickLock.text = if (config.lockPosition) "已鎖定" else "可拖曳"
        b.btnQuickKeepOn.text = if (config.keepScreenOn) "常亮:開" else "常亮:關"

        // Update WindowManager LayoutParams
        updateAlpha(config.alphaPercent / 100f)
        updateFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, config.keepScreenOn)
        updateFlag(WindowManager.LayoutParams.FLAG_SECURE, config.secureFlag)

        // Sync position if changed externally or size/collapse changed
        if (newConfig.posX >= 0 && newConfig.posY >= 0 && (newConfig.posX != viewX || newConfig.posY != viewY)) {
            viewX = newConfig.posX
            viewY = newConfig.posY
        }
        if (prevCollapsed != config.collapsed || prevSize != config.sizePreset) {
            clampCoordinates()
        }
        updatePosition(viewX, viewY)
        updateCoordsText()

        val synced = config.copy(posX = viewX, posY = viewY)
        config = synced
        if (saveAndNotify) {
            WindowConfigStore.save(synced)
            onConfigUpdatedListener?.invoke(synced)
        }
    }

    private fun applyThemeStyling(b: WindowMediaMenuBinding) {
        val radiusPx = dp2px(config.cornerRadiusDp.toFloat()).toFloat()
        val btnRadiusPx = dp2px(10f).toFloat()
        val orbRadiusPx = dp2px(28f).toFloat()

        val cardBgColor: Int
        val cardStrokeColor: Int
        val titleColor: Int
        val subTextColor: Int
        val accentColor: Int
        val dividerColor: Int
        val stripBgColor: Int
        val btnBgColor: Int
        val btnTextColor: Int
        val orbBgColor: Int

        when (config.themePreset) {
            WindowThemePreset.DARK -> {
                cardBgColor = Color.parseColor("#1B2028")
                cardStrokeColor = Color.parseColor("#354150")
                titleColor = Color.WHITE
                subTextColor = Color.parseColor("#90A4AE")
                accentColor = Color.parseColor("#03DAC5")
                dividerColor = Color.parseColor("#2B333F")
                stripBgColor = Color.parseColor("#12161C")
                btnBgColor = Color.parseColor("#27303D")
                btnTextColor = Color.WHITE
                orbBgColor = Color.parseColor("#019889")
            }

            WindowThemePreset.TEAL -> {
                cardBgColor = Color.parseColor("#0C3B3D")
                cardStrokeColor = Color.parseColor("#26A69A")
                titleColor = Color.WHITE
                subTextColor = Color.parseColor("#B2DFDB")
                accentColor = Color.parseColor("#64FFDA")
                dividerColor = Color.parseColor("#18575A")
                stripBgColor = Color.parseColor("#082A2C")
                btnBgColor = Color.parseColor("#145255")
                btnTextColor = Color.WHITE
                orbBgColor = Color.parseColor("#00796B")
            }

            WindowThemePreset.LIGHT -> {
                cardBgColor = Color.parseColor("#FFFFFF")
                cardStrokeColor = Color.parseColor("#CFD8DC")
                titleColor = Color.parseColor("#1C252C")
                subTextColor = Color.parseColor("#607D8B")
                accentColor = Color.parseColor("#019889")
                dividerColor = Color.parseColor("#E3E8EE")
                stripBgColor = Color.parseColor("#F3F6F9")
                btnBgColor = Color.parseColor("#EBF1F5")
                btnTextColor = Color.parseColor("#1C252C")
                orbBgColor = Color.parseColor("#019889")
            }
        }

        b.llExpandedCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(cardBgColor)
            setStroke(dp2px(1.2f), cardStrokeColor)
        }

        b.tvCoords.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp2px(8f).toFloat()
            setColor(stripBgColor)
        }

        val actionButtons = listOf(b.btnQuickTheme, b.btnQuickLock, b.btnQuickSnap, b.btnQuickKeepOn)
        actionButtons.forEachIndexed { index, tv ->
            val highlight = (index == 1 && config.lockPosition) || (index == 3 && config.keepScreenOn)
            tv.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = btnRadiusPx
                setColor(if (highlight) Color.parseColor("#019889") else btnBgColor)
            }
            tv.setTextColor(if (highlight) Color.WHITE else btnTextColor)
        }

        b.llCollapsedOrb.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = orbRadiusPx
            setColor(orbBgColor)
            setStroke(dp2px(1.5f), Color.parseColor("#80FFFFFF"))
        }

        b.tvWindowTitle.setTextColor(titleColor)
        b.tvModeBadge.setTextColor(accentColor)
        b.ivDragIcon.setColorFilter(accentColor)
        b.btnCollapse.setColorFilter(subTextColor)
        b.viewDivider.setBackgroundColor(dividerColor)
        b.tvCoords.setTextColor(subTextColor)
        b.tvHint.setTextColor(subTextColor)
    }

    fun snapToCorner(corner: DockCorner) {
        val (screenW, screenH) = getScreenBounds()
        val w = getActiveWidthPx()
        val h = getActiveHeightPx()
        val margin = dp2px(12f)
        val topMargin = dp2px(68f)
        val bottomMargin = dp2px(48f)

        val targetX = when (corner) {
            DockCorner.TOP_LEFT, DockCorner.BOTTOM_LEFT -> margin
            DockCorner.TOP_RIGHT, DockCorner.BOTTOM_RIGHT -> (screenW - w - margin).coerceAtLeast(margin)
        }
        val targetY = when (corner) {
            DockCorner.TOP_LEFT, DockCorner.TOP_RIGHT -> topMargin
            DockCorner.BOTTOM_LEFT, DockCorner.BOTTOM_RIGHT -> (screenH - h - bottomMargin).coerceAtLeast(topMargin)
        }

        animateToPosition(targetX, targetY)
    }

    fun resetToDefaultPosition() {
        val (screenW, _) = getScreenBounds()
        val w = getActiveWidthPx()
        val margin = dp2px(12f)
        val targetX = (screenW - w - margin).coerceAtLeast(margin)
        val targetY = dp2px(88f)
        animateToPosition(targetX, targetY)
    }

    private fun snapToNearestEdge(animate: Boolean) {
        val (screenW, _) = getScreenBounds()
        val w = getActiveWidthPx()
        val margin = dp2px(12f)
        val centerX = viewX + w / 2
        val targetX = if (centerX < screenW / 2) {
            margin
        } else {
            (screenW - w - margin).coerceAtLeast(margin)
        }
        if (animate) {
            animateToPosition(targetX, viewY)
        } else {
            viewX = targetX
            clampCoordinates()
            updatePosition(viewX, viewY)
            syncPositionState()
        }
    }

    private fun animateToPosition(targetX: Int, targetY: Int) {
        snapAnimator?.cancel()
        val startX = viewX
        val startY = viewY
        snapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                viewX = (startX + (targetX - startX) * f).roundToInt()
                viewY = (startY + (targetY - startY) * f).roundToInt()
                clampCoordinates()
                updatePosition(viewX, viewY)
                updateCoordsText()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    syncPositionState()
                }
            })
            start()
        }
    }

    private fun createDragTouchListener(onTap: (() -> Unit)?): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var lastRawX = 0f
            private var lastRawY = 0f
            private var downRawX = 0f
            private var downRawY = 0f
            private var hasMoved = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        snapAnimator?.cancel()
                        lastRawX = event.rawX
                        lastRawY = event.rawY
                        downRawX = event.rawX
                        downRawY = event.rawY
                        hasMoved = false
                        view.isPressed = true
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dxTotal = event.rawX - downRawX
                        val dyTotal = event.rawY - downRawY
                        if (!hasMoved && hypot(dxTotal.toDouble(), dyTotal.toDouble()) > touchSlop) {
                            hasMoved = true
                            view.isPressed = false
                        }
                        if (hasMoved && !config.lockPosition) {
                            val dx = (event.rawX - lastRawX).roundToInt()
                            val dy = (event.rawY - lastRawY).roundToInt()
                            lastRawX = event.rawX
                            lastRawY = event.rawY

                            viewX += dx
                            viewY += dy
                            clampCoordinates()
                            updatePosition(viewX, viewY)
                            updateCoordsText()
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.isPressed = false
                        if (!hasMoved) {
                            view.performClick()
                            onTap?.invoke()
                        } else if (!config.lockPosition) {
                            if (config.autoSnapEdge) {
                                snapToNearestEdge(animate = true)
                            } else {
                                syncPositionState()
                            }
                        }
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun clampCoordinates() {
        val (screenW, screenH) = getScreenBounds()
        val w = getActiveWidthPx()
        val h = getActiveHeightPx()
        val margin = dp2px(8f)
        val maxX = (screenW - w - margin).coerceAtLeast(margin)
        val maxY = (screenH - h - dp2px(24f)).coerceAtLeast(dp2px(32f))
        viewX = viewX.coerceIn(margin, maxX)
        viewY = viewY.coerceIn(dp2px(24f), maxY)
    }

    private fun getActiveWidthPx(): Int {
        val b = getBinding()
        if (config.collapsed) {
            val w = b?.llCollapsedOrb?.width ?: 0
            return if (w > 0) w else dp2px(64f)
        }
        val measured = b?.llExpandedCard?.width ?: 0
        return if (measured > 0) measured else dp2px(config.sizePreset.widthDp.toFloat()) + dp2px(8f)
    }

    private fun getActiveHeightPx(): Int {
        val b = getBinding()
        if (config.collapsed) {
            val h = b?.llCollapsedOrb?.height ?: 0
            return if (h > 0) h else dp2px(64f)
        }
        val measured = b?.llExpandedCard?.height ?: 0
        return if (measured > 0) measured else dp2px(210f)
    }

    private fun updateCoordsText() {
        val b = getBinding() ?: return
        val lockStr = if (config.lockPosition) "已鎖定" else "可拖曳"
        b.tvCoords.text = "X:${viewX}  Y:${viewY} • ${config.sizePreset.label} • $lockStr"
    }

    private fun syncPositionState() {
        val updated = config.copy(posX = viewX, posY = viewY)
        config = updated
        WindowConfigStore.save(updated)
        onConfigUpdatedListener?.invoke(updated)
    }

    override fun dismiss() {
        snapAnimator?.cancel()
        snapAnimator = null
        super.dismiss()
    }
}

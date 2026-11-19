package com.example.lin_new_project_kotlin_ai.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.sqrt

@SuppressLint("AppCompatCustomView")
class ScaleImageView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(mContext, attrs, defStyleAttr) {

    // 初始狀態的 Matrix
    private val mMatrix = Matrix()

    // 進行變動狀況下的 Matrix
    private val mChangeMatrix = Matrix()

    // 用於讀取 Matrix 數值
    private val mMatrixValues = FloatArray(9)

    // 圖片的 Bitmap
    private var mBitmap: Bitmap? = null
    private var mOriginalBitmap: Bitmap? = null

    // 手機畫面尺寸資訊
    private var mDisplayMetrics: DisplayMetrics? = null

    // 設定縮放最小/最大比例
    private val mMinScale = 1.0f
    private val mMaxScale = 5.0f

    // 當下的狀態
    private var mState: Int = STATE_NONE

    // 單指拖動上一個座標點
    private val mLastPointF = PointF()

    // 雙指縮放中心點
    private val mMidPointF = PointF()

    // 雙指初始距離與起始縮放倍率
    private var mDistance = 1f
    private var mStartScale = 1f

    // 圖片中心座標
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var width = 0
    private var height = 0 // View 的長寬
    var CustomLeft: Int = 0

    // 雙擊縮放手勢偵測
    private val mGestureDetector = GestureDetector(
        mContext,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (mBitmap == null) return false
                val currentScale = getCurrentScale()
                val targetScale = if (currentScale > mMinScale + 0.1f) {
                    mMinScale
                } else {
                    2.5f.coerceAtMost(mMaxScale)
                }
                val factor = if (currentScale > 0f) targetScale / currentScale else 1f
                mMatrix.postScale(factor, factor, e.x, e.y)
                fixTranslationBounds()
                imageMatrix = mMatrix
                return true
            }
        }
    )

    private fun getViewWidth(): Int = if (getWidth() > 0) getWidth() else width

    private fun getViewHeight(): Int = if (getHeight() > 0) getHeight() else height

    private fun getCurrentScale(matrix: Matrix = mMatrix): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[Matrix.MSCALE_X]
    }

    // 圖片縮放層級保護
    private fun Scale() {
        val currentScale = getCurrentScale()
        if (currentScale < mMinScale && currentScale > 0f) {
            val factor = mMinScale / currentScale
            mMatrix.postScale(factor, factor, getViewWidth() / 2f, getViewHeight() / 2f)
        } else if (currentScale > mMaxScale) {
            val factor = mMaxScale / currentScale
            mMatrix.postScale(factor, factor, getViewWidth() / 2f, getViewHeight() / 2f)
        }
        fixTranslationBounds()
    }

    // 修正圖片位移邊界，避免拖移或縮放後超出 View 範圍產生留白
    private fun fixTranslationBounds() {
        val bitmap = mBitmap ?: return
        val viewW = getViewWidth().toFloat()
        val viewH = getViewHeight().toFloat()
        if (viewW <= 0f || viewH <= 0f) return

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        mMatrix.mapRect(rect)

        val (deltaX, deltaY) = computeTranslationDelta(
            rectLeft = rect.left,
            rectTop = rect.top,
            rectRight = rect.right,
            rectBottom = rect.bottom,
            viewWidth = viewW,
            viewHeight = viewH
        )
        if (deltaX != 0f || deltaY != 0f) {
            mMatrix.postTranslate(deltaX, deltaY)
        }
    }

    // 兩點距離
    private fun Spacing(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    // 兩點中心
    private fun MidPoint(point: PointF, event: MotionEvent) {
        if (event.pointerCount < 2) return
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2f, y / 2f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            width = w
            height = h
            if (mBitmap != null) {
                fixTranslationBounds()
                imageMatrix = mMatrix
            }
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    // 圖片縮放設定
    @SuppressLint("ClickableViewAccessibility")
    fun build_image() {
        val bitmap = mBitmap ?: return
        // 取得手機畫面尺寸資訊
        mDisplayMetrics = mContext.resources.displayMetrics

        setBackgroundColor(Color.BLACK)
        // 設置縮放的型態
        scaleType = ScaleType.MATRIX
        // 將 Bitmap 帶入
        setImageBitmap(bitmap)

        val viewW = getViewWidth()
        val viewH = getViewHeight()
        mCenterX = ((viewW / 2f) - (bitmap.width / 2f))
        mCenterY = ((viewH / 2f) - (bitmap.height / 2f))

        Log.d("資料HH", "$viewW,${bitmap.width}")
        Log.d("資料HH", "$viewH,${bitmap.height}")
        Log.d("資料", "$mCenterX")
        Log.d("資料", "$mCenterY")

        mMatrix.reset()
        mMatrix.postTranslate(mCenterX, mCenterY)
        fixTranslationBounds()
        imageMatrix = mMatrix

        // 設置 Touch 觸發的 Listener 動作
        setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true
                }

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        mLastPointF.set(event.x, event.y)
                        mState = STATE_DRAG
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        val distance = Spacing(event)
                        // 只要兩點距離大於 10 就判定為多點觸碰
                        if (distance > 10f) {
                            mDistance = distance
                            mChangeMatrix.set(mMatrix)
                            mStartScale = getCurrentScale(mChangeMatrix)
                            MidPoint(mMidPointF, event)
                            mState = STATE_ZOOM
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (mState == STATE_DRAG) {
                            val dx = event.x - mLastPointF.x
                            val dy = event.y - mLastPointF.y
                            mMatrix.postTranslate(dx, dy)
                            fixTranslationBounds()
                            mLastPointF.set(event.x, event.y)
                        } else if (mState == STATE_ZOOM && event.pointerCount >= 2) {
                            val newDistance = Spacing(event)
                            if (newDistance > 10f && mDistance > 0f) {
                                mMatrix.set(mChangeMatrix)
                                val rawScale = newDistance / mDistance
                                val clampedFactor = computeClampedScaleFactor(
                                    startScale = mStartScale,
                                    rawScaleFactor = rawScale,
                                    minScale = mMinScale,
                                    maxScale = mMaxScale
                                )
                                mMatrix.postScale(
                                    clampedFactor,
                                    clampedFactor,
                                    mMidPointF.x,
                                    mMidPointF.y
                                )
                                fixTranslationBounds()
                            }
                        }
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        if (event.pointerCount == 2) {
                            val remainingIndex = if (event.actionIndex == 0) 1 else 0
                            mLastPointF.set(
                                event.getX(remainingIndex),
                                event.getY(remainingIndex)
                            )
                            mState = STATE_DRAG
                        } else {
                            mState = STATE_NONE
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        mState = STATE_NONE
                        Scale()
                        v?.performClick()
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        mState = STATE_NONE
                        Scale()
                    }
                }

                // 將 mMatrix 滑動縮放控制帶入
                this@ScaleImageView.imageMatrix = mMatrix
                return true
            }
        })
    }

    fun setData(width: Int, height: Int, left: Int, top: Int, right: Int, bottom: Int) {
        if (width <= 0 || height <= 0) return
        this.width = width
        this.height = height

        val sourceBitmap = mOriginalBitmap ?: (drawable as? BitmapDrawable)?.bitmap
        if (sourceBitmap != null) {
            mOriginalBitmap = sourceBitmap
            val viewRatio = width.toFloat() / height.toFloat()
            val bmpRatio = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()
            mBitmap = if (viewRatio > bmpRatio) {
                enlarge(sourceBitmap, height, false)
            } else {
                enlarge(sourceBitmap, width, true)
            }
            build_image()
        }
    }

    fun dip2px(dpValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(pxValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dpFromPx(px: Float): Int {
        return (px / mContext.resources.displayMetrics.density).toInt()
    }

    fun pxFromDp(dp: Float): Int {
        return (dp * mContext.resources.displayMetrics.density).toInt()
    }

    companion object {
        // 圖片狀態 - 初始狀態
        private const val STATE_NONE = 0

        // 圖片狀態 - 拖動狀態
        private const val STATE_DRAG = 1

        // 圖片狀態 - 縮放狀態
        private const val STATE_ZOOM = 2

        internal fun computeClampedScaleFactor(
            startScale: Float,
            rawScaleFactor: Float,
            minScale: Float,
            maxScale: Float
        ): Float {
            if (startScale <= 0f || rawScaleFactor <= 0f) return 1f
            val targetScale = (startScale * rawScaleFactor).coerceIn(minScale, maxScale)
            return targetScale / startScale
        }

        internal fun computeTranslationDelta(
            rectLeft: Float,
            rectTop: Float,
            rectRight: Float,
            rectBottom: Float,
            viewWidth: Float,
            viewHeight: Float
        ): Pair<Float, Float> {
            if (viewWidth <= 0f || viewHeight <= 0f) return 0f to 0f
            val rectWidth = rectRight - rectLeft
            val rectHeight = rectBottom - rectTop

            val deltaX = when {
                rectWidth <= viewWidth -> (viewWidth - rectWidth) / 2f - rectLeft
                rectLeft > 0f -> -rectLeft
                rectRight < viewWidth -> viewWidth - rectRight
                else -> 0f
            }

            val deltaY = when {
                rectHeight <= viewHeight -> (viewHeight - rectHeight) / 2f - rectTop
                rectTop > 0f -> -rectTop
                rectBottom < viewHeight -> viewHeight - rectBottom
                else -> 0f
            }

            return deltaX to deltaY
        }

        fun enlarge(bit: Bitmap, MaxPx: Int, bb: Boolean): Bitmap {
            val width = bit.width
            val height = bit.height
            Log.d("圖片大小", "$width,$height,$MaxPx")
            val newWidth: Int
            val newHeight: Int
            val ratio: Float
            if (bb) {
                ratio = MaxPx.toFloat() / width.toFloat()
                newWidth = MaxPx
                newHeight = (ratio * height).toInt().coerceAtLeast(1)
                Log.d("圖片大小", "寬$newWidth,$newHeight,$ratio")
            } else {
                ratio = MaxPx.toFloat() / height.toFloat()
                newHeight = MaxPx
                newWidth = (ratio * width).toInt().coerceAtLeast(1)
                Log.d("圖片大小", "高$newWidth,$newHeight,$ratio")
            }

            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height
            val matrix = Matrix()
            matrix.reset()
            matrix.postScale(scaleWidth, scaleHeight)
            return Bitmap.createBitmap(bit, 0, 0, width, height, matrix, true)
        }
    }
}

package com.example.lin_new_project_kotlin_ai.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.sqrt

@SuppressLint("AppCompatCustomView")
class ScaleImageView(private val context: Context) : ImageView(
    context
) {
    //初始狀態的Matrix
    private val mMatrix = Matrix()

    //進行變動狀況下的Matrix
    private val mChangeMatrix = Matrix()

    //圖片的Bitmap
    private var mBitmap: Bitmap? = null

    //手機畫面尺寸資訊
    private var mDisplayMetrics: DisplayMetrics? = null

    //設定縮放最小比例
    private val mMinScale = 1.0f

    //設定縮放最大比例
    private val mMaxScale = 5.0f

    //當下的狀態
    private var mState: Int = STATE_NONE

    //第一點按下的座標
    private val mFirstPointF = PointF()

    //第二點按下的座標
    private val mSecondPointF = PointF()

    //兩點距離
    private var mDistance = 1f

    //圖片中心座標
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var width = 0
    private var height = 0 //View 的長寬
    var CustomLeft: Int = 0
//    var top: Int = 0
//    var right: Int = 0
//    var bottom: Int = 0

    //圖片縮放層級設定
    private fun Scale() {
        //取得圖片縮放的層級
        val level: FloatArray? = FloatArray(9)
        mMatrix.getValues(level)

        //狀態為縮放時進入
        if (mState == STATE_ZOOM) {
            if (level!![0] < mMinScale) {
                Log.d("縮放狀態", "小於")
                //若層級小於1則縮放至原始大小
//                mMatrix.setScale(mMinScale, mMinScale);
//                mMatrix.postTranslate(mCenterX, mCenterY);
            } else {
                Log.d("縮放狀態", "於於於於")
            }

            //若縮放層級大於最大層級則顯示最大層級
            if (level[0] > mMaxScale) {
//                mMatrix.set(mChangeMatrix);
                Log.d("縮放狀態", "大於")
            }
        }
    }

    //兩點距離
    private fun Spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)

        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    //兩點中心
    private fun MidPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    //圖片縮放設定
    fun build_image() {
        //取得Context
        val mContext = getContext()
        //取得手機畫面尺寸資訊
        mDisplayMetrics = mContext.getResources().getDisplayMetrics()

        this.setBackgroundColor(Color.BLACK)
        //設置縮放的型態
        this.setScaleType(ScaleType.MATRIX)
        //將Bitmap帶入
        this.setImageBitmap(mBitmap)
        mCenterX = ((width / 2) - (mBitmap!!.getWidth() / 2)).toFloat()
        mCenterY = ((height / 2) - (mBitmap!!.getHeight() / 2)).toFloat()

        //將圖片放置畫面中央
        Log.d("資料HH", width.toString() + "," + mBitmap!!.getWidth())
        Log.d("資料HH", height.toString() + "," + mBitmap!!.getHeight())
        Log.d("資料", mCenterX.toString() + "")
        Log.d("資料", mCenterY.toString() + "")
        mMatrix.postTranslate(mCenterX, mCenterY)


        //this.post(new Runnable() {
//    @Override
//    public void run() {
//        Log.d("資料長寬",getWidth()+","+getHeight());
//
//    }
//});


        //將mMatrix帶入
        this.setImageMatrix(mMatrix)

        //設置Touch觸發的Listener動作
        this.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                //多點觸碰偵測
                when (event.getAction() and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        mChangeMatrix.set(mMatrix)
                        mFirstPointF.set(event.getX(), event.getY())
                        mState = STATE_DRAG
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        mDistance = Spacing(event)
                        //只要兩點距離大於10就判定為多點觸碰
                        if (Spacing(event) > 10f) {
                            mChangeMatrix.set(mMatrix)
                            MidPoint(mSecondPointF, event)
                            mState = STATE_ZOOM
                        }
                    }

                    MotionEvent.ACTION_UP -> {}
                    MotionEvent.ACTION_POINTER_UP -> mState = STATE_NONE
                    MotionEvent.ACTION_MOVE -> if (mState == STATE_DRAG) {
                        mMatrix.set(mChangeMatrix)
                        mMatrix.postTranslate(
                            event.getX() - mFirstPointF.x,
                            event.getY() - mFirstPointF.y
                        )
                    } else if (mState == STATE_ZOOM) {
                        val NewDistance = Spacing(event)
                        if (NewDistance > 10f) {
                            mMatrix.set(mChangeMatrix)
                            val NewScale = NewDistance / mDistance
                            mMatrix.postScale(NewScale, NewScale, mSecondPointF.x, mSecondPointF.y)
                        }
                    }
                }

                //將mMatrix滑動縮放控制帶入
                this@ScaleImageView.setImageMatrix(mMatrix)
                //縮放設定
                Scale()

                return true
            }
        })
    }

    fun setData(width: Int, height: Int, left: Int, top: Int, right: Int, bottom: Int) {
        //取得圖片Bitmap
        this.width = width
        this.height = height
//        this.left = left
//        this.top = top
//        this.right = right
//        this.bottom = bottom
        val mBitmapDrawable = this.getDrawable() as BitmapDrawable?
        if (mBitmapDrawable != null) {
            if (width > height) {
                mBitmap = enlarge(mBitmapDrawable.getBitmap(), height, false)
            } else {
                mBitmap = enlarge(mBitmapDrawable.getBitmap(), width, true)
            }
            //            mBitmap = mBitmapDrawable.getBitmap();
            build_image()
        }
    }

    fun dip2px(dpValue: Float): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(pxValue: Float): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dpFromPx(px: Float): Int {
        return (px / context.getResources().getDisplayMetrics().density).toInt()
    }

    fun pxFromDp(dp: Float): Int {
        return (dp * context.getResources().getDisplayMetrics().density).toInt()
    }

    companion object {
        //圖片狀態 - 初始狀態
        private const val STATE_NONE = 0

        //圖片狀態 - 拖動狀態
        private const val STATE_DRAG = 1

        //圖片狀態 - 縮放狀態
        private const val STATE_ZOOM = 2
        fun enlarge(bit: Bitmap, MaxPx: Int, bb: Boolean): Bitmap {
            val width = bit.getWidth()
            val height = bit.getHeight()
            Log.d("圖片大小", width.toString() + "," + height + "," + MaxPx)
            // 设置想要的大小
            var newWidth = 0
            var newHeight = 0
            var ratio = 1f // 縮放比例
            if (bb) {
                ratio = MaxPx.toFloat() / width.toFloat()
                newWidth = MaxPx
                newHeight = (ratio * height).toInt()
                Log.d("圖片大小", "寬" + newWidth + "," + newHeight + "," + ratio)
            } else {
                ratio = MaxPx.toFloat() / height.toFloat()
                newHeight = MaxPx
                newWidth = (ratio * width).toInt()
                Log.d("圖片大小", "高" + newWidth + "," + newHeight + "," + ratio)
            }

            // 计算缩放比例
            val scaleWidth = (newWidth.toFloat()) / width
            val scaleHeight = (newHeight.toFloat()) / height
            // 取得想要缩放的matrix参数
            val matrix = Matrix()
            matrix.reset()
            matrix.postScale(scaleWidth, scaleHeight)
            // 得到新的图片
            val newbm = Bitmap.createBitmap(
                bit, 0, 0, width, height, matrix,
                true
            )

            return newbm
        }
    }
}

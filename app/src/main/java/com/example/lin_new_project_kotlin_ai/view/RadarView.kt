package com.example.lin_new_project_kotlin_ai.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lin_new_project_kotlin_ai.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RadarView(context: Context) : View(context) {
    private var count = 5 //几边形
    private var layerCount = 4 //层数
    private var angle: Float //每条边对应的圆心角
    private var centerX = 0 //圆心x
    private var centerY = 0 //圆心y
    private var radius = 0f //半径
    private val polygonPaint: Paint //边框paint
    private val linePaint: Paint //连线paint
    private val txtPaint: Paint //文字paint
    private val circlePaint: Paint //圆点paint
    private val regionColorPaint: Paint //覆盖区域paint
    private val percents = arrayOf<Double?>(0.91, 0.35, 0.12, 0.8, 0.5) //覆盖区域百分比
    private val titles =
        arrayOf<String>("dota", "斗地主", "大吉大利，晚上吃鸡", "炉石传说", "跳一跳") //文字


    init {
        //计算圆心角
        angle = (Math.PI * 2 / count).toFloat()

        polygonPaint = Paint()
        polygonPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
        polygonPaint.setAntiAlias(true)
        polygonPaint.setStyle(Paint.Style.STROKE)
        polygonPaint.setStrokeWidth(4f)

        linePaint = Paint()
        linePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        linePaint.setAntiAlias(true)
        linePaint.setStyle(Paint.Style.STROKE)
        linePaint.setStrokeWidth(2f)

        txtPaint = Paint()
        txtPaint.setColor(ContextCompat.getColor(context, R.color.black))
        txtPaint.setAntiAlias(true)
        txtPaint.setStyle(Paint.Style.STROKE)
        txtPaint.setTextSize(30f)

        circlePaint = Paint()
        circlePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent))
        circlePaint.setAntiAlias(true)

        regionColorPaint = Paint()
        regionColorPaint.setColor(ContextCompat.getColor(context, R.color.black))
        regionColorPaint.setStyle(Paint.Style.FILL)
        regionColorPaint.setAntiAlias(true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //        drawRegion(canvas);
//        drawLines(canvas);
//        drawText(canvas);
        drawPolygon(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //在控件大小發生改變時調用。所以這裡初始化會被調用一次
        super.onSizeChanged(w, h, oldw, oldh) //此視圖的當前寬度,此視圖的當前高度,此視圖的舊寬度,此視圖的舊高度
        radius = min(h, w) / 2 * 0.7f //Math.min(h, w) 返回兩這值中的較小者
        centerX = w / 2
        centerY = h / 2
    }

    fun drawPolygon(canvas: Canvas) {
        val path = Path()
        val r = radius / layerCount //半徑/層級數
        for (i in 1..layerCount) {
            val curR = r * i //当前所在层的半径
            for (j in 0..<count) {
                if (j == 0) {
                    //每一层第一个点坐标
                    path.moveTo(centerX.toFloat(), centerY - curR) //起點座標
                } else {
                    //顺时针记录其余顶角的点坐标
                    val x = (centerX + sin((angle * j).toDouble()) * curR).toFloat()
                    val y = (centerY - cos((angle * j).toDouble()) * curR).toFloat()
                    path.lineTo(x, y) //從最後一個點到指定點（x，y）添加一條線。
                }
            }
            //最外层的顶角外面的五个小圆点(图中红色部分)
            if (i == layerCount) {
                for (j in 0..<count) {
                    val x = (centerX + sin((angle * j).toDouble()) * (curR + 12)).toFloat()
                    val y = (centerY - cos((angle * j).toDouble()) * (curR + 12)).toFloat()
                    canvas.drawCircle(x, y, 4f, circlePaint)
                }
            }
            path.close()
            canvas.drawPath(path, polygonPaint)
        }
    }

    private fun drawLines(canvas: Canvas) {
        val r = radius / layerCount
        for (i in 0..<count) {
            //起始坐标 从中心开始的话 startx=centerX , startY=centerY
            val startX = (centerX + sin((angle * i).toDouble()) * r).toFloat()
            val startY = (centerY - cos((angle * i).toDouble()) * r).toFloat()
            //末端坐标
            val endX = (centerX + sin((angle * i).toDouble()) * radius).toFloat()
            val endY = (centerY - cos((angle * i).toDouble()) * radius).toFloat()
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }

    //设置几边形，**注意：设置几边形需要重新计算圆心角**
    fun setCount(count: Int) {
        this.count = count
        angle = (Math.PI * 2 / count).toFloat()
        invalidate()
    }

    //设置层数
    fun setLayerCount(layerCount: Int) {
        this.layerCount = layerCount
        invalidate()
    }

    private fun drawText(canvas: Canvas) {
        for (i in 0..<count) {
            //获取到雷达图最外边的坐标
            val x = (centerX + sin((angle * i).toDouble()) * (radius + 12)).toFloat()
            val y = (centerY - cos((angle * i).toDouble()) * (radius + 12)).toFloat()
            if (angle * i == 0f) {
                //第一个文字位于顶角正上方
                txtPaint.setTextAlign(Paint.Align.CENTER)
                canvas.drawText(titles[i], x, y - 18, txtPaint)
                txtPaint.setTextAlign(Paint.Align.LEFT)
            } else if (angle * i > 0 && angle * i < Math.PI / 2) {
                //微调
                canvas.drawText(titles[i], x + 18, y + 10, txtPaint)
            } else if (angle * i >= Math.PI / 2 && angle * i < Math.PI) {
                //最右下的文字获取到文字的长、宽，按文字长度百分比向左移
                val txt = titles[i]
                val bounds = Rect()
                txtPaint.getTextBounds(txt, 0, txt.length, bounds)
                val height = (bounds.bottom - bounds.top).toFloat()
                val width = txtPaint.measureText(txt)
                canvas.drawText(txt, x - width * 0.4f, y + height + 18, txtPaint)
            } else if (angle * i >= Math.PI && angle * i < 3 * Math.PI / 2) {
                //同理最左下的文字获取到文字的长、宽，按文字长度百分比向左移
                val txt = titles[i]
                val bounds = Rect()
                txtPaint.getTextBounds(txt, 0, txt.length, bounds)
                val width = txtPaint.measureText(txt)
                val height = (bounds.bottom - bounds.top).toFloat()
                canvas.drawText(txt, x - width * 0.6f, y + height + 18, txtPaint)
            } else if (angle * i >= 3 * Math.PI / 2 && angle * i < 2 * Math.PI) {
                //文字向左移动
                val txt = titles[i]
                val width = txtPaint.measureText(txt)
                canvas.drawText(txt, x - width - 18, y + 10, txtPaint)
            }
        }
    }

    private fun drawRegion(canvas: Canvas) {
        val path = Path()
        val r = radius / layerCount //每层的间距
        for (i in 0..<count) {
            if (i == 0) {
                path.moveTo(
                    centerX.toFloat(),
                    (centerY - r - (radius - r) * percents[i]!!).toFloat()
                )
            } else {
                val x =
                    (centerX + sin((angle * i).toDouble()) * (percents[i]!! * (radius - r) + r)).toFloat()
                val y =
                    (centerY - cos((angle * i).toDouble()) * (percents[i]!! * (radius - r) + r)).toFloat()
                path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, regionColorPaint)
    }
}

package com.example.lin_new_project_kotlin_ai.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lin_new_project_kotlin_ai.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Radar2View(context: Context) : View(context) {
    private var count = 6 //几边形
    private var layerCount = 5 //层数
    private var angle: Float //每条边对应的圆心角
    private var centerX = 0 //圆心x
    private var centerY = 0 //圆心y
    private var radius = 0f //半径
    private val polygonPaint: Paint //边框paint
    private val linePaint: Paint //连线paint
    private val txtPaint: Paint //文字paint
    private val circlePaint: Paint //圆点paint
    private val regionColorPaint: Paint //覆盖区域paint
    private var percents: Array<Double?>? =
        arrayOf<Double?>(0.8, 0.4, 0.12, 0.4, 0.8, 0.99) //覆盖区域百分比
    private val titles = arrayOf<String>("擊殺", "生存", "支援", "經濟", "殺怪", "輸出") //文字


    init {
        //计算圆心角
        angle = (Math.PI * 2 / count).toFloat()

        polygonPaint = Paint()
        polygonPaint.setColor(ContextCompat.getColor(context, R.color.black))
        polygonPaint.setAntiAlias(true)
        polygonPaint.setStyle(Paint.Style.STROKE)
        polygonPaint.setStrokeWidth(0.3f)

        linePaint = Paint()
        linePaint.setColor(ContextCompat.getColor(context, R.color.black))
        linePaint.setAntiAlias(true)
        linePaint.setStyle(Paint.Style.STROKE)
        linePaint.setStrokeWidth(0.3f)

        txtPaint = Paint()
        txtPaint.setColor(ContextCompat.getColor(context, R.color.black))
        txtPaint.setAntiAlias(true)
        txtPaint.setStyle(Paint.Style.FILL)
        txtPaint.setTextSize(40f)

        circlePaint = Paint()
        circlePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent))
        circlePaint.setAntiAlias(true)

        regionColorPaint = Paint()
        regionColorPaint.setColor(ContextCompat.getColor(context, R.color.color1))
        regionColorPaint.setStyle(Paint.Style.FILL)
        regionColorPaint.setAntiAlias(true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRegion(canvas)
        drawLines(canvas)
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
//                for (int j = 0; j < count; j++) {
//                    float x = (float) (centerX + Math.sin(angle * j) * (curR + 12));
//                    float y = (float) (centerY - Math.cos(angle * j) * (curR + 12));
//                    canvas.drawCircle(x, y, 4, circlePaint);
//                }
                val ff = r * (i + 0.3f)
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), ff, polygonPaint)
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
            val endX = (centerX + sin((angle * i).toDouble()) * radius * 1.15f).toFloat()
            val endY = (centerY - cos((angle * i).toDouble()) * radius * 1.15f).toFloat()
            Log.d("座標:", "endX:" + endX + "\nendY" + endY)
            if (i == 0) {
                txtPaint.setTextAlign(Paint.Align.CENTER)
                canvas.drawText(titles[i], endX, endY * 0.9f, txtPaint)
            } else if (i == 1) {
                txtPaint.setTextAlign(Paint.Align.LEFT)
                canvas.drawText(titles[i], endX * 1.01f, endY, txtPaint)
            } else if (i == 2) {
                txtPaint.setTextAlign(Paint.Align.LEFT)
                canvas.drawText(titles[i], endX * 1.01f, endY * 1.05f, txtPaint)
            } else if (i == 3) {
                txtPaint.setTextAlign(Paint.Align.CENTER)
                canvas.drawText(titles[i], endX, endY * 1.05f, txtPaint)
            } else if (i == 4) {
                txtPaint.setTextAlign(Paint.Align.RIGHT)
                canvas.drawText(titles[i], endX * 0.99f, endY * 1.05f, txtPaint)
            } else if (i == 5) {
                txtPaint.setTextAlign(Paint.Align.RIGHT)
                canvas.drawText(titles[i], endX * 0.99f, endY, txtPaint)
            }


            canvas.drawLine(centerX.toFloat(), centerY.toFloat(), endX, endY, linePaint)
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

    fun setPercents(percents: Array<Double?>?) {
        this.percents = percents
        invalidate()
    }

    private fun drawText(canvas: Canvas) {
        for (i in 0..<count) {
            //获取到雷达图最外边的坐标
            val x = (centerX + sin((angle * i).toDouble()) * (radius + 12) * 1.1f).toFloat()
            val y = (centerY - cos((angle * i).toDouble()) * (radius + 12) * 1.1f).toFloat()
            val ff = angle * i
            Log.d("顯示文字", "x:" + x + "\ny:" + y + "\nff" + ff)
            if (angle * i == 0f) {
                //第一个文字位于顶角正上方
                txtPaint.setTextAlign(Paint.Align.CENTER)
                canvas.drawText(titles[i], x, y - 18, txtPaint)
                txtPaint.setTextAlign(Paint.Align.LEFT)
                Log.d("顯示文字0", titles[i])
            } else if (angle * i > 0 && angle * i < Math.PI / 2) {
                //微调
                canvas.drawText(titles[i], x + 18, y + 10, txtPaint)
                Log.d("顯示文字1", titles[i])
            } else if (angle * i >= Math.PI / 2 && angle * i < Math.PI) {
                //最右下的文字获取到文字的长、宽，按文字长度百分比向左移
                val txt = titles[i]
                val bounds = Rect()
                txtPaint.getTextBounds(txt, 0, txt.length, bounds)
                val height = (bounds.bottom - bounds.top).toFloat()
                val width = txtPaint.measureText(txt)
                canvas.drawText(txt, x - width * 0.4f, y + height + 18, txtPaint)
                Log.d("顯示文字2", titles[i])
            } else if (angle * i >= Math.PI && angle * i < 3 * Math.PI / 2) {
                //同理最左下的文字获取到文字的长、宽，按文字长度百分比向左移
                val txt = titles[i]
                val bounds = Rect()
                txtPaint.getTextBounds(txt, 0, txt.length, bounds)
                val width = txtPaint.measureText(txt)
                val height = (bounds.bottom - bounds.top).toFloat()
                canvas.drawText(txt, x - width * 0.6f, y + height + 18, txtPaint)
                Log.d("顯示文字3", titles[i])
            } else if (angle * i >= 3 * Math.PI / 2 && angle * i < 2 * Math.PI) {
                //文字向左移动
                val txt = titles[i]
                val width = txtPaint.measureText(txt)
                canvas.drawText(txt, x - width - 18, y + 10, txtPaint)
                Log.d("顯示文字4", titles[i])
            }
        }
    }

    private fun drawRegion(canvas: Canvas) {
        if (percents == null) {
            return
        }
        val path = Path()
        val r = radius / layerCount //每层的间距
        for (i in 0..<count) {
            if (i == 0) {
                path.moveTo(
                    centerX.toFloat(),
                    (centerY - r - (radius - r) * percents!![i]!!).toFloat()
                )
            } else {
                val x =
                    (centerX + sin((angle * i).toDouble()) * (percents!![i]!! * (radius - r) + r)).toFloat()
                val y =
                    (centerY - cos((angle * i).toDouble()) * (percents!![i]!! * (radius - r) + r)).toFloat()
                path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, regionColorPaint)
    }
}

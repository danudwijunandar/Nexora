package com.example.nexora1.ui.activity

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.nexora1.R
import java.util.Calendar

class WeeklyChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bluePrimary = ContextCompat.getColor(context, R.color.colorPrimary)
    
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bluePrimary
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bluePrimary
        style = Paint.Style.FILL
    }

    private val pointOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bluePrimary
        strokeWidth = 3f
    }

    private val path = Path()
    private val fillPath = Path()
    private var points = emptyList<PointF>()
    private var dataValues = listOf(0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f)
    private var selectedDayIndex = -1

    init {
        val cal = Calendar.getInstance()
        var day = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (day < 0) day = 6
        selectedDayIndex = day
    }

    fun setData(values: List<Float>) {
        if (values.size == 7) {
            dataValues = values
            invalidate()
        }
    }

    fun setSelectedDay(index: Int) {
        selectedDayIndex = index
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculatePoints()
    }

    private fun calculatePoints() {
        if (width == 0 || height == 0) return
        
        val w = width.toFloat()
        val h = height.toFloat()
        
        val stepX = w / 7f
        points = dataValues.mapIndexed { index, value ->
            PointF((index + 0.5f) * stepX, h * value)
        }

        updatePaths(w, h)
        updateGradient(h)
    }

    private fun updatePaths(w: Float, h: Float) {
        if (points.isEmpty()) return
        path.reset()
        
        path.moveTo(points[0].x, points[0].y)
        
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val midX = (p1.x + p2.x) / 2
            path.cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
        }

        fillPath.set(path)
        fillPath.lineTo(points.last().x, h)
        fillPath.lineTo(points.first().x, h)
        fillPath.close()
    }

    private fun updateGradient(h: Float) {
        val startColor = Color.argb(60, Color.red(bluePrimary), Color.green(bluePrimary), Color.blue(bluePrimary))
        fillPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            startColor,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calculatePoints()
        if (points.isEmpty()) return

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)
        drawIndicator(canvas)
    }

    private fun drawIndicator(canvas: Canvas) {
        if (points.isEmpty()) return
        if (selectedDayIndex < 0 || selectedDayIndex >= points.size) return
        
        val h = height.toFloat()
        val p = points[selectedDayIndex]
        
        canvas.drawLine(p.x, p.y, p.x, h, indicatorPaint)
        canvas.drawCircle(p.x, p.y, 12f, pointOutlinePaint)
        canvas.drawCircle(p.x, p.y, 8f, pointPaint)
    }
}
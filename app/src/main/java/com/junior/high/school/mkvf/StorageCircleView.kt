package com.junior.high.school.mkvf

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * 自定义双圆环存储进度View
 */
class StorageCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔
    private val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerRingBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerRingBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerRingProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 圆环宽度
    private val outerRingWidth = 50f
    private val innerRingWidth = 40f
    private val borderWidth = 1f

    // 进度（0-100）
    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // 外圆环白色边框
        outerBorderPaint.color = Color.WHITE
        outerBorderPaint.style = Paint.Style.STROKE
        outerBorderPaint.strokeWidth = outerRingWidth + borderWidth * 2

        // 外圆环背景（完整的渐变圆环）
        outerRingBgPaint.style = Paint.Style.STROKE
        outerRingBgPaint.strokeWidth = outerRingWidth
        outerRingBgPaint.strokeCap = Paint.Cap.ROUND

        // 内圆环背景（完整的渐变圆环）
        innerRingBgPaint.style = Paint.Style.STROKE
        innerRingBgPaint.strokeWidth = innerRingWidth
        innerRingBgPaint.strokeCap = Paint.Cap.ROUND

        // 内圆环进度（覆盖在背景上的渐变进度条）
        innerRingProgressPaint.style = Paint.Style.STROKE
        innerRingProgressPaint.strokeWidth = innerRingWidth
        innerRingProgressPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = min(width, height) / 2f

        // 1. 绘制外圆环背景（完整的渐变圆环）
        val outerRadius = maxRadius - outerRingWidth / 2

        // 外圆环渐变色（从#DBE8F7到#75ACE8）
        val outerGradient = SweepGradient(
            centerX, centerY,
            intArrayOf(
                Color.parseColor("#DBE8F7"),
                Color.parseColor("#75ACE8"),
                Color.parseColor("#DBE8F7")
            ),
            null
        )
        outerRingBgPaint.shader = outerGradient

        // 绘制外圆环白色边框
        canvas.drawCircle(centerX, centerY, outerRadius, outerBorderPaint)
        // 绘制完整的外圆环
        canvas.drawCircle(centerX, centerY, outerRadius, outerRingBgPaint)

        // 2. 绘制内圆环背景（完整的渐变圆环）
        val innerRadius = outerRadius - outerRingWidth / 2 - innerRingWidth / 2 - 5f

        // 内圆环背景渐变色（淡色，从#DBE8F7到#75ACE8）
        val innerBgGradient = SweepGradient(
            centerX, centerY,
            intArrayOf(
                Color.parseColor("#DBE8F7"),
                Color.parseColor("#75ACE8"),
                Color.parseColor("#87BFED")
            ),
            null
        )
        innerRingBgPaint.shader = innerBgGradient

        // 绘制完整的内圆环背景
        canvas.drawCircle(centerX, centerY, innerRadius, innerRingBgPaint)

        // 3. 绘制内圆环进度（覆盖在背景上）
        if (progress > 0) {
            val innerRect = RectF(
                centerX - innerRadius,
                centerY - innerRadius,
                centerX + innerRadius,
                centerY + innerRadius
            )

            // 内圆环进度渐变色（从#D7E4F5到#4C90F5）
            val innerProgressGradient = SweepGradient(
                centerX, centerY,
                intArrayOf(
                    Color.parseColor("#DBE8F7"),
                    Color.parseColor("#75ACE8"),
                    Color.parseColor("#87BFED"),
                    Color.parseColor("#4C90F5")
                ),
                null
            )
            innerRingProgressPaint.shader = innerProgressGradient

            // 绘制进度圆弧（从-90度开始，即从顶部开始）
            canvas.drawArc(innerRect, -90f, progress * 3.6f, false, innerRingProgressPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }
}

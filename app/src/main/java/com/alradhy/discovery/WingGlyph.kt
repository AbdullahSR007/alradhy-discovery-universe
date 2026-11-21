package com.alradhy.discovery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import kotlin.math.min

internal class ProgressTrack(context: Context, private val accent: Int, private val fraction: Float) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val radius = height / 2f
        paint.color = 0xff26354e.toInt()
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        if (fraction > 0f) {
            paint.color = accent
            canvas.drawRoundRect(width * (1 - fraction), 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        }
    }
}

internal class WingGlyph(context: Context, private val kind: String, color: Int) : View(context) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val shape = Path()
    private val oval = RectF(3f, 10f, 21f, 14f)

    override fun onDraw(canvas: Canvas) {
        val side = min(width, height) * .84f
        if (side <= 0) return
        canvas.save()
        canvas.translate((width - side) / 2, (height - side) / 2)
        canvas.scale(side / 24, side / 24)
        when (kind) {
            "scale" -> {
                circle(canvas, 10f, 10f, 6f)
                lines(canvas, 15f, 15f, 21f, 21f)
                lines(canvas, 7f, 10f, 13f, 10f)
                lines(canvas, 10f, 7f, 10f, 13f)
            }
            "mechanics" -> {
                circle(canvas, 12f, 12f, 9f)
                circle(canvas, 10f, 9f, .7f)
                circle(canvas, 15f, 11f, .7f)
                circle(canvas, 13f, 6f, .7f)
            }
            "fields" -> lines(canvas, 13f, 2f, 4f, 13f, 10f, 13f, 9f, 22f, 20f, 10f, 14f, 10f, 13f, 2f)
            "light" -> {
                lines(canvas, 12f, 2f, 21f, 7f, 21f, 17f, 12f, 22f, 3f, 17f, 3f, 7f, 12f, 2f)
                lines(canvas, 3f, 7f, 12f, 12f, 21f, 7f)
                lines(canvas, 12f, 12f, 12f, 22f)
            }
            "astro" -> {
                circle(canvas, 12f, 12f, 6f)
                canvas.save()
                canvas.rotate(39f, 12f, 12f)
                canvas.drawOval(oval, ink)
                canvas.restore()
            }
            "telecom" -> {
                lines(canvas, 3f, 9f, 21f, 9f)
                lines(canvas, 12f, 5f, 12f, 22f)
                lines(canvas, 5f, 6f, 5f, 12f)
                lines(canvas, 8.5f, 5f, 8.5f, 13f)
                lines(canvas, 16f, 6f, 16f, 12f)
                lines(canvas, 20f, 4f, 20f, 14f)
            }
            "matter" -> {
                circle(canvas, 6f, 17f, 3f)
                circle(canvas, 13f, 10f, 3.5f)
                circle(canvas, 19f, 5f, 2.5f)
                lines(canvas, 8f, 15f, 10.5f, 12.5f)
                lines(canvas, 15.5f, 7.5f, 17f, 6f)
                lines(canvas, 4f, 20f, 2.5f, 22f)
            }
            "progress" -> {
                lines(canvas, 7f, 3f, 17f, 3f, 17f, 12f, 14f, 16f, 10f, 16f, 7f, 12f, 7f, 3f)
                lines(canvas, 7f, 6f, 3f, 6f, 3f, 11f, 7f, 13f)
                lines(canvas, 17f, 6f, 21f, 6f, 21f, 11f, 17f, 13f)
                lines(canvas, 12f, 16f, 12f, 21f)
                lines(canvas, 8f, 21f, 16f, 21f)
            }
            "settings" -> {
                circle(canvas, 12f, 12f, 7f)
                circle(canvas, 12f, 12f, 3f)
                for (i in 0..7) {
                    canvas.save()
                    canvas.rotate(i * 45f, 12f, 12f)
                    lines(canvas, 12f, 3f, 12f, 5f)
                    canvas.restore()
                }
            }
            else -> {
                circle(canvas, 12f, 12f, 3.4f)
                canvas.drawArc(3f, 3f, 21f, 21f, 202f, 98f, false, ink)
                canvas.drawArc(3f, 3f, 21f, 21f, 24f, 98f, false, ink)
                circle(canvas, 4.5f, 16f, .7f)
                circle(canvas, 19.5f, 8f, .7f)
            }
        }
        canvas.restore()
    }

    private fun circle(canvas: Canvas, x: Float, y: Float, r: Float) = canvas.drawCircle(x, y, r, ink)

    private fun lines(canvas: Canvas, vararg points: Float) {
        shape.reset()
        shape.moveTo(points[0], points[1])
        for (i in 2 until points.size step 2) shape.lineTo(points[i], points[i + 1])
        canvas.drawPath(shape, ink)
    }
}

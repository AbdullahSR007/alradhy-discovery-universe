package com.alradhy.discovery

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.random.Random

internal class GalaxyBackground : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stars = Random(672).let { random -> FloatArray(125) { random.nextFloat() } }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        paint.shader = LinearGradient(bounds.left.toFloat(), bounds.bottom.toFloat(), bounds.right.toFloat(),
            bounds.top.toFloat(), 0xff102d3e.toInt(), 0xff10162c.toInt(), Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        val shader = paint.shader
        canvas.drawRoundRect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(),
            bounds.bottom.toFloat(), 24f, 24f, paint)
        paint.shader = null
        for (i in stars.indices step 5) {
            val x = bounds.left + bounds.width() * stars[i]
            val y = bounds.top + bounds.height() * stars[i + 1]
            paint.color = if (i % 15 == 0) 0xaa5eead4.toInt() else 0x405f88a7
            canvas.drawCircle(x, y, 1 + 1.7f * stars[i + 2], paint)
        }
        paint.shader = shader
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

internal class ScaleRuler(context: Context) : View(context) {
    var exponent = 0.0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }

    override fun onDraw(canvas: Canvas) {
        val left = width * .1f
        val right = width * .9f
        val middle = height * .49f
        if (middle <= 1) return
        fun x(exponent: Double) = (left + (right - left) * (exponent + 35) / 62).toFloat()
        paint.strokeWidth = 2f
        paint.color = 0xff385271.toInt()
        canvas.drawLine(left, middle, right, middle, paint)
        for (power in -35..27) {
            val long = power % 10 == 0 || power == -35 || power == 27
            if (!long && power % 5 != 0) continue
            paint.color = if (long) muted else 0xff385271.toInt()
            val half = height * if (long) .18f else .1f
            canvas.drawLine(x(power.toDouble()), middle - half, x(power.toDouble()), middle + half, paint)
        }
        paint.color = teal
        canvas.drawCircle(x(exponent), middle, 6 * resources.displayMetrics.density, paint)
        paint.textSize = minOf(13f * resources.displayMetrics.density, height * .27f)
        paint.color = muted
        if (paint.textSize > 0) {
            canvas.drawText("10⁻³⁵", left, height * .92f, paint)
            val label = "10²⁷"
            canvas.drawText(label, right - paint.measureText(label), height * .92f, paint)
        }
    }
}

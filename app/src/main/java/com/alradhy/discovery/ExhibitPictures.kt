package com.alradhy.discovery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import kotlin.math.log10

internal class ExhibitPictures {
    private val bitmaps = mutableMapOf<String, Bitmap>()

    fun view(context: Context, key: String) = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        outlineProvider = ViewOutlineProvider.BOUNDS
        clipToOutline = true
        cropToPadding = true
        show(this, key)
    }

    fun show(view: ImageView, key: String) {
        if (view.tag == key) return
        val bitmap = bitmaps.getOrPut(key) {
            view.context.assets.open("exhibits/$key.webp").use { stream ->
                checkNotNull(BitmapFactory.decodeStream(stream)) { "Could not decode exhibit $key" }
            }
        }
        view.setImageBitmap(bitmap)
        view.tag = key
    }
}

/** A logarithmic time scale; dots record evidence dates, not asserted ancestors. */
internal class LifeRuler(context: Context, private val ages: List<Long>) : View(context) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val density = resources.displayMetrics.density
    private val span = log10(ages.first() + 1.0)

    var selected = 0
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val inset = 12 * density
        val baseline = height * .35f
        fun x(age: Long): Float = inset + (width - 2 * inset) * (1 - log10(age + 1.0) / span).toFloat()
        ink.color = 0xff3b4d67.toInt()
        ink.strokeWidth = 1.2f * density
        canvas.drawLine(inset, baseline, width - inset, baseline, ink)
        ages.forEachIndexed { index, age ->
            if (index != selected) {
                ink.color = 0xff82a59b.toInt()
                canvas.drawCircle(x(age), baseline, 2.1f * density, ink)
            }
        }
        ink.color = gold
        canvas.drawCircle(x(ages[selected]), baseline, 4.7f * density, ink)
        ink.color = muted
        ink.textSize = 10 * density
        ink.textAlign = Paint.Align.LEFT
        canvas.drawText("4.2 Ga", inset, height * .88f, ink)
        ink.textAlign = Paint.Align.RIGHT
        canvas.drawText("NOW", width - inset, height * .88f, ink)
    }
}

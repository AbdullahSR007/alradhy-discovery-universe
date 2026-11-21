package com.alradhy.discovery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.SystemClock
import android.view.View
import com.alradhy.discoverylab.LegacyLabArt
import com.alradhy.discoverylab.ScienceExtensions as Science
import kotlin.math.*
import kotlin.random.Random

internal class ExperimentView(context: Context, val topic: Topic, private val accent: Int) : View(context) {
    var values = topic.defaults()
    var baseline: DoubleArray? = null
    var compare = false
    var running = true
    var speed = 1.0
    var drag: ((Float, Float, Boolean) -> Unit)? = null
    private var dragX = 0f
    private var dragY = 0f
    private var elapsed = 0.0
    private var lastFrame = 0L
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val legacy = LegacyLabArt()
    private val stars = Random(551).let { random -> FloatArray(96) { random.nextFloat() } }
    private val noise = java.util.Random(740).let { random -> DoubleArray(256) { random.nextGaussian() } }

    fun rewind() {
        elapsed = 0.0
        lastFrame = 0L
        invalidate()
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        val listener = drag ?: return false
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                dragX = event.x
                dragY = event.y
                parent.requestDisallowInterceptTouchEvent(true)
                listener(0f, 0f, true)
            }
            android.view.MotionEvent.ACTION_MOVE -> listener(
                (event.x - dragX) / width.coerceAtLeast(1), (dragY - event.y) / height.coerceAtLeast(1), false
            )
            android.view.MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
                performClick()
            }
            android.view.MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
            else -> return false
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    override fun onDraw(canvas: Canvas) {
        val now = SystemClock.uptimeMillis()
        if (running && lastFrame > 0) elapsed += (now - lastFrame).coerceIn(0, 40) / 1000.0 * speed
        lastFrame = now
        ink.style = Paint.Style.FILL
        ink.color = 0xff0b1425.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), ink)
        val saved = baseline
        val split = compare && saved != null
        val paneWidth = if (split) width / 2f else width.toFloat()
        drawPane(canvas, values, 0f, paneWidth, if (split) "CURRENT" else "")
        if (split && saved != null) {
            drawPane(canvas, saved, paneWidth, paneWidth, "BASELINE")
            line(canvas, paneWidth, 0f, paneWidth, height.toFloat(), 0xff64748b.toInt(), 2f)
        }
        if (running) {
            when (topic.id) {
                "telecom.link-budget", "telecom.fresnel", "fields.rc", "fields.wire" -> Unit
                else -> postInvalidateOnAnimation()
            }
        }
    }

    private fun drawPane(c: Canvas, p: DoubleArray, left: Float, width: Float, tag: String) {
        c.save()
        c.clipRect(left, 0f, left + width, height.toFloat())
        c.translate(left, 0f)
        c.scale(width / 1000f, height / 440f)
        if (topic.legacy != null) {
            val a = topic.controls[0]
            val b = topic.controls[1]
            legacy.draw(c, topic.legacy, p[0], p[1], p.getOrElse(2) { 2.0 },
                (p[0] - a.min) / (a.max - a.min), (p[1] - b.min) / (b.max - b.min), elapsed)
        } else {
            for (i in stars.indices step 2) {
                val y = stars[i + 1] * 424 + 8
                dot(c, stars[i] * 980 + 10, y, if (i % 6 == 0) 2.3f else 1.3f, 0xff30425e.toInt())
            }
            when (topic.id) {
                "telecom.link-budget" -> waterfall(c, p)
                "telecom.fresnel" -> fresnel(c, p)
                "telecom.qam" -> qam(c, p)
                "telecom.orbit-latency" -> latency(c, p)
                "light.galileo" -> lantern(c, p)
                "light.roemer" -> roemer(c, p)
                "light.fizeau" -> fizeau(c, p)
                "fields.rc" -> capacitor(c, p)
                "fields.wire" -> wire(c, p)
            }
        }
        if (tag.isNotEmpty()) {
            ink.style = Paint.Style.FILL
            ink.color = 0xee09111e.toInt()
            c.drawRoundRect(20f, 7f, 202f, 47f, 12f, 12f, ink)
            text(c, tag, 34f, 36f, 20f, 0xffe8eef9.toInt())
        }
        c.restore()
    }

    private fun waterfall(c: Canvas, p: DoubleArray) {
        val stages = Science.linkStages(p)
        val labels = arrayOf("TX", "CABLE", "TX GAIN", "FSPL", "RAIN", "RX GAIN", "RX")
        val low = min(-135.0, stages.min() - 8)
        val high = max(55.0, stages.max() + 8)
        fun y(v: Double) = (68 + 247 * (high - v) / (high - low)).toFloat()
        val threshold = y(p[8])
        line(c, 59f, threshold, 955f, threshold, 0xfffbbf24.toInt(), 2f)
        text(c, "SENS  " + fmt(p[8], 0) + " dBm", 680f, 62f, 20f, 0xfffbbf24.toInt())
        stages.forEachIndexed { i, level ->
            val x = 52f + 131 * i
            val previous = if (i == 0) 0.0 else stages[i - 1]
            val a = y(level)
            val b = y(previous)
            val color = if (level >= previous) accent else 0xfffb7185.toInt()
            ink.color = color
            ink.style = Paint.Style.FILL
            c.drawRoundRect(x, min(a,b) - 2, x + 94, max(a,b) + 2, 5f, 5f, ink)
            if (i < stages.lastIndex) line(c, x + 94, a, x + 131, a, 0xff64748b.toInt(), 2f)
            text(c, fmt(level, 1), x + 3, min(a,b) - 9, 23f, 0xffe8eef9.toInt())
            text(c, labels[i], x, 337f, 19f, 0xffa9bdd6.toInt())
        }
        text(c, "Δ " + fmt(stages.last() - p[8], 1) + " dB", 705f, 406f, 28f, accent)
    }

    private fun fresnel(c: Canvas, p: DoubleArray) {
        val distance = p[0]
        val fraction = (p[3] / 100).toFloat()
        val verticalMax = maxOf(p[4], p[5], p[2] + Science.earthBulge(distance, p[6], fraction.toDouble()),
            max(p[4], p[5]) + Science.fresnelRadius(distance, p[1], .5) + 5)
        fun y(m: Double) = 351f - (260 * m / verticalMax).toFloat()
        fun los(f: Float) = p[4] + (p[5] - p[4]) * f
        val start = 72f
        val end = 928f
        path.reset()
        for (i in 0..160) {
            val f = if (i <= 80) i / 80f else (160 - i) / 80f
            val radius = Science.fresnelRadius(distance, p[1], f.toDouble())
            val pos = y(los(f) + if (i <= 80) radius else -radius)
            if (i == 0) path.moveTo(start, pos) else path.lineTo(start + (end - start) * f, pos)
        }
        path.close()
        ink.style = Paint.Style.FILL
        ink.color = (accent and 0x00ffffff) or 0x40000000
        c.drawPath(path, ink)
        ink.style = Paint.Style.STROKE
        ink.strokeWidth = 3f
        ink.color = accent
        c.drawPath(path, ink)
        for (i in 1..80) {
            val a = (i - 1) / 80.0
            val b = i / 80.0
            line(c, start + (end - start) * a.toFloat(), y(Science.earthBulge(distance, p[6], a)),
                start + (end - start) * b.toFloat(), y(Science.earthBulge(distance, p[6], b)), 0xff8394ae.toInt(), 2f)
        }
        line(c, start, y(p[4]), end, y(p[5]), 0xffe8eef9.toInt(), 3f)
        val obstacleX = start + (end - start) * fraction
        val bulge = Science.earthBulge(distance, p[6], fraction.toDouble())
        val color = if (Science.clearancePercent(p) >= 60) 0xff4ade80.toInt() else 0xfffb7185.toInt()
        line(c, obstacleX, y(bulge), obstacleX, y(p[2] + bulge), color, 19f)
        line(c, obstacleX + 18, y(los(fraction)), obstacleX + 18, y(p[2] + bulge), color, 3f)
        dot(c, start, y(p[4]), 12f, accent)
        dot(c, end, y(p[5]), 12f, accent)
        text(c, "TX", start - 17, y(p[4]) - 22, 25f, accent)
        text(c, "RX", end - 18, y(p[5]) - 22, 25f, accent)
        text(c, "F1   " + fmt(Science.clearancePercent(p), 1) + "%", 650f, 407f, 28f, color)
    }

    private fun qam(c: Canvas, p: DoubleArray) {
        val order = 1 shl Science.qamBits(p[0])
        val side = sqrt(order.toDouble()).toInt()
        val sigma = Science.evmPercent(p[1]) / 100 / sqrt(2.0)
        val amplitude = sqrt(2.0 * (order - 1) / 3)
        val cx = 500f
        val cy = 207f
        val scale = 120f
        line(c, 170f, cy, 830f, cy, 0xff60738e.toInt(), 2f)
        line(c, cx, 23f, cx, 396f, 0xff60738e.toInt(), 2f)
        for (i in 0 until 128) {
            val code = (i * 73 + (elapsed * 2).toInt() * 29) % order
            val x = ((2 * (code % side) - side + 1) / amplitude + sigma * noise[i * 2]) * scale
            val y = ((2 * (code / side) - side + 1) / amplitude + sigma * noise[i * 2 + 1]) * scale
            dot(c, cx + x.toFloat(), cy - y.toFloat(), 4f, 0xadbba5fa.toInt())
        }
        for (i in 0 until order) {
            val x = ((2 * (i % side) - side + 1) / amplitude * scale).toFloat()
            val y = ((2 * (i / side) - side + 1) / amplitude * scale).toFloat()
            dot(c, cx + x, cy - y, if (side > 8) 3f else 6f, accent)
        }
        text(c, "$order-QAM", 43f, 67f, 32f, accent)
        text(c, "SNR " + fmt(p[1], 0) + " dB", 43f, 109f, 25f, 0xffe8eef9.toInt())
        text(c, "I", 837f, cy - 12, 25f, accent)
        text(c, "Q", cx + 14, 46f, 25f, accent)
    }

    private fun latency(c: Canvas, p: DoubleArray) {
        val sy = (295 - 205 * ln(p[0] / 300) / ln(42000.0 / 300)).toFloat()
        val horizon = 332f
        line(c, 132f, horizon, 500f, sy, 0xff536b88.toInt(), 4f)
        line(c, 500f, sy, 868f, horizon, 0xff536b88.toInt(), 4f)
        val phase = (elapsed / (1.4 + Science.satelliteRtt(p[0], p[1]) / 180) % 1)
        val x = (132 + phase * 736).toFloat()
        val y = horizon - (horizon - sy) * (1 - abs(phase * 2 - 1)).toFloat()
        dot(c, 132f, horizon, 14f, accent)
        dot(c, 868f, horizon, 14f, accent)
        dot(c, 500f, sy, 21f, 0xfffbbf24.toInt())
        dot(c, x, y, 10f, 0xffe8eef9.toInt())
        text(c, "RTT  " + fmt(Science.satelliteRtt(p[0], p[1]), 1) + " ms", 610f, 69f, 29f, accent)
        text(c, fmt(p[0], 0) + " km", 340f, sy - 26, 25f, accent)
    }

    private fun lantern(c: Canvas, p: DoubleArray) {
        val step = (elapsed % 2) / 2
        for (x in listOf(130f, 870f)) {
            dot(c, x, 248f, 32f, 0xfffbbf24.toInt())
            line(c, x, 288f, x, 350f, 0xff64748b.toInt(), 12f)
        }
        line(c, 130f, 248f, 870f, 248f, 0xff52637c.toInt(), 4f)
        dot(c, (130 + 740 * (1 - abs(2 * step - 1))).toFloat(), 248f, 11f, accent)
        text(c, "2 × " + fmt(p[0], 0) + " m", 390f, 216f, 35f, accent)
        text(c, "Δt  " + fmt(Science.galileoMicroseconds(p[0])) + " µs", 350f, 381f, 28f, 0xffe8eef9.toInt())
    }

    private fun roemer(c: Canvas, p: DoubleArray) {
        val earth = (503 - p[0] * 41).toFloat()
        line(c, earth, 222f, 814f, 222f, 0xff52637c.toInt(), 4f)
        dot(c, 814f, 222f, 49f, 0xfffdba74.toInt())
        dot(c, 814f + (90 * cos(elapsed * 2)).toFloat(), 222f + (65 * sin(elapsed * 2)).toFloat(), 9f, accent)
        dot(c, earth, 222f, 33f, 0xff60a5fa.toInt())
        dot(c, 510f, 222f, 21f, 0xfffbbf24.toInt())
        text(c, "Δt  " + fmt(Science.roemerDelay(p[0]), 1) + " s", 276f, 377f, 30f, accent)
    }

    private fun fizeau(c: Canvas, p: DoubleArray) {
        val intensity = Science.fizeauBrightness(p[0], p[1])
        val glow = (48 + intensity * 2).toInt()
        line(c, 105f, 183f, 865f, 183f, 0xfffbbf24.toInt(), 5f)
        line(c, 865f, 247f, 105f, 247f, Color.argb(glow, 192, 132, 252), 7f)
        dot(c, 363f, 215f, 93f, 0xff172846.toInt())
        for (i in 0 until 24) {
            val angle = i * PI / 12 + elapsed * p[0] / 500
            line(c, 363 + (66 * cos(angle)).toFloat(), 215 + (66 * sin(angle)).toFloat(),
                363 + (97 * cos(angle)).toFloat(), 215 + (97 * sin(angle)).toFloat(), accent, 7f)
        }
        line(c, 865f, 141f, 865f, 285f, 0xffe8eef9.toInt(), 10f)
        text(c, "RETURN  " + fmt(intensity, 1) + "%", 616f, 366f, 28f, accent)
    }

    private fun capacitor(c: Canvas, p: DoubleArray) {
        line(c, 175f, 120f, 825f, 120f, accent, 6f)
        line(c, 175f, 330f, 825f, 330f, accent, 6f)
        line(c, 175f, 120f, 175f, 330f, accent, 5f)
        line(c, 825f, 120f, 825f, 193f, accent, 5f)
        line(c, 825f, 257f, 825f, 330f, accent, 5f)
        line(c, 762f, 193f, 888f, 193f, 0xffe8eef9.toInt(), 10f)
        line(c, 762f, 257f, 888f, 257f, 0xffe8eef9.toInt(), 10f)
        for (i in 0..9) {
            if (i < reading(topic, p) * 2) dot(c, 765f + i * 13, 219f, 5f, accent)
        }
        text(c, "5 V", 83f, 234f, 29f, accent)
        text(c, "V(C) = " + fmt(reading(topic,p)) + " V", 340f, 231f, 38f, accent)
    }

    private fun wire(c: Canvas, p: DoubleArray) {
        ink.style = Paint.Style.STROKE
        ink.strokeWidth = 3f
        for (i in 1..5) {
            ink.color = Color.argb((35 + p[1] * 10 - i * 4).toInt().coerceIn(20, 255), 251, 146, 60)
            c.drawCircle(468f, 211f, i * 35f, ink)
        }
        dot(c, 468f, 211f, 17f, accent)
        val r = (min(50.0, p[0]) / 50 * 175).toFloat()
        line(c, 468f, 211f, 468f + r, 211f, 0xffe8eef9.toInt(), 3f)
        dot(c, 468f + r, 211f, 8f, 0xff4ade80.toInt())
        text(c, "B  " + fmt(reading(topic, p)) + " µT", 707f, 120f, 27f, accent)
    }

    private fun line(c: Canvas, a: Float, b: Float, x: Float, y: Float, color: Int, stroke: Float) {
        ink.color = color
        ink.strokeWidth = stroke
        c.drawLine(a, b, x, y, ink)
    }

    private fun dot(c: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        ink.color = color
        ink.style = Paint.Style.FILL
        c.drawCircle(x, y, radius, ink)
    }

    private fun text(c: Canvas, text: String, x: Float, y: Float, size: Float, color: Int) {
        ink.color = color
        ink.typeface = Typeface.MONOSPACE
        ink.style = Paint.Style.FILL
        ink.textSize = size
        c.drawText(text, x, y, ink)
    }
}

internal class CurveView(context: Context, private val topic: Topic, private val accent: Int) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val dashes = android.graphics.DashPathEffect(floatArrayOf(9f, 7f), 0f)
    private val axis = graphAxis(topic)
    private var sampled = DoubleArray(65)
    private var old: DoubleArray? = null
    private var dotX = .5
    private var dotY = 0.0
    private var low = 0.0
    private var high = 1.0

    fun update(values: DoubleArray, baseline: DoubleArray?) {
        val control = topic.controls[axis]
        fun sample(values: DoubleArray): DoubleArray {
            val probe = values.copyOf()
            return DoubleArray(65) {
                probe[axis] = control.min + (control.max - control.min) * it / 64
                reading(topic, probe)
            }
        }
        sampled = sample(values)
        old = baseline?.let(::sample)
        dotX = (values[axis] - control.min) / (control.max - control.min)
        dotY = reading(topic, values)
        val finite = (sampled.toList() + old.orEmptyList() + dotY).filter { it.isFinite() }
        low = finite.minOrNull() ?: 0.0
        high = finite.maxOrNull() ?: 1.0
        if (abs(high - low) < 1e-9) {
            val margin = max(abs(high) * .05, .0001)
            low -= margin
            high += margin
        }
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val left = 8f
        val right = width - 8f
        val top = 8f
        val bottom = height - 8f
        if (right <= left || bottom <= top) return
        fun y(value: Double) = bottom - ((value - low) / (high - low)).toFloat() * (bottom - top)
        paint.strokeWidth = 1f
        paint.color = 0xff2e4160.toInt()
        for (i in 0..4) {
            val row = top + (bottom - top) * i / 4
            c.drawLine(left, row, right, row, paint)
        }
        fun curve(values: DoubleArray, dashed: Boolean) {
            path.reset()
            var connected = false
            values.forEachIndexed { i, value ->
                if (value.isFinite()) {
                    val x = left + (right - left) * i / (values.size - 1)
                    if (connected) path.lineTo(x, y(value)) else path.moveTo(x, y(value))
                    connected = true
                } else connected = false
            }
            paint.color = if (dashed) 0xffb4c4d9.toInt() else accent
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.pathEffect = if (dashed) dashes else null
            c.drawPath(path, paint)
            paint.pathEffect = null
        }
        old?.let { curve(it, true) }
        curve(sampled, false)
        if (dotY.isFinite()) {
            paint.color = 0xfffbbf24.toInt()
            paint.style = Paint.Style.FILL
            c.drawCircle(left + (right - left) * dotX.toFloat(), y(dotY), 6f, paint)
        }
    }

    private fun DoubleArray?.orEmptyList() = this?.toList().orEmpty()
}

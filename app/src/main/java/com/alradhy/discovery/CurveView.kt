package com.alradhy.discovery

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View
import com.alradhy.discoverylab.GraphSweep
import com.alradhy.discoverylab.MuseumMeasurements
import kotlin.math.*

internal class CurveView(context: Context, private val topic: Topic, private val accent: Int) : View(context) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val path = Path()
    private val dashes = DashPathEffect(floatArrayOf(9f, 7f), 0f)
    private val density = resources.displayMetrics.density
    private val live = 0xff4ade80.toInt()
    private val savedColor = 0xff93a8c6.toInt()
    private val logarithmic = topic.id == "scale.power-ratio"
    private var axis = graphAxis(topic)
    private var active: GraphSweep.Series? = null
    private var saved: GraphSweep.Series? = null
    private var ticks = emptyList<Double>()

    fun update(values: DoubleArray, baseline: DoubleArray?, selectedAxis: Int) {
        axis = selectedAxis
        val control = topic.controls[axis]
        val step = if (control.discrete) control.step else 0.0
        fun sample(configuration: DoubleArray): GraphSweep.Series {
            val breakpoints = MuseumMeasurements.breakpoints(topic.id, axis, configuration)
            return GraphSweep.sample(control.min, control.max, step, axis, configuration, breakpoints) {
                reading(topic, it)
            }
        }
        active = sample(values)
        saved = baseline?.let(::sample)
        val ordinates = listOfNotNull(active, saved).flatMap { it.y.asIterable() }
            .filter(Double::isFinite).map(::transform)
        ticks = ticks(ordinates.minOrNull() ?: 0.0, ordinates.maxOrNull() ?: 1.0)
        val current = active!!
        contentDescription = control.label + " / " + topic.metric + " • " +
            axisValue(current.markerX, 3) + " → " + fmt(current.markerY, 3) + headlineUnit(topic)
        invalidate()
    }

    private fun transform(value: Double): Double = if (logarithmic) log10(value) else value

    private fun ticks(minimum: Double, maximum: Double): List<Double> {
        val spread = maximum - minimum
        val pad = if (spread > max(abs(maximum), abs(minimum)) * 1e-10) 0.0 else max(abs(maximum) * .1, .01)
        val start = minimum - pad
        val end = maximum + pad
        val rawStep = (end - start) / 3
        val base = 10.0.pow(floor(log10(rawStep)))
        val spacing = listOf(1.0, 2.0, 5.0, 10.0).first { it * base >= rawStep * (1 - 1e-12) } * base
        val lower = floor(start / spacing).toInt()
        val upper = ceil(end / spacing).toInt()
        return (lower..upper).map { it * spacing }
    }

    private fun axisValue(value: Double, digits: Int): String {
        return if (topic.id == "telecom.qam" && axis == 0) (1 shl (2 + 2 * value.roundToInt())).toString()
        else label(value, digits.coerceAtLeast(if (topic.id == "telecom.availability") 4 else 0))
    }

    private fun label(value: Double, digits: Int): String {
        val result = fmt(value, digits)
        if (result.contains('e') || !result.contains('.')) return result
        return result.trimEnd('0').trimEnd('.')
    }

    override fun onDraw(canvas: Canvas) {
        val current = active ?: return
        val control = topic.controls[axis]
        val previous = saved
        val left = 66f * density
        val right = width - 25f * density
        val top = 13f * density
        val bottom = height - 26f * density
        if (right <= left || bottom <= top || ticks.size < 2) return
        fun x(value: Double) = left + ((value - control.min) / (control.max - control.min)).toFloat() * (right - left)
        fun y(value: Double) = bottom - ((transform(value) - ticks.first()) / (ticks.last() - ticks.first())).toFloat() * (bottom - top)
        ink.textSize = 11f * density
        ink.style = Paint.Style.FILL
        val grid = 0xff273753.toInt()
        val ruler = 0xff4f6584.toInt()
        for (tick in ticks) {
            val position = if (logarithmic) y(10.0.pow(tick)) else y(tick)
            ink.color = if (tick == 0.0 && !logarithmic) ruler else grid
            ink.strokeWidth = density
            canvas.drawLine(left, position, right, position, ink)
            ink.color = savedColor
            ink.textAlign = Paint.Align.RIGHT
            canvas.drawText(if (logarithmic) "10^" + label(tick, 1) else label(tick, 4),
                left - 7f * density, position + 3.5f * density, ink)
        }
        val positions = if (control.discrete && (control.max - control.min) / control.step <= 8) {
            (0..((control.max - control.min) / control.step).roundToInt()).map { control.min + it * control.step }
        } else if (control.discrete) {
            (0..4).map { control.min + ((control.max - control.min) / control.step * it / 4).roundToInt() * control.step }
        } else (0..4).map { control.min + (control.max - control.min) * it / 4 }
        ink.color = ruler
        canvas.drawLine(left, top, left, bottom, ink)
        canvas.drawLine(left, bottom, right, bottom, ink)
        positions.forEachIndexed { index, value ->
            ink.color = ruler
            val point = x(value)
            canvas.drawLine(point, bottom, point, bottom + 4f * density, ink)
            ink.textAlign = when (index) {
                0 -> Paint.Align.LEFT
                positions.lastIndex -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            ink.color = savedColor
            canvas.drawText(axisValue(value, 2), point, bottom + 16f * density, ink)
        }
        canvas.save()
        canvas.clipRect(left - 4 * density, top - 3 * density, right + 4 * density, bottom + 3 * density)
        fun drawSeries(series: GraphSweep.Series, baseline: Boolean) {
            ink.color = if (baseline) savedColor else accent
            ink.strokeWidth = (if (baseline) 1.4f else 2.4f) * density
            if (control.discrete) {
                ink.style = Paint.Style.FILL
                series.x.indices.filter { series.y[it].isFinite() }.forEach {
                    val px = x(series.x[it])
                    val py = y(series.y[it])
                    if (baseline) canvas.drawRect(px - 2 * density, py - density, px + 2 * density, py + density, ink)
                    else canvas.drawCircle(px, py, 1.7f * density, ink)
                }
            } else {
                path.reset()
                var connected = false
                for (i in series.x.indices) {
                    val v = series.y[i]
                    if (!v.isFinite()) { connected = false; continue }
                    val px = x(series.x[i])
                    val py = y(v)
                    if (connected) path.lineTo(px, py) else path.moveTo(px, py)
                    connected = true
                }
                ink.style = Paint.Style.STROKE
                ink.pathEffect = if (baseline) dashes else null
                canvas.drawPath(path, ink)
                ink.pathEffect = null
            }
        }
        if (previous != null) drawSeries(previous, true)
        drawSeries(current, false)
        if (previous != null && previous.markerY.isFinite()) {
            ink.style = Paint.Style.STROKE
            ink.strokeWidth = 1.6f * density
            ink.color = savedColor
            canvas.drawCircle(x(previous.markerX), y(previous.markerY), 3.6f * density, ink)
        }
        if (current.markerY.isFinite()) {
            ink.style = Paint.Style.FILL
            ink.color = live
            canvas.drawCircle(x(current.markerX), y(current.markerY), 5.2f * density, ink)
        }
        canvas.restore()
    }
}

package com.alradhy.discovery

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class UniverseCanvasView(context: Context) : View(context) {

    private val category: Int = 1
    var stationIndex: Int = 0
    var visualType: Int = 0
    var simSpeed: Float = 1.0f
    var energyLevel: Float = 1.0f
    private var phase: Float = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starX = FloatArray(100) { (Math.random() * 1920).toFloat() }
    private val starY = FloatArray(100) { (Math.random() * 1080).toFloat() }
    private val starRadius = FloatArray(100) { (Math.random() * 2.5 + 0.8).toFloat() }

    private val palettePrimary = arrayOf(
        "#00E5FF", "#FFD54F", "#4ADE80", "#38BDF8", "#F43F5E",
        "#C084FC", "#FB923C", "#2DD4BF", "#FACC15", "#60A5FA"
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Deep Cosmic Starfield
        paint.style = Paint.Style.FILL
        for (i in starX.indices) {
            val alpha = (130 + 110 * sin((phase * 2f + i).toDouble())).toInt().coerceIn(40, 255)
            paint.color = Color.argb(alpha, 200, 230, 255)
            val sx = (starX[i] % w.coerceAtLeast(1f))
            val sy = (starY[i] % h.coerceAtLeast(1f))
            canvas.drawCircle(sx, sy, starRadius[i], paint)
        }

        val cx = w * 0.23f
        val cy = h * 0.56f
        val baseR = minOf(w, h) * 0.24f
        val accentHex = palettePrimary[(stationIndex + category * 3) % palettePrimary.size]

        // 2. Dynamic Scale Ring Grid
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.parseColor("#1E293B")
        canvas.drawCircle(cx, cy, baseR * 1.08f, paint)

        // Dispatch across 10 procedural simulation engines parameterized by stationIndex (50 distinct visuals)
        when (visualType % 10) {
            0 -> drawAstrolabeAndGeometry(canvas, cx, cy, baseR, accentHex, stationIndex)
            1 -> drawOpticsAndRays(canvas, cx, cy, baseR, accentHex, stationIndex)
            2 -> drawElectromagneticWaves(canvas, cx, cy, baseR, accentHex, stationIndex)
            3 -> drawExpandingCluster(canvas, cx, cy, baseR, accentHex, stationIndex)
            4 -> drawHexagonalArray(canvas, cx, cy, baseR, accentHex, stationIndex)
            5 -> drawSubatomicQuarks(canvas, cx, cy, baseR, accentHex, stationIndex)
            6 -> drawQuantumAtomOrbits(canvas, cx, cy, baseR, accentHex, stationIndex)
            7 -> drawHelixAndMolecular(canvas, cx, cy, baseR, accentHex, stationIndex)
            8 -> drawPlanetaryOrbits(canvas, cx, cy, baseR, accentHex, stationIndex)
            9 -> drawSpiralGalaxyAndWeb(canvas, cx, cy, baseR, accentHex, stationIndex)
        }

        phase += 0.025f * simSpeed
        if (simSpeed > 0f) postInvalidateOnAnimation()
    }

    private fun drawAstrolabeAndGeometry(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        val spokes = 6 + (idx % 7) * 2
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, r, paint)
        paint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, r * 0.75f, paint)
        canvas.drawCircle(cx, cy, r * 0.48f, paint)

        for (i in 0 until spokes) {
            val angle = phase + i * (2 * Math.PI / spokes)
            val x2 = cx + (r * cos(angle)).toFloat()
            val y2 = cy + (r * sin(angle)).toFloat()
            paint.color = Color.parseColor(accent)
            canvas.drawLine(cx, cy, x2, y2, paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 12f * energyLevel, paint)
    }

    private fun drawOpticsAndRays(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(cx, cy - r, cx, cy - 18f, paint)
        canvas.drawLine(cx, cy + 18f, cx, cy + r, paint)

        val rayCount = 3 + (idx % 4)
        for (k in 1..rayCount) {
            val offset = k * (r * 0.18f)
            val pulse = (sin((phase + k).toDouble()) * 14f).toFloat()
            paint.strokeWidth = 3f
            paint.color = if (k % 2 == 0) Color.parseColor(accent) else Color.parseColor("#FFD54F")
            canvas.drawLine(cx - r * 0.9f, cy - offset + pulse, cx + r * 0.9f, cy + offset - pulse, paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor(accent)
        canvas.drawCircle(cx, cy, 9f * energyLevel, paint)
    }

    private fun drawElectromagneticWaves(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        val coils = 3 + (idx % 4)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f
        for (i in -coils..coils) {
            val offset = i * (r * 0.18f)
            paint.color = Color.parseColor(accent)
            canvas.drawOval(cx - r * 0.32f + offset, cy - r * 0.58f, cx + r * 0.32f + offset, cy + r * 0.58f, paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        val particles = 6 + (idx % 6)
        for (i in 0 until particles) {
            val angle = phase * 3f + i * (2 * Math.PI / particles)
            val ex = cx + (r * 0.85f * cos(angle)).toFloat()
            val ey = cy + (r * 0.45f * sin(angle)).toFloat()
            canvas.drawCircle(ex, ey, 7f * energyLevel, paint)
        }
    }

    private fun drawExpandingCluster(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        val expansion = (0.35f + 0.65f * ((sin(phase.toDouble()) + 1.0) / 2.0)).toFloat()
        val nodes = 8 + (idx % 8)
        for (i in 0 until nodes) {
            val angle = i * (2 * Math.PI / nodes) + phase * 0.35
            val dist = r * expansion
            val gx = cx + (dist * cos(angle)).toFloat()
            val gy = cy + (dist * sin(angle)).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = if (i % 2 == 0) Color.parseColor(accent) else Color.parseColor("#FF5252")
            canvas.drawCircle(gx, gy, 10f * energyLevel, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#475569")
        canvas.drawCircle(cx, cy, r * expansion, paint)
    }

    private fun drawHexagonalArray(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        val hexR = r * 0.24f
        val offsets = listOf(
            Pair(0f, 0f),
            Pair(hexR * 1.8f, 0f), Pair(-hexR * 1.8f, 0f),
            Pair(hexR * 0.9f, hexR * 1.6f), Pair(-hexR * 0.9f, hexR * 1.6f),
            Pair(hexR * 0.9f, -hexR * 1.6f), Pair(-hexR * 0.9f, -hexR * 1.6f)
        )
        val rot = phase * 18f + idx * 15f
        canvas.save()
        canvas.rotate(rot, cx, cy)
        for ((dx, dy) in offsets) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(cx + dx, cy + dy, hexR * 0.72f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor(accent)
            canvas.drawCircle(cx + dx, cy + dy, hexR * 0.85f * energyLevel, paint)
        }
        canvas.restore()
    }

    private fun drawSubatomicQuarks(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.parseColor(accent)
        canvas.drawCircle(cx, cy, r * 0.78f, paint)

        val count = 3 + (idx % 3)
        val coords = Array(count) { i ->
            val a = phase * 3f + i * (2 * Math.PI / count)
            Pair(cx + (r * 0.45f * cos(a)).toFloat(), cy + (r * 0.45f * sin(a)).toFloat())
        }
        paint.strokeWidth = 5f
        paint.color = Color.parseColor("#F43F5E")
        for (i in 0 until count) {
            val next = (i + 1) % count
            canvas.drawLine(coords[i].first, coords[i].second, coords[next].first, coords[next].second, paint)
        }
        paint.style = Paint.Style.FILL
        for (i in 0 until count) {
            paint.color = Color.parseColor(palettePrimary[(i + idx) % palettePrimary.size])
            canvas.drawCircle(coords[i].first, coords[i].second, 15f * energyLevel, paint)
        }
    }

    private fun drawQuantumAtomOrbits(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F43F5E")
        canvas.drawCircle(cx, cy, 15f * energyLevel, paint)

        val orbits = 3 + (idx % 3)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        for (i in 0 until orbits) {
            canvas.save()
            canvas.rotate(i * (180f / orbits), cx, cy)
            paint.color = Color.parseColor(accent)
            canvas.drawOval(cx - r, cy - r * 0.36f, cx + r, cy + r * 0.36f, paint)

            val angle = phase * 3.5f + i * 2f
            val ex = cx + (r * cos(angle.toDouble())).toFloat()
            val ey = cy + (r * 0.36f * sin(angle.toDouble())).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(ex, ey, 8f * energyLevel, paint)
            paint.style = Paint.Style.STROKE
            canvas.restore()
        }
    }

    private fun drawHelixAndMolecular(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        val rungs = 7 + (idx % 4)
        for (i in -rungs..rungs) {
            val y = cy + i * (r * 0.1f)
            val wave = sin((phase * 2.5f + i * 0.45f).toDouble()).toFloat()
            val x1 = cx + wave * (r * 0.55f)
            val x2 = cx - wave * (r * 0.55f)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#475569")
            canvas.drawLine(x1, y, x2, y, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor(accent)
            canvas.drawCircle(x1, y, 7f * energyLevel, paint)
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(x2, y, 7f * energyLevel, paint)
        }
    }

    private fun drawPlanetaryOrbits(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 20f * energyLevel, paint)

        val rings = 3 + (idx % 3)
        for (i in 1..rings) {
            val rad = r * (i.toFloat() / rings)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            paint.color = Color.parseColor("#334155")
            canvas.drawCircle(cx, cy, rad, paint)

            val a = phase * (3.2f / i) + i * 1.3f
            val px = cx + (rad * cos(a.toDouble())).toFloat()
            val py = cy + (rad * sin(a.toDouble())).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = if (i % 2 == 0) Color.parseColor(accent) else Color.parseColor("#F97316")
            canvas.drawCircle(px, py, (6f + i * 2f) * energyLevel, paint)
        }
    }

    private fun drawSpiralGalaxyAndWeb(canvas: Canvas, cx: Float, cy: Float, r: Float, accent: String, idx: Int) {
        paint.style = Paint.Style.FILL
        val arms = 3 + (idx % 3)
        for (arm in 0 until arms) {
            val armOffset = arm * (2 * Math.PI / arms)
            for (j in 1..26) {
                val frac = j / 26f
                val dist = frac * r
                val angle = phase * 1.2f + armOffset + frac * 4.2f
                val gx = cx + (dist * cos(angle.toDouble())).toFloat()
                val gy = cy + (dist * sin(angle.toDouble())).toFloat()
                paint.color = if (j % 2 == 0) Color.parseColor(accent) else Color.parseColor("#E0F2FE")
                canvas.drawCircle(gx, gy, (4.5f - frac * 2.5f) * energyLevel, paint)
            }
        }
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 14f * energyLevel, paint)
    }
}

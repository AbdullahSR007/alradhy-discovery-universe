package com.alradhy.discovery

import android.app.Activity
import android.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.roundToInt

internal class LabWorkbench(
    private val activity: Activity,
    private val ui: TvStyle,
    private val store: ProgressStore,
    private val topic: Topic,
    private val accent: Int,
    private val bind: (String, View) -> View,
    private val quiz: () -> Unit,
    private val speak: (String) -> Unit
) {
    private var values = store.values(topic)
    private var baseline = store.baseline(topic)
    private var prediction = store.prediction(topic)
    private var compare = false
    private var running = ui.motion
    private var speed = 1.0
    private var showGraph = store.flag("graph", true)
    private var activeControl = 0
    private var chartAxis = graphAxis(topic)
    private val chartTitle = ui.text("", 13, muted)
    private val legend = ui.text("", 12, muted)
    private var stage = if (store.flag("guided", true) && prediction !in 0..2) 0 else 1
    private var dragStart = values.copyOf()
    private val trends = listOf("تزيد", "تنقص", "تبقى ثابتة")
    private val hero = ExperimentView(activity, topic, accent)
    private val curve = CurveView(activity, topic, accent)
    private val info = ui.column()
    private val side = ui.column()
    private val metric = ui.text(topic.metric, 14, muted).apply { gravity = Gravity.CENTER }
    private val number = ui.number("", 26, 0xff4ade80.toInt()).apply { gravity = Gravity.CENTER }
    private val output = ui.text("", 14, ui.foreground, true).apply { gravity = Gravity.CENTER }
    private val comparison = ui.text("", 16, accent, true)
    private val fineHint = ui.text("", 14, muted)
    private val sliderRefs = mutableListOf<TvSlider>()
    private val labels = mutableListOf<TextView>()
    val navigation = ui.row()
    private val chart = ui.column()
    private val play: TextView
    private val slow: TextView
    private val juxtapose: TextView
    private val plot: TextView
    val initial = if (stage == 0) "step.0" else "control.0"

    val view = ui.row().apply { gravity = Gravity.TOP }

    init {
        ui.pad(side, 10, 9)
        side.background = ui.shape(ui.surface)
        val equation = if (topic.id == "mechanics.newton2") "F = m · a" else topic.equation
        ui.add(side, ui.number(equation, if (equation.length <= 20) 29 else 16, accent).apply { gravity = Gravity.CENTER }, 6)
        val readout = ui.column().apply {
            background = ui.shape(0xff0d1425.toInt(), 0xff1b2943.toInt(), 12)
            ui.pad(this, 10, 7)
        }
        ui.add(readout, metric)
        ui.add(readout, number, 3)
        ui.add(readout, output, 3)
        ui.add(readout, comparison, 4)
        comparison.gravity = Gravity.CENTER
        side.addView(ui.scroll(info), LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(8) })
        ui.add(side, readout, 9)
        ui.add(side, fineHint, 3)

        val theatre = ui.column()
        val simulation = ui.column().apply {
            background = ui.shape(0xff0b1425.toInt(), ui.border, 16)
            ui.pad(this, 6, 6)
        }
        hero.running = running
        hero.values = values
        hero.baseline = baseline
        hero.contentDescription = "محاكاة " + topic.title
        hero.drag = ::dragPlot
        hero.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        simulation.addView(hero, LinearLayout.LayoutParams(-1, 0, 1f))
        val tools = ui.row()
        play = command("play", "إيقاف", 15) { togglePlay() }
        slow = command("slow", "1×", 15) { changeSpeed() }
        juxtapose = command("compare", "ماذا لو؟", 15) { toggleCompare() }
        plot = command("chart", "المنحنى", 15) { toggleGraph() }
        ui.cells(tools, listOf(play, slow, juxtapose, plot, command("menu", "أدوات", 15) { quickMenu() }))
        ui.add(simulation, tools, 4)
        theatre.addView(simulation, LinearLayout.LayoutParams(-1, 0, 1f))
        chart.background = ui.shape(ui.surface)
        ui.pad(chart, 10, 5)
        val chartHead = ui.row()
        chartHead.addView(chartTitle, LinearLayout.LayoutParams(0, -2, 1f))
        if (topic.controls.size > 1) {
            val pick = command("axis", "محور X", 13) { selectAxis() }.apply {
                minimumHeight = 0
                ui.pad(this, 8, 2)
            }
            chartHead.addView(pick, LinearLayout.LayoutParams(ui.dp(84), ui.dp(30)).apply { marginStart = ui.dp(8) })
        }
        ui.add(chart, chartHead)
        ui.add(chart, curve, 2, 110)
        legend.maxLines = 1
        legend.ellipsize = android.text.TextUtils.TruncateAt.END
        ui.add(chart, legend, 1)
        ui.add(theatre, chart, 7)

        view.addView(theatre, LinearLayout.LayoutParams(0, -1, 1.88f))
        view.addView(side, LinearLayout.LayoutParams(0, -1, 1f).apply { marginStart = ui.dp(12) })
        refreshStep()
        refresh()
    }

    private fun command(key: String, label: String, size: Int = 17, action: () -> Unit) =
        ui.button(label, accent, size, action).also { it.tag = key; bind(key, it) }

    private fun refreshStep(focus: String? = null) {
        navigation.removeAllViews()
        val tags = listOf("① توقّع", "② جرّب", "③ قارن", "④ افهم")
        val buttons = tags.mapIndexed { index, label ->
            command("step.$index", label, 13) {
                if (index != 0 && locked()) {
                    fineHint.text = "اختر توقّعًا أولًا لفتح المنزلقات."
                    showStage(0, "prediction.0")
                } else if (index != stage) {
                    showStage(index, "step.$index")
                }
            }.apply {
                minimumHeight = 0
                ui.pad(this, 3, 1)
                if (stage == index) {
                    setTextColor(0xff07111c.toInt())
                    background = android.graphics.drawable.StateListDrawable().apply {
                        addState(intArrayOf(android.R.attr.state_focused), ui.shape(accent, ui.foreground, 9, 3))
                        addState(intArrayOf(), ui.shape(accent, accent, 9))
                    }
                }
            }
        }
        ui.cells(navigation, buttons, 5, 33)
        info.removeAllViews()
        sliderRefs.clear()
        labels.clear()
        fineHint.text = ""
        when (stage) {
            0 -> {
                val axis = graphAxis(topic)
                val title = "إذا زدت «${topic.controls[axis].label}» وثبّت بقية الإعدادات، كيف تتغيّر «${topic.metric}»؟"
                ui.add(info, ui.text(title, 17, muted))
                trends.forEachIndexed { index, label ->
                    ui.add(info, command("prediction.$index", (if (prediction == index) "✓ " else "") + label) {
                        prediction = index
                        store.predict(topic, index)
                        baseline = values.copyOf()
                        store.saveBaseline(topic, values)
                        showStage(1, "control.0")
                    }, 5)
                }
            }
            1 -> showControls()
            2 -> {
                ui.add(info, ui.text("ثبّت القراءة الحالية كمرجع، ثم غيّر المدخلات وشاهد الفرق في المحاكاتين.", 17, muted))
                ui.add(info, command("baseline", "احفظ الحالة المرجعية") {
                    capture()
                    refresh()
                }, 7)
                ui.add(info, command("split", "بدّل المقارنة جنبًا إلى جنب") { toggleCompare() }, 7)
                ui.add(info, command("record", "سجّل القياس") { record() }, 7)
                store.measured(topic)?.let { ui.add(info, ui.text("آخر قياس  •  " + ltr(fmt(it, 3)), 15, accent), 8) }
            }
            else -> {
                ui.add(info, ui.text(topic.idea, 19, ui.foreground, true))
                detail("افتراضات النموذج", topic.assumptions)
                detail("جرّب هذا التحدّي", topic.challenge)
                detail("أين نستخدمه؟", topic.application)
                topic.facts.forEach { ui.add(info, ui.text(it, 16, accent), 5) }
                detail("ملاحظة اكتشاف", topic.note)
                ui.add(info, command("quiz", "اختبر فهمك", action = quiz), 8)
                if (store.flag("narration", true)) {
                    ui.add(info, command("narrate", "استمع للشرح") { speak(topic.title + ". " + topic.idea) }, 6)
                }
            }
        }
        if (focus != null) {
            val destination = if (focus == "control.0") sliderRefs.firstOrNull() else
                (buttons + (0 until info.childCount).map { info.getChildAt(it) }).firstOrNull { it.tag == focus }
            destination?.post { destination.requestFocus() }
        }
    }

    private fun detail(label: String, body: String) {
        if (body.isBlank()) return
        ui.add(info, ui.text(label, 15, accent, true), 7)
        ui.add(info, ui.text(body, 17, muted), 2)
    }

    private fun showControls() {
        ui.add(info, ui.text(topic.challenge, 15, muted))
        val choices = presets(topic)
        if (choices.isNotEmpty()) {
            val row = ui.row()
            ui.cells(row, choices.mapIndexed { index, preset ->
                command("preset.$index", preset.label, 12) {
                    values = preset.values.copyOf()
                    changed()
                }.apply { minimumHeight = 0; ui.pad(this, 4, 3) }
            }, gap = 4, height = 37)
            ui.add(info, row, 7)
        }
        topic.controls.forEachIndexed { index, spec ->
            val label = ui.text("", 16, ui.foreground, true)
            val discrete = spec.discrete
            val slider = TvSlider(activity, ui, spec, accent, discrete) {
                activeControl = index
                values[index] = it
                changed()
            }
            slider.onFine = { fine -> fineHint.text = if (fine) "الضبط الدقيق مفعّل" else "" }
            slider.onFocusChangeListener = View.OnFocusChangeListener { _, focused ->
                if (focused) activeControl = index
            }
            bind("control.$index", slider)
            labels += label
            sliderRefs += slider
            ui.add(info, label, 6)
            ui.add(info, slider, 2, 36)
        }
        sliderRefs.zipWithNext().forEach { (top, bottom) ->
            top.nextFocusDownId = bottom.id
            bottom.nextFocusUpId = top.id
        }
        ui.add(info, command("measure", "سجّل القياس") { record() }, 7)
    }

    private fun showStage(next: Int, focus: String? = null) {
        stage = next
        refreshStep(focus)
        refresh()
    }

    private fun locked() = store.flag("guided", true) && prediction !in 0..2

    private fun changed() {
        store.saveValues(topic, values)
        refresh()
    }

    private fun capture() {
        baseline = values.copyOf()
        store.saveBaseline(topic, values)
    }

    private fun dragPlot(x: Float, y: Float, begin: Boolean) {
        if (locked() || stage != 1) return
        if (begin) {
            dragStart = values.copyOf()
            return
        }
        val indices = if (topic.controls.size == 1) intArrayOf(0) else intArrayOf(activeControl, if (activeControl == 0) 1 else 0)
        indices.forEachIndexed { order, index ->
            val control = topic.controls[index]
            val fraction = if (order == 0) x else y
            val step = control.step
            val delta = ((control.max - control.min) * fraction / step).roundToInt() * step
            values[index] = control.clamp(dragStart[index] + delta)
        }
        changed()
    }

    private fun actual(current: Double, old: Double): Int {
        val threshold = maxOf(abs(current), abs(old), 1e-12) * 1e-6
        return when {
            current - old > threshold -> 0
            old - current > threshold -> 1
            else -> 2
        }
    }

    private fun record() {
        if (locked()) {
            showStage(0, "prediction.0")
            return
        }
        val value = reading(topic, values)
        if (!value.isFinite()) {
            fineHint.text = "غيّر الإعدادات للعثور على قراءة قابلة للتسجيل."
            return
        }
        val saved = baseline
        val changed = saved == null || !values.contentEquals(saved)
        if (!changed && !store.done(topic)) {
            fineHint.text = "غيّر أحد المتغيرات قبل تسجيل القياس."
            return
        }
        val result = saved?.let { actual(value, reading(topic, it)) } ?: -1
        store.markDone(topic, result, value)
        refresh()
        fineHint.text = "✓ سُجّل القياس"
    }

    fun togglePlay() {
        running = !running
        refreshPlayback()
    }

    private fun changeSpeed() {
        val rates = listOf(.1, .25, .5, 1.0, 2.0)
        speed = rates[(rates.indexOf(speed) + 1) % rates.size]
        refreshPlayback()
    }

    private fun refreshPlayback() {
        hero.running = running
        hero.speed = speed
        hero.invalidate()
        play.text = if (running) "إيقاف" else "تشغيل"
        slow.text = ltr((if (speed == 1.0 || speed == 2.0) fmt(speed, 0) else speed.toString()) + "×")
    }

    private fun toggleCompare() {
        if (locked()) {
            showStage(0, "prediction.0")
            return
        }
        if (baseline == null) capture()
        compare = !compare
        refresh()
    }

    private fun toggleGraph() {
        showGraph = !showGraph
        refresh()
    }

    private fun axisLabel(symbol: String, unit: String): String =
        if (unit.isEmpty()) symbol else "$symbol ($unit)"

    private fun selectAxis() {
        val labels = topic.controls.map { it.label + " • " + ltr(axisLabel(it.symbol, it.unit)) }
        AlertDialog.Builder(activity).setTitle("المتغيّر على محور X")
            .setSingleChoiceItems(labels.toTypedArray(), chartAxis) { dialog, index ->
                chartAxis = index
                dialog.dismiss()
                refresh()
            }.setNegativeButton("رجوع", null).show()
    }

    private fun describeConfiguration(configuration: DoubleArray): String =
        topic.controls.indices.filter { it != chartAxis }.joinToString(" · ") { i ->
            val control = topic.controls[i]
            control.symbol + "=" + formatControl(topic, i, configuration[i]) +
                if (control.unit.isEmpty()) "" else " " + control.unit
        }

    private fun refreshChart() {
        val x = topic.controls[chartAxis]
        val scaleLabel = if (topic.id == "scale.power-ratio") "  •  Y لوغاريتمي" else ""
        chartTitle.text = "Y " + ltr(axisLabel(topic.symbol, topic.unit)) +
            "   /   X " + ltr(axisLabel(x.symbol, x.unit)) + scaleLabel
        val fixed = describeConfiguration(values)
        val saved = baseline?.let(::describeConfiguration)
        val currentLabel = if (fixed.isEmpty()) "● الحالي" else "● الحالي (" + ltr(fixed) + ")"
        val baselineLabel = if (saved.isNullOrEmpty()) "المحفوظ" else "المحفوظ (" + ltr(saved) + ")"
        val baselineMark = if (x.discrete) "▪" else "┄"
        legend.text = currentLabel + if (baseline == null) "" else "   $baselineMark $baselineLabel"
        curve.update(values, baseline, chartAxis)
    }

    private fun refresh() {
        refreshPlayback()
        hero.values = values
        hero.baseline = baseline
        hero.compare = compare
        hero.invalidate()
        juxtapose.text = if (compare) "✓ ماذا لو؟" else "ماذا لو؟"
        plot.text = if (showGraph) "✓ المنحنى" else "المنحنى"
        chart.visibility = if (showGraph) View.VISIBLE else View.GONE
        if (showGraph) refreshChart()
        val current = reading(topic, values)
        number.setTextColor(if (topic.id == "telecom.link-budget" && current < 0) 0xfffb7185.toInt() else 0xff4ade80.toInt())
        number.text = if (current.isFinite()) ltr(fmt(current, if (topic.id == "mechanics.newton2") 1 else 2) + headlineUnit(topic)) else "—"
        val detail = labReadout(topic, values)
        val rich = topic.id in detailedReadouts || (topic.legacy ?: -1) >= 12 && topic.id != "mechanics.newton2"
        output.text = if (rich) detail else ""
        output.visibility = if (rich) View.VISIBLE else View.GONE
        val reference = baseline
        comparison.text = if (reference != null && !reference.contentEquals(values)) {
            val value = reading(topic, values)
            val old = reading(topic, reference)
            val result = actual(value, old)
            val verdict = if (prediction in 0..2 && value.isFinite() && old.isFinite()) "  •  النتيجة ${trends[result]}" else ""
            if (value.isFinite() && old.isFinite()) "Δ " + ltr(fmt(value - old, 3) + headlineUnit(topic)) + verdict else ""
        } else if (store.done(topic)) "✓ تجربة مكتملة" else ""
        comparison.visibility = if (comparison.text.isEmpty()) View.GONE else View.VISIBLE
        if (stage == 1) values.forEachIndexed { index, value ->
            sliderRefs[index].show(value)
            val control = topic.controls[index]
            val readout = formatControl(topic, index, value)
            val unit = if (control.unit.isEmpty()) "" else " " + control.unit
            labels[index].text = control.label + "   " + ltr(readout + unit)
        }
    }

    fun quickMenu() {
        val controls = arrayOf("إعادة القيم الافتراضية", "إعادة تشغيل الزمن", "حفظ الحالة المرجعية", "توقّع وقارن", "اختبر فهمك")
        AlertDialog.Builder(activity).setTitle("أدوات التجربة").setItems(controls) { _, selected ->
            when (selected) {
                0 -> { values = topic.defaults(); changed() }
                1 -> hero.rewind()
                2 -> if (!locked()) { capture(); refresh() } else showStage(0, "prediction.0")
                3 -> showStage(if (locked()) 0 else 2)
                4 -> quiz()
            }
        }.show()
    }

    fun pause() {
        hero.running = false
    }

    fun resume() {
        hero.running = running
        hero.invalidate()
    }
}

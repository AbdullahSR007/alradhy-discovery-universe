package com.alradhy.discovery

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log10

class MainActivity : Activity() {
    private data class Screen(val name: String, val id: String = "", var focus: String? = null, var filter: Int = 0)
    private val pages = mutableListOf(Screen("home"))
    private val handler = Handler(Looper.getMainLooper())
    private val idle = Runnable { if (pages.last().name == "home" && dialog == null && store.flag("ambient")) open("ambient") }
    private lateinit var content: MuseumContent
    private lateinit var store: ProgressStore
    private lateinit var ui: TvStyle
    private lateinit var root: LinearLayout
    private lateinit var footer: TextView
    private val focusViews = linkedMapOf<String, View>()
    private var preferred: String? = null
    private var mouse = false
    private var lastExit = 0L
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var tone: ToneGenerator? = null
    private var dialog: Dialog? = null
    private var workbench: LabWorkbench? = null
    private var scaleMove: ((Int) -> Unit)? = null
    private var scaleIndex = 0
    private var featuredIndex = 0
    private val featured = listOf("telecom.link-budget", "telecom.fresnel", "telecom.qam", "telecom.orbit-latency")
    private val scaleAuto = object : Runnable {
        override fun run() {
            if (pages.last().name == "ambient") {
                scaleMove?.invoke(1)
                handler.postDelayed(this, 5000)
            }
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        content = MuseumContent(this)
        store = ProgressStore(this, content)
        ui = TvStyle(this, store)
        scaleIndex = content.scale.indexOfFirst { it.id == store.string("scale.last", "") }.coerceAtLeast(0)
        state?.getStringArrayList("screens")?.forEach { route ->
            val parts = route.split('|')
            if (parts.size == 3) pages += Screen(parts[0], parts[1], parts[2].ifBlank { null })
        }
        immersive()
        render()
    }

    override fun onSaveInstanceState(out: Bundle) {
        rememberFocus()
        out.putStringArrayList("screens", ArrayList(pages.drop(1).filter { it.name != "ambient" }.map {
            "${it.name}|${it.id}|${it.focus.orEmpty()}"
        }))
        super.onSaveInstanceState(out)
    }

    private fun immersive() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) immersive()
    }

    override fun onResume() {
        super.onResume()
        refreshIdle()
        if (pages.last().name == "ambient") {
            handler.removeCallbacks(scaleAuto)
            handler.postDelayed(scaleAuto, 5000)
        }
        workbench?.resume()
    }

    override fun onPause() {
        handler.removeCallbacks(idle)
        handler.removeCallbacks(scaleAuto)
        workbench?.pause()
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        tts?.shutdown()
        tone?.release()
        super.onDestroy()
    }

    private fun refreshIdle() {
        handler.removeCallbacks(idle)
        if (store.flag("ambient")) handler.postDelayed(idle, 180000)
    }

    private fun rememberFocus() {
        val focused = currentFocus ?: return
        focusViews.entries.firstOrNull { it.value == focused }?.let { pages.last().focus = it.key }
    }

    private fun open(name: String, id: String = "") {
        val current = pages.last()
        if (current.name == name && current.id == id) return
        rememberFocus()
        tts?.stop()
        if (name == "home") {
            if (pages.size > 1) pages.subList(1, pages.size).clear()
        } else {
            if (name == "topic" || name == "lab") content.byId[id]?.let(store::visit)
            pages += Screen(name, id)
        }
        render()
    }

    override fun onBackPressed() {
        dialog?.let { it.dismiss(); return }
        if (pages.size > 1) {
            pages.removeAt(pages.lastIndex)
            tts?.stop()
            render()
        } else if (SystemClock.elapsedRealtime() - lastExit <= 2000 && lastExit != 0L) {
            finish()
        } else {
            lastExit = SystemClock.elapsedRealtime()
            Toast.makeText(this, "اضغط رجوع مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (mouse) { mouse = false; setHint() }
            refreshIdle()
            if (pages.last().name == "ambient") { onBackPressed(); return true }
            if (event.repeatCount == 0 && activeShortcut(event.keyCode)) return true
        }
        if (event.keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            return super.dispatchKeyEvent(KeyEvent(event.action, KeyEvent.KEYCODE_DPAD_CENTER))
        }
        return super.dispatchKeyEvent(event)
    }

    private fun activeShortcut(code: Int): Boolean {
        val lab = workbench ?: return false
        return when (code) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { lab.togglePlay(); true }
            KeyEvent.KEYCODE_MENU -> { lab.quickMenu(); true }
            else -> false
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            if (!mouse) { mouse = true; setHint() }
            refreshIdle()
            if (pages.last().name == "ambient") { onBackPressed(); return true }
            if (event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS &&
                (event.actionButton == MotionEvent.BUTTON_SECONDARY || event.actionButton == MotionEvent.BUTTON_BACK)) {
                onBackPressed()
                return true
            }
            if (event.actionMasked == MotionEvent.ACTION_SCROLL && pages.last().name == "scale") {
                val delta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                if (delta != 0f) { scaleMove?.invoke(if (delta > 0) 1 else -1); return true }
            }
        }
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        refreshIdle()
        if (pages.last().name == "ambient") { onBackPressed(); return true }
        return super.dispatchTouchEvent(event)
    }

    private fun setHint() {
        if (!::footer.isInitialized) return
        footer.text = when {
            pages.last().name == "ambient" -> "حرّك الماوس أو اضغط زرًا للعودة"
            mouse && pages.last().name == "lab" -> "اسحب المحاكاة أو المنزلق  •  عجلة الماوس تضبط القيمة  •  زر الفأرة الأيمن يرجع"
            mouse && pages.last().name == "scale" -> "عجلة الماوس تغيّر المقياس  •  انقر للتفاصيل"
            mouse -> "حرّك المؤشر للاختيار  •  انقر للفتح  •  زر الفأرة الأيمن يرجع"
            pages.last().name == "lab" -> "◀ ▶ تعديل المنزلق   •   ▲ ▼ تنقّل   •   OK ضبط دقيق   •   BACK رجوع"
            pages.last().name == "scale" -> "▲ ▼ تغيير المقياس   •   OK تفاصيل المحطة   •   BACK رجوع"
            else -> "▲ ▼ ◀ ▶ تنقّل   •   OK فتح   •   BACK رجوع"
        }
    }

    private fun render() {
        workbench?.pause()
        workbench = null
        scaleMove = null
        handler.removeCallbacks(scaleAuto)
        focusViews.clear()
        preferred = null
        val page = pages.last()
        val isAmbient = page.name == "ambient"
        root = ui.column().apply {
            val base = if (store.flag("contrast")) Color.BLACK else 0xff0b1020.toInt()
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xff05070f.toInt(), base))
            val compact = resources.configuration.screenWidthDp < 825
            val x = if (compact) 20 else 34
            val y = if (compact) 12 else 18
            setPadding(ui.dp(x), ui.dp(y), ui.dp(x), ui.dp(y))
        }
        val screen = when (page.name) {
            "home" -> home()
            "wing" -> wing(content.wing(page.id), page)
            "life" -> lifeJourney()
            "scale", "ambient" -> scale(isAmbient)
            "topic" -> topic(content.byId.getValue(page.id))
            "lab" -> lab(content.byId.getValue(page.id))
            "search" -> search()
            "progress" -> progress()
            "settings" -> settings()
            else -> home()
        }
        if (!isAmbient) ui.add(root, header(page), height = 37)
        val body = ui.row().apply { gravity = Gravity.TOP }
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(if (isAmbient) 0 else 11) })
        if (page.name in listOf("wing", "search", "progress", "settings")) {
            body.addView(rail(), LinearLayout.LayoutParams(ui.dp(67), -1).apply { marginEnd = ui.dp(14) })
        }
        val stage = object : FrameLayout(this) {
            override fun dispatchDraw(canvas: Canvas) {
                val save = canvas.save()
                if (page.name == "home") canvas.clipRect(0, 0, width, height)
                super.dispatchDraw(canvas)
                canvas.restoreToCount(save)
            }
        }.apply {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            clipChildren = false
            clipToPadding = false
        }
        body.addView(stage, LinearLayout.LayoutParams(0, -1, 1f))
        stage.addView(screen, FrameLayout.LayoutParams(-1, -1))
        footer = ui.text("", 12, muted).apply { gravity = Gravity.CENTER }
        ui.add(root, footer, 8, 18)
        setHint()
        setContentView(root)
        root.post {
            val key = page.focus
            (focusViews[key] ?: focusViews[preferred] ?: focusViews.values.firstOrNull())?.requestFocus()
        }
        refreshIdle()
    }

    private fun header(page: Screen): View {
        val heading = when (page.name) {
            "wing" -> content.wing(page.id).title
            "scale" -> "رحلة المقاييس"
            "life" -> "من LUCA إلى الإنسان اليوم"
            "topic", "lab" -> content.wing(content.byId.getValue(page.id).wing).title
            "search" -> "بحث"
            "progress" -> "تقدّمي"
            "settings" -> "الإعدادات"
            else -> "متحف العلوم"
        }
        return ui.row().apply {
            if (page.name == "home") {
                val emblem = WingGlyph(this@MainActivity, "observatory", 0xff38bdf8.toInt())
                addView(emblem, LinearLayout.LayoutParams(ui.dp(29), ui.dp(29)).apply { marginEnd = ui.dp(9) })
                addView(ui.text("ALRADHY Discovery  •  متحف العلوم", 21, ui.foreground, true).apply {
                    gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                },
                    LinearLayout.LayoutParams(0, -1, 1f))
                val tools = ui.row()
                val stars = content.topics.count(store::starred)
                val buttons = listOf(
                    topTool("top.search", "بحث", "scale", teal) { open("search") },
                    action("top.progress", "★  $stars", gold, 16) { open("progress") }.apply { setTextColor(gold) },
                    topTool("top.settings", "الإعدادات", "settings", muted) { open("settings") }
                )
                buttons.forEach { button -> tools.addView(button, LinearLayout.LayoutParams(ui.dp(55), -1).apply { marginStart = ui.dp(6) }) }
                addView(tools, LinearLayout.LayoutParams(-2, -1))
            } else {
                addView(action("header.back", "→", teal, 21) { onBackPressed() },
                    LinearLayout.LayoutParams(ui.dp(44), ui.dp(35)).apply { marginEnd = ui.dp(9) })
                val labTitle = if (page.name == "lab") content.byId.getValue(page.id).title else null
                val title = if (labTitle == null) heading else "$heading  /  $labTitle"
                addView(ui.text(title, if (labTitle == null) 25 else 21, ui.foreground, true).apply { maxLines = 2 },
                    LinearLayout.LayoutParams(0, -1, 1f))
                workbench?.let {
                    addView(it.navigation, LinearLayout.LayoutParams(ui.dp(258), -1).apply { marginStart = ui.dp(10) })
                }
            }
        }
    }

    private fun topTool(key: String, description: String, glyph: String, color: Int, activate: () -> Unit): View =
        WingGlyph(this, glyph, color).also {
            it.contentDescription = description
            ui.interactive(it, color, activate)
            bind(key, it)
        }

    private fun rail(): View {
        val column = ui.column()
        val names = mapOf("scale" to "المقاييس", "mechanics" to "الحركة", "fields" to "الكهرباء",
            "light" to "الضوء", "astro" to "الفلك", "telecom" to "الاتصالات", "matter" to "الحياة")
        fun add(key: String, label: String, color: Int = teal, callback: () -> Unit) {
            val button = action("rail.$key", label, color, 13, callback).apply {
                minimumHeight = 0
                ui.pad(this, 1, 1)
            }
            ui.add(column, button, if (column.childCount == 0) 0 else 4, 30)
        }
        add("home", "الرئيسية") { open("home") }
        content.wings.forEach { wing ->
            add(wing.id, names.getValue(wing.id), wing.color) { openWing(wing) }
        }
        add("search", "بحث") { open("search") }
        add("progress", "تقدّمي") { open("progress") }
        add("settings", "الإعدادات") { open("settings") }
        return ui.scroll(column)
    }

    private fun openWing(wing: Wing) {
        if (wing.id == "scale") open("scale") else open("wing", wing.id)
    }

    private fun home(): View {
        val column = ui.column()
        val item = content.byId.getValue(featured[featuredIndex])
        val color = content.wing(item.wing).color
        val hero = ui.row().apply {
            background = GalaxyBackground()
            ui.pad(this, 17, 8)
        }
        val main = ui.column().apply { gravity = Gravity.CENTER_VERTICAL }
        val displayName = if (item.id == "telecom.link-budget") "ميزانية الوصلة الراديوية" else item.title
        val headline = if (item.id == "telecom.link-budget")
            "هل تصل إشارة ${ltr("80 GHz")} لمسافة ${ltr("2 km")} في المطر؟ احسب هامش الأمان بنفسك." else item.summary
        ui.add(main, ui.text("تجربة اليوم  •  علوم الاتصالات", 14, color, true), height = 22)
        ui.add(main, ui.text(displayName, 27, ui.foreground, true), 2, 40)
        ui.add(main, ui.text(headline, 16, muted).apply { maxLines = 2 }, 3, 45)
        val actions = ui.row()
        val launch = action("hero", "▷  ابدأ التجربة", color, 16) { open("lab", item.id) }.apply {
            minimumHeight = ui.dp(36)
            setTextColor(0xff061819.toInt())
            background = android.graphics.drawable.StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_focused), ui.shape(0xff80fff2.toInt(), Color.WHITE, 11, 2))
                addState(intArrayOf(), ui.shape(color, color, 11))
            }
        }
        val rotate = action("rotate", "تجربة ثانية", color, 14) {
            featuredIndex = (featuredIndex + 1) % featured.size
            pages.last().focus = "rotate"
            render()
        }.apply { minimumHeight = ui.dp(36) }
        actions.addView(launch, LinearLayout.LayoutParams(-2, -2))
        actions.addView(rotate, LinearLayout.LayoutParams(-2, -2).apply { marginStart = ui.dp(7) })
        launch.nextFocusLeftId = rotate.id
        rotate.nextFocusRightId = launch.id
        ui.add(main, actions, 4, 35)
        hero.addView(main, LinearLayout.LayoutParams(0, -1, 1.0f))
        hero.addView(ExperimentView(this, item, color).apply {
            presentation = true
            running = ui.motion
            contentDescription = item.english
        }, LinearLayout.LayoutParams(0, -1, 1.05f).apply { marginStart = ui.dp(15) })
        ui.add(column, hero, height = 174)
        preferred = "wing.telecom"

        val order = listOf("scale", "mechanics", "fields", "light", "astro", "telecom", "matter")
        order.chunked(4).forEach { ids ->
            val strip = ui.row()
            val cards = ids.map { wingCard(content.wing(it)) }
            val views = if (cards.size == 4) cards else cards + progressCard()
            ui.cells(strip, views, gap = 12, height = 110)
            ui.add(column, strip, 12)
        }
        val today = dailyTopic()
        val daily = ui.row()
        val recent = store.recent().firstOrNull()
        ui.cells(daily, listOf(
            action("daily", "تحدّي اليوم  ·  ${today.title}", 0xfffacc15.toInt(), 16) { quiz(today, true) },
            action("continue", recent?.let { "تابع  ·  ${it.title}" } ?: "ابدأ رحلة المقاييس", teal, 16) {
                if (recent == null) open("scale") else open("lab", recent.id)
            }
        ))
        ui.add(column, daily, 16)
        val rest = store.recent().drop(1)
        if (rest.isNotEmpty()) {
            val r = ui.row()
            ui.cells(r, rest.map { prior -> action("recent.${prior.id}", prior.title, teal, 15) { open("lab", prior.id) } })
            ui.add(column, r, 9)
        }
        return ui.scroll(column).apply {
            clipChildren = true
            clipToPadding = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun dailyTopic(): Topic {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toInt()
        return content.labs[Math.floorMod(date, content.labs.size)]
    }

    private fun wingCard(wing: Wing): View {
        val experiments = wing.topics.filter(Topic::isLab)
        val seen = wing.topics.count(store::seen)
        val complete = experiments.count(store::done)
        val caption = when {
            wing.id == "scale" -> "من طول بلانك إلى حافة الكون"
            wing.id == "matter" -> "${experiments.size} مختبرًا  •  رحلة LUCA"
            experiments.isEmpty() -> "${wing.topics.size} موضوعًا"
            else -> "${experiments.size} مختبرًا" + if (complete > 0) "  •  $complete مكتمل" else ""
        }
        val progress = if (wing.id == "scale" || experiments.isEmpty()) seen.toFloat() / wing.topics.size else complete.toFloat() / experiments.size
        return showcaseCard("wing.${wing.id}", wing.id, wing.title, caption, wing.color, progress) { openWing(wing) }
    }

    private fun progressCard(): View {
        val stars = content.topics.count(store::starred)
        val labs = content.labs.count(store::done)
        return showcaseCard("progress.card", "progress", "تقدّمي", "$stars نجمة  •  $labs تجربة",
            0xfff472b6.toInt(), labs.toFloat() / content.labs.size) { open("progress") }
    }

    private fun showcaseCard(key: String, glyph: String, title: String, caption: String, color: Int,
        fraction: Float, open: () -> Unit): View {
        val card = ui.column().apply {
            ui.pad(this, 13, 8)
            val icon = WingGlyph(this@MainActivity, glyph, color)
            addView(icon, LinearLayout.LayoutParams(ui.dp(29), ui.dp(29)))
            ui.add(this, ui.text(title, 18, ui.foreground, true), 3)
            ui.add(this, ui.text(caption, 13, muted), 3)
            addView(View(this@MainActivity), LinearLayout.LayoutParams(-1, 0, 1f))
            ui.add(this, ProgressTrack(this@MainActivity, color, fraction), 5, 4)
        }
        ui.interactive(card, color, open)
        return bind(key, card)
    }

    private fun wing(wing: Wing, page: Screen): View {
        val body = ui.row().apply { gravity = Gravity.TOP }
        val menu = ui.column()
        val filter = ui.row()
        ui.cells(filter, listOf("الكل", "تجارب", "قيد التقدّم").mapIndexed { index, label ->
            action("filter.$index", (if (page.filter == index) "● " else "") + label, wing.color, 15) {
                page.filter = index
                page.focus = "filter.$index"
                render()
            }
        })
        if (wing.id != "scale") ui.add(menu, filter)
        if (wing.id == "matter") {
            ui.add(menu, action("life.start", "من LUCA إلى الإنسان اليوم", gold, 18) { open("life") }, 6)
        }
        val list = ui.column()
        val filtered = if (wing.id == "scale") wing.topics.filter(Topic::isLab) else when (page.filter) {
            1 -> wing.topics.filter(Topic::isLab)
            2 -> wing.topics.filterNot { store.done(it) || store.starred(it) }
            else -> wing.topics
        }
        val entries = if (wing.id == "matter") filtered.filter { it.life == null } else filtered
        val preview = FrameLayout(this)
        var currentPreview: String? = null
        fun show(item: Topic) {
            if (currentPreview == item.id) return
            currentPreview = item.id
            preview.removeAllViews()
            val inner = ui.column().apply {
                background = ui.shape(ui.surface)
                ui.pad(this, 11, 10)
            }
            val art = if (item.wing == "telecom" && item.section.isNotBlank()) {
                when (item.section) {
                    "ran", "rf" -> "ran"
                    "optical", "transport" -> "transport"
                    else -> null
                }
            } else null
            inner.addView(if (art == null) visual(item, wing.color) else pictures.view(this, art),
                LinearLayout.LayoutParams(-1, 0, 1f))
            if (art != null) ui.add(inner, ui.text("رسم توضيحي للمجال", 11, muted), 3)
            ui.add(inner, ui.text(item.title, 22, ui.foreground, true), 8)
            ui.add(inner, ui.number(item.equation, 15, wing.color), 4)
            ui.add(inner, ui.text(item.summary, 17, muted).apply { maxLines = 3 }, 7)
            preview.addView(inner, FrameLayout.LayoutParams(-1, -1))
        }
        if (entries.isEmpty()) ui.add(list, ui.text("ما فيه مواضيع بهذا الفلتر", 19, muted), 12)
        val groups = when (wing.id) {
            "telecom" -> entries.groupBy(::telecomSection).toSortedMap(compareBy {
                listOf("شبكات النفاذ  ·  RAN", "الانتشار والهوائيات  ·  RF", "الألياف الضوئية",
                    "النقل وزمن الشبكة", "الإشارة والتضمين").indexOf(it)
            })
            "matter" -> entries.groupBy {
                when (it.section) {
                    "cells" -> "الخلايا والطاقة"
                    "evolution" -> "الوراثة والتطوّر"
                    "human" -> "وظائف جسم الإنسان"
                    else -> "المادة والكائنات الحية"
                }
            }
            else -> linkedMapOf("" to entries)
        }
        groups.forEach { (heading, items) ->
            if (heading.isNotBlank()) ui.add(list, ui.text(heading, 15, wing.color, true), 10)
            items.forEach { item ->
                val prefix = if (store.done(item) || store.starred(item)) "✓  " else ""
                val button = action("item.${item.id}", prefix + item.title, wing.color, 18) { open("topic", item.id) }
                val focus = button.onFocusChangeListener
                button.onFocusChangeListener = View.OnFocusChangeListener { view, selected ->
                    focus?.onFocusChange(view, selected)
                    if (selected) show(item)
                }
                ui.add(list, button, 7)
            }
        }
        groups.values.firstOrNull()?.firstOrNull()?.let { show(it); preferred = "item.${it.id}" }
        if (wing.id == "matter" && page.filter == 0) preferred = "life.start"
        menu.addView(ui.scroll(list), LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(6) })
        body.addView(menu, LinearLayout.LayoutParams(0, -1, 1f))
        body.addView(preview, LinearLayout.LayoutParams(0, -1, 1.13f).apply { marginStart = ui.dp(12) })
        return body
    }

    private fun telecomSection(topic: Topic): String {
        when (topic.section) {
            "ran" -> return "شبكات النفاذ  ·  RAN"
            "rf" -> return "الانتشار والهوائيات  ·  RF"
            "optical" -> return "الألياف الضوئية"
            "transport" -> return "النقل وزمن الشبكة"
            "signal" -> return "الإشارة والتضمين"
        }
        return when (topic.id.substringAfter('.')) {
        "nr-grid", "rsrq", "sinr", "sensitivity", "cell-radius" -> "شبكات النفاذ  ·  RAN"
        "fresnel", "rain", "link-budget", "friis", "fspl", "wavelength", "horizon", "dish", "tilt",
        "eirp", "vswr", "power-sum" -> "الانتشار والهوائيات  ·  RF"
        "fiber", "optical-budget", "dispersion" -> "الألياف الضوئية"
        "packet-delay", "availability", "satellite", "orbit-latency" -> "النقل وزمن الشبكة"
        else -> "الإشارة والتضمين"
        }
    }

    private fun visual(item: Topic, color: Int): View = if (item.isLab) ExperimentView(this, item, color).apply {
        values = store.values(item)
        running = ui.motion
    } else UniverseCanvasView(this).apply {
        visualType = item.visual
        stationIndex = content.topics.indexOf(item).coerceAtLeast(0)
        simSpeed = if (ui.motion) .5f else 0f
    }

    private fun scale(ambient: Boolean): View {
        val row = ui.row()
        val art = UniverseCanvasView(this).apply { simSpeed = if (ui.motion) .65f else 0f }
        val ruler = ui.column().apply {
            ui.pad(this, 11, 8)
            background = ui.shape(ui.surface)
        }
        val description = ui.text("", 18, muted)
        val name = ui.text("", 29, ui.foreground, true)
        val value = ui.number("", 23, 0xff7dd3fc.toInt())
        val current = action("scale.current", "تفاصيل المقياس", 0xff7dd3fc.toInt(), 16) {
            if (ambient) onBackPressed() else open("topic", content.scale[scaleIndex].id)
        }
        val marks = ScaleRuler(this)
        fun renderMark() {
            val item = content.scale[scaleIndex]
            value.text = ltr(item.equation)
            name.text = item.title
            description.text = item.summary
            art.visualType = item.visual
            art.stationIndex = scaleIndex
            art.invalidate()
            marks.exponent = log10(item.scale!!)
            marks.invalidate()
        }
        scaleMove = { delta ->
            val old = scaleIndex
            scaleIndex = if (ambient) (scaleIndex + delta + content.scale.size) % content.scale.size
                else (scaleIndex + delta).coerceIn(0, content.scale.lastIndex)
            if (ui.motion && old != scaleIndex) {
                art.alpha = .48f
                art.animate().alpha(1f).setDuration(210).start()
            }
            if (!ambient) store.visit(content.scale[scaleIndex])
            renderMark()
        }
        current.setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key in listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN)) {
                scaleMove?.invoke(if (key == KeyEvent.KEYCODE_DPAD_UP) 1 else -1)
                true
            } else false
        }
        ruler.addView(marks, LinearLayout.LayoutParams(-1, 0, 1f))
        ui.add(ruler, name, 7)
        ui.add(ruler, value, 3)
        ui.add(ruler, description, 6)
        if (!ambient) {
            val buttons = ui.row()
            ui.cells(buttons, listOf(
                action("scale.more", "أكبر", teal, 15) { scaleMove?.invoke(1) },
                action("scale.less", "أصغر", teal, 15) { scaleMove?.invoke(-1) }
            ))
            ui.add(ruler, buttons, 9)
            current.nextFocusRightId = buttons.getChildAt(0).id
            current.nextFocusLeftId = buttons.getChildAt(1).id
            val actions = ui.row()
            val experiments = action("scale.labs", "مختبرات المقاييس", teal, 16) { open("wing", "scale") }
            ui.cells(actions, listOf(current, experiments))
            ui.add(ruler, actions, 6)
            preferred = "scale.current"
            store.visit(content.scale[scaleIndex])
        } else {
            handler.postDelayed(scaleAuto, 5000)
        }
        row.addView(ruler, LinearLayout.LayoutParams(0, -1, .85f))
        row.addView(art, LinearLayout.LayoutParams(0, -1, 1.65f).apply { marginStart = ui.dp(13) })
        renderMark()
        return row
    }

    private fun topic(item: Topic): View {
        val color = if (item.life == null) content.wing(item.wing).color else gold
        val body = ui.row().apply { gravity = Gravity.TOP }
        val info = ui.column().apply {
            background = ui.shape(ui.surface)
            ui.pad(this, 12, 9)
        }
        ui.add(info, ui.text(item.title, 31, ui.foreground, true))
        ui.add(info, ui.number(item.english, 13, muted), 4)
        item.life?.let { ui.add(info, ui.text(it.date, 17, gold, true), 4) }
        if (item.equation.isNotBlank()) ui.add(info, ui.number(item.equation, 21, color), 10)
        fun paragraph(key: String, text: String, size: Int, emphasized: Boolean = false): TextView =
            ui.text(text, size, ui.foreground, emphasized).apply {
                if (item.life != null) {
                    isFocusable = true
                    ui.pad(this, 6, 4)
                    background = android.graphics.drawable.StateListDrawable().apply {
                        addState(intArrayOf(android.R.attr.state_focused), ui.shape(0xff1e2a40.toInt(), gold, 7))
                        addState(intArrayOf(), ui.shape(ui.surface, ui.surface, 7))
                    }
                    bind(key, this)
                }
            }
        ui.add(info, paragraph("topic.story", item.summary, 21), 10)
        if (item.idea != item.summary) ui.add(info, paragraph("topic.caveat", item.idea, 19, true), 9)
        item.life?.let {
            ui.add(info, ui.text("الدليل", 18, gold, true), 10)
            ui.add(info, paragraph("topic.evidence", it.evidence, 19), 5)
            ui.add(info, ui.text(it.credit, 12, muted), 7)
        }
        item.facts.forEach { ui.add(info, ui.text(it, 17, color), 7) }
        if (item.note.isNotBlank()) ui.add(info, ui.text(item.note, 16, muted), 9)
        val buttons = ui.row()
        val choices = buildList<View> {
            if (item.isLab) add(action("topic.lab", "افتح التجربة", color, 16) { open("lab", item.id) })
            item.life?.let { chapter -> add(action("topic.related", "تجربة مرتبطة", color, 15) { open("lab", chapter.related) }) }
            add(action("topic.quiz", "اختبر فهمك", color, 16) { quiz(item) })
            if (store.flag("narration", true)) add(action("topic.speak", "استمع", color, 15) { speak(item.title + ". " + item.summary) })
        }
        ui.cells(buttons, choices)
        ui.add(info, buttons, 11)
        preferred = when {
            item.life != null -> "topic.story"
            item.isLab -> "topic.lab"
            else -> "topic.quiz"
        }
        body.addView(ui.scroll(info), LinearLayout.LayoutParams(0, -1, 1f))
        val art = if (item.life == null) visual(item, color) else ui.column().apply {
            addView(pictures.view(this@MainActivity, item.life.art), LinearLayout.LayoutParams(-1, 0, 1f))
            ui.add(this, ui.text("رسم توضيحي للحقبة", 12, muted), 5)
        }
        body.addView(art, LinearLayout.LayoutParams(0, -1, 1.4f).apply { marginStart = ui.dp(12) })
        return body
    }

    private val pictures = ExhibitPictures()

    private fun lifeJourney(): View {
        val body = ui.row().apply { gravity = Gravity.TOP }
        val directory = ui.column()
        val list = ui.column()
        val card = ui.column().apply {
            background = ui.shape(ui.surface, 0xff4d4330.toInt(), 16)
            ui.pad(this, 12, 9)
        }
        val title = ui.text("", 24, ui.foreground, true)
        val date = ui.text("", 17, gold, true)
        val summary = ui.text("", 18, muted).apply {
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        val caption = ui.text("رسم توضيحي للحقبة", 11, muted)
        val image = pictures.view(this, content.life.first().life!!.art)
        val rail = LifeRuler(this, content.life.map { it.life!!.yearsAgo })
        var selected = content.life.first()
        val details = action("life.detail", "اقرأ الدليل", gold, 16) { open("topic", selected.id) }
        fun preview(index: Int, item: Topic) {
            selected = item
            val chapter = requireNotNull(item.life)
            title.text = item.title
            date.text = chapter.date
            summary.text = item.summary
            pictures.show(image, chapter.art)
            rail.selected = index
        }
        ui.add(directory, ui.text("${content.life.size} محطة  •  اختر محطة بالأسهم", 14, gold, true))
        content.life.forEachIndexed { index, item ->
            val action = action("life.${item.id}", item.title, gold, 17) {
                open("topic", item.id)
            }
            val focus = action.onFocusChangeListener
            action.onFocusChangeListener = View.OnFocusChangeListener { view, selected ->
                focus?.onFocusChange(view, selected)
                if (selected) preview(index, item)
            }
            ui.add(list, action, 6)
        }
        directory.addView(ui.scroll(list), LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(5) })
        ui.add(card, date)
        ui.add(card, title, 3)
        card.addView(image, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(7) })
        ui.add(card, caption, 3)
        ui.add(card, rail, 3, 34)
        ui.add(card, summary, 4)
        ui.add(card, details, 7)
        preview(0, selected)
        preferred = "life.${selected.id}"
        body.addView(directory, LinearLayout.LayoutParams(0, -1, .8f))
        body.addView(card, LinearLayout.LayoutParams(0, -1, 1.2f).apply { marginStart = ui.dp(12) })
        return body
    }

    private fun lab(item: Topic): View {
        val lab = LabWorkbench(this, ui, store, item, if (item.wing == "mechanics") gold else content.wing(item.wing).color,
            { key, view -> bind(key, view) }, { quiz(item) }, { speak(it) })
        workbench = lab
        preferred = lab.initial
        return lab.view
    }

    private fun quiz(item: Topic, daily: Boolean = false) {
        val previous = currentFocus
        val color = content.wing(item.wing).color
        val box = ui.column().apply {
            background = ui.shape(ui.surface, color, 19)
            ui.pad(this, 18, 14)
        }
        val popup = backDialog()
        ui.add(box, ui.text(if (daily) "تحدّي اليوم" else item.title, 18, color, true))
        ui.add(box, ui.text(item.quiz.question, 27, ui.foreground, true), 7)
        val feedback = ui.text("", 19, muted)
        var first: View? = null
        val options = item.quiz.options.mapIndexed { index, option ->
            ui.button(option, color, 19) {
                val success = index == item.quiz.correct
                if (success) {
                    store.star(item)
                    if (daily) {
                        val format = SimpleDateFormat("yyyyMMdd", Locale.US)
                        val now = Calendar.getInstance()
                        val today = format.format(now.time)
                        now.add(Calendar.DAY_OF_YEAR, -1)
                        store.dailyWin(today, format.format(now.time))
                    }
                }
                if (store.flag("sound")) {
                    if (tone == null) tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 32) }.getOrNull()
                    tone?.startTone(if (success) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK, 100)
                }
                feedback.setTextColor(if (success) 0xff86efac.toInt() else 0xfffda4af.toInt())
                feedback.text = if (success) "✓  ${item.quiz.explanation}" else "جرّب مرة ثانية. فكّر في ${item.title}."
            }.apply { id = View.generateViewId(); if (index == 0) first = this }
        }
        options.chunked(2).forEach { items ->
            val row = ui.row()
            ui.cells(row, items, height = 65)
            ui.add(box, row, 9)
        }
        ui.add(box, feedback, 9)
        ui.add(box, ui.button("إغلاق", color, 17) { popup.dismiss() }, 8)
        popup.setContentView(ui.scroll(box))
        popup.setOnDismissListener { dialog = null; previous?.requestFocus(); refreshIdle() }
        dialog = popup
        popup.show()
        popup.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(.72f)
            setLayout(minOf(ui.dp(650), resources.displayMetrics.widthPixels - ui.dp(50)), -2)
        }
        first?.requestFocus()
    }

    private fun backDialog() = object : Dialog(this) {
        override fun onGenericMotionEvent(event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS &&
                (event.actionButton == MotionEvent.BUTTON_SECONDARY || event.actionButton == MotionEvent.BUTTON_BACK)) {
                dismiss()
                return true
            }
            return super.onGenericMotionEvent(event)
        }
    }

    private fun search(): View {
        val box = ui.column()
        val input = EditText(this).apply {
            id = View.generateViewId()
            hint = "اسم الموضوع أو المعادلة أو الوحدة: dBm، نيوتن"
            setHintTextColor(muted)
            setTextColor(ui.foreground)
            textSize = 21f
            setSingleLine()
            background = ui.shape(ui.surface, teal)
            ui.pad(this, 12, 8)
            setText(store.string("search", ""))
        }
        bind("search.input", input)
        val row = ui.row()
        row.addView(input, LinearLayout.LayoutParams(0, ui.dp(49), 1f))
        val keyboard = action("search.keyboard", "لوحة المفاتيح", teal, 15) {
            input.requestFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
        row.addView(keyboard, LinearLayout.LayoutParams(-2, -1).apply { marginStart = ui.dp(9) })
        val results = ui.column()
        fun show(query: String) {
            results.removeAllViews()
            focusViews.keys.removeAll { it.startsWith("result.") }
            val words = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            val matches = content.topics.filter { item ->
                val haystack = "${item.title} ${item.english} ${item.equation} ${item.metric} ${item.summary} ${item.controls.joinToString(" ") { it.label }}"
                words.all { haystack.contains(it, ignoreCase = true) }
            }
            ui.add(results, ui.text("${matches.size} موضوع", 15, muted), 4)
            matches.chunked(2).forEach { chunk ->
                val line = ui.row()
                ui.cells(line, chunk.map { item ->
                    action("result.${item.id}", item.title, content.wing(item.wing).color, 17) {
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(input.windowToken, 0)
                        open("topic", item.id)
                    }
                })
                ui.add(results, line, 6)
            }
        }
        show(input.text.toString())
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty()
                store.set("search", q)
                show(q)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        ui.add(box, row)
        box.addView(ui.scroll(results), LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = ui.dp(9) })
        preferred = "search.input"
        return box
    }

    private fun progress(): View {
        val box = ui.column()
        val done = content.labs.count(store::done)
        val stars = content.topics.count(store::starred)
        ui.add(box, ui.text("$done / ${content.labs.size} تجربة مكتملة    •    $stars نجمة", 27, ui.foreground, true))
        val streak = store.number("daily.streak", 0)
        if (streak > 0) ui.add(box, ui.text("سلسلة تحدّي اليوم  ·  $streak", 19, teal), 6)
        content.wings.forEach { wing ->
            val labs = wing.topics.filter(Topic::isLab)
            val line = "${wing.title}   ·   ${wing.topics.count(store::starred)} ★   ·   " +
                if (labs.isNotEmpty()) "${labs.count(store::done)}/${labs.size} تجربة" else "${wing.topics.count(store::seen)}/${wing.topics.size} شوهد"
            ui.add(box, action("progress.${wing.id}", line, wing.color, 19) { openWing(wing) }, 6)
        }
        preferred = "progress.telecom"
        return ui.scroll(box)
    }

    private fun settings(): View {
        val box = ui.column()
        fun setting(key: String, label: String, value: String, change: () -> Unit) {
            ui.add(box, action("settings.$key", "$label   •   $value", teal, 19) {
                change()
                pages.last().focus = "settings.$key"
                render()
            }, 6)
        }
        fun toggle(key: String, label: String, default: Boolean = false) {
            val current = store.flag(key, default)
            setting(key, label, if (current) "مفعّل" else "متوقف") { store.set(key, !current) }
        }
        val size = store.number("text", 1).coerceIn(0, 3)
        setting("text", "حجم النص", listOf("صغير", "متوسط", "كبير", "كبير جدًا")[size]) { store.set("text", (size + 1) % 4) }
        toggle("contrast", "التباين العالي")
        toggle("reduced", "تقليل الحركة")
        toggle("guided", "اشترط التوقّع قبل التجربة", true)
        toggle("graph", "إظهار الرسم البياني افتراضيًا", true)
        toggle("ambient", "عرض المقاييس بعد 3 دقائق من الخمول")
        toggle("narration", "القراءة الصوتية", true)
        val speed = store.number("tts.speed", 1).coerceIn(0, 2)
        setting("tts.speed", "سرعة القراءة", listOf("بطيئة", "عادية", "سريعة")[speed]) { store.set("tts.speed", (speed + 1) % 3) }
        toggle("sound", "صوت الإجابة")
        ui.add(box, action("settings.preview", "ابدأ عرض المقاييس الآن", teal, 19) { open("ambient") }, 6)
        ui.add(box, ui.text("ALRADHY Discovery Science  ·  1.2.0\nيعمل المتحف دون اتصال بالإنترنت.", 17, muted), 12)
        preferred = "settings.text"
        return ui.scroll(box)
    }

    private fun speak(text: String) {
        if (!store.flag("narration", true)) return
        val engine = tts
        if (engine != null) {
            if (ttsReady) playSpeech(engine, text)
            else Toast.makeText(this, "الصوت العربي غير متاح على الجهاز", Toast.LENGTH_SHORT).show()
            return
        }
        tts = TextToSpeech(this) { result ->
            val service = tts
            if (service != null && result == TextToSpeech.SUCCESS && service.isLanguageAvailable(Locale("ar")) >= TextToSpeech.LANG_AVAILABLE) {
                service.language = Locale("ar")
                ttsReady = true
                playSpeech(service, text)
            } else Toast.makeText(this, "الصوت العربي غير متاح على الجهاز", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playSpeech(engine: TextToSpeech, text: String) {
        engine.setSpeechRate(listOf(.75f, 1f, 1.2f)[store.number("tts.speed", 1).coerceIn(0, 2)])
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "museum-topic")
    }

    private fun action(key: String, label: String, color: Int, size: Int, block: () -> Unit): TextView =
        bind(key, ui.button(label, color, size, block))

    private fun <T : View> bind(key: String, view: T): T {
        if (view.id == View.NO_ID) view.id = View.generateViewId()
        focusViews[key] = view
        return view
    }
}

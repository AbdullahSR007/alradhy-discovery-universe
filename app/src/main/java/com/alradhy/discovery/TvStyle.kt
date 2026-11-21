package com.alradhy.discovery

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import kotlin.math.roundToInt

internal val paper = 0xffe8eef9.toInt()
internal val muted = 0xffa6b7d2.toInt()
internal val gold = 0xffffcc19.toInt()
internal val teal = 0xff5eead4.toInt()
internal val panel = 0xff111a2e.toInt()
internal val stroke = 0xff263654.toInt()

internal class TvStyle(private val context: Context, private val store: ProgressStore) {
    private val regular = context.resources.getFont(R.font.ibmplexsansarabic_regular)
    private val medium = context.resources.getFont(R.font.ibmplexsansarabic_medium)

    fun dp(value: Int) = (value * context.resources.displayMetrics.density + .5f).toInt()
    val motion get() = !store.flag("reduced")
    val foreground get() = if (store.flag("contrast")) -1 else paper
    val surface get() = if (store.flag("contrast")) 0xff06080c.toInt() else panel
    val border get() = if (store.flag("contrast")) 0xff92a5c4.toInt() else stroke
    private val textScale get() = listOf(.9f, 1f, 1.15f, 1.3f)[store.number("text", 1).coerceIn(0, 3)]

    fun shape(color: Int, line: Int = border, radius: Int = 13, thickness: Int = 1) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(thickness), line)
    }

    fun pad(view: View, horizontal: Int, vertical: Int) {
        view.setPadding(dp(horizontal), dp(vertical), dp(horizontal), dp(vertical))
    }

    fun column() = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        clipChildren = false
        clipToPadding = false
    }

    fun row() = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        clipChildren = false
        clipToPadding = false
    }

    fun text(value: CharSequence, size: Int = 22, color: Int = foreground, bold: Boolean = false) = TextView(context).apply {
        text = value
        textSize = size * textScale
        setTextColor(if (store.flag("contrast") && color == muted) paper else color)
        typeface = if (bold) medium else regular
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        includeFontPadding = false
    }

    fun number(value: String, size: Int, color: Int = foreground) = text(ltr(value), size, color, true).apply {
        typeface = Typeface.MONOSPACE
        textDirection = View.TEXT_DIRECTION_LTR
    }

    fun button(label: String, color: Int = teal, size: Int = 19, onClick: () -> Unit) =
        text(label, size, foreground, true).apply {
            gravity = Gravity.CENTER
            minimumHeight = dp(43)
            pad(this, 11, 6)
            interactive(this, color, onClick)
        }

    fun interactive(view: View, accent: Int, onClick: () -> Unit) {
        val highlight = (accent and 0x00ffffff) or 0xff000000.toInt()
        view.background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), shape(0xff344467.toInt(), highlight, thickness = 3))
            addState(intArrayOf(android.R.attr.state_focused), shape(0xff1c2d49.toInt(), highlight, thickness = 3))
            addState(intArrayOf(android.R.attr.state_hovered), shape(0xff1a2841.toInt(), highlight, thickness = 2))
            addState(intArrayOf(), shape(surface))
        }
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setOnClickListener { onClick() }
        view.setOnTouchListener { target, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN && !target.isFocused) target.requestFocus()
            false
        }
        view.setOnHoverListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_HOVER_ENTER && v.hasWindowFocus()) v.requestFocus()
            false
        }
        view.onFocusChangeListener = View.OnFocusChangeListener { v, focused ->
            val scale = if (focused && motion) 1.035f else 1f
            v.animate().scaleX(scale).scaleY(scale).translationZ(dp(if (focused) 6 else 0).toFloat())
                .setDuration(if (motion) 130 else 0).start()
        }
    }

    fun scroll(child: View): ScrollView = ScrollView(context).apply {
        outlineProvider = ViewOutlineProvider.BOUNDS
        clipToOutline = true
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        clipToPadding = false
        isFillViewport = false
        isVerticalScrollBarEnabled = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isFocusable = false
        addView(child, ViewGroup.LayoutParams(-1, -2))
    }

    fun add(parent: LinearLayout, view: View, top: Int = 0, height: Int = -2) {
        parent.addView(view, LinearLayout.LayoutParams(-1, if (height > 0) dp(height) else height).apply { topMargin = dp(top) })
    }

    fun cells(row: LinearLayout, views: List<View>, gap: Int = 8, height: Int = -2) {
        views.forEachIndexed { index, view ->
            row.addView(view, LinearLayout.LayoutParams(0, if (height > 0) dp(height) else height, 1f).apply {
                if (index > 0) marginStart = dp(gap)
            })
        }
        views.zipWithNext().forEach { (right, left) ->
            right.nextFocusLeftId = left.id
            left.nextFocusRightId = right.id
        }
    }
}

internal class TvSlider(
    context: Context, private val ui: TvStyle, private val control: Control,
    private val accent: Int, val discrete: Boolean, private val changed: (Double) -> Unit
) : SeekBar(context) {
    private val ticks = if (discrete) ((control.max - control.min) / control.step).roundToInt().coerceAtLeast(1) else 10000
    private var fine = false
    private var editing = false
    private var supplied = control.initial
    private val isDiscrete get() = discrete
    var onFine: ((Boolean) -> Unit)? = null

    init {
        max = ticks
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        isFocusable = true
        isFocusableInTouchMode = true
        minimumHeight = ui.dp(37)
        val track = ui.shape(0xff26354e.toInt(), 0xff26354e.toInt(), 4)
        val tint = ui.shape(accent, accent, 4)
        progressDrawable = LayerDrawable(arrayOf(track, ClipDrawable(tint, Gravity.LEFT, ClipDrawable.HORIZONTAL))).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
            for (index in 0..1) {
                setLayerHeight(index, ui.dp(6))
                setLayerGravity(index, Gravity.CENTER_VERTICAL or Gravity.FILL_HORIZONTAL)
            }
        }
        fun knob(fill: Int, border: Int, thickness: Int) = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(ui.dp(27), ui.dp(27))
            setColor(fill)
            setStroke(ui.dp(thickness), border)
        }
        thumb = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), knob(0xff0b1222.toInt(), accent, 3))
            addState(intArrayOf(), knob(accent, 0xff0b1222.toInt(), 3))
        }
        thumbOffset = ui.dp(13)
        ui.pad(this, 8, 3)
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), ui.shape(0xff142139.toInt(), 0xff293a58.toInt(), radius = 12))
            addState(intArrayOf(), ui.shape(ui.surface, ui.surface, radius = 12))
        }
        setOnHoverListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_HOVER_ENTER && v.hasWindowFocus()) v.requestFocus()
            false
        }
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && !editing) {
                    val raw = control.min + (control.max - control.min) * progress / ticks
                    val step = control.step / if (fine && !isDiscrete) 10 else 1
                    val target = control.min + ((raw - control.min) / step).roundToInt() * step
                    edit(target)
                }
            }
            override fun onStartTrackingTouch(slider: SeekBar) { requestFocus() }
            override fun onStopTrackingTouch(slider: SeekBar) = Unit
        })
        show(control.initial)
    }

    fun show(value: Double) {
        supplied = control.clamp(value)
        editing = true
        progress = ((supplied - control.min) / (control.max - control.min) * ticks).roundToInt()
        editing = false
        contentDescription = control.label + " " + fmt(supplied)
    }

    private fun edit(value: Double) {
        val next = control.clamp(value)
        show(next)
        changed(next)
    }

    private fun advance(direction: Int, multiplier: Int = 1) {
        edit(supplied + direction * multiplier * control.step / if (fine && !isDiscrete) 10 else 1)
    }

    override fun onKeyDown(code: Int, event: KeyEvent): Boolean {
        when (code) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                advance(if (code == KeyEvent.KEYCODE_DPAD_LEFT) -1 else 1, if (event.repeatCount >= 5) 5 else 1)
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (event.repeatCount == 0 && !isDiscrete) {
                    fine = !fine
                    onFine?.invoke(fine)
                }
                return true
            }
        }
        return super.onKeyDown(code, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_SCROLL) {
            val wheel = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (wheel != 0f) {
                advance(if (wheel > 0) 1 else -1)
                return true
            }
        }
        return super.onGenericMotionEvent(event)
    }
}

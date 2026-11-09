package com.alradhy.worldwindow

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    data class Destination(
        val title: String,
        val subtitle: String,
        val url: String
    )

    private val destinations = listOf(
        Destination(
            "طوكيو",
            "اليابان - شيبويا",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Shibuya_crossing_time-lapse_2019-01-05.webm"
        ),
        Destination(
            "جبال الألب",
            "سويسرا - ماترهورن",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Timelapse_of_Swiss_mountains.webm"
        ),
        Destination(
            "كوكب الأرض",
            "محطة الفضاء الدولية",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Earth_Illuminated-_ISS_Time-lapse_Photography.webm"
        ),
        Destination(
            "أعماق البحر",
            "استكشاف أعماق المحيط",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Ex1402-dive11_fish.webm"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.rgb(4, 13, 23))
            setPadding(dp(44), dp(30), dp(44), dp(30))
        }

        val branding = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(40), 0)
        }

        branding.addView(text("ALRADHY", 18, Color.rgb(73, 207, 190), true))
        branding.addView(text("WORLD\nWINDOW", 44, Color.WHITE, true))
        branding.addView(text("نافذة على العالم", 22, Color.rgb(183, 203, 211), false))
        branding.addView(text("\nاختر وجهة لبدء المشاهدة", 16, Color.rgb(120, 160, 170), false))

        root.addView(
            branding,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.38f)
        )

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        destinations.forEachIndexed { index, destination ->
            val card = TextView(this).apply {
                text = "${destination.title}\n${destination.subtitle}"
                textSize = 20f
                setTextColor(Color.WHITE)
                setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(28), dp(15), dp(28), dp(15))
                isFocusable = true
                isClickable = true
                background = cardBackground(false)

                setOnFocusChangeListener { view, focused ->
                    view.background = cardBackground(focused)
                    view.animate()
                        .scaleX(if (focused) 1.025f else 1f)
                        .scaleY(if (focused) 1.025f else 1f)
                        .setDuration(120)
                        .start()
                }

                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, PlayerActivity::class.java).apply {
                            putExtra("title", destination.title)
                            putExtra("subtitle", destination.subtitle)
                            putExtra("url", destination.url)
                        }
                    )
                }
            }

            list.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(105)
                ).apply {
                    setMargins(0, dp(7), 0, dp(7))
                }
            )

            if (index == 0) {
                card.post { card.requestFocus() }
            }
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(list)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.62f)
        )

        setContentView(root)
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun cardBackground(focused: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(
                if (focused) Color.rgb(18, 112, 115)
                else Color.rgb(17, 34, 47)
            )
            setStroke(
                dp(if (focused) 3 else 1),
                if (focused) Color.rgb(134, 244, 220)
                else Color.rgb(47, 74, 88)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

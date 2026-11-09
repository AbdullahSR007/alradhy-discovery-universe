package com.alradhy.worldwindow

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this)

        title.textSize = 28f

        title.text =
            intent.getStringExtra("title")
                ?: "World Window"

        setContentView(title)
    }
}

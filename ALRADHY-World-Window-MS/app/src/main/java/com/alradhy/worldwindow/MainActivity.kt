package com.alradhy.worldwindow

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val places = arrayOf(
        "Riyadh",
        "Jeddah",
        "Tokyo",
        "Paris",
        "Swiss Alps",
        "ISS"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val list = ListView(this)

        list.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            places
        )

        list.setOnItemClickListener { _, _, p, _ ->

            val intent = Intent(
                this,
                PlayerActivity::class.java
            )

            intent.putExtra(
                "title",
                places[p]
            )

            startActivity(intent)
        }

        setContentView(list)
    }
}

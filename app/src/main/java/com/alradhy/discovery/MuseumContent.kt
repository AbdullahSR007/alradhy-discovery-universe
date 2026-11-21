package com.alradhy.discovery

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

internal data class Control(val label: String, val min: Double, val max: Double, val initial: Double, val step: Double,
    val symbol: String = "", val unit: String = "", val discrete: Boolean = false) {
    fun fromLegacy(position: Int) = min + (max - min) * position.coerceIn(0, 100) / 100
    fun clamp(value: Double): Double {
        if (!value.isFinite()) return initial
        val bounded = value.coerceIn(min, max)
        return if (discrete) (min + Math.round((bounded - min) / step) * step).coerceIn(min, max) else bounded
    }
}

internal data class Quiz(val question: String, val options: List<String>, val correct: Int, val explanation: String)
internal data class OldStation(val axis: Int, val index: Int, val titleHash: Int)

internal data class LifeChapter(
    val yearsAgo: Long, val date: String, val evidence: String, val source: String,
    val credit: String, val art: String, val related: String
)

internal data class Topic(
    val id: String, val wing: String, val title: String, val english: String, val summary: String,
    val idea: String, val equation: String, val quiz: Quiz, val visual: Int, val scale: Double?,
    val legacy: Int?, val station: OldStation?, val controls: List<Control>, val metric: String,
    val assumptions: String, val challenge: String, val note: String, val application: String, val facts: List<String>,
    val unit: String, val symbol: String, val axis: Int,
    val section: String = "", val life: LifeChapter? = null
) {
    val isLab get() = controls.isNotEmpty()
    fun defaults() = controls.map { it.initial }.toDoubleArray()
}

internal data class Wing(
    val id: String, val title: String, val english: String, val color: Int,
    val summary: String, val topics: List<Topic>
)

internal class MuseumContent(context: Context) {
    val wings = listOf("scale", "mechanics", "fields", "light", "astro", "telecom", "matter").map { id ->
        val data = context.assets.open("content/$id.json").bufferedReader().use { JSONObject(it.readText()) }
        val items = data.getJSONArray("topics").mapObjects { readTopic(it, id) }
        Wing(id, data.getString("titleAr"), data.getString("titleEn"), Color.parseColor(data.getString("accent")), data.getString("summary"), items)
    }
    val topics = wings.flatMap { it.topics }
    val labs = topics.filter { it.isLab }
    val life = wing("matter").topics.filter { it.life != null }.sortedByDescending { it.life!!.yearsAgo }
    val scale = wings.first().topics.sortedBy { it.scale }
    val byId = topics.associateBy { it.id }

    fun wing(id: String) = wings.first { it.id == id }

    private fun readTopic(data: JSONObject, wing: String): Topic {
        val legacy = data.optInt("legacyIndex", -1).takeIf { it >= 0 }
        val rawControls = data.optJSONArray("controls")
        val controls = rawControls?.mapObjects {
            Control(it.getString("label"), it.getDouble("min"), it.getDouble("max"), it.getDouble("init"),
                it.getDouble("step"), it.optString("symbol"), it.optString("unit"), it.optBoolean("discrete"))
        }.orEmpty()
        val facts = data.optJSONArray("facts")
        val q = data.getJSONObject("quiz")
        val options = q.getJSONArray("options")
        val station = data.optJSONObject("legacyStation")?.let {
            OldStation(it.getInt("axis"), it.getInt("index"), it.getInt("hash"))
        }
        return Topic(
            data.getString("id"), wing, data.getString("titleAr"), data.getString("titleEn"),
            data.getString("summary"), data.getString("keyIdea"), data.getString("equation"),
            Quiz(q.getString("question"), List(options.length()) { options.getString(it) }, q.getInt("correctIndex"), q.getString("explanation")),
            data.optInt("visualType", 9), data.optDouble("scaleMeters").takeIf { it.isFinite() }, legacy, station, controls,
            data.optString("metric"), data.optString("assumptions"), data.optString("challenge"),
            data.optString("discoveryNote"), data.optString("application"),
            facts?.let { List(it.length()) { index -> it.getString(index) } }.orEmpty(),
            data.optString("unit"), data.optString("symbol"), data.optInt("axis"),
            data.optString("section"), data.optJSONObject("timeline")?.let {
                LifeChapter(it.getLong("yearsAgo"), it.getString("date"), it.getString("evidence"),
                    it.getString("source"), it.getString("credit"), it.getString("art"), it.getString("related"))
            }
        )
    }

    private fun <T> JSONArray.mapObjects(read: (JSONObject) -> T): List<T> =
        List(length()) { read(getJSONObject(it)) }
}

internal class ProgressStore(private val context: Context, private val content: MuseumContent) {
    private fun open(name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val prefs = open("science_museum_v1")

    init {
        migrate()
    }

    private fun migrate() {
        if (prefs.getBoolean("migrated_v1", false)) return
        val science = open("alradhy_discovery_museum_v05").all
        val light = open("alradhy_light_v05").all
        val hub = open("discovery_fusion_progress").all
        val editor = prefs.edit()
        fun copyAll(prefix: String, data: Map<String, *>) {
            data.forEach { (k, v) ->
                when (v) {
                    is Boolean -> editor.putBoolean("$prefix.$k", v)
                    is Int -> editor.putInt("$prefix.$k", v)
                    is Float -> editor.putFloat("$prefix.$k", v)
                    is Long -> editor.putLong("$prefix.$k", v)
                    is String -> editor.putString("$prefix.$k", v)
                }
            }
        }
        // Preserve observations that have no matching science topic, including removed stations.
        copyAll("legacy.hub", hub)
        copyAll("legacy.light", light)
        editor.putInt("credits", science["credits"] as? Int ?: 0)
        editor.putBoolean("guided", (science["guided"] ?: light["guideOn"]) as? Boolean ?: true)
        editor.putBoolean("graph", science["showGraph"] as? Boolean ?: true)
        for (topic in content.topics) {
            topic.legacy?.let { index ->
                val id = topic.id
                val values = topic.defaults()
                listOf("a", "b").forEachIndexed { idx, key ->
                    (science[key + "_" + index] as? Number)?.let { values[idx] = topic.controls[idx].fromLegacy(it.toInt()) }
                }
                if (index == 2) values[2] = topic.controls[2].clamp((science["q2"] as? Number)?.toDouble() ?: values[2])
                editor.putString("values.$id", encode(values))
                    .putInt("prediction.$id", science["pred_$index"] as? Int ?: -1)
                    .putInt("actual.$id", science["predact_$index"] as? Int ?: -1)
                    .putBoolean("done.$id", science["done_$index"] == true)
                if (science["compare_has_$index"] == true) {
                    val baseline = values.copyOf()
                    listOf("compare_a", "compare_b", "compare_q2").take(values.size).forEachIndexed { idx, key ->
                        (science[key + "_" + index] as? Number)?.let { baseline[idx] = topic.controls[idx].clamp(it.toDouble()) }
                    }
                    editor.putString("baseline.$id", encode(baseline))
                }
            }
            topic.station?.let {
                val key = "${it.axis}_${it.titleHash}"
                if (hub["seen_$key"] == true) editor.putBoolean("seen.${topic.id}", true)
                if (hub["quiz_$key"] == true) editor.putBoolean("quiz.${topic.id}", true)
                if (hub["category"] == it.axis && hub["station"] == it.index && hub["seen_$key"] == true) {
                    editor.putString("scale.last", topic.id)
                }
            }
        }
        val lightControls = listOf(
            "light.galileo" to listOf("galileoDistance", "observerReactionMs"),
            "light.roemer" to listOf("romerPosition"),
            "light.fizeau" to listOf("rpm", "distance")
        )
        lightControls.forEachIndexed { index, (id, keys) ->
            val topic = content.byId.getValue(id)
            val values = topic.defaults()
            keys.forEachIndexed { i, key ->
                (light[key] as? Number)?.let { values[i] = topic.controls[i].clamp(it.toDouble()) }
            }
            if (keys.any { light.containsKey(it) }) editor.putString("values.$id", encode(values))
            if (light["hasMeasure_$index"] == true) {
                editor.putBoolean("done.$id", true)
                (light["measure_$index"] as? Long)?.let { editor.putLong("reading.$id", it) }
            }
        }
        // The migration marker and its values are committed in a single transaction.
        check(editor.putBoolean("migrated_v1", true).commit()) { "Unable to save museum progress" }
    }

    private fun encode(values: DoubleArray) = values.joinToString(",")
    private fun decode(key: String, topic: Topic): DoubleArray? {
        val saved = prefs.getString(key, null)?.split(",") ?: return null
        if (saved.size != topic.controls.size) return null
        return DoubleArray(saved.size) { i -> topic.controls[i].clamp(saved[i].toDoubleOrNull() ?: topic.controls[i].initial) }
    }

    fun values(topic: Topic) = decode("values.${topic.id}", topic) ?: topic.defaults()
    fun baseline(topic: Topic) = decode("baseline.${topic.id}", topic)
    fun saveValues(topic: Topic, values: DoubleArray) = prefs.edit().putString("values.${topic.id}", encode(values)).apply()
    fun saveBaseline(topic: Topic, values: DoubleArray) = prefs.edit().putString("baseline.${topic.id}", encode(values)).apply()
    fun prediction(topic: Topic) = prefs.getInt("prediction.${topic.id}", -1)
    fun predict(topic: Topic, choice: Int) = prefs.edit().putInt("prediction.${topic.id}", choice).apply()
    fun measured(topic: Topic): Double? = if (prefs.contains("reading.${topic.id}")) {
        Double.fromBits(prefs.getLong("reading.${topic.id}", 0L)).takeIf { it.isFinite() }
    } else null
    fun markDone(topic: Topic, actual: Int, reading: Double) =
        prefs.edit().putBoolean("done.${topic.id}", true).putInt("actual.${topic.id}", actual)
            .putLong("reading.${topic.id}", reading.toBits()).apply()

    fun done(topic: Topic) = prefs.getBoolean("done.${topic.id}", false)
    fun starred(topic: Topic) = prefs.getBoolean("quiz.${topic.id}", false)
    fun seen(topic: Topic) = prefs.getBoolean("seen.${topic.id}", false)
    fun star(topic: Topic) = prefs.edit().putBoolean("quiz.${topic.id}", true).apply()
    fun dailyWin(today: String, yesterday: String) {
        val last = string("daily.date", "")
        if (last == today) return
        val streak = if (last == yesterday) number("daily.streak", 0) + 1 else 1
        prefs.edit().putString("daily.date", today).putInt("daily.streak", streak).apply()
    }
    fun visit(topic: Topic) {
        val editor = prefs.edit().putBoolean("seen.${topic.id}", true)
        if (topic.isLab) editor.putString("recent", (listOf(topic.id) + recent().map { it.id }).distinct().take(4).joinToString(","))
        if (topic.scale != null) editor.putString("scale.last", topic.id)
        editor.apply()
    }
    fun recent() = string("recent", "").split(",").mapNotNull(content.byId::get)
    fun flag(key: String, default: Boolean = false) = prefs.getBoolean(key, default)
    fun number(key: String, default: Int) = prefs.getInt(key, default)
    fun string(key: String, default: String) = prefs.getString(key, default) ?: default
    fun set(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun set(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun set(key: String, value: String) = prefs.edit().putString(key, value).apply()
}

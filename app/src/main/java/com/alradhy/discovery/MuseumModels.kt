package com.alradhy.discovery

import com.alradhy.discoverylab.LabCatalog
import com.alradhy.discoverylab.ExpansionModels
import com.alradhy.discoverylab.MuseumMeasurements
import com.alradhy.discoverylab.ScienceExtensions as Science
import java.util.Locale
import kotlin.math.abs

internal fun fmt(number: Double, digits: Int = 2): String {
    if (!number.isFinite()) return "—"
    if (number != 0.0 && (abs(number) < .0001 || abs(number) >= 1e7)) return String.format(Locale.US, "%.2e", number)
    return String.format(Locale.US, "%.$digits" + "f", number)
}

internal fun ltr(text: String) = "\u2066$text\u2069"

internal data class Preset(val label: String, val values: DoubleArray)

internal fun presets(topic: Topic): List<Preset> = when (topic.id) {
    "telecom.link-budget" -> listOf(
        Preset("E-band · 80 GHz", doubleArrayOf(2.0, 80.0, 18.0, 1.0, 43.0, 43.0, 1.0, 6.0, -72.0)),
        Preset("TX hop · 6 GHz", doubleArrayOf(40.0, 6.0, 25.0, 1.5, 34.0, 34.0, 1.5, 2.0, -78.0)),
        Preset("LTE 1800 · حافة الخلية", doubleArrayOf(2.5, 1.8, 43.0, 2.0, 17.0, 0.0, 0.0, 0.0, -100.0))
    )
    "telecom.fresnel" -> listOf(
        Preset("TX hop · 40 km", doubleArrayOf(40.0, 6.0, 25.0, 50.0, 60.0, 60.0, 1.33)),
        Preset("E-band · 2 km", doubleArrayOf(2.0, 80.0, 16.0, 40.0, 20.0, 22.0, 1.33))
    )
    "telecom.orbit-latency" -> listOf(
        Preset("GEO · 35,786 km", doubleArrayOf(35_786.0, 20.0)),
        Preset("LEO · 550 km", doubleArrayOf(550.0, 20.0))
    )
    else -> emptyList()
}

internal fun graphAxis(topic: Topic): Int = topic.axis

internal fun formatControl(topic: Topic, index: Int, value: Double, digits: Int = 2): String {
    if (topic.id == "telecom.qam" && index == 0) return (1 shl Science.qamBits(value)).toString() + "-QAM"
    if (topic.id == "telecom.pon-split" && index == 0) return fmt(value, 0) + " (1:" + (1 shl value.toInt()) + ")"
    if (topic.id == "telecom.nr-grid" && index == 1) return fmt(value, 0) + " (" + fmt(15 * Math.pow(2.0, value), 0) + " kHz)"
    val spec = topic.controls[index]
    val precision = if (spec.discrete) 0 else if (spec.step <= .0001) 4 else digits
    return fmt(value, precision)
}

internal val Topic.isExpansion: Boolean get() = ExpansionModels.MODELS.containsKey(id)


internal fun reading(topic: Topic, p: DoubleArray): Double =
    MuseumMeasurements.measure(topic.id, topic.legacy ?: -1, p)

internal fun labReadout(topic: Topic, p: DoubleArray): String {
    val value = reading(topic, p)
    if (topic.id == "light.total-reflection" && value.isFinite() && abs(value - 90) < 1e-5)
        return "الشعاع المنكسر يلامس السطح عند الزاوية الحرجة"
    topic.legacy?.takeIf { it >= 12 }?.let { return LabCatalog.D[it - 12].r_(p[0], p[1]) }
    return when (topic.id) {
        "telecom.link-budget" -> {
            val rx = Science.linkStages(p).last()
            "استقبال ${ltr("${fmt(rx, 1)} dBm")}   •   هامش ${ltr("${fmt(value, 1)} dB")}\nفقد المسار ${ltr("${fmt(Science.linkLoss(p), 1)} dB")}"
        }
        "telecom.fresnel" -> {
            val radius = Science.fresnelRadius(p[0], p[1], p[3] / 100)
            "الخلوص ${ltr("${fmt(value, 1)}%")}   •   نصف القطر ${ltr("${fmt(radius)} m")}\nفوق العائق ${ltr("${fmt(Science.clearanceMetres(p))} m")}"
        }
        "telecom.qam" -> {
            val gross = p[2] * Science.qamBits(p[0])
            "EVM ${ltr("${fmt(Science.evmPercent(p[1]))}%")}   •   SER ${ltr("${fmt(100 * Science.symbolError(p[0], p[1]))}%")}\nالمعدل ${ltr("${fmt(value, 1)} / ${fmt(gross, 1)} Mb/s")}"
        }
        "telecom.orbit-latency" -> "زمن الجولة ${ltr("${fmt(value)} ms")}\nالانتشار وحده ${ltr("${fmt(value - p[1])} ms")}"
        "light.galileo" -> "زمن الضوء ${ltr("${fmt(value)} µs")}\nالاستجابة أبطأ بنحو ${ltr(fmt(p[1] * 1000 / value, 0))} مرة"
        "light.fizeau" -> "السطوع ${ltr("${fmt(value, 1)}%")}\nسرعة أول انطفاء ${ltr("${fmt(Science.fizeauExtinction(p[1]), 1)} rpm")}"
        else -> "${topic.metric}\n${ltr(fmt(value, 3))}"
    }
}

internal val detailedReadouts = setOf("telecom.link-budget", "telecom.fresnel", "telecom.qam", "telecom.orbit-latency", "light.galileo", "light.fizeau")

internal fun headlineUnit(topic: Topic): String = when (topic.unit) {
    "" -> ""
    "%", "°", "×" -> topic.unit
    else -> " " + topic.unit
}

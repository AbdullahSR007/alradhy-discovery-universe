package com.alradhy.discovery

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {
    private var currentIndex = 0
    private lateinit var titleView: TextView
    private lateinit var descView: TextView
    private lateinit var counterView: TextView
    private lateinit var hintView: TextView

    private data class Station(val id: Int, val category: String, val title: String, val description: String)

    private val stations = listOf(
        Station(1, "History", "Station 1: The Dawn of Civilization", "Exploring the earliest human settlements in Mesopotamia and writing around 3500 BC."),
        Station(2, "History", "Station 2: The Pyramids of Giza", "The monumental engineering feats of Ancient Egypt built as eternal tombs."),
        Station(3, "History", "Station 3: The Indus Valley", "Advanced urban planning and trade networks of Harappa and Mohenjo-daro."),
        Station(4, "History", "Station 4: Ancient Greece", "The birth of democracy, philosophy, and classical architecture in Athens."),
        Station(5, "History", "Station 5: The Roman Empire", "Engineering marvels and vast military expansion across the Mediterranean."),
        Station(6, "History", "Station 6: The Silk Road", "The historic network of Eurasian trade routes connecting East and West."),
        Station(7, "History", "Station 7: The Islamic Golden Age", "A flourishing period of science, mathematics, and optics centered in Baghdad."),
        Station(8, "History", "Station 8: The Tang Dynasty", "A golden age of Chinese cosmopolitan culture, poetry, and inventions."),
        Station(9, "History", "Station 9: The Viking Age", "Seafaring Norse explorers and traders shaping European history."),
        Station(10, "History", "Station 10: The Mayan Civilization", "Advanced astronomy, mathematics, and pyramid architecture in Mesoamerica."),
        Station(11, "History", "Station 11: The Crusades", "Medieval military expeditions and cultural exchanges in the Levant."),
        Station(12, "History", "Station 12: The Mongol Empire", "The largest contiguous land empire in history founded by Genghis Khan."),
        Station(13, "History", "Station 13: The Renaissance", "The cultural rebirth of European art, science, and humanism in Italy."),
        Station(14, "History", "Station 14: The Age of Discovery", "Global maritime explorations linking the Old World with the Americas."),
        Station(15, "History", "Station 15: The Ottoman Empire", "A vast transcontinental empire bridging East and West for centuries."),
        Station(16, "History", "Station 16: The Industrial Revolution", "Transition to new manufacturing powered by steam and coal engines."),
        Station(17, "History", "Station 17: The American Revolution", "The birth of a constitutional republic and modern democratic ideals."),
        Station(18, "History", "Station 18: The French Revolution", "A period of radical social and political upheaval in Europe."),
        Station(19, "History", "Station 19: The Meiji Restoration", "The rapid modernization and industrialization of Imperial Japan."),
        Station(20, "History", "Station 20: World War I", "The global conflict that reshaped geopolitical borders and empires."),
        Station(21, "History", "Station 21: The Roaring Twenties", "An era of economic prosperity and jazz culture adoption."),
        Station(22, "History", "Station 22: World War II", "The defining global conflict leading to the United Nations formation."),
        Station(23, "History", "Station 23: The Space Age", "Humanity breaks Earth's gravity leading to orbital satellites and lunar missions."),
        Station(24, "History", "Station 24: The Digital Revolution", "The shift from mechanical systems to digital computing and microprocessors."),
        Station(25, "History", "Station 25: The Information Age", "Global interconnectedness through the internet and mobile networks."),
        Station(26, "Cosmos", "Station 26: The Big Bang", "The monumental explosion and rapid expansion that birthed our universe."),
        Station(27, "Cosmos", "Station 27: Stellar Nurseries", "Nebulae where massive clouds of gas collapse to form new baby stars."),
        Station(28, "Cosmos", "Station 28: Main Sequence Stars", "The stable lifetime of stars converting hydrogen into helium via fusion."),
        Station(29, "Cosmos", "Station 29: Red Giants", "The bloated aging phase of stellar evolution when core fuel runs low."),
        Station(30, "Cosmos", "Station 30: Supernovae", "Cataclysmic stellar explosions forging heavy elements in space."),
        Station(31, "Cosmos", "Station 31: White Dwarfs", "The dense, cooling carbon embers left behind by dead stars."),
        Station(32, "Cosmos", "Station 32: Neutron Stars", "Inconceivably dense remnants of collapsed star cores spinning rapidly."),
        Station(33, "Cosmos", "Station 33: Stellar Black Holes", "Regions of spacetime where gravity traps everything, even light."),
        Station(34, "Cosmos", "Station 34: Supermassive Black Holes", "Monsters residing at the hearts of galaxies holding millions of solar masses."),
        Station(35, "Cosmos", "Station 35: The Solar System", "Our home planetary system orbiting a yellow dwarf sun."),
        Station(36, "Cosmos", "Station 36: Inner Rocky Planets", "Mercury, Venus, Earth, and Mars—dense terrestrial worlds."),
        Station(37, "Cosmos", "Station 37: Gas Giants", "Jupiter and Saturn—colossal worlds of hydrogen and helium."),
        Station(38, "Cosmos", "Station 38: Ice Giants", "Uranus and Neptune—frigid distant worlds rich in water and methane."),
        Station(39, "Cosmos", "Station 39: The Asteroid Belt", "A vast ring of rocky debris residing between Mars and Jupiter."),
        Station(40, "Cosmos", "Station 40: The Kuiper Belt", "A circumstellar disc in the outer solar system housing dwarf planets."),
        Station(41, "Cosmos", "Station 41: The Oort Cloud", "The distant spherical shell enclosing our entire solar system."),
        Station(42, "Cosmos", "Station 42: Exoplanets", "Alien worlds orbiting distant stars detected across the galaxy."),
        Station(43, "Cosmos", "Station 43: Milky Way Galaxy", "Our barred spiral home containing over 100 billion stars."),
        Station(44, "Cosmos", "Station 44: Local Galactic Group", "The gravitational cluster including the Milky Way and Andromeda."),
        Station(45, "Cosmos", "Station 45: Galaxy Clusters", "Vast cosmic structures bound together containing thousands of galaxies."),
        Station(46, "Cosmos", "Station 46: Cosmic Web", "The large-scale filamentary structure of the universe."),
        Station(47, "Cosmos", "Station 47: Dark Matter", "The mysterious invisible substance accounting for most matter."),
        Station(48, "Cosmos", "Station 48: Dark Energy", "The enigmatic repulsive force driving cosmic acceleration."),
        Station(49, "Cosmos", "Station 49: Cosmic Microwave Background", "The oldest electromagnetic radiation left over from the early universe."),
        Station(50, "Cosmos", "Station 50: The Edge of Time", "Pondering the ultimate cosmological fate of our expanding universe.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#07090E"))
            setPadding(80, 60, 80, 60)
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isFocusableInTouchMode = true
        }

        counterView = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.parseColor("#38BDF8"))
            setPadding(0, 0, 0, 20)
        }
        rootLayout.addView(counterView)

        titleView = TextView(this).apply {
            textSize = 34f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 30)
        }
        rootLayout.addView(titleView)

        descView = TextView(this).apply {
            textSize = 22f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 0, 0, 50)
        }
        rootLayout.addView(descView)

        hintView = TextView(this).apply {
            text = "◀ استعمل أزرار اليمين واليسار (أو انقر بالماوس/الشاشة) للتنقل بين المحطات (50 / 1) ▶"
            textSize = 15f
            setTextColor(Color.parseColor("#64748B"))
        }
        rootLayout.addView(hintView)

        // دعم النقر بالماوس لتغيير المحطة فوراً
        rootLayout.setOnClickListener {
            currentIndex = (currentIndex + 1) % stations.size
            updateUI()
        }

        setContentView(rootLayout)
        updateUI()
    }

    private fun updateUI() {
        val s = stations[currentIndex]
        counterView.text = "SECTION: ${s.category.uppercase()}  |  STATION ${s.id} OF 50"
        titleView.text = s.title
        descView.text = s.description
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (currentIndex < stations.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0 // العودة للأولى عند النهاية
                }
                updateUI()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                if (currentIndex > 0) {
                    currentIndex--
                } else {
                    currentIndex = stations.size - 1 // الانتقال للأخيرة
                }
                updateUI()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

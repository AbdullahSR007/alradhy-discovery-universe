package com.alradhy.discovery

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class DiscoveryItem(
    val badge: String,
    val title: String,
    val subtitle: String,
    val region: String,
    val summary: String,
    val facts: List<String>,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctIndex: Int,
    val visualType: Int
)

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var toneGen: ToneGenerator? = null

    private var currentCategory = 0 // 0 = Time Machine, 1 = Cosmic Scale
    private var currentIndex = 0
    private var simSpeed = 1.0f
    private var energyBoost = 1.0f

    private lateinit var universeCanvas: UniverseCanvasView
    private lateinit var stationRow: LinearLayout
    private lateinit var badgeText: TextView
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var summaryText: TextView
    private lateinit var factsContainer: LinearLayout
    private lateinit var speedLabel: TextView
    private lateinit var tabTimeBtn: TextView
    private lateinit var tabCosmosBtn: TextView

    private val historyStations = listOf(
        DiscoveryItem(
            badge = "[ 750 م ]",
            title = "العصر الذهبي وبيت الحكمة",
            subtitle = "بغداد والجزيرة العربية — ثورة الفلك والرياضيات",
            region = "النطاق الجغرافي: الجزيرة العربية والعراق والشام",
            summary = "تحولت الحواضر الإسلامية إلى مركز الإشعاع العلمي العالمي؛ حيث طوّر العلماء الإسطرلاب الفلكي لتحديد مواقع النجوم والملاحة، وأسس الخوارزمي علم الجبر وخوارزميات الحساب التي تقوم عليها الحواسيب الحديثة اليوم.",
            facts = listOf(
                "1. قياس محيط الأرض بدقة مذهلة في صحراء سنجار بأمر الخليفة المأمون.",
                "2. تطوير الإسطرلاب الكروي والمسطح لرصد حركة الكواكب وتحديد الأوقات.",
                "3. تأسيس علم الجبر والمقابلة وحساب المثلثات الكروية."
            ),
            quizQuestion = "ما هي الأداة الفلكية التي طورها علماء العصر العباسي لرصد النجوم وتحديد المواقع؟",
            quizOptions = listOf("1. الإسطرلاب الفلكي", "2. البوصلة المغناطيسية", "3. التلسكوب العاكس"),
            correctIndex = 0,
            visualType = 0
        ),
        DiscoveryItem(
            badge = "[ 1011 م ]",
            title = "الحسن بن الهيثم وثورة البصريات",
            subtitle = "كتاب المناظر — اكتشاف قوانين الضوء والقمرة المظلمة",
            region = "النطاق الجغرافي: البصرة والقاهرة",
            summary = "قلب الحسن بن الهيثم مفاهيم الفيزياء القديمة رأساً على عقب بإثباته التجريبي أن الضوء ينطلق من الأجسام إلى العين في خطوط مستقيمة، وصمم القمرة المظلمة التي شكلت الأساس العلمي للكاميرا وشاشات العرض الحديثة.",
            facts = listOf(
                "1. إثبات أن الرؤية تحدث بانعكاس الضوء من الأجسام نحو شبكية العين.",
                "2. اختراع تجربة القمرة المظلمة (Camera Obscura) وتفسير انعكاس الصورة.",
                "3. تأسيس المنهج العلمي التجريبي القائم على الفرضية والاختبار والبرهان."
            ),
            quizQuestion = "كيف فسر الحسن بن الهيثم عملية الإبصار علمياً لأول مرة؟",
            quizOptions = listOf("1. خروج أشعة من العين", "2. انعكاس الضوء من الأجسام إلى العين", "3. تفاعل الهواء مع الحرارة"),
            correctIndex = 1,
            visualType = 1
        ),
        DiscoveryItem(
            badge = "[ 1879 م ]",
            title = "عصر الكهرباء والكهرومغناطيسية",
            subtitle = "من تجارب الحث المغناطيسي إلى إضاءة العالم",
            region = "النطاق الجغرافي: أوروبا وأمريكا الشمالية",
            summary = "بعد اكتشاف فاراداي للعلاقة بين المغناطيسية والكهرباء وصياغة ماكسويل لمعادلات الموجات الكهرومغناطيسية، انتقل العالم إلى عصر توليد الطاقة الكهربائية وشبكات الإضاءة والاتصالات السلكية واللاسلكية.",
            facts = listOf(
                "1. توليد التيار الكهربائي عبر حركة الموصلات داخل مجال مغناطيسي متغير.",
                "2. توحيد الكهرباء والمغناطيسية والضوء في نظرية كهرومغناطيسية واحدة.",
                "3. تمهيد الطريق لهندسة شبكات الاتصالات والراديو والمحركات الكهربائية."
            ),
            quizQuestion = "ما هي الظاهرة الفيزيائية الأساسية التي تعمل بها المولدات الكهربائية؟",
            quizOptions = listOf("1. الانشطار النووي", "2. الحث الكهرومغناطيسي", "3. التفاعل الكيميائي الحراري"),
            correctIndex = 1,
            visualType = 2
        ),
        DiscoveryItem(
            badge = "[ 1929 م ]",
            title = "إدوين هابل وتمدد الكون",
            subtitle = "اكتشاف المجرات الخارجية والانزياح الأحمر",
            region = "النطاق الجغرافي: مرصد جبل ويلسون — الفضاء السحيق",
            summary = "برصد الطيف الضوئي للمجرات البعيدة، اكتشف إدوين هابل أن مجرة درب التبانة ليست سوى واحدة من مليارات المجرات، وأن جميع المجرات تبتعد عن بعضها بسرعة تتناسب مع مسافتها، مما أثبت أن الكون في حالة تمدد مستمر.",
            facts = listOf(
                "1. رصد ظاهرة الانزياح نحو الأحمر (Redshift) في أطياف المجرات البعيدة.",
                "2. صياغة قانون هابل الذي يربط بين سرعة تباعد المجرة وبعدها عنا.",
                "3. إثبات أن للكون بداية زمنية محددة وتطور تاريخي مستمر."
            ),
            quizQuestion = "ماذا يعني انزياح ضوء المجرات البعيدة نحو اللون الأحمر (Redshift)؟",
            quizOptions = listOf("1. أن المجرات تقترب منا", "2. أن المجرات تبتعد والكون يتمدد", "3. أن النجوم تبرد وتتوقف"),
            correctIndex = 1,
            visualType = 3
        ),
        DiscoveryItem(
            badge = "[ 2026 م ]",
            title = "تلسكوب جيمس ويب والذكاء الاصطناعي",
            subtitle = "رصد فجر الكون والحوسبة العصبية المتقدمة",
            region = "النطاق الجغرافي: مدار لاغرانج L2 ومراكز البيانات العالمية",
            summary = "التقت هندسة الفضاء الدقيقة بمرآة جيمس ويب المطلية بالذهب (18 قطعة سداسية) لرصد الأشعة تحت الحمراء من أقدم المجرات، بالتوازي مع ثورة الذكاء الاصطناعي التوليدي والشبكات العصبية التي تعيد صياغة العلوم والاتصالات.",
            facts = listOf(
                "1. رصد المجرات الأولى التي تشكلت بعد 300 مليون سنة فقط من نشأة الكون.",
                "2. تحليل الغلاف الجوي للكواكب الخارجية (Exoplanets) للبحث عن بخار الماء.",
                "3. دمج نماذج الذكاء الاصطناعي في تحليل البيانات الفلكية وشبكات الجيل المتقدم."
            ),
            quizQuestion = "لماذا صُممت مرآة تلسكوب جيمس ويب بطلاء ذهبي وتركيبة سداسية؟",
            quizOptions = listOf("1. لعكس الأشعة تحت الحمراء بأعلى كفاءة", "2. لحمايته من النيازك فقط", "3. لتقليل وزنه عند الهبوط على المريخ"),
            correctIndex = 0,
            visualType = 4
        )
    )

    private val cosmicStations = listOf(
        DiscoveryItem(
            badge = "[ 10⁻¹⁵ م ]",
            title = "نواة الذرة وعالم الكواركات",
            subtitle = "مقياس الفيمتومتر — أعمق بنية للمادة",
            region = "المقياس الفيزيائي: 0.000000000000001 متر",
            summary = "عند الغوص داخل نواة الذرة، نجد البروتونات والنيوترونات، وبداخل كل منها 3 جسيمات أولية تُسمى (الكواركات) ترتبط ببعضها عبر القوة النووية الشديدة التي تنقلها جسيمات الغلونات في حركة كمومية فائقة السرعة.",
            facts = listOf(
                "1. يتكون البروتون الواحد من كواركين علويين (Up) وكوارك سفلي واحد (Down).",
                "2. القوة النووية الشديدة هنا أقوى من الجاذبية بمليارات المرات.",
                "3. تشكل النواة 99.9% من كتلة الذرة رغم صغر حجمها المتناهي."
            ),
            quizQuestion = "ما هي الجسيمات الأولية الثلاثة التي يتكون منها البروتون داخل النواة؟",
            quizOptions = listOf("1. الفوتونات", "2. الكواركات", "3. الإلكترونات"),
            correctIndex = 1,
            visualType = 5
        ),
        DiscoveryItem(
            badge = "[ 10⁻¹⁰ م ]",
            title = "الذرة والمدارات الإلكترونية",
            subtitle = "مقياس الأنغستروم — لغة الضوء والقفزات الكمومية",
            region = "المقياس الفيزيائي: 0.0000000001 متر",
            summary = "تدور الإلكترونات حول النواة في مستويات طاقة محددة بدقة. عندما يكتسب الإلكترون طاقة فإنه يقفز إلى مدار أعلى، وعند عودته يطلق فرق الطاقة على شكل (فوتون ضوئي) بلون محدد يميز كل عنصر كيميائي في الكون.",
            facts = listOf(
                "1. معظم حجم الذرة فراغ هائل؛ لو كانت النواة بحجم كرة تنس لكانت الإلكترونات على بعد كيلومترات.",
                "2. طيف الانبعاث الذري يعمل كبصمة ضوئية نعرف بها مكونات النجوم البعيدة.",
                "3. تتحرك الإلكترونات كجسيمات وموجات في آن واحد وفق ميكانيكا الكم."
            ),
            quizQuestion = "ماذا يحدث عندما ينتقل الإلكترون من مدار طاقة مرتفع إلى مدار طاقة أقل؟",
            quizOptions = listOf("1. يطلق فوتوناً ضوئياً", "2. يتحول إلى بروتون", "3. تتلاشى الذرة"),
            correctIndex = 0,
            visualType = 6
        ),
        DiscoveryItem(
            badge = "[ 10⁻⁹ م ]",
            title = "شريط الحياة (الحمض النووي DNA)",
            subtitle = "مقياس النانومتر — الهندسة الجزيئية للشفرة الوراثية",
            region = "المقياس الفيزيائي: 2 نانومتر (عرض الحلزون المزدوج)",
            summary = "على مقياس النانومتر يظهر شريط الـ DNA بتصميمه الحلزوني المزدوج الفريد؛ حيث تصطف أربعة أحرف كيميائية (A, T, C, G) لتكتب الشفرة الكاملة لبناء الكائن الحي بدقة تخزينية تفوق أحدث الرقائق الإلكترونية.",
            facts = listOf(
                "1. لو تم فرد شريط الـ DNA الموجود في خلية بشرية واحدة لوصل طوله إلى مترين.",
                "2. يرتبط الأدينين (A) دائماً بالثايمين (T)، والسيتوزين (C) بالجوانين (G).",
                "3. غرام واحد من الـ DNA قادر نظرياً على تخزين أكثر من 200 بيتابايت من البيانات."
            ),
            quizQuestion = "كم يبلغ عرض شريط الـ DNA الحلزوني المزدوج تقريباً؟",
            quizOptions = listOf("1. 2 نانومتر", "2. 2 مليمتر", "3. 2 ميكرومتر"),
            correctIndex = 0,
            visualType = 7
        ),
        DiscoveryItem(
            badge = "[ 10¹² م ]",
            title = "النظام الشمسي ومدارات الكواكب",
            subtitle = "مقياس التيرامتر — الجاذبية وهندسة الفضاء القريب",
            region = "المقياس الفيزيائي: 1,000,000,000,000 متر",
            summary = "تسيطر الشمس بكتلتها التي تمثل 99.86% من كتلة النظام الشمسي على حركة الكواكب الثمانية في مدارات إهليلجية دقيقة، حيث يستغرق ضوء الشمس نحو 8 دقائق و20 ثانية ليصل إلى كوكب الأرض.",
            facts = listOf(
                "1. يدور كوكب الأرض حول الشمس بسرعة تقارب 107,000 كيلومتر في الساعة.",
                "2. يعمل كوكب المشتري العملاق كدرع جاذبي يحمي الأرض من الكثير من المذنبات.",
                "3. تتكون حلقات زحل البديعة من مليارات القطع الجليدية والصخرية المتفاوتة الحجم."
            ),
            quizQuestion = "كم يستغرق ضوء الشمس تقريباً للوصول إلى كوكب الأرض؟",
            quizOptions = listOf("1. ثانية واحدة", "2. 8 دقائق و20 ثانية", "3. ساعتان كاملتان"),
            correctIndex = 1,
            visualType = 8
        ),
        DiscoveryItem(
            badge = "[ 10²¹ م ]",
            title = "مجرة درب التبانة والثقب الأسود المركزي",
            subtitle = "مقياس 100 ألف سنة ضوئية — المدينة النجمية الكبرى",
            region = "المقياس الفيزيائي: 1,000,000,000,000,000,000,000 متر",
            summary = "مجرتنا الحلزونية تضم أكثر من 200 مليار نجم تدور جميعها حول مركز المجرة الذي يحتضن ثقباً أسود فائق الكتلة يُسمى (الرامي Sagittarius A*)، وتستغرق مجموعتنا الشمسية 230 مليون سنة لإتمام دورة واحدة حول المجرة.",
            facts = listOf(
                "1. تقع مجموعتنا الشمسية في أحد الأذرع الحلزونية الفرعية ويسمى ذراع الجبار (Orion Arm).",
                "2. تبلغ كتلة الثقب الأسود المركزي في مجرتنا نحو 4 ملايين ضعف كتلة الشمس.",
                "3. الضوء الذي يقطع 300 ألف كم في الثانية يحتاج 100 ألف سنة ليعبر مجرتنا من طرف لآخر."
            ),
            quizQuestion = "كم يبلغ قطر مجرة درب التبانة بالسنوات الضوئية تقريباً؟",
            quizOptions = listOf("1. 100 سنة ضوئية", "2. 100,000 سنة ضوئية", "3. مليون سنة ضوئية"),
            correctIndex = 1,
            visualType = 9
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 35)
        } catch (_: Exception) {}
        try {
            tts = TextToSpeech(this, this)
        } catch (_: Exception) {}

        buildDashboardUI()
        selectCategory(0)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ar"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun playClickSound(tone: Int = ToneGenerator.TONE_PROP_BEEP) {
        try {
            toneGen?.startTone(tone, 35)
        } catch (_: Exception) {}
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun makeRoundedBox(bgHex: String, borderHex: String, borderDp: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(bgHex))
            setStroke(dp(borderDp), Color.parseColor(borderHex))
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun styleInteractiveButton(
        view: TextView,
        normalBg: String = "#131D38",
        normalBorder: String = "#2A3F6E",
        focusBg: String = "#1E3264",
        focusBorder: String = "#FFD54F"
    ) {
        view.isFocusable = true
        view.isFocusableInTouchMode = false
        view.isClickable = true
        view.background = makeRoundedBox(normalBg, normalBorder, 2, 12)
        view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.background = makeRoundedBox(focusBg, focusBorder, 3, 12)
                v.scaleX = 1.05f
                v.scaleY = 1.05f
                playClickSound(ToneGenerator.TONE_PROP_BEEP)
            } else {
                v.background = makeRoundedBox(normalBg, normalBorder, 2, 12)
                v.scaleX = 1.0f
                v.scaleY = 1.0f
            }
        }
    }

    private fun buildDashboardUI() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#060A17"))
        }

        // 1. Live Interactive Cosmic Canvas (Full Background + Left Viewport)
        universeCanvas = UniverseCanvasView(this)
        root.addView(
            universeCanvas,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // 2. Main Foreground Layout
        val mainColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(18), dp(24), dp(18))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // --- TOP HEADER BAR ---
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = makeRoundedBox("#CC0C152B", "#263B6A", 1, 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val brandTitle = TextView(this).apply {
            text = "🌌 ALRADHY DISCOVERY UNIVERSE"
            setTextColor(Color.parseColor("#00E5FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            typeface = Typeface.DEFAULT_BOLD
        }
        headerRow.addView(brandTitle, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        tabTimeBtn = TextView(this).apply {
            text = "🌍 آلة الزمن والتاريخ"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(16), dp(8), dp(16), dp(8))
            styleInteractiveButton(this)
            setOnClickListener {
                playClickSound(ToneGenerator.TONE_PROP_ACK)
                selectCategory(0)
            }
        }
        val tabParams1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = dp(10)
        }
        headerRow.addView(tabTimeBtn, tabParams1)

        tabCosmosBtn = TextView(this).apply {
            text = "🔭 مقياس الكون السحري"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(16), dp(8), dp(16), dp(8))
            styleInteractiveButton(this)
            setOnClickListener {
                playClickSound(ToneGenerator.TONE_PROP_ACK)
                selectCategory(1)
            }
        }
        headerRow.addView(tabCosmosBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        mainColumn.addView(headerRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        // --- STATION SELECTOR STRIP (Timeline / Cosmic Scale Bar) ---
        val scrollStrip = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, dp(10), 0, dp(10))
        }
        stationRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        scrollStrip.addView(stationRow)
        mainColumn.addView(scrollStrip, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        // --- CENTER CONTENT AREA (Right: Knowledge Card | Left: Interactive Simulation) ---
        val centerArea = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 10f
        }

        // Right Panel: Scientific & Historical Details (5.5 weight)
        val infoCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = makeRoundedBox("#D90B1328", "#294073", 2, 16)
            setPadding(dp(20), dp(16), dp(20), dp(16))
        }

        badgeText = TextView(this).apply {
            setTextColor(Color.parseColor("#FFD54F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
        }
        infoCard.addView(badgeText)

        titleText = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(2), 0, dp(2))
        }
        infoCard.addView(titleText)

        subtitleText = TextView(this).apply {
            setTextColor(Color.parseColor("#00E5FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, 0, dp(8))
        }
        infoCard.addView(subtitleText)

        val infoScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
        }
        val scrollInner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        summaryText = TextView(this).apply {
            setTextColor(Color.parseColor("#E2E8F0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setLineSpacing(dp(4).toFloat(), 1.0f)
            setPadding(0, 0, 0, dp(10))
        }
        scrollInner.addView(summaryText)

        val factsHeader = TextView(this).apply {
            text = "✨ أبرز الحقائق والاكتشافات العلمية:"
            setTextColor(Color.parseColor("#FFD54F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(4), 0, dp(6))
        }
        scrollInner.addView(factsHeader)

        factsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollInner.addView(factsContainer)

        infoScroll.addView(scrollInner)
        infoCard.addView(infoScroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        centerArea.addView(
            infoCard,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 5.6f)
        )

        // Left Viewport Spacer (4.4 weight) so the live Canvas simulation shines unobstructed
        val canvasOverlayBox = FrameLayout(this).apply {
            setPadding(dp(14), dp(8), dp(8), dp(8))
        }
        val simHintBadge = TextView(this).apply {
            text = "⚡ المحاكاة الفيزيائية الحية (60 FPS)"
            setTextColor(Color.parseColor("#00E5FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            background = makeRoundedBox("#AA0A1226", "#00E5FF", 1, 10)
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }
        canvasOverlayBox.addView(
            simHintBadge,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.LEFT
            )
        )
        centerArea.addView(
            canvasOverlayBox,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 4.4f)
        )

        mainColumn.addView(centerArea, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // --- BOTTOM INTERACTIVE CONTROL BAR ---
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = makeRoundedBox("#CC0C152B", "#263B6A", 1, 14)
            setPadding(dp(14), dp(8), dp(14), dp(8))
        }

        val ttsBtn = TextView(this).apply {
            text = "🔊 الراوي الصوتي"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(8), dp(14), dp(8))
            styleInteractiveButton(this, "#10344A", "#00E5FF")
            setOnClickListener {
                speakCurrentStation()
            }
        }
        bottomBar.addView(ttsBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) })

        val quizBtn = TextView(this).apply {
            text = "🧠 تحدي المعرفة"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(8), dp(14), dp(8))
            styleInteractiveButton(this, "#3A280D", "#FFD54F")
            setOnClickListener {
                showQuizDialog()
            }
        }
        bottomBar.addView(quizBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) })

        val speedBtn = TextView(this).apply {
            text = "⏩ سرعة المحاكاة"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            styleInteractiveButton(this)
            setOnClickListener {
                simSpeed = when (simSpeed) {
                    1.0f -> 2.0f
                    2.0f -> 4.0f
                    4.0f -> 0.5f
                    else -> 1.0f
                }
                universeCanvas.simSpeed = simSpeed
                speedLabel.text = "السرعة: ${simSpeed}x | الطاقة: ${energyBoost}x"
                playClickSound(ToneGenerator.TONE_PROP_ACK)
            }
        }
        bottomBar.addView(speedBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) })

        val energyBtn = TextView(this).apply {
            text = "⚡ نبضة طاقة"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            styleInteractiveButton(this)
            setOnClickListener {
                energyBoost = if (energyBoost >= 2.5f) 1.0f else energyBoost + 0.5f
                universeCanvas.energyLevel = energyBoost
                speedLabel.text = "السرعة: ${simSpeed}x | الطاقة: ${energyBoost}x"
                playClickSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
            }
        }
        bottomBar.addView(energyBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(12) })

        speedLabel = TextView(this).apply {
            text = "السرعة: 1.0x | الطاقة: 1.0x"
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        bottomBar.addView(speedLabel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val nextBtn = TextView(this).apply {
            text = "المحطة التالية ⬅️"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(8), dp(14), dp(8))
            styleInteractiveButton(this, "#1E293B", "#38BDF8")
            setOnClickListener {
                val list = getCurrentList()
                selectStation((currentIndex + 1) % list.size)
            }
        }
        bottomBar.addView(nextBtn)

        val bottomParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(10)
        }
        mainColumn.addView(bottomBar, bottomParams)

        root.addView(mainColumn, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)
    }

    private fun getCurrentList(): List<DiscoveryItem> {
        return if (currentCategory == 0) historyStations else cosmicStations
    }

    private fun selectCategory(cat: Int) {
        currentCategory = cat
        currentIndex = 0

        if (cat == 0) {
            tabTimeBtn.setTextColor(Color.parseColor("#FFD54F"))
            tabCosmosBtn.setTextColor(Color.WHITE)
        } else {
            tabTimeBtn.setTextColor(Color.WHITE)
            tabCosmosBtn.setTextColor(Color.parseColor("#00E5FF"))
        }

        stationRow.removeAllViews()
        val list = getCurrentList()
        list.forEachIndexed { idx, item ->
            val btn = TextView(this).apply {
                text = "${item.badge} ${item.title}"
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                styleInteractiveButton(this)
                setOnClickListener {
                    playClickSound(ToneGenerator.TONE_PROP_ACK)
                    selectStation(idx)
                }
            }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(8)
            }
            stationRow.addView(btn, lp)
        }

        selectStation(0)
        stationRow.getChildAt(0)?.requestFocus()
    }

    private fun selectStation(index: Int) {
        val list = getCurrentList()
        if (index !in list.indices) return
        currentIndex = index
        val item = list[index]

        badgeText.text = "${item.badge}  •  ${item.region}"
        titleText.text = item.title
        subtitleText.text = item.subtitle
        summaryText.text = item.summary

        factsContainer.removeAllViews()
        item.facts.forEach { fact ->
            val tv = TextView(this).apply {
                text = fact
                setTextColor(Color.parseColor("#CCFBF1"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                background = makeRoundedBox("#111E3B", "#1E3A8A", 1, 8)
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            factsContainer.addView(tv, lp)
        }

        for (i in 0 until stationRow.childCount) {
            val child = stationRow.getChildAt(i) as? TextView ?: continue
            if (i == index) {
                child.setTextColor(Color.parseColor("#FFD54F"))
                child.typeface = Typeface.DEFAULT_BOLD
            } else {
                child.setTextColor(Color.WHITE)
                child.typeface = Typeface.DEFAULT
            }
        }

        universeCanvas.visualType = item.visualType
    }

    private fun speakCurrentStation() {
        val item = getCurrentList()[currentIndex]
        val speechText = "${item.title}. ${item.summary}"
        if (ttsReady) {
            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "station_tts")
            Toast.makeText(this, "🔊 جاري القراءة الصوتية...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "💡 المحرك الصوتي العربي يجهز الآن أو غير مفعل في النظام", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showQuizDialog() {
        val item = getCurrentList()[currentIndex]
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🧠 تحدي المعرفة: ${item.title}")
        val optionsArray = item.quizOptions.toTypedArray()
        builder.setItems(optionsArray) { dialog, which ->
            if (which == item.correctIndex) {
                playClickSound(ToneGenerator.TONE_PROP_ACK)
                Toast.makeText(this, "✅ إجابة صحيحة! أحسنت يا بطل المعرفة", Toast.LENGTH_LONG).show()
            } else {
                playClickSound(ToneGenerator.TONE_PROP_NACK)
                Toast.makeText(this, "❌ إجابة غير دقيقة، الإجابة الصحيحة هي: ${item.quizOptions[item.correctIndex]}", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("إغلاق") { d, _ -> d.dismiss() }
        builder.show()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        toneGen?.release()
        super.onDestroy()
    }
}

class UniverseCanvasView(context: Context) : View(context) {

    var visualType: Int = 0
    var simSpeed: Float = 1.0f
    var energyLevel: Float = 1.0f
    private var phase: Float = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starX = FloatArray(90) { (Math.random() * 1920).toFloat() }
    private val starY = FloatArray(90) { (Math.random() * 1080).toFloat() }
    private val starRadius = FloatArray(90) { (Math.random() * 2.5 + 0.8).toFloat() }

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

        // Simulation Center on the Left Viewport (since RTL layout puts info card on the right)
        val cx = w * 0.23f
        val cy = h * 0.56f
        val baseR = minOf(w, h) * 0.24f

        when (visualType) {
            0 -> drawAstrolabe(canvas, cx, cy, baseR)
            1 -> drawOpticsSimulation(canvas, cx, cy, baseR)
            2 -> drawElectromagneticCoil(canvas, cx, cy, baseR)
            3 -> drawExpandingUniverse(canvas, cx, cy, baseR)
            4 -> drawJamesWebbAndAI(canvas, cx, cy, baseR)
            5 -> drawQuarksNucleus(canvas, cx, cy, baseR)
            6 -> drawQuantumAtom(canvas, cx, cy, baseR)
            7 -> drawDnaHelix(canvas, cx, cy, baseR)
            8 -> drawSolarSystem(canvas, cx, cy, baseR)
            9 -> drawMilkyWayGalaxy(canvas, cx, cy, baseR)
        }

        phase += 0.025f * simSpeed
        postInvalidateOnAnimation()
    }

    private fun drawAstrolabe(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, r, paint)
        paint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, r * 0.78f, paint)
        canvas.drawCircle(cx, cy, r * 0.52f, paint)

        // Rotating celestial rete
        for (i in 0 until 12) {
            val angle = phase + i * (Math.PI / 6)
            val x2 = cx + (r * cos(angle)).toFloat()
            val y2 = cy + (r * sin(angle)).toFloat()
            paint.color = Color.parseColor("#4DD0E1")
            canvas.drawLine(cx, cy, x2, y2, paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 12f * energyLevel, paint)
    }

    private fun drawOpticsSimulation(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        // Pinhole barrier in middle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(cx, cy - r, cx, cy - 16f, paint)
        canvas.drawLine(cx, cy + 16f, cx, cy + r, paint)

        // Light Rays crossing through pinhole
        val pulse = (sin(phase.toDouble()) * 15f).toFloat()
        paint.strokeWidth = 3f
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawLine(cx - r * 0.9f, cy - r * 0.6f + pulse, cx + r * 0.9f, cy + r * 0.6f - pulse, paint)
        paint.color = Color.parseColor("#00E5FF")
        canvas.drawLine(cx - r * 0.9f, cy + r * 0.6f - pulse, cx + r * 0.9f, cy - r * 0.6f + pulse, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 7f * energyLevel, paint)
    }

    private fun drawElectromagneticCoil(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        for (i in -3..3) {
            val offset = i * (r * 0.22f)
            paint.color = Color.parseColor("#FFAB40")
            canvas.drawOval(cx - r * 0.35f + offset, cy - r * 0.6f, cx + r * 0.35f + offset, cy + r * 0.6f, paint)
        }
        // Moving electrons
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#00E5FF")
        for (i in 0 until 8) {
            val angle = phase * 3f + i * 0.8
            val ex = cx + (r * 0.8f * cos(angle)).toFloat()
            val ey = cy + (r * 0.45f * sin(angle)).toFloat()
            canvas.drawCircle(ex, ey, 7f * energyLevel, paint)
        }
    }

    private fun drawExpandingUniverse(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val expansion = (0.4f + 0.6f * ((sin(phase.toDouble()) + 1.0) / 2.0)).toFloat()
        for (i in 0 until 8) {
            val angle = i * (Math.PI / 4) + phase * 0.3
            val dist = r * expansion
            val gx = cx + (dist * cos(angle)).toFloat()
            val gy = cy + (dist * sin(angle)).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = if (expansion > 0.7f) Color.parseColor("#FF5252") else Color.parseColor("#40C4FF")
            canvas.drawCircle(gx, gy, 11f * energyLevel, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#475569")
        canvas.drawCircle(cx, cy, r * expansion, paint)
    }

    private fun drawJamesWebbAndAI(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        val hexR = r * 0.25f
        val offsets = listOf(
            Pair(0f, 0f),
            Pair(hexR * 1.8f, 0f), Pair(-hexR * 1.8f, 0f),
            Pair(hexR * 0.9f, hexR * 1.6f), Pair(-hexR * 0.9f, hexR * 1.6f),
            Pair(hexR * 0.9f, -hexR * 1.6f), Pair(-hexR * 0.9f, -hexR * 1.6f)
        )
        for ((dx, dy) in offsets) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(cx + dx, cy + dy, hexR * 0.78f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#00E5FF")
            canvas.drawCircle(cx + dx, cy + dy, hexR * 0.85f * energyLevel, paint)
        }
    }

    private fun drawQuarksNucleus(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.parseColor("#38BDF8")
        canvas.drawCircle(cx, cy, r * 0.75f, paint)

        val coords = Array(3) { i ->
            val a = phase * 3f + i * (2 * Math.PI / 3)
            Pair(cx + (r * 0.42f * cos(a)).toFloat(), cy + (r * 0.42f * sin(a)).toFloat())
        }
        paint.strokeWidth = 6f
        paint.color = Color.parseColor("#F43F5E")
        canvas.drawLine(coords[0].first, coords[0].second, coords[1].first, coords[1].second, paint)
        canvas.drawLine(coords[1].first, coords[1].second, coords[2].first, coords[2].second, paint)
        canvas.drawLine(coords[2].first, coords[2].second, coords[0].first, coords[0].second, paint)

        paint.style = Paint.Style.FILL
        val colors = listOf("#00E5FF", "#FFD54F", "#4ADE80")
        for (i in 0..2) {
            paint.color = Color.parseColor(colors[i])
            canvas.drawCircle(coords[i].first, coords[i].second, 18f * energyLevel, paint)
        }
    }

    private fun drawQuantumAtom(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F43F5E")
        canvas.drawCircle(cx, cy, 16f * energyLevel, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        for (i in 0 until 3) {
            canvas.save()
            canvas.rotate(i * 60f, cx, cy)
            paint.color = Color.parseColor("#38BDF8")
            canvas.drawOval(cx - r, cy - r * 0.38f, cx + r, cy + r * 0.38f, paint)

            val angle = phase * 3.5f + i * 2f
            val ex = cx + (r * cos(angle.toDouble())).toFloat()
            val ey = cy + (r * 0.38f * sin(angle.toDouble())).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(ex, ey, 8f * energyLevel, paint)
            paint.style = Paint.Style.STROKE
            canvas.restore()
        }
    }

    private fun drawDnaHelix(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        for (i in -8..8) {
            val y = cy + i * (r * 0.11f)
            val wave = sin((phase * 2.5f + i * 0.45f).toDouble()).toFloat()
            val x1 = cx + wave * (r * 0.55f)
            val x2 = cx - wave * (r * 0.55f)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#475569")
            canvas.drawLine(x1, y, x2, y, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#00E5FF")
            canvas.drawCircle(x1, y, 7f * energyLevel, paint)
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(x2, y, 7f * energyLevel, paint)
        }
    }

    private fun drawSolarSystem(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 22f * energyLevel, paint)

        val radii = floatArrayOf(r * 0.32f, r * 0.52f, r * 0.74f, r * 0.96f)
        val speeds = floatArrayOf(2.6f, 1.8f, 1.2f, 0.7f)
        val colors = arrayOf("#94A3B8", "#38BDF8", "#F97316", "#FACC15")
        val sizes = floatArrayOf(6f, 9f, 8f, 13f)

        for (i in radii.indices) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            paint.color = Color.parseColor("#334155")
            canvas.drawCircle(cx, cy, radii[i], paint)

            val a = phase * speeds[i] + i
            val px = cx + (radii[i] * cos(a.toDouble())).toFloat()
            val py = cy + (radii[i] * sin(a.toDouble())).toFloat()
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor(colors[i])
            canvas.drawCircle(px, py, sizes[i] * energyLevel, paint)
        }
    }

    private fun drawMilkyWayGalaxy(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        for (arm in 0 until 3) {
            val armOffset = arm * (2 * Math.PI / 3)
            for (j in 1..28) {
                val frac = j / 28f
                val dist = frac * r
                val angle = phase * 1.2f + armOffset + frac * 4.2f
                val gx = cx + (dist * cos(angle.toDouble())).toFloat()
                val gy = cy + (dist * sin(angle.toDouble())).toFloat()
                paint.color = if (j % 2 == 0) Color.parseColor("#38BDF8") else Color.parseColor("#E0F2FE")
                canvas.drawCircle(gx, gy, (4.5f - frac * 2.5f) * energyLevel, paint)
            }
        }
        paint.color = Color.parseColor("#FFD54F")
        canvas.drawCircle(cx, cy, 14f * energyLevel, paint)
    }
}

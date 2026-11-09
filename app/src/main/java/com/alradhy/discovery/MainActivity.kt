package com.alradhy.discovery

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {
    private var currentIndex = 0
    private lateinit var titleView: TextView
    private lateinit var descView: TextView
    private lateinit var counterView: TextView
    private lateinit var categoryView: TextView

    private data class Station(val id: Int, val category: String, val title: String, val arabicTitle: String, val fullContent: String)

    private val stations = listOf(
        Station(1, "History", "Station 1: The Dawn of Civilization", "مهد الحضارات في بلاد ما بين النهرين", 
            "تعتبر بلاد ما بين النهرين (موسوبوتاميا) المهد الأولي للحضارة البشرية حوالي عام 3500 ق.م. ظهرت هنا الكتابة المسمارية الأولى على يد السومريين، وتشكلت المدن الكبرى مثل أور وURUK، وتطورت أنظمة الري الكبرى والقوانين المدنية الأولى مثل شريعة حمورابي، مما غير شكل التاريخ البشري للأبد."),
        Station(2, "History", "Station 2: The Pyramids of Giza", "أهرامات الجيزة وعجائب الهندسة الفرعونية", 
            "تمثل أهرامات الجيزة قمة الهندسة المعمارية والفلكية في العالم القديم. بُنيت كمقابرمقدسة للفراعنة (خوفو، خفرع، ومنقاورع) باستخدام ملايين الأحجار الضخمة المنحوتة بدقة متناهية، وتدل على تنظيم إداري واجتماعي هائل وتقدم علمي فريد في علم الفلك والرياضيات."),
        Station(3, "History", "Station 3: The Indus Valley", "حضارة وادي السند والتخطيط العمراني", 
            "ازدهرت حضارة وادي السند (هارابا وموهينجو دارو) في شبه القارة الهندية ابتداءً من 2500 ق.م. تميزت بتخطيط عمراني متقدم جداً يضم شبكات صرف صحي مغطاة، ومنازل منتظمة من الآجر المحروق، وأنظمة قياس دقيقة وشبكات تجارية واسعة النطاق مع بلاد الرافدين."),
        Station(4, "History", "Station 4: Ancient Greece", "اليونان القديمة: مهد الديمقراطية والفلسفة", 
            "رست اليونان القديمة (أثينا وإسبرطة وغيرها) قواعد الفكر البشري الحديث؛ حيث نشأت الديمقراطية، وفلسفة العقل مع سقراط وأفلاطون وأرسطو، والرياضيات مع أقليدس وفيثاغورس، فضلاً عن الفنون المعمارية الكلاسيكية والألعاب الأولمبية التي خلّدت إرثهم حتى اليوم."),
        Station(5, "History", "Station 5: The Roman Empire", "الإمبراطورية الرومانية: الهندسة والقانون", 
            "بنت الإمبراطورية الرومانية شبكة واسعة من الطرق والجسور وقنوات المياه التي تربط قارات العالم القديم. أسست النظام القانوني والتشريعي الذي تققوم عليه القوانين المعاصرة، ونشرت الثقافة واللغة اللاتينية عبر حوض البحر الأبيض المتوسط وأوروبا."),
        Station(6, "History", "Station 6: The Silk Road", "طريق الحرير: شريان التبادل الحضاري والتجاري", 
            "شبكة تاريخية معقدة من الطرق التجارية البرية والبحرية التي ربطت الشرق بالغرب لمواسم وأقراب طويلة، امتدت من الصين عبر آسيا الوسطى وبلاد فارس والعالم الإسلامي وصولاً إلى أوروبا. لم تكن لنقل الحرير والبهارات فحسب، بل كانت جسراً لتلاقح الأفكار والعلوم والديانات والتكنولوجيا."),
        Station(7, "History", "Station 7: The Islamic Golden Age", "العصر الذهبي الإسلامي: منارة العلم والمعرفة", 
            "فترة زاهية امتدت من القرن الثامن حتى القرن الرابع عشر الميلادي، حيث أصبحت بغداد وقرطبة والقاهرة مراكز عالمية للعلم. برز فيها علماء كبار مثل الخوارزمي (مؤسس الجبر)، ابن الهيثم (مؤسس البصريات)، ابن سينا، والزراعيين والفلكيين الذين ترجموا وطوروا علوم اليونان والهند وأسسوا المنهج العلمي التجريبي."),
        Station(8, "History", "Station 8: The Tang Dynasty", "سلالة تانغ الصينية: العصر الذهبي للفنون", 
            "تعتبر سلالة تانغ (618–907 م) أزهى عصور الحضارة الصينية؛ حيث انتعش الشعر والفنون، وتطورت التجارة عبر طريق الحرير، واختُرعت الطباعة والبارود، وكانت عاصمتها تشانغآن أكبر مدينة في العالم آنذاك وأكثرها انفتاحاً وتنوعاً ثقافياً."),
        Station(9, "History", "Station 9: The Viking Age", "عصر الفايكنج: الملاحون والمستكشفون", 
            "انطلق بحارة ومحاربو الدول الإسكندنافية (الفايكنج) بين القرنين الثامن والحادي عشر لاستكشاف السواحل والأنهار في أوروبا وآسيا وأمريكا الشمالية (قبل كولومبوس). لم يكونوا مجرد محاربين، بل مهرة في صناعة السفن، وتجاراً ومؤسسين لممالك ومدن كبرى مثل كيف ودبلن."),
        Station(10, "History", "Station 10: The Mayan Civilization", "حضارة المايا: الفلك والرياضيات في أمريكا الوسطى", 
            "حضارة أمريكية وسطى متقدمة عُرفت بنظام كتابتها الهيروغليفي المعقد، وبراعتها الفلكية الهائلة وتقويمها الأدق من التقويم الغريغوري، فضلاً عن بناء الأهرامات المدرجة المعمارية الضخمة ومدن الغابات الاستوائية المزدهرة."),
        
        // محطات الكون والفضاء بتفاصيل علمية كاملة
        Station(26, "Cosmos", "Station 26: The Big Bang", "الانفجار العظيم وولادة الكون", 
            "قبل حوالي 13.8 مليار سنة، كان الكون بأكمله مضغوطاً في نقطة مفردة (Singularity) ذات كثافة وحرارة لا متناهيتين. بدأ التوسع الهائل واللحظي الذي أفرد المكان والزمان، وكون المادة الأولى (الهيدروجين والهيليوم) التي تشكلت منها لاحقاً كل نجوم ومجرات الكون."),
        Station(27, "Cosmos", "Station 27: Stellar Nurseries", "حضانات النجوم والسدم الكونية", 
            "السدم (Nebulae) هي سحابات ضخمة وهائلة من الغاز (الهيدروجين بشكل أساسي) والغبار الكوني. بفعل الجاذبية الهائلة، تنضغط أجزاء من هذه السحابات وترتفع حرارتها حتى تبدأ تفاعلات الاندماج النووي، وتولد نجوم جديدة ومضيئة."),
        Station(28, "Cosmos", "Station 28: Main Sequence Stars", "النجوم في النسق الأساسي وحياة الشمس", 
            "المرحلة الأطول والأكثر استقراراً في حياة النجم (مثل شمسنا). يحرق النجم وقوده من الهيدروجين في مركزه ليتحول إلى هيليوم عبر الاندماج النووي، مراجعاً طاقة هائلة على شكل ضوء وحرارة تحافظ على توازن النجم ضد قوى الجاذبية."),
        Station(29, "Cosmos", "Station 29: Red Giants", "العملاقة الحمراء ومراحل شيخوخة النجوم", 
            "عندما ينفد الهيدروجين في قلب النجم المتوسط، يبدأ قلبه بالانكماش وتتوسع طبقاته الخارجية وتبرد ليتحول إلى عملاق أحمر (Red Giant). هذه المرحلة تسبق نهاية حياة النجوم وتمددها لتغطي مدارات الكواكب القريبة."),
        Station(30, "Cosmos", "Station 30: Supernovae", "المستعرات العظمى ومصانع العناصر الثقيلة", 
            "نهاية كارثية وعنيفة للنجوم الضخمة جداً؛ ينفجر النجم بقوة هائلة ناثراً في الفضاء عناصر كالكربون والحديد والذهب والفضة التي تكونت في أحشائه، ومخلفاً خلفه نجماً نترونياً أو ثقباً أسود."),
        Station(31, "Cosmos", "Station 31: White Dwarfs", "الأقزام البيضاء: بقايا النجوم الميتة", 
            "عندما يموت نجم بحجم الشمس، يلفظ طبقاته الخارجية ويبقى قلبه الكثيف المكون من الكربون والأكسجين والمعروف بالقزم الأبيض (White Dwarf). تتبدد حرارته ببطء شديد على مدى مليارات السنين حتى يبرد تماماً."),
        Station(32, "Cosmos", "Station 32: Neutron Stars", "النجوم النترونية وكثافتها الخيالية", 
            "بقايا انهيار نجوم عملاقة؛ تكون كثافتها لدرجة أن ملعقة صغيرة منها توزن مليارات الأطنان على الأرض. تدور بسرعة فائقة وتصدر حزم إشعاعية قوية تُعرف بالنباضات (Pulsars)."),
        Station(33, "Cosmos", "Station 33: Stellar Black Holes", "الثقوب السوداء النجمية وحبس الضوء", 
            "مناطق في الزمكان ذات جاذبية قوية جداً لدرجة تجعل الإفلات منها مستحيلاً حتى على الضوء نفسه. تتكون نتيجة انهيار الجاذبية للنجوم الضخمة جداً بعد انفجارات المستعرات العظمى."),
        Station(34, "Cosmos", "Station 34: Supermassive Black Holes", "الثقوب السوداء الهائلة في مراكز المجرات", 
            "وحوش كونية عملاقة تتواجد في مراكز أغلب المجرات (بما فيها مجرتنا درب التبانة)، وتبلغ كتلتها ملايين أو مليارات أضعاف كتلة الشمس، وتلعب دوراً أساسياً في تطور المجرات ونشأتها."),
        Station(35, "Cosmos", "Station 35: The Solar System", "النظام الشمسي وبيئتنا الكونية", 
            "نظام كوكبي يتكون من الشمس وجميع الأجسام التي تدور حولها بفعل الجاذبية، بما في ذلك الكواكب الأربعة الصخرية الداخلية والعملاقة الغازية والجليدية الخارجية، إضافة إلى الكويكبات والمذنبات.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#07090E"))
            setPadding(60, 40, 60, 40)
            isFocusable = true
            isFocusableInTouchMode = true
        }

        categoryView = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#38BDF8"))
            setPadding(0, 0, 0, 10)
        }
        rootLayout.addView(categoryView)

        counterView = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 0, 0, 15)
        }
        rootLayout.addView(counterView)

        titleView = TextView(this).apply {
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 20)
        }
        rootLayout.addView(titleView)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        descView = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.parseColor("#CBD5E1"))
            setPadding(0, 0, 0, 20)
            lineSpacingMultiplier = 1.3f
        }
        scrollView.addView(descView)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)
        updateUI()
    }

    private fun updateUI() {
        val s = stations[currentIndex]
        categoryView.text = "SECTION: ${s.category.uppercase()}"
        counterView.text = "المحطة ${currentIndex + 1} من ${stations.size}  |  (استعمل أزرار الريموت يمين/يسار أو الماوس للتنقل)"
        titleView.text = s.title + " - " + s.arabicTitle
        descView.text = s.fullContent
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (currentIndex < stations.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }
                updateUI()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                if (currentIndex > 0) {
                    currentIndex--
                } else {
                    currentIndex = stations.size - 1
                }
                updateUI()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

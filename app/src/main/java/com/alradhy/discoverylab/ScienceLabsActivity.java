package com.alradhy.discoverylab;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

/**
 * Twelve independent numerical science models in addition to the original
 * Galileo / Roemer / Fizeau experiments. All models are offline and
 * distinguish physical calculations from idealized educational drawings.
 */
public final class ScienceLabsActivity extends Activity {
    private static final int BG=Color.rgb(7,15,29), CARD=Color.rgb(17,34,57);
    private static final int WHITE=Color.rgb(235,245,255), GOLD=Color.rgb(246,197,99);
    private static final int MUTED=Color.rgb(158,186,212), CYAN=Color.rgb(81,219,237);
    private static final int ORANGE=Color.rgb(255,143,92), GREEN=Color.rgb(113,221,163);
    private static final double C=299792458, G=9.80665, AU=149597870700.0;
    private static final String[] NAMES={
        "نيوتن • تحليل الضوء بالمنشور", "فوكو • دوران الأرض بالبندول",
        "كولوم • القوة الكهربائية", "نيوتن • المدفع المداري",
        "يونغ • تداخل الضوء", "أرخميدس • الطفو",
        "فاراداي • الحث الكهرومغناطيسي", "أوم • الدائرة الكهربائية",
        "كبلر • المدارات الإهليلجية", "دوبلر • الموجات الصوتية",
        "غاليليو • زمن البندول", "أينشتاين • التأثير الكهروضوئي"
    };
    private static final String[] A_LABEL={
        "زاوية رأس المنشور (درجة)", "خط العرض (درجة)", "الشحنة الأولى q1 (ميكروكولوم؛ السالب يسار الصفر)",
        "الارتفاع عن الأرض (كم)", "الطول الموجي (نانومتر)", "كثافة السائل (كغ/م³)",
        "سعة تغير المجال (تسلا)", "فرق الجهد (فولت)", "نصف المحور الأكبر (AU)",
        "تردد المصدر (هرتز)", "طول الخيط (متر)", "الطول الموجي (نانومتر)"
    };
    private static final String[] B_LABEL={
        "بعد الشاشة (متر)", "الزمن المنقضي (ساعة)", "المسافة (متر)",
        "السرعة الأفقية (كم/ث)", "فاصل الشقين (مم)", "كثافة الجسم (كغ/م³)",
        "تردد المجال (هرتز)", "المقاومة (أوم)", "الاختلاف المركزي e",
        "سرعة الاقتراب (م/ث)", "الجاذبية (م/ث²)", "دالة شغل المادة (eV)"
    };
    private static final String[] LIMITS={
        "المنشور من زجاج BK7، واستُخدمت صيغة الانحراف الأصغر؛ أهملنا امتصاص الزجاج والتشتت داخل الحزمة.",
        "معدل سبق بندول فوكو 15.041 درجة/ساعة × جيب خط العرض؛ نموذج مثالي يتجاهل الاحتكاك.",
        "قانون كولوم لشحنتين نقطيتين ساكنتين في الفراغ. تقدر تغيّر مقدار وإشارة كل شحنة والمسافة بينهما.",
        "نموذج مدار كبلري دون مقاومة جوية. السرعة دون المدارية قد تصطدم بالأرض؛ لا يمثل إطلاقاً حقيقياً.",
        "فاصل الأهداب يقارب λL/d، عند شاشة تبعد مترين ومع زوايا صغيرة، دون غلاف حيود الشق الواحد.",
        "الحجم لتر واحد. قوة الطفو المعروضة للانغمار الكامل، ونسبة الغمر عند الطفو مفترضة لاتزان هادئ.",
        "ملف ثابت من 50 لفة ومساحته 0.01 م²؛ مجال جيبي متجانس، والقيمة المعروضة قمة الجهد المستحث.",
        "دائرة مثالية بمقاومة ثابتة من دون مقاومة داخلية أو سخونة. القدرة محسوبة على المقاومة.",
        "مدار شمسي كبلري مثالي؛ وحدة AU والسنة الأرضية وكتلة الشمس ثابتة.",
        "مصدر صوت يتحرك نحو مستمع ثابت، في هواء ساكن وسرعة صوت 343 م/ث؛ لا تأثيرات رياح.",
        "تقدير الفترة في تقريب الزاوية الصغيرة وبندول نقطي بلا احتكاك. يختلف عند السعات الكبيرة.",
        "انبعاث إلكترون فقط إذا تجاوزت طاقة الفوتون دالة الشغل؛ لا يحدد النموذج عدد الإلكترونات."
    };
    private static final String[] CHALLENGES={
        "ارفع زاوية المنشور: هل يزيد الفصل بين الأحمر والأزرق؟",
        "ضع خط العرض عند الصفر، ثم عند القطب: كيف يتغيّر سبق البندول؟",
        "جرّب (+,+) ثم (+,−) ثم (−,−). اضغط الصفر، وبعدها غيّر المسافة وراقب مقدار القوة واتجاهها.",
        "اضبط السرعة حتى تقترب من السرعة المدارية الدائرية.",
        "قلّل المسافة بين الشقين ولاحظ تباعد أهداب التداخل.",
        "اجعل كثافة الجسم أقل من السائل ثم أكبر منه.",
        "ضاعف التردد وتحقق من تضاعف الجهد المستحث الأقصى.",
        "ضاعف المقاومة مع ثبات الجهد، وقارن التيار والقدرة.",
        "ارفع الاختلاف المركزي ولاحظ تغير مسافتي الحضيض والأوج.",
        "اقترب بالمصدر من المستمع ثم اجعله يبتعد.",
        "ضاعف طول الخيط تقريباً أربع مرات وقارن الفترة.",
        "ارفع طول الموجة حتى يتوقف انبعاث الإلكترونات."
    };
    private static final double[] A_MIN={30,-80,-5,200,380,700,.1,1,.3,200,.2,250};
    private static final double[] A_MAX={70,80,5,1500,750,1300,1.0,24,4,1200,3,800};
    private static final double[] B_MIN={.5,0,.1,5, .1,500,.2,10,0,-120,1.6,1.5};
    private static final double[] B_MAX={3,24,2,12,1,1500,4,500,.7,120,20,4};
    private static final int[] A_INIT={50,72,70,25,35,50,50,50,30,50,40,40};
    private static final int[] B_INIT={40,25,45,40,40,50,40,20,25,50,48,40};

    private static final String[] HISTORY = {
        "1666: استخدم نيوتن منشورًا لتوضيح أن الضوء الأبيض يحمل ألوانًا مختلفة.",
        "1851: عرض فوكو بندولًا طويلًا في باريس لإظهار دوران الأرض.",
        "1785: استخدم شارل كولوم ميزان الالتواء لدراسة القوى بين الشحنات.",
        "1687: تخيّل نيوتن إطلاق مقذوف بسرعة تجعل الأرض تنحني تحته باستمرار.",
        "1801: وضّح توماس يونغ التداخل في تجارب الشقين لبحث الطبيعة الموجية للضوء.",
        "القرن الثالث ق.م: شرح أرخميدس قوة الطفو الناتجة من السائل المزاح.",
        "1831: اكتشف فاراداي الحث الكهرومغناطيسي عند تغيّر الفيض المغناطيسي.",
        "1827: صاغ أوم علاقة بين فرق الجهد والتيار والمقاومة.",
        "1609–1619: استنتج كبلر قوانين حركة الكواكب من أرصاد دقيقة.",
        "1842: وصف كريستيان دوبلر تغير التردد بسبب الحركة النسبية.",
        "القرن السابع عشر: درس غاليليو علاقة طول البندول وزمن تأرجحه.",
        "1905: فسّر أينشتاين التأثير الكهروضوئي بكمّات من الضوء."
    };
    private static final String[] APPLICATIONS = {
        "اليوم: المبددات الطيفية، المطياف، تحليل الضوء في المختبرات والاتصالات البصرية.",
        "اليوم: أنظمة مرجعية للحركة ودروس في فيزياء الأرض وأجهزة قياس دقيقة.",
        "اليوم: تصميم الحساسات الكهروستاتيكية وفهم سلوك الشحنات في الأجهزة.",
        "اليوم: حساب مدارات الأقمار الصناعية ومهام الفضاء.",
        "اليوم: المقاييس التداخلية والقياسات البصرية عالية الدقة.",
        "اليوم: قياس الكثافة، تصميم السفن والغواصات.",
        "اليوم: المولدات والمحولات وأنظمة استشعار المجال.",
        "اليوم: تصميم الدوائر الكهربائية وحماية الإلكترونيات.",
        "اليوم: تخطيط مدارات الأقمار والمركبات الفضائية.",
        "اليوم: الرادار والسرعة النسبية والقياسات الطبية بالموجات فوق الصوتية.",
        "اليوم: الساعات الميكانيكية وحساسات الاهتزاز والتجارب التعليمية.",
        "اليوم: الكواشف الضوئية والخلايا الشمسية وحساسات الكاميرات."
    };
    private static final String[] INSIGHT = {
        "يرتبط معامل انكسار الزجاج بالطول الموجي؛ لذلك ينحرف الأزرق والأحمر بزاويتين مختلفتين.",
        "معدل دوران مستوى تأرجح بندول فوكو يتناسب مع جيب خط العرض. عند خط الاستواء لا يدور بالنسبة إلى الأرض بهذا النموذج.",
        "عند ثبات الشحنتين، مضاعفة المسافة تُقلّل مقدار القوة إلى الربع. الإشارة تحدد تجاذبًا أم تنافرًا.",
        "السرعة المدارية تتحدد ببعد المركبة عن مركز الأرض؛ مقاومة الهواء غير ممثلة هنا.",
        "يزداد تباعد الأهداب بزيادة الطول الموجي أو تقليل المسافة بين الشقين.",
        "إذا كانت كثافة الجسم أقل من كثافة السائل فإنه يطفو مع انغمار جزء منه؛ وإلا يغوص.",
        "زيادة معدل تغيّر الفيض المغناطيسي تزيد مقدار الجهد المستحث.",
        "عند ثبات فرق الجهد، زيادة المقاومة تقلل التيار وفق قانون أوم.",
        "يزداد الزمن المداري بازدياد المحور الأكبر؛ وتؤثر اللامركزية على موضعَي الحضيض والأوج.",
        "يُسمع تردد أعلى عند اقتراب مصدر الصوت وأقل عند ابتعاده في الهواء الساكن.",
        "يزداد زمن الدورة مع الجذر التربيعي لطول الخيط، ويقل بزيادة الجاذبية.",
        "ينبعث إلكترون فقط إذا كانت طاقة الفوتون أكبر من دالة الشغل، لا بمجرد زيادة سطوع الضوء."
    };
    private static final String[] GRAPH_LABEL = {
        "فصل اللونين (سم)", "سبق البندول (درجة/ساعة)", "القوة الموقّعة (نيوتن)",
        "السرعة الدائرية (كم/ث)", "مسافة الأهداب (مم)", "قوة الطفو (نيوتن)",
        "أقصى جهد مستحث (فولت)", "التيار (أمبير)", "زمن المدار (سنة)",
        "التردد المسموع (هرتز)", "الدورة (ثانية)", "طاقة الإلكترون (eV)"
    };
    private final int[] aProgress=A_INIT.clone(), bProgress=B_INIT.clone();
    private final boolean[] done=new boolean[12];
    private boolean guided=true, showExplanation=false, showGraph=false;
    private int guideStep=0, currentFocus=0;
    private double[] savedA=new double[12],savedB=new double[12],savedQ2=new double[12];
    private boolean[] hasSaved=new boolean[12];
    private SharedPreferences storage;
    private TextView guideText, compareText;
    private LabGraphView graphView;
    private SeekBar firstSlider,secondSlider;
    private ScrollView scrolling;
    private int learningCredits=0;
    // Coulomb lab: q1 is the signed A slider (-5 .. +5 µC);
    // q2 is independently adjustable, so both polarities can be explored.
    private double secondChargeMicroCoulombs=2.0;
    private SeekBar chargeOneSlider;
    private int selected=-1; // -1: all 15 labs
    private LinearLayout page;
    private TextView result, aValue, bValue;
    private LabCanvas drawing;
    private long clock=SystemClock.uptimeMillis();

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        storage=getSharedPreferences("alradhy_discovery_museum_v05",MODE_PRIVATE);
        for(int i=0;i<12;i++){
            aProgress[i]=storage.getInt("a_"+i,A_INIT[i]);
            bProgress[i]=storage.getInt("b_"+i,B_INIT[i]);
            savedA[i]=storage.getFloat("compare_a_"+i,0);
            savedB[i]=storage.getFloat("compare_b_"+i,0);
            savedQ2[i]=storage.getFloat("compare_q2_"+i,2);
            hasSaved[i]=storage.getBoolean("compare_has_"+i,false);
            done[i]=storage.getBoolean("done_"+i,false);
        }
        guided=storage.getBoolean("guided",true);
        showGraph=storage.getBoolean("showGraph",false);
        learningCredits=storage.getInt("credits",0);
        secondChargeMicroCoulombs=storage.getFloat("q2",2f);
        if(state==null && getIntent()!=null)
            selected=getIntent().getIntExtra("labIndex",-1);
        if(selected < -1 || selected >= 12) selected=-1;
        if(state!=null){selected=state.getInt("selected",-1);
            int[] ap=state.getIntArray("a"),bp=state.getIntArray("b");
            if(ap!=null&&ap.length==12) System.arraycopy(ap,0,aProgress,0,12);
            if(bp!=null&&bp.length==12) System.arraycopy(bp,0,bProgress,0,12);
            secondChargeMicroCoulombs=state.getDouble("charge2",2.0);
            guided=state.getBoolean("guided",guided);
            showGraph=state.getBoolean("showGraph",showGraph);
            guideStep=state.getInt("guideStep",0);
            showExplanation=state.getBoolean("showExplanation",false);
        }
        render();
    }
    @Override protected void onSaveInstanceState(Bundle state){
        state.putInt("selected",selected);state.putIntArray("a",aProgress);state.putIntArray("b",bProgress);
        state.putDouble("charge2",secondChargeMicroCoulombs);
        state.putBoolean("guided",guided);
        state.putBoolean("showGraph",showGraph);
        state.putInt("guideStep",guideStep);
        state.putBoolean("showExplanation",showExplanation);
        super.onSaveInstanceState(state);
    }
    @Override public void onBackPressed(){
        if(selected>=0){selected=-1;render();} else super.onBackPressed();
    }
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private GradientDrawable shape(int color,int outline){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(13));d.setStroke(dp(1),outline);return d;
    }
    private TextView text(String s,int size,int color){
        TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setPadding(dp(6),dp(6),dp(6),dp(6));return v;
    }
    private void line(String s,int size,int color){
        TextView t=text(s,size,color);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);page.addView(t,p);
    }
    private Button button(String title,Runnable action){
        Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setTextSize(17);b.setTextColor(WHITE);
        b.setBackground(shape(CARD,Color.rgb(55,89,128)));b.setMinimumHeight(dp(54));
        b.setOnFocusChangeListener((view,focus)->{
            b.setTextColor(focus?Color.WHITE:WHITE);b.setBackground(shape(focus?Color.rgb(90,66,25):CARD,focus?GOLD:Color.rgb(55,89,128)));
        });b.setOnClickListener(v->action.run());return b;
    }
    private void addButton(String title,Runnable action){
        Button b=button(title,action);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.bottomMargin=dp(10);page.addView(b,p);
    }
    private void row(String[] labels,Runnable[] actions){
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=0;i<labels.length;i++){
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(59),1);
            p.leftMargin=dp(4);p.rightMargin=dp(4);bar.addView(button(labels[i],actions[i]),p);
        }LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(14);page.addView(bar,p);
    }
    private void persist() {
        if(storage==null)return;
        SharedPreferences.Editor e=storage.edit();
        for(int i=0;i<12;i++){
            e.putInt("a_"+i,aProgress[i]).putInt("b_"+i,bProgress[i]);
            e.putBoolean("done_"+i,done[i]);
            e.putBoolean("compare_has_"+i,hasSaved[i]);
            e.putFloat("compare_a_"+i,(float)savedA[i])
             .putFloat("compare_b_"+i,(float)savedB[i])
             .putFloat("compare_q2_"+i,(float)savedQ2[i]);
        }
        e.putFloat("q2",(float)secondChargeMicroCoulombs);
        e.putBoolean("guided",guided).putBoolean("showGraph",showGraph)
         .putInt("credits",learningCredits).apply();
    }
    private void resetThisLab(){
        if(selected<0)return;
        aProgress[selected]=A_INIT[selected];
        bProgress[selected]=B_INIT[selected];
        if(selected==2)secondChargeMicroCoulombs=2;
        guideStep=0;showExplanation=false;
        persist();render();
    }
    private void goLab(int index){
        selected=index;
        guideStep=0;showExplanation=false;currentFocus=0;
        persist();render();
    }
    private void stepProgress(boolean forward){
        guideStep=(guideStep+(forward?1:2))%3;
        if(guideStep==2 && !done[selected]){
            done[selected]=true;
            learningCredits++;
            persist();
        }
        render();
    }
    private void saveComparison(){
        if(selected<0)return;
        hasSaved[selected]=true;savedA[selected]=a();savedB[selected]=b();
        savedQ2[selected]=secondChargeMicroCoulombs;
        persist();updateComparison();
    }
    private void setProgress(boolean first,int progress){
        if(selected<0)return;
        int p=Math.max(0,Math.min(100,progress));
        if(first){aProgress[selected]=p;if(firstSlider!=null)firstSlider.setProgress(p);}
        else{bProgress[selected]=p;if(secondSlider!=null)secondSlider.setProgress(p);}
        persist();update();
    }
    private void render(){
        firstSlider=null;secondSlider=null;graphView=null;
        guideText=null;compareText=null;
        chargeOneSlider=null;
        drawing=null;result=null;aValue=null;bValue=null;
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);
        scrolling=sc;
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(46),dp(20),dp(46),dp(40));
        sc.addView(page);setContentView(sc);
        line("ALRADHY DISCOVERY • متحف العلوم التفاعلي V0.5  •  15 مختبرًا",18,GOLD);
        if(selected<0){renderHub();return;}
        final int i=selected;
        line("المختبر "+(i+4)+" من 15  •  "+NAMES[i],27,WHITE);
        row(new String[]{"🏛️ المختبرات","◀ السابق","التالي ▶"},new Runnable[]{
            ()->{selected=-1;render();},
            ()->goLab((selected+11)%12),
            ()->goLab((selected+1)%12)
        });
        row(new String[]{guided?"☑ تعلّم معي":"□ تعلّم معي",guided?"استكشف بحرّيتك":"تعلّم خطوة بخطوة","⟲ إعادة ضبط"},new Runnable[]{
            ()->{guided=!guided;persist();render();},
            ()->{guided=!guided;persist();render();},
            ()->resetThisLab()
        });
        if(guided){
            line("🔎 رحلة الاكتشاف",20,GOLD);
            guideText=text("",18,WHITE);
            guideText.setBackground(shape(CARD,Color.rgb(62,89,115)));
            page.addView(guideText,new LinearLayout.LayoutParams(-1,-2));
            updateGuide();
            row(new String[]{"الخطوة السابقة","الخطوة التالية","ليش صار كذا؟"},new Runnable[]{
                ()->stepProgress(false),
                ()->stepProgress(true),
                ()->{showExplanation=!showExplanation;render();}
            });
        }
        line("🕰️ القصة: "+HISTORY[i],17,MUTED);
        line("🌍 تطبيقات اليوم: "+APPLICATIONS[i],17,CYAN);
        drawing=new LabCanvas();
        drawing.setFocusable(true);
        drawing.setContentDescription("اسحب يمين ويسار داخل التجربة لتعديل المتغير الأول، وفوق وتحت لتعديل المتغير الثاني");
        drawing.setOnKeyListener((v,key,ev)->{
            if(ev.getAction()!=KeyEvent.ACTION_DOWN)return false;
            if(key==KeyEvent.KEYCODE_DPAD_LEFT){setProgress(true,aProgress[selected]-2);return true;}
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT){setProgress(true,aProgress[selected]+2);return true;}
            if(key==KeyEvent.KEYCODE_DPAD_UP){setProgress(false,bProgress[selected]+2);return true;}
            if(key==KeyEvent.KEYCODE_DPAD_DOWN){setProgress(false,bProgress[selected]-2);return true;}
            return false;
        });
        page.addView(drawing,new LinearLayout.LayoutParams(-1,dp(280)));
        line("🖱️ اسحب داخل الرسم يمين/يسار لتغيير المتغير الأول، أو فوق/تحت للمتغير الثاني. الريموت: الأسهم بعد تحديد الرسم.",15,CYAN);
        result=text("",20,WHITE);result.setBackground(shape(CARD,Color.rgb(44,73,104)));
        result.setPadding(dp(18),dp(14),dp(18),dp(14));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(13);p.bottomMargin=dp(14);page.addView(result,p);
        if(i==2){
            line("⚡ تحكم مباشر في قطبية كل شحنة — بالماوس أو بأسهم الريموت وزر OK",19,CYAN);
            line("قطبية الشحنة الأولى q1",18,WHITE);
            row(new String[]{"q1 سالب (−)","q1 صفر (0)","q1 موجب (+)"},new Runnable[]{
                ()->setFirstCharge(-Math.max(0.5,Math.abs(a()))),
                ()->setFirstCharge(0),
                ()->setFirstCharge(Math.max(0.5,Math.abs(a())))
            });
            line("قطبية الشحنة الثانية q2",18,WHITE);
            row(new String[]{"q2 سالب (−)","q2 صفر (0)","q2 موجب (+)"},new Runnable[]{
                ()->setSecondCharge(-Math.max(0.5,Math.abs(secondChargeMicroCoulombs))),
                ()->setSecondCharge(0),
                ()->setSecondCharge(Math.max(0.5,Math.abs(secondChargeMicroCoulombs)))
            });
        }
        aValue=control(i,true);bValue=control(i,false);
        row(new String[]{"A −","A +","B −","B +"},new Runnable[]{
            ()->setProgress(true,aProgress[selected]-5),
            ()->setProgress(true,aProgress[selected]+5),
            ()->setProgress(false,bProgress[selected]-5),
            ()->setProgress(false,bProgress[selected]+5)
        });
        row(new String[]{"حالة منخفضة","حالة متوسطة","حالة مرتفعة"},new Runnable[]{
            ()->{setProgress(true,10);setProgress(false,10);},
            ()->{setProgress(true,50);setProgress(false,50);},
            ()->{setProgress(true,90);setProgress(false,90);}
        });
        row(new String[]{"📊 رسم العلاقة","📌 احفظ للمقارنة","❔ التفسير"},new Runnable[]{
            ()->{showGraph=!showGraph;persist();render();},
            ()->saveComparison(),
            ()->{showExplanation=!showExplanation;render();}
        });
        compareText=text("",17,WHITE);
        compareText.setBackground(shape(CARD,Color.rgb(50,84,109)));
        compareText.setPadding(dp(16),dp(12),dp(16),dp(12));
        page.addView(compareText,new LinearLayout.LayoutParams(-1,-2));
        updateComparison();
        if(showGraph){
            line("📈 كيف تتغير النتيجة عند تغيير "+A_LABEL[i]+" مع ثبات "+B_LABEL[i]+"؟",18,GOLD);
            graphView=new LabGraphView();
            graphView.setFocusable(false);
            page.addView(graphView,new LinearLayout.LayoutParams(-1,dp(230)));
            line("المحور الأفقي: "+A_LABEL[i]+" | المحور الرأسي: "+GRAPH_LABEL[i],15,MUTED);
        }
        if(showExplanation) line("💡 ليش صار كذا؟ "+INSIGHT[i],18,GREEN);
        if(i==2){
            row(new String[]{"q1 −0.5 µC","q1 +0.5 µC","q2 −0.5 µC","q2 +0.5 µC"},new Runnable[]{
                ()->setFirstCharge(a()-0.5),
                ()->setFirstCharge(a()+0.5),
                ()->setSecondCharge(secondChargeMicroCoulombs-0.5),
                ()->setSecondCharge(secondChargeMicroCoulombs+0.5)
            });
            line("تقدر تسحب الشريط، أو تستخدم أزرار الزيادة والنقصان للضبط الدقيق. العلامات الموجبة تتنافر والمختلفة تتجاذب، وعند الصفر تنعدم القوة.",17,MUTED);
        }
        row(new String[]{done[i]?"✅ تم الإنجاز":"✓ أنجزت التحدي", "إظهار / إخفاء النتيجة"},new Runnable[]{
            ()->{if(!done[selected])learningCredits++;done[selected]=true;persist();render();},
            ()->{showExplanation=!showExplanation;render();}
        });
        line("🎯 جرّب بنفسك: "+CHALLENGES[i],18,GOLD);
        line("ما الذي يفترضه النموذج؟ "+LIMITS[i],16,MUTED);
        update();
    }
    private void renderHub(){
        line("3 مختبرات أصلية + 12 مختبرًا جديدًا. استكشف بنفسك أو اختر التعلّم الموجّه، وقارن النتائج واحفظ تقدمك.",23,WHITE);
        int completed=0;for(boolean d:done)if(d)completed++;
        line("⭐ مكتمل: "+completed+" من 12 مختبرًا جديدًا   •   نقاط الاستكشاف: "+learningCredits,18,GREEN);
        line("المختبرات الأصلية • قياس سرعة الضوء",22,GOLD);
        row(new String[]{"غاليليو • الفوانيس","رومر • خسوف آيو","فيزو • العجلة"},new Runnable[]{
            ()->openOriginal("galileo"),()->openOriginal("romer"),()->openOriginal("fizeau")
        });
        line("12 مختبرًا تفاعليًا جديدًا",23,GOLD);
        for(int k=0;k<12;k+=2){
            final int left=k,right=k+1;
            row(new String[]{(k+4)+". "+NAMES[left],(k+5)+". "+NAMES[right]},new Runnable[]{
                ()->{selected=left;render();},()->{selected=right;render();}
            });
        }
        line("جميع النماذج تعمل دون إنترنت. المخرجات حسابات فيزيائية مبسطة، وليست بيانات من أجهزة قياس حقيقية.",17,MUTED);
        addButton("العودة إلى رحلة المحطات الخمسين",()->finish());
    }
    private void openOriginal(String id){
        Intent t=new Intent(this,MainActivity.class);t.putExtra("experiment",id);startActivity(t);
    }
    private TextView control(int i,boolean first){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        TextView title=text("",19,CYAN);box.addView(title);
        SeekBar slider=new SeekBar(this);slider.setMax(100);slider.setProgress(first?aProgress[i]:bProgress[i]);
        slider.setFocusable(true);slider.setContentDescription(first?A_LABEL[i]:B_LABEL[i]);
        if(i==2&&first)chargeOneSlider=slider;
        if(first)firstSlider=slider;else secondSlider=slider;
        box.addView(slider,new LinearLayout.LayoutParams(-1,dp(44)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(5);page.addView(box,p);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int value,boolean user){
                if(first)aProgress[i]=value;else bProgress[i]=value;
                persist();update();
            }
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
        });
        return title;
    }
    private void setFirstCharge(double charge){
        double bounded=Math.max(-5.0,Math.min(5.0,charge));
        int progress=(int)Math.round((bounded+5.0)*10.0);
        aProgress[2]=Math.max(0,Math.min(100,progress));
        if(chargeOneSlider!=null)chargeOneSlider.setProgress(aProgress[2]);
        persist();update();
    }
    private void setSecondCharge(double charge){
        secondChargeMicroCoulombs=Math.max(-5.0,Math.min(5.0,Math.round(charge*2.0)/2.0));
        persist();update();
    }
    private double a(){return value(A_MIN[selected],A_MAX[selected],aProgress[selected]);}
    private double b(){return value(B_MIN[selected],B_MAX[selected],bProgress[selected]);}
    private double value(double min,double max,int progress){return min+(max-min)*progress/100.0;}
    private String n(double v,int decimals){return String.format(Locale.US,"%."+decimals+"f",v);}
    private static double rad(double degrees){return Math.toRadians(degrees);}
    private static double bk7(double lambdaNm){
        double l=lambdaNm/1000.0,l2=l*l;
        return Math.sqrt(1+1.03961212*l2/(l2-.00600069867)+.231792344*l2/(l2-.0200179144)+
            1.01046945*l2/(l2-103.560653));
    }
    private void update(){
        if(selected<0||result==null)return;
        int i=selected;double x=a(),y=b();
        if(aValue!=null)aValue.setText(A_LABEL[i]+": "+n(x,(i==3||i==5||i==8||i==9||i==11)?1:2));
        if(bValue!=null)bValue.setText(B_LABEL[i]+": "+n(y,(i==1||i==5||i==7||i==9)?1:2));
        String out="";
        switch(i){
            case 0:{
                double A=rad(x), dr=2*Math.asin(bk7(650)*Math.sin(A/2))-A,
                    db=2*Math.asin(bk7(450)*Math.sin(A/2))-A;
                double spread=y*(Math.tan(db)-Math.tan(dr))*100;
                out="انحراف الأحمر: "+n(Math.toDegrees(dr),2)+"°   |   الأزرق: "+n(Math.toDegrees(db),2)+"°\n"+
                    "فصل اللونين على الشاشة ≈ "+n(spread,1)+" سم";break;
            }
            case 1:{
                double rate=15.041067*Math.sin(rad(x)),angle=rate*y;
                out="سرعة سبق مستوى التأرجح: "+n(rate,2)+"°/ساعة\n"+
                    "الدوران المتراكم خلال "+n(y,1)+" ساعة = "+n(angle,1)+"°";break;
            }
            case 2:{
                double force=8.9875517923e9*(x*1e-6)*(secondChargeMicroCoulombs*1e-6)/(y*y);
                out="q1 = "+n(x,2)+" µC     |     q2 = "+n(secondChargeMicroCoulombs,2)+" µC\n"+
                    "المسافة = "+n(y,2)+" متر     |     مقدار القوة = "+n(Math.abs(force),4)+" نيوتن\n"+
                    (force<0?"تجاذب: شحنتان مختلفتا الإشارة، والأسهم للداخل":force>0?"تنافر: شحنتان متماثلتا الإشارة، والأسهم للخارج":"القوة صفر لأن إحدى الشحنتين صفر");break;
            }
            case 3:{
                double r=(6371+x)*1000,v=y*1000,vc=Math.sqrt(3.986004418e14/r),ve=vc*Math.sqrt(2);
                double energy=.5*v*v-3.986004418e14/r;
                String kind=v>=ve?"مسار هروب":v>vc?"مدار إهليلجي (إن لم يتقاطع مع الأرض)":"أقل من السرعة الدائرية؛ قد يرتطم بالأرض";
                out="السرعة الدائرية: "+n(vc/1000,3)+" كم/ث   |   الإفلات: "+n(ve/1000,3)+" كم/ث\n"+
                    "الطاقة المدارية النوعية: "+n(energy/1e6,1)+" ميغاجول/كغ\n"+kind;break;
            }
            case 4:{
                double mm=x*1e-9*2/(y*1e-3)*1000;
                out="التباعد بين هدبتين مضيئتين متتاليتين ≈ "+n(mm,2)+" مم\n"+
                    "العلاقة: Δy = λ × 2 متر ÷ مسافة الشقين";break;
            }
            case 5:{
                double buoy=x*G*.001,weight=y*G*.001,fraction=Math.min(100,100*y/x);
                out="الطفو عند الغمر الكامل: "+n(buoy,2)+" نيوتن   |   الوزن: "+n(weight,2)+" نيوتن\n"+
                    (y<x?"الجسم يطفو، النسبة المغمورة ≈ "+n(fraction,1)+"%":y>x?"الجسم يغوص لأن وزنه أكبر من الطفو":"تعادل كثافة الجسم والسائل؛ اتزان محايد");break;
            }
            case 6:{
                double emf=50*.01*2*Math.PI*y*x;
                out="أقصى قوة دافعة كهربائية مستحثة: "+n(emf,2)+" فولت\n"+
                    "قانون فاراداي: |ε| = N × A × 2πf × B";break;
            }
            case 7:{
                double I=x/y,power=x*I;
                out="التيار: "+n(I,3)+" أمبير\n"+
                    "القدرة على المقاومة: "+n(power,2)+" واط   |   I = V / R";break;
            }
            case 8:{
                double per=Math.sqrt(x*x*x),near=x*(1-y),far=x*(1+y);
                out="الدورة المدارية: "+n(per,2)+" سنة أرضية\n"+
                    "الحضيض: "+n(near,2)+" AU   |   الأوج: "+n(far,2)+" AU";break;
            }
            case 9:{
                double f=x*343/(343-y),front=343-y,back=343+y;
                out="التردد المسموع: "+n(f,1)+" هرتز\n"+
                    "الطول الموجي أمام المصدر: "+n(front/x,3)+" م   |   خلفه: "+n(back/x,3)+" م";break;
            }
            case 10:{
                double period=2*Math.PI*Math.sqrt(x/y);
                out="زمن دورة التأرجح الواحدة: "+n(period,3)+" ثانية\n"+
                    "العلاقة: T = 2π √(L/g)";break;
            }
            case 11:{
                double ev=1239.841984/x,energy=Math.max(0,ev-y);
                out="طاقة الفوتون: "+n(ev,3)+" eV   |   حد الانبعاث: "+n(1239.841984/y,1)+" نانومتر\n"+
                    (energy>0?"ينبعث إلكترون؛ أقصى طاقة حركية "+n(energy,3)+" eV":"لا انبعاث كهروضوئي عند هذا الطول الموجي");break;
            }
        }
        result.setText(out);
        if(drawing!=null)drawing.invalidate();
        if(graphView!=null)graphView.invalidate();
        updateComparison();
    }

    private void updateGuide() {
        if(guideText==null||selected<0)return;
        String lead=guideStep==0?"① التوقّع":guideStep==1?"② التجربة":"③ استنتاج النتيجة";
        String body=guideStep==0 ? "قبل ما تغيّر الإعدادات، توقّع النتيجة: "+CHALLENGES[selected] :
            guideStep==1 ? "غيّر الأدوات بالماوس أو أسهم الريموت وشاهد كيف تتغيّر الأرقام والرسم. جرب وضعين مختلفين ثم احفظ النتيجة الأولى." :
            "قارن النتيجتين ثم فسّر الفرق بنفسك. "+INSIGHT[selected];
        guideText.setText(lead+"  •  "+body);
    }
    private void updateComparison() {
        if(compareText==null||selected<0)return;
        if(!hasSaved[selected]){
            compareText.setText("📌 المقارنة: احفظ قياسًا أولًا، وبعدها غيّر متغيرًا واحدًا وقارن النتيجتين.");
            return;
        }
        int i=selected;
        double before=metric(i,savedA[i],savedB[i],savedQ2[i]);
        double after=metric(i,a(),b(),secondChargeMicroCoulombs);
        double diff=after-before;
        compareText.setText(
            "📌 القياس المحفوظ: "+A_LABEL[i]+" = "+n(savedA[i],2)+", "+B_LABEL[i]+" = "+n(savedB[i],2)+
            (i==2?", q2 = "+n(savedQ2[i],2)+" µC":"")+"\n"+
            "قبل: "+n(before,4)+"  |  الآن: "+n(after,4)+"  |  الفرق: "+(diff>0?"+":"")+n(diff,4)+"\n"+
            "الوحدة: "+GRAPH_LABEL[i]+"  •  غيّر أداة واحدة لتعرف السبب."
        );
    }
    private double metric(int i,double x,double y,double q2){
        switch(i){
            case 0: {
                double A=rad(x),red=2*Math.asin(bk7(650)*Math.sin(A/2))-A,
                  blue=2*Math.asin(bk7(450)*Math.sin(A/2))-A;
                return y*(Math.tan(blue)-Math.tan(red))*100;
            }
            case 1: return 15.041067*Math.sin(rad(x));
            case 2: return 8.9875517923e9*(x*1e-6)*(q2*1e-6)/(y*y);
            case 3: return Math.sqrt(3.986004418e14/((6371+x)*1000.0))/1000.0;
            case 4: return x*1e-9*2.0/(y*1e-3)*1000.0;
            case 5: return x*G*.001;
            case 6: return 50*.01*2*Math.PI*y*x;
            case 7: return x/y;
            case 8: return Math.sqrt(x*x*x);
            case 9: return x*343/(343-y);
            case 10: return 2*Math.PI*Math.sqrt(x/y);
            case 11: return Math.max(0,1239.841984/x-y);
            default: return 0;
        }
    }
    private final class LabGraphView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        LabGraphView(){super(ScienceLabsActivity.this);setBackground(shape(CARD,Color.rgb(48,75,104)));}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            if(selected<0)return;
            float w=getWidth(),h=getHeight(),left=dp(36),right=w-dp(24),top=dp(22),bottom=h-dp(42);
            if(right-left<80||bottom-top<80)return;
            double bx=b();
            double low=Double.POSITIVE_INFINITY,high=Double.NEGATIVE_INFINITY;
            double[] vals=new double[101];
            for(int step=0;step<=100;step++){
                double ax=value(A_MIN[selected],A_MAX[selected],step);
                double v=metric(selected,ax,bx,secondChargeMicroCoulombs);
                if(!Double.isFinite(v))v=0;
                vals[step]=v;low=Math.min(low,v);high=Math.max(high,v);
            }
            double spread=high-low;
            if(spread<1e-10){high+=1;low-=1;}else{high+=.12*spread;low-=.12*spread;}
            p.setStrokeWidth(dp(1));p.setColor(MUTED);p.setStyle(Paint.Style.STROKE);
            for(int i=0;i<=4;i++){
                float y=top+(bottom-top)*i/4f;
                canvas.drawLine(left,y,right,y,p);
            }
            p.setStyle(Paint.Style.FILL);p.setColor(MUTED);p.setTextSize(dp(12));
            canvas.drawText(n(high,2),dp(4),top+dp(6),p);
            canvas.drawText(n(low,2),dp(4),bottom,p);
            canvas.drawText(n(A_MIN[selected],1),left,bottom+dp(24),p);
            canvas.drawText(n(A_MAX[selected],1),right-dp(35),bottom+dp(24),p);
            Path curve=new Path();
            for(int step=0;step<=100;step++){
                float xx=left+(right-left)*step/100f;
                float yy=(float)(bottom-(vals[step]-low)/(high-low)*(bottom-top));
                if(step==0)curve.moveTo(xx,yy);else curve.lineTo(xx,yy);
            }
            p.setColor(CYAN);p.setStrokeWidth(dp(3));p.setStyle(Paint.Style.STROKE);
            canvas.drawPath(curve,p);
            double v=metric(selected,a(),bx,secondChargeMicroCoulombs);
            float sx=left+(right-left)*aProgress[selected]/100f;
            float sy=(float)(bottom-(v-low)/(high-low)*(bottom-top));
            p.setStyle(Paint.Style.FILL);p.setColor(GOLD);canvas.drawCircle(sx,sy,dp(7),p);
            p.setColor(WHITE);p.setTextSize(dp(13));
            canvas.drawText("● "+n(v,3),Math.max(left,Math.min(right-dp(85),sx-dp(25))),Math.max(top+dp(15),sy-dp(12)),p);
        }
    }
    private final class LabCanvas extends View {
        private final Paint p=new Paint(3);
        private float downX,downY;
        private int downA,downB;
        private double downQ2;
        private boolean dragSecondCharge;
        LabCanvas(){super(ScienceLabsActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override public boolean onTouchEvent(MotionEvent ev){
            if(selected<0)return false;
            switch(ev.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    downX=ev.getX();downY=ev.getY();
                    downA=aProgress[selected];downB=bProgress[selected];
                    downQ2=secondChargeMicroCoulombs;
                    dragSecondCharge=selected==2&&downX>getWidth()*.57f;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    requestFocus();
                    return true;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    float dx=ev.getX()-downX,dy=ev.getY()-downY;
                    if(selected==2 && dragSecondCharge){
                        setSecondCharge(downQ2+dx/Math.max(1f,getWidth())*10.0);
                    }else if(Math.abs(dx)>=Math.abs(dy)){
                        setProgress(true,downA+Math.round(dx/Math.max(1f,getWidth())*100));
                    }else{
                        setProgress(false,downB-Math.round(dy/Math.max(1f,getHeight())*100));
                    }
                    if(ev.getActionMasked()==MotionEvent.ACTION_UP){
                        getParent().requestDisallowInterceptTouchEvent(false);
                        performClick();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                default:return super.onTouchEvent(ev);
            }
        }
        @Override public boolean performClick(){super.performClick();return true;}
        private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){
            p.setColor(color);p.setStrokeWidth(width);p.setStyle(Paint.Style.STROKE);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
        }
        private void circle(Canvas c,float x,float y,float r,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
        private void box(Canvas c,float x,float y,float w,float h,int color){
            p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);
        }
        private void caption(Canvas c,String text,float x,float y,int color,int size){
            p.setColor(color);p.setTextSize(size);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(text,x,y,p);
        }
        private void arrow(Canvas c,float x1,float y1,float x2,float y2,int col,float sw){
            line(c,x1,y1,x2,y2,col,sw);
            float dx=x2-x1,dy=y2-y1;
            float len=(float)Math.sqrt(dx*dx+dy*dy);
            if(len<1)return;
            float ux=dx/len,uy=dy/len;
            line(c,x2,y2,x2-ux*15-uy*9,y2-uy*15+ux*9,col,sw);
            line(c,x2,y2,x2-ux*15+uy*9,y2-uy*15-ux*9,col,sw);
        }
        private void strokeCircle(Canvas c,float x,float y,float r,int col,float sw){
            p.setColor(col);p.setStrokeWidth(sw);p.setStyle(Paint.Style.STROKE);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);
        }
        private void ellipse(Canvas c,float x,float y,float rx,float ry,int col,float sw){
            p.setColor(col);p.setStrokeWidth(sw);p.setStyle(Paint.Style.STROKE);
            c.drawOval(new RectF(x-rx,y-ry,x+rx,y+ry),p);p.setStyle(Paint.Style.FILL);
        }
        @Override protected void onDraw(Canvas actual){
            super.onDraw(actual);int i=selected;if(i<0)return;
            actual.save();actual.scale(getWidth()/1000f,getHeight()/440f);
            Canvas c=actual;box(c,0,0,1000,440,CARD);
            for(int k=1;k<10;k++)line(c,k*100,10,k*100,430,Color.rgb(26,49,71),1);
            for(int k=1;k<5;k++)line(c,10,k*88,990,k*88,Color.rgb(26,49,71),1);
            double x=a(),y=b(), t=(SystemClock.uptimeMillis()-clock)/1000.0;
            switch(i){
                case 0:{ // BK7 prism: blue and red diverging rays
                    Path prism=new Path();prism.moveTo(490,90);prism.lineTo(340,360);prism.lineTo(640,360);prism.close();
                    p.setColor(Color.argb(100,93,164,213));c.drawPath(prism,p);
                    p.setColor(CYAN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);c.drawPath(prism,p);p.setStyle(Paint.Style.FILL);
                    line(c,70,218,421,218,GOLD,6);
                    double angle=rad(x),dB=2*Math.asin(bk7(450)*Math.sin(angle/2))-angle,
                        dR=2*Math.asin(bk7(650)*Math.sin(angle/2))-angle;
                    float rY=(float)(185+160*Math.tan(dR)),bY=(float)(185+160*Math.tan(dB));
                    line(c,475,205,745,260,WHITE,4);line(c,745,260,880,rY,ORANGE,5);line(c,745,260,880,bY,CYAN,5);
                    line(c,896,75,896,400,MUTED,3);caption(c,"BK7",470,305,WHITE,26);caption(c,"RED",902,Math.min(420,rY),ORANGE,18);caption(c,"BLUE",902,Math.min(425,bY),CYAN,18);break;
                }
                case 1:{ // Foucault: floor precession of oscillation plane
                    ellipse(c,500,345,300,70,MUTED,2);circle(c,500,76,12,GOLD);
                    double orientation=rad(15.041067*Math.sin(rad(x))*y);
                    double swing=Math.sin(t*2.1)*160;float endX=(float)(500+Math.cos(orientation)*swing),endY=(float)(310+Math.sin(orientation)*swing*.42);
                    line(c,500,76,endX,endY,CYAN,5);circle(c,endX,endY,23,GOLD);
                    float ax=(float)(500+Math.cos(orientation)*250),ay=(float)(345+Math.sin(orientation)*50);
                    float bx=(float)(500-Math.cos(orientation)*250),by=(float)(345-Math.sin(orientation)*50);
                    line(c,ax,ay,bx,by,ORANGE,5);caption(c,"EARTH",60,75,MUTED,22);break;
                }
                case 2:{ // Coulomb: charges and force vectors
                    float apart=(float)(180+240*y/2);float left=500-apart/2,right=500+apart/2;
                    double f=x*secondChargeMicroCoulombs;
                    int col1=x<0?ORANGE:x>0?CYAN:MUTED;
                    int col2=secondChargeMicroCoulombs<0?ORANGE:secondChargeMicroCoulombs>0?CYAN:MUTED;
                    circle(c,left,215,46,col1);circle(c,right,215,46,col2);
                    caption(c,x<0?"−":x>0?"+":"0",left-14,225,BG,32);
                    caption(c,secondChargeMicroCoulombs<0?"−":secondChargeMicroCoulombs>0?"+":"0",right-14,225,BG,32);
                    line(c,left,300,right,300,MUTED,3);
                    if(f!=0){
                        float direction=f<0?1:-1;
                        float length=(float)(50+Math.min(70,Math.abs(f)*8/y));
                        arrow(c,left+direction*52,215,left+direction*(52+length),215,GOLD,6);
                        arrow(c,right-direction*52,215,right-direction*(52+length),215,GOLD,6);
                    }
                    caption(c,f<0?"ATTRACTION":f>0?"REPULSION":"ZERO FORCE",365,385,WHITE,27);
                    caption(c,"q1 "+n(x,1)+" uC",left-80,128,col1,22);
                    caption(c,"q2 "+n(secondChargeMicroCoulombs,1)+" uC",right-80,128,col2,22);break;
                }
                case 3:{ // orbital speed relative to ideal circle
                    circle(c,500,217,70,CYAN);strokeCircle(c,500,217,185,MUTED,3);
                    double vc=Math.sqrt(3.986004418e14/((6371+x)*1000));float radius=185;
                    float px=500+radius,py=217;circle(c,px,py,14,GOLD);float vector=(float)(y*28);
                    line(c,px,py,px,py-vector,ORANGE,7);
                    caption(c,"EARTH",456,225,BG,18);caption(c,"v = "+n(y,1)+" km/s",665,70,GOLD,22);
                    caption(c,"vc = "+n(vc/1000,1)+" km/s",85,385,WHITE,24);break;
                }
                case 4:{ // interference intensity at screen; sinusoidal fringes
                    box(c,110,125,35,220,Color.rgb(50,85,122));circle(c,127,185,8,GOLD);circle(c,127,282,8,GOLD);
                    line(c,145,185,795,115,CYAN,2);line(c,145,282,795,335,CYAN,2);
                    box(c,790,40,62,360,Color.rgb(31,54,81));
                    double period=x*1e-9*2/(y*1e-3),scale=Math.max(8,period*18000);
                    for(int k=0;k<350;k+=3){
                        double pos=k-175,phase=Math.PI*pos/scale;
                        double intensity=Math.pow(Math.cos(phase),2);
                        line(c,790,42+k,852,42+k,Color.rgb((int)(24+200*intensity),(int)(28+165*intensity),(int)(42+210*intensity)),3);
                    }
                    caption(c,"2 SLITS",76,85,WHITE,22);caption(c,"SCREEN",765,430,WHITE,20);break;
                }
                case 5:{ // Archimedes: floating fraction depends on density
                    box(c,300,95,400,315,Color.rgb(42,64,84));box(c,320,166,360,223,Color.rgb(32,119,164));
                    double ratio=y/x;float objY=(float)(ratio<=1?166+80*(1-ratio):320);
                    box(c,458,objY,95,98,ORANGE);
                    line(c,505,objY+90,505,objY+30,GOLD,5);line(c,505,objY+5,505,objY-60,CYAN,5);
                    caption(c,"BUOYANCY",575,220,CYAN,22);caption(c,"WEIGHT",575,330,GOLD,22);
                    caption(c,ratio<1?"FLOAT":"SINK",85,190,WHITE,30);break;
                }
                case 6:{ // Faraday sinusoidal flux and emf (quadrature)
                    for(int n=0;n<7;n++)ellipse(c,240+n*11,225,36,100,CYAN,3);
                    box(c,90,179,70,90,ORANGE);caption(c,"N",110,225,WHITE,25);
                    line(c,375,225,940,225,MUTED,2);line(c,420,95,420,345,MUTED,2);
                    Path wave=new Path();Path flux=new Path();
                    for(int xx=420;xx<=940;xx+=4){
                        float phi=(float)((xx-420)/520.0*2*Math.PI*y);
                        float eyy=(float)(225-110*Math.sin(phi)),byy=(float)(225-95*Math.cos(phi));
                        if(xx==420){wave.moveTo(xx,eyy);flux.moveTo(xx,byy);}
                        else{wave.lineTo(xx,eyy);flux.lineTo(xx,byy);}
                    }p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(GOLD);c.drawPath(wave,p);
                    p.setColor(CYAN);p.setStrokeWidth(2);c.drawPath(flux,p);p.setStyle(Paint.Style.FILL);
                    caption(c,"EMF",640,65,GOLD,24);break;
                }
                case 7:{ // Ohm circuit
                    line(c,190,115,790,115,CYAN,6);line(c,790,115,790,330,CYAN,6);
                    line(c,790,330,190,330,CYAN,6);line(c,190,330,190,115,CYAN,6);
                    box(c,160,178,60,90,GOLD);caption(c,"+",175,208,BG,27);
                    box(c,670,92,98,47,ORANGE);caption(c,"R",706,125,BG,26);
                    double amp=x/y;float brightness=(float)Math.min(1,amp/1.0);
                    strokeCircle(c,480,330,46,WHITE,5);
                    circle(c,480,330,12,Color.rgb((int)(80+175*brightness),(int)(72+155*brightness),56));
                    caption(c,"I = "+n(amp,2)+" A",377,63,WHITE,26);break;
                }
                case 8:{ // Kepler ellipse with focus star
                    float rx=290,ry=(float)(290*Math.sqrt(1-y*y)*.7);
                    ellipse(c,500,228,rx,ry,CYAN,4);
                    float sun=500-(float)(rx*y);circle(c,sun,228,28,GOLD);circle(c,790,228,12,ORANGE);
                    line(c,sun,228,790,228,GOLD,2);caption(c,"SUN",sun-24,185,WHITE,19);
                    caption(c,"a = "+n(x,2)+" AU",350,65,WHITE,25);break;
                }
                case 9:{ // Doppler wavefronts compressed ahead for approach
                    float sx=(float)(500+Math.sin(t*.5)*40),spacingAhead=(float)(45*(343-y)/343),spacingBack=(float)(45*(343+y)/343);
                    circle(c,sx,220,24,ORANGE);
                    for(int j=1;j<=5;j++){strokeCircle(c,sx+j*spacingAhead,220,17+j*6,CYAN,3);
                        strokeCircle(c,sx-j*spacingBack,220,17+j*6,GOLD,3);}
                    caption(c,"LISTENER",775,95,WHITE,20);
                    box(c,820,190,28,70,MUTED);break;
                }
                case 10:{ // pendulum
                    float px=500,py=65;double period=2*Math.PI*Math.sqrt(x/y);
                    double theta=.34*Math.sin(t*2*Math.PI/period);
                    float len=250,xx=px+(float)(len*Math.sin(theta)),yy=py+(float)(len*Math.cos(theta));
                    line(c,px,py,xx,yy,CYAN,5);circle(c,xx,yy,28,ORANGE);
                    line(c,px,py,px,py+len,MUTED,2);circle(c,px,py,13,GOLD);
                    caption(c,"T = "+n(period,2)+" s",65,380,WHITE,26);break;
                }
                case 11:{ // photoelectric ejection threshold
                    box(c,555,105,34,235,MUTED);caption(c,"METAL",520,390,WHITE,22);
                    for(int k=0;k<4;k++){
                        float yy=140+k*58;line(c,155,yy-25,536,yy,CYAN,5);circle(c,510,yy,12,GOLD);
                    }
                    double ke=1239.841984/x-y;
                    if(ke>0){for(int k=0;k<3;k++){circle(c,650+k*80,148+k*75,11,ORANGE);
                        line(c,600+k*80,148+k*75,650+k*80,148+k*75,ORANGE,3);}}
                    else caption(c,"NO ELECTRONS",644,210,ORANGE,25);
                    caption(c,"hν = "+n(1239.841984/x,2)+" eV",90,80,GOLD,22);break;
                }
            }
            actual.restore();
            // Only animate experiments whose picture changes with time.
            if(i==1||i==9||i==10)postInvalidateDelayed(80);
        }
    }
}

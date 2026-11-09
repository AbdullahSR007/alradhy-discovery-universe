package com.alradhy.discoverylab;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/**
 * ALRADHY Discovery Fusion - integrated native Fizeau laboratory.
 * No network, storage access, root, services, or dependencies required.
 */
public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(7, 15, 29);
    private static final int PANEL = Color.rgb(16, 31, 54);
    private static final int GOLD = Color.rgb(244, 191, 91);
    private static final int TEXT = Color.rgb(240, 246, 255);
    private static final int MUTED = Color.rgb(169, 192, 215);

    // Educational model: 720 teeth, 8,633 m one-way path, vacuum speed of light.
    private static final double LIGHT_SPEED = 299_792_458.0;
    private int distanceMeters = 8_633;
    private static final int TEETH = 720;
    private double firstExtinctionRpm() { return (60.0 * LIGHT_SPEED) / (4.0 * TEETH * distanceMeters); }

    private final String[] chapterNames = {
        "السؤال", "غاليليو", "رومر", "فيزو", "اليوم"
    };
    private final String[] chapterYears = {
        "كيف نقيس شيئًا سريعًا جدًا؟", "القرن السابع عشر", "1676", "1849", "القياس الحديث"
    };
    private final String[] chapterBody = {
        "هل الضوء يصل فورًا؟\n\nسننتقل عبر ثلاثة قرون من الأسئلة والقياسات. يمكنك مشاهدة القصة أو الدخول إلى التجربة مباشرةً.",
        "حاول غاليليو استخدام الفوانيس لمعرفة زمن انتقال الضوء. لكن زمن استجابة البشر كان أكبر بكثير من التأخير المراد قياسه.\n\nالدرس: نحتاج مسافة كبيرة أو وسيلة توقيت أدق.",
        "لاحظ أولي رومر اختلاف مواعيد ظواهر قمر آيو حول المشتري مع تغيّر المسافة بين الأرض والمشتري.\n\nالدرس: السماء يمكن أن تكون مختبرًا للزمن.",
        "استخدم هيبوليت فيزو عجلةً مسننة وشعاع ضوء ومرآة بعيدة. عندما تعود الأشعة، قد تصادف سِنًّا بدل الفتحة التي مرّت منها.\n\nالدرس: نستنتج زمن الرحلة من سرعة دوران العجلة.",
        "القيمة المعرّفة لسرعة الضوء في الفراغ اليوم: 299,792,458 مترًا في الثانية.\n\nهذا النموذج التعليمي يستخدم هذه القيمة، ولا يمثل الجهاز التاريخي بكامل تفاصيله."
    };
    private int page = 0; // 0 home, 1 story, 2 Fizeau, 3 Galileo, 4 Roemer
    private int galileoDistanceMeters = 1000;
    private int observerReactionMs = 250;
    private boolean galileoFired = false;
    private long galileoPulseStart = 0L;
    private int romerEarthPosition = 0;
    private boolean romerObserved = false;
    private GalileoView galileoView;
    private RoemerView roemerView;
    private static final double AU_METERS = 149_597_870_700.0;
    private int chapter = 0;
    private int rpm = 350;
    private TextView readout;
    private LightLabView labView;
    private LinearLayout column;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (state != null) {
            page = state.getInt("page", 0);
            chapter = state.getInt("chapter", 0);
            rpm = state.getInt("rpm", 350);
            distanceMeters = state.getInt("distanceMeters", 8633);
            galileoDistanceMeters = state.getInt("galileoDistanceMeters", 1000);
            observerReactionMs = state.getInt("observerReactionMs", 250);
            romerEarthPosition = state.getInt("romerEarthPosition", 0);
            galileoFired = state.getBoolean("galileoFired", false);
            romerObserved = state.getBoolean("romerObserved", false);
        }
        if (state == null && getIntent() != null) {
            String entry = getIntent().getStringExtra("experiment");
            if ("galileo".equals(entry)) page = 3;
            else if ("romer".equals(entry)) page = 4;
            else if ("fizeau".equals(entry)) page = 2;
        }
        render();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("page", page);
        out.putInt("chapter", chapter);
        out.putInt("rpm", rpm);
        out.putInt("distanceMeters", distanceMeters);
        out.putInt("galileoDistanceMeters", galileoDistanceMeters);
        out.putInt("observerReactionMs", observerReactionMs);
        out.putInt("romerEarthPosition", romerEarthPosition);
        out.putBoolean("galileoFired", galileoFired);
        out.putBoolean("romerObserved", romerObserved);
        super.onSaveInstanceState(out);
    }
    private int dp(float v) { return (int)(getResources().getDisplayMetrics().density * v + .5f); }
    private GradientDrawable shape(int color, int border) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        d.setStroke(dp(1), border);
        return d;
    }
    private TextView label(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setLineSpacing(dp(3), 1.08f);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }
    private void addText(String text, int sp, int color, boolean bold, int bottom) {
        TextView t = label(text, sp, color, bold);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(bottom);
        column.addView(t, p);
    }
    private Button button(String text, Runnable action) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextSize(17);
        b.setTextColor(TEXT);
        b.setBackground(shape(PANEL, Color.rgb(60, 93, 124)));
        b.setPadding(dp(18), dp(9), dp(18), dp(9));
        b.setMinimumHeight(dp(56));
        b.setOnClickListener(v -> action.run());
        b.setOnFocusChangeListener((v, hasFocus) -> {
            b.setBackground(shape(hasFocus ? Color.rgb(102, 70, 24) : PANEL,
                                  hasFocus ? GOLD : Color.rgb(60, 93, 124)));
            b.setTextColor(hasFocus ? Color.WHITE : TEXT);
            b.setScaleX(hasFocus ? 1.035f : 1f);
            b.setScaleY(hasFocus ? 1.035f : 1f);
        });
        return b;
    }
    private void addButton(String text, Runnable action) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(62));
        lp.bottomMargin = dp(12);
        column.addView(button(text, action), lp);
    }
    private void rowButtons(String[] titles, Runnable[] actions) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        for (int i=0; i<titles.length; i++) {
            Button b = button(titles[i], actions[i]);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(62), 1);
            p.leftMargin = dp(5);
            p.rightMargin = dp(5);
            row.addView(b, p);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(18);
        column.addView(row, lp);
    }
    private void render() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{BG, Color.rgb(13, 33, 57), BG}));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        readout = null;
        labView = null;
        galileoView = null;
        roemerView = null;
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(52), dp(30), dp(52), dp(34));
        scroll.addView(column);
        container.addView(scroll);
        setContentView(container);
        addText("✦  ALRADHY DISCOVERY FUSION  /  مختبر الضوء", 16, GOLD, true, 14);
        if (page == 0) home();
        else if (page == 1) story();
        else if (page == 2) lab();
        else if (page == 3) galileoLab();
        else if (page == 4) roemerLab();
        else home();
    }
    private void home() {
        addText("اكتشف كيف قاس الإنسان سرعة الضوء", 34, TEXT, true, 16);
        addText("حكاية سينمائية قصيرة، ثم مختبر تفاعلي حقيقي — مصمم للريموت والماوس ويعمل دون إنترنت.",
                21, MUTED, false, 26);
        addCard("ثلاث تجارب علمية مختلفة فعلًا",
                "غاليليو: قِس زمن الاستجابة البشرية بالفوانيس. رومر: غيّر مسافة الأرض عن المشتري وتتبّع تأخر خسوف آيو. فيزو: غيّر سرعة العجلة والمسافة وشاهد إطفاء الشعاع. كل مختبر له مشهد وحسابات وتحكم مختلف.");
        rowButtons(new String[]{"ابدأ رحلة الاكتشاف", "ادخل مختبر فيزو"}, new Runnable[]{
            () -> {page=1;chapter=0;render();},
            () -> {page=2;render();}
        });
        rowButtons(new String[]{"غاليليو • الفوانيس", "رومر • قمر آيو", "فيزو • العجلة"}, new Runnable[]{
            () -> {page=3;render();},
            () -> {page=4;render();},
            () -> {page=2;render();}
        });
        addText("تنقل بالأسهم، واضغط OK للاختيار. استخدم الماوس للنقر أو التمرير.",
                16, MUTED, false, 0);
    }
    private void addCard(String heading, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setBackground(shape(PANEL, Color.rgb(39, 73, 108)));
        TextView title = label(heading, 25, GOLD, true);
        TextView copy = label(body, 19, TEXT, false);
        card.addView(title);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(-1, -2);
        textP.topMargin=dp(12);
        card.addView(copy,textP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(24);
        column.addView(card,lp);
    }
    private void story() {
        addText("آلة الزمن  /  "+chapterYears[chapter],17,GOLD,true,10);
        addText(chapterNames[chapter],34,TEXT,true,18);
        String[] names = new String[chapterNames.length];
        Runnable[] acts = new Runnable[chapterNames.length];
        for(int i=0;i<names.length;i++) {
            final int n=i;
            names[i]=(chapter==i ? "● " : "")+chapterNames[i];
            acts[i]=()->{chapter=n;render();};
        }
        rowButtons(names,acts);
        addCard("المشهد "+(chapter+1)+" / "+chapterNames.length, chapterBody[chapter]);
        rowButtons(new String[]{"السابق", "التالي", "المختبر"},new Runnable[]{
            () -> {chapter=(chapter+chapterNames.length-1)%chapterNames.length;render();},
            () -> {chapter=(chapter+1)%chapterNames.length;render();},
            () -> {page=(chapter==1 ? 3 : chapter==2 ? 4 : 2);render();}
        });
        addButton("الصفحة الرئيسية", ()->{page=0;render();});
    }

    // ==================== Galileo: two-lantern timing ====================
    private void galileoLab() {
        addText("المختبر ١ / غاليليو", 17, GOLD, true, 8);
        addText("هل يمكن قياس سرعة الضوء بفانوسين؟", 31, TEXT, true, 10);
        addText("غيّر المسافة بين الراصدين وزمن استجابة الإنسان. أشعل الفانوس وراقب الفرق بين زمن انتقال الضوء والتأخير الذي يسببه الإنسان. هذه محاكاة تعليمية لتجربة تاريخية.", 18, MUTED, false, 15);
        galileoView = new GalileoView();
        galileoView.setBackground(shape(PANEL, Color.rgb(62, 89, 120)));
        galileoView.setFocusable(true);
        galileoView.setOnKeyListener((v, code, ev) -> {
            if (ev.getAction()!=KeyEvent.ACTION_DOWN) return false;
            if (code==KeyEvent.KEYCODE_DPAD_RIGHT) { changeGalileoDistance(500);return true; }
            if (code==KeyEvent.KEYCODE_DPAD_LEFT) { changeGalileoDistance(-500);return true; }
            return false;
        });
        LinearLayout.LayoutParams canvasP = new LinearLayout.LayoutParams(-1, dp(230));
        canvasP.bottomMargin = dp(14);
        column.addView(galileoView, canvasP);
        readout = label("", 19, TEXT, true);
        readout.setBackground(shape(PANEL, Color.rgb(43,70,99)));
        readout.setPadding(dp(18),dp(12),dp(18),dp(12));
        column.addView(readout, new LinearLayout.LayoutParams(-1,-2));
        updateGalileo();
        rowButtons(new String[]{"المسافة −500 م", "المسافة +500 م", "استجابة −50 ms", "استجابة +50 ms"}, new Runnable[]{
            () -> changeGalileoDistance(-500),
            () -> changeGalileoDistance(500),
            () -> {observerReactionMs=Math.max(50,observerReactionMs-50);updateGalileo();},
            () -> {observerReactionMs=Math.min(600,observerReactionMs+50);updateGalileo();}
        });
        rowButtons(new String[]{"أشعل الفانوس وسجّل الزمن", "إعادة التجربة", "المشهد التاريخي"}, new Runnable[]{
            () -> {galileoFired=true;galileoPulseStart=SystemClock.uptimeMillis();updateGalileo();},
            () -> {galileoFired=false;galileoDistanceMeters=1000;observerReactionMs=250;updateGalileo();},
            () -> {page=1;chapter=1;render();}
        });
        addText("سؤال التجربة: هل يمكن فصل تأخر الضوء، الذي يُقاس بالميكروثواني، عن استجابة الإنسان التي تُقاس بالميلي ثانية؟ جرّب أكثر من مسافة.", 16, MUTED, false, 0);
    }
    private void changeGalileoDistance(int delta) {
        galileoDistanceMeters=Math.max(500,Math.min(10000,galileoDistanceMeters+delta));
        galileoFired=false;
        updateGalileo();
    }
    private double galileoMicroseconds() { return 2.0*galileoDistanceMeters/LIGHT_SPEED*1e6; }
    private void updateGalileo() {
        if (readout != null) {
            String first = "المسافة بين الراصدين: "+galileoDistanceMeters+" متر  •  زمن استجابة الراصد: "+observerReactionMs+" ملّي ثانية";
            String second = "زمن الضوء ذهابًا وإيابًا: "+String.format(Locale.US,"%.2f",galileoMicroseconds())+" ميكروثانية";
            String third = galileoFired ?
                "قراءة التجربة التقريبية: "+String.format(Locale.US,"%.4f",observerReactionMs+galileoMicroseconds()/1000.0)+" ملّي ثانية\nالنتيجة: التأخر البشري يغطي تقريبًا تأخر الضوء." :
                "اضغط «أشعل الفانوس» ثم قارن بين زمن الضوء وزمن استجابة الإنسان.";
            readout.setText(first+"\n"+second+"\n"+third);
        }
        if (galileoView != null) galileoView.invalidate();
    }

    // ==================== Rømer: orbital-delay eclipse lab ====================
    private void roemerLab() {
        addText("المختبر ٢ / أولي رومر • 1676", 17, GOLD, true, 8);
        addText("قمر آيو: الساعة الطبيعية في السماء", 31, TEXT, true, 10);
        addText("غيّر موضع الأرض بالنسبة إلى المشتري. الزمن المتوقع لخسوف قمر آيو لا يتغير في هذا النموذج، لكن الضوء يصلنا متأخرًا كلما زادت المسافة. الفارق هنا نسبي بين موضعين، وليس وقت حدوث الخسوف الحقيقي.", 18, MUTED, false, 15);
        roemerView = new RoemerView();
        roemerView.setFocusable(true);
        roemerView.setBackground(shape(PANEL, Color.rgb(62,89,120)));
        roemerView.setOnKeyListener((v,code,ev)->{
            if(ev.getAction()!=KeyEvent.ACTION_DOWN) return false;
            if(code==KeyEvent.KEYCODE_DPAD_RIGHT){changeEarthPosition(1);return true;}
            if(code==KeyEvent.KEYCODE_DPAD_LEFT){changeEarthPosition(-1);return true;}
            return false;
        });
        LinearLayout.LayoutParams canvasP = new LinearLayout.LayoutParams(-1, dp(230));
        canvasP.bottomMargin=dp(14);
        column.addView(roemerView,canvasP);
        readout=label("",19,TEXT,true);
        readout.setBackground(shape(PANEL,Color.rgb(43,70,99)));
        readout.setPadding(dp(18),dp(12),dp(18),dp(12));
        column.addView(readout,new LinearLayout.LayoutParams(-1,-2));
        updateRoemer();
        rowButtons(new String[]{"الأرض أقرب −", "الأرض أبعد +", "أقرب موضع", "أبعد موضع"}, new Runnable[]{
            () -> changeEarthPosition(-1),
            () -> changeEarthPosition(1),
            () -> {romerEarthPosition=0;romerObserved=false;updateRoemer();},
            () -> {romerEarthPosition=8;romerObserved=false;updateRoemer();}
        });
        rowButtons(new String[]{"ارصد خسوف آيو", "أعد الحساب", "المشهد التاريخي"}, new Runnable[]{
            () -> {romerObserved=true;updateRoemer();},
            () -> {romerEarthPosition=0;romerObserved=false;updateRoemer();},
            () -> {page=1;chapter=2;render();}
        });
        addText("استخدم رومر تراكم اختلافات مواعيد خسوف آيو عبر مدارات متعددة لاستنتاج أن سرعة الضوء محدودة. استخدم النموذج فرق مسافة أقصاه وحدتان فلكيتان للتوضيح.", 16, MUTED, false, 0);
    }
    private void changeEarthPosition(int delta) {
        romerEarthPosition=Math.max(0,Math.min(8,romerEarthPosition+delta));
        romerObserved=false;
        updateRoemer();
    }
    private double romerExtraMeters() { return (2.0*AU_METERS*romerEarthPosition)/8.0; }
    private double romerExtraSeconds() { return romerExtraMeters()/LIGHT_SPEED; }
    private void updateRoemer() {
        if(readout!=null){
            double seconds=romerExtraSeconds();
            double au=romerExtraMeters()/AU_METERS;
            String lead="موضع الأرض: "+romerEarthPosition+" من 8  •  فرق طول المسار: "+String.format(Locale.US,"%.2f",au)+" AU";
            String delay="تأخر الضوء النظري: "+String.format(Locale.US,"%.1f",seconds)+" ثانية (≈ "+String.format(Locale.US,"%.2f",seconds/60.0)+" دقيقة)";
            String result=romerObserved ? "رصدتَ الخسوف! عند هذا الموضع، تصل إشارته متأخرةً بنحو "+String.format(Locale.US,"%.1f",seconds)+" ثانية قياسًا بالموضع القريب." :
                "اضغط «ارصد خسوف آيو» لقراءة الفرق بين الموضعين.";
            readout.setText(lead+"\n"+delay+"\n"+result);
        }
        if(roemerView!=null)roemerView.invalidate();
    }

    private final class GalileoView extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        GalileoView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight(),y=h*.65f;
            if(w<=0||h<=0)return;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(24,46,67));
            c.drawRect(0,y+dp(15),w,h,p);
            float ax=w*.17f,bx=w*.83f;
            p.setColor(Color.rgb(104,151,184));
            c.drawRect(ax-dp(16),y-dp(2),ax+dp(16),y+dp(40),p);
            c.drawRect(bx-dp(16),y-dp(2),bx+dp(16),y+dp(40),p);
            p.setColor(GOLD);
            c.drawCircle(ax,y-dp(12),dp(13),p);
            p.setColor(Color.rgb(101,224,255));
            c.drawCircle(bx,y-dp(12),dp(13),p);
            p.setStrokeWidth(dp(2));p.setColor(Color.rgb(68,98,127));
            c.drawLine(ax,y-dp(12),bx,y-dp(12),p);
            if(galileoFired){
                double motion=((SystemClock.uptimeMillis()-galileoPulseStart)%1800L)/1800.0;
                float pos=(float)(motion<.5 ? motion*2 : (1-motion)*2);
                p.setColor(motion<.5?GOLD:Color.rgb(115,233,252));
                c.drawCircle(ax+(bx-ax)*pos,y-dp(12),dp(8),p);
                postInvalidateDelayed(35);
            }
            p.setTextSize(dp(17));p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            p.setTextAlign(Paint.Align.CENTER);p.setColor(TEXT);
            c.drawText("GALILEO",ax,y-dp(45),p);
            c.drawText("OBSERVER",bx,y-dp(45),p);
            p.setColor(GOLD);
            c.drawText(galileoDistanceMeters+" m",w*.5f,y-dp(35),p);
            p.setTextSize(dp(15));p.setColor(MUTED);
            c.drawText("LIGHT: "+String.format(Locale.US,"%.2f",galileoMicroseconds())+" microseconds",w*.5f,h*.86f,p);
        }
    }

    private final class RoemerView extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        RoemerView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight(); if(w<=0||h<=0)return;
            float jy=h*.50f,jx=w*.82f;
            float ex=w*(.43f-.034f*romerEarthPosition),ey=h*.50f;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));
            p.setColor(Color.rgb(56,88,121)); c.drawOval(w*.08f,h*.23f,w*.50f,h*.77f,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(201,146,103));c.drawCircle(jx,jy,dp(49),p);
            p.setColor(Color.rgb(234,197,128));c.drawCircle(jx+dp(72),jy-dp(16),dp(9),p);
            p.setColor(Color.rgb(59,150,224));c.drawCircle(ex,ey,dp(22),p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setColor(GOLD);
            c.drawLine(jx+dp(66),jy-dp(16),ex,ey,p);
            p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            p.setTextSize(dp(16));p.setColor(TEXT);
            c.drawText("EARTH",ex,ey+dp(48),p);
            c.drawText("JUPITER",jx,jy+dp(81),p);
            c.drawText("Io",jx+dp(73),jy-dp(37),p);
            p.setColor(GOLD);
            c.drawText(String.format(Locale.US,"+%.1f min",romerExtraSeconds()/60.0),w*.56f,h*.23f,p);
            if(romerObserved){
                p.setColor(Color.rgb(122,230,164));
                c.drawCircle(jx+dp(72),jy-dp(16),dp(12),p);
            }
        }
    }

    private void lab() {
        addText("مختبر فيزو  /  التجربة التفاعلية",17,GOLD,true,8);
        addText("هل تستطيع إطفاء الضوء بالعجلة؟",32,TEXT,true,8);
        addText("عدّل سرعة الدوران والمسافة ولاحظ تغيّر شدة الضوء العائد. هذا نموذج مبسط لعجلة ذات 720 سنًا، وليس إعادة تمثيل دقيقة لكل ظروف تجربة 1849.",
                18,MUTED,false,14);
        labView = new LightLabView();
        labView.setFocusable(true);
        labView.setBackground(shape(PANEL,Color.rgb(50,90,125)));
        labView.setOnKeyListener((v, code, event)-> {
            if(event.getAction()!=KeyEvent.ACTION_DOWN) return false;
            if(code==KeyEvent.KEYCODE_DPAD_RIGHT) {adjust(10);return true;}
            if(code==KeyEvent.KEYCODE_DPAD_LEFT) {adjust(-10);return true;}
            if(code==KeyEvent.KEYCODE_DPAD_UP) {adjust(100);return true;}
            if(code==KeyEvent.KEYCODE_DPAD_DOWN) {adjust(-100);return true;}
            return false;
        });
        LinearLayout.LayoutParams canvasP = new LinearLayout.LayoutParams(-1,dp(230));
        canvasP.bottomMargin=dp(18);
        column.addView(labView,canvasP);
        readout=label("",20,TEXT,true);
        readout.setBackground(shape(PANEL,Color.rgb(43,70,99)));
        readout.setPadding(dp(20),dp(13),dp(20),dp(13));
        LinearLayout.LayoutParams readP = new LinearLayout.LayoutParams(-1,-2);
        readP.bottomMargin=dp(14);
        column.addView(readout,readP);
        updateReadout();
        rowButtons(new String[]{"−100", "−10", "+10", "+100"},
            new Runnable[]{()->adjust(-100),()->adjust(-10),()->adjust(10),()->adjust(100)});
        rowButtons(new String[]{"المسافة −1000 م", "المسافة +1000 م", "ضبط المسافة"}, new Runnable[]{
            () -> {distanceMeters=Math.max(1000,distanceMeters-1000);updateReadout();},
            () -> {distanceMeters=Math.min(30000,distanceMeters+1000);updateReadout();},
            () -> {distanceMeters=8633;updateReadout();}
        });
        rowButtons(new String[]{"اذهب إلى أول انطفاء", "العودة إلى القصة"},
            new Runnable[]{()->{rpm=(int)Math.round(firstExtinctionRpm());updateReadout();},
                            ()->{page=1;chapter=3;render();}});
        addText("على رسم التجربة: يمين/يسار ±10، أعلى/أسفل ±100 دورة/دقيقة. "
                +"النتيجة محاكاة مثالية مبسطة، لا قياس فعلي أو إعادة بناء كاملة للجهاز التاريخي.",
                15,MUTED,false,0);
    }
    private void adjust(int delta) {
        rpm=Math.max(0, Math.min(1400, rpm+delta));
        updateReadout();
    }
    private double brightness() {
        double cycles = rpm / 60.0 * (2.0 * distanceMeters / LIGHT_SPEED) * TEETH;
        double f=cycles-Math.floor(cycles);
        return Math.max(0, 1.0-2.0*Math.min(f,1.0-f));
    }
    private void updateReadout() {
        if(readout!=null) {
            String b=String.format(Locale.US,"%.0f",100*brightness());
            readout.setText("المسافة: "+distanceMeters+" متر     •     سرعة العجلة: "+rpm+" دورة/دقيقة\nالضوء العائد: "+b+
                "%\nأول انطفاء نظري ≈ "+Math.round(firstExtinctionRpm())+
                " دورة/دقيقة     •     زمن الرحلة ذهابًا وإيابًا ≈ "+
                String.format(Locale.US,"%.2f",2*distanceMeters/LIGHT_SPEED*1e6)+" ميكروثانية");
        }
        if(labView!=null)labView.invalidate();
    }
    @Override public void onBackPressed() {
        if(page>=2){chapter=page==3 ? 1 : page==4 ? 2 : 3;page=1;render();}
        else if(page==1){page=0;render();}
        else super.onBackPressed();
    }
    private final class LightLabView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        LightLabView(){super(MainActivity.this);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int color,float thick){
            p.setShader(null);p.setColor(color);p.setStrokeWidth(thick);
            c.drawLine(x1,y1,x2,y2,p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), cy=h*0.53f;
            if(w<=0||h<=0)return;
            float wheelX=w*.38f,mirrorX=w*.84f,sourceX=w*.10f;
            line(c,sourceX,cy,wheelX,cy,GOLD,dp(3));
            line(c,wheelX,cy,mirrorX,cy,Color.rgb(243,183,63),dp(3));
            int alpha=(int)(40+215*brightness());
            line(c,mirrorX,cy+dp(9),wheelX,cy+dp(9),
                Color.argb(alpha,120,222,255),dp(3));
            p.setColor(Color.rgb(238,246,255));
            c.drawRoundRect(mirrorX,cy-dp(64),mirrorX+dp(9),cy+dp(64),dp(5),dp(5),p);
            p.setColor(GOLD);
            c.drawCircle(sourceX,cy,dp(12),p);
            c.save();
            c.rotate((float)((SystemClock.uptimeMillis()/1000.0)*rpm*.65)%360,wheelX,cy);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(7));p.setColor(Color.rgb(117,175,207));
            c.drawCircle(wheelX,cy,dp(45),p);
            for(int i=0;i<24;i++) {
                double a=i*Math.PI*2/24;
                float a1x=wheelX+(float)Math.cos(a)*dp(38);
                float a1y=cy+(float)Math.sin(a)*dp(38);
                float a2x=wheelX+(float)Math.cos(a)*dp(55);
                float a2y=cy+(float)Math.sin(a)*dp(55);
                line(c,a1x,a1y,a2x,a2y,Color.rgb(175,207,224),dp(6));
            }
            c.restore();
            p.setStyle(Paint.Style.FILL);
            p.setColor(GOLD);p.setTextSize(dp(16));p.setTextAlign(Paint.Align.CENTER);
            c.drawText("LIGHT",sourceX,cy-dp(29),p);
            c.drawText("720",wheelX,cy-dp(67),p);
            c.drawText("MIRROR",mirrorX,cy-dp(80),p);
            c.drawText(String.format(Locale.US,"%.0f%% RETURN",brightness()*100),
                w*.64f,cy+dp(50),p);
            postInvalidateDelayed(50);
        }
    }
}
#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT="$HOME/alradhy-discovery-universe/ALRADHY-World-Window-MS"
REPO="$HOME/alradhy-discovery-universe"

cd "$PROJECT"

echo "1/8 Creating directories..."

mkdir -p app/src/main/java/com/alradhy/worldwindow
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/drawable
mkdir -p gradle/wrapper
mkdir -p "$REPO/.github/workflows"

echo "2/8 Fixing settings.gradle..."

cat > settings.gradle <<'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ALRADHY-World-Window-MS"
include(":app")
EOF

echo "3/8 Fixing root build.gradle..."

cat > build.gradle <<'EOF'
plugins {
    id 'com.android.application' version '8.9.2' apply false
    id 'org.jetbrains.kotlin.android' version '2.0.21' apply false
}
EOF

cat > gradle.properties <<'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
EOF

cat > gradle/wrapper/gradle-wrapper.properties <<'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

echo "4/8 Fixing app/build.gradle..."

cat > app/build.gradle <<'EOF'
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'com.alradhy.worldwindow'
    compileSdk 35

    defaultConfig {
        applicationId 'com.alradhy.worldwindow'
        minSdk 26
        targetSdk 35
        versionCode 1
        versionName '0.1.0'
    }

    buildTypes {
        release {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.media3:media3-exoplayer:1.7.1'
    implementation 'androidx.media3:media3-ui:1.7.1'
}
EOF

echo "5/8 Fixing AndroidManifest.xml..."

cat > app/src/main/AndroidManifest.xml <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <uses-feature
        android:name="android.software.leanback"
        android:required="false" />

    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:allowBackup="true"
        android:banner="@drawable/tv_banner"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.WorldWindow">

        <activity
            android:name=".PlayerActivity"
            android:configChanges="keyboard|keyboardHidden|navigation|orientation|screenSize"
            android:screenOrientation="landscape"
            android:exported="false" />

        <activity
            android:name=".MainActivity"
            android:configChanges="keyboard|keyboardHidden|navigation|orientation|screenSize"
            android:screenOrientation="landscape"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

        </activity>
    </application>

</manifest>
EOF

echo "6/8 Creating working Kotlin files..."

cat > app/src/main/java/com/alradhy/worldwindow/MainActivity.kt <<'EOF'
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
EOF

cat > app/src/main/java/com/alradhy/worldwindow/PlayerActivity.kt <<'EOF'
package com.alradhy.worldwindow

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : Activity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        playerView = PlayerView(this).apply {
            useController = true
            controllerShowTimeoutMs = 5000
        }

        root.addView(
            playerView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val title = intent.getStringExtra("title") ?: "World Window"
        val subtitle = intent.getStringExtra("subtitle") ?: ""

        val titleView = TextView(this).apply {
            text = "$title\n$subtitle"
            textSize = 22f
            setTextColor(Color.WHITE)
            setShadowLayer(8f, 2f, 2f, Color.BLACK)
            setPadding(dp(32), dp(22), dp(32), dp(22))
        }

        root.addView(
            titleView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            )
        )

        setContentView(root)

        val url = intent.getStringExtra("url")
        if (!url.isNullOrBlank()) {
            player = ExoPlayer.Builder(this).build().also {
                playerView.player = it
                it.setMediaItem(MediaItem.fromUri(url))
                it.prepare()
                it.playWhenReady = true
            }
        }
    }

    override fun onStop() {
        player?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
EOF

echo "7/8 Creating resources..."

cat > app/src/main/res/values/strings.xml <<'EOF'
<resources>
    <string name="app_name">ALRADHY World Window</string>
</resources>
EOF

cat > app/src/main/res/values/colors.xml <<'EOF'
<resources>
    <color name="world_background">#040D17</color>
    <color name="world_accent">#49CFBE</color>
    <color name="white">#FFFFFF</color>
</resources>
EOF

cat > app/src/main/res/values/themes.xml <<'EOF'
<resources>
    <style name="Theme.WorldWindow" parent="@android:style/Theme.Material.NoActionBar">
        <item name="android:fontFamily">sans</item>
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowActionModeOverlay">true</item>
        <item name="android:colorAccent">#49CFBE</item>
        <item name="android:navigationBarColor">#040D17</item>
        <item name="android:windowLightNavigationBar">false</item>
    </style>
</resources>
EOF

cat > app/src/main/res/drawable/ic_launcher.xml <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#071925" android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#49CFBE"
        android:pathData="M54,18a36,36 0,1 0,0 72a36,36 0,1 0,0 -72M54,25c7,0 13,11 15,25H39c2,-14 8,-25 15,-25M32,30c-3,6 -5,13 -6,20H18c1,-8 6,-15 14,-20M76,30c8,5 13,12 14,20H82c-1,-7 -3,-14 -6,-20M18,58h8c1,7 3,14 6,20c-8,-5 -13,-12 -14,-20M39,58h30c-2,14 -8,25 -15,25s-13,-11 -15,-25M82,58h8c-1,8 -6,15 -14,20c3,-6 5,-13 6,-20"/>
</vector>
EOF

cat > app/src/main/res/drawable/tv_banner.xml <<'EOF'
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:angle="0"
        android:startColor="#071925"
        android:endColor="#126F73" />
    <corners android:radius="8dp" />
</shape>
EOF

rm -f app/src/main/res/layout/activity_main.xml
rmdir app/src/main/res/layout 2>/dev/null || true

echo "8/8 Creating GitHub Actions workflow in repository root..."

cat > "$REPO/.github/workflows/world-window-apk.yml" <<'EOF'
name: ALRADHY World Window APK

on:
  workflow_dispatch:
  push:
    branches:
      - main
    paths:
      - 'ALRADHY-World-Window-MS/**'
      - '.github/workflows/world-window-apk.yml'

jobs:
  build:
    runs-on: ubuntu-latest

    permissions:
      contents: read

    defaults:
      run:
        working-directory: ALRADHY-World-Window-MS

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Install Android SDK 35
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"

      - name: Set up Gradle 8.11.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.11.1'

      - name: Build debug APK
        run: gradle :app:assembleDebug --no-daemon --stacktrace

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: ALRADHY-World-Window-MS-V0.1.0-debug
          path: ALRADHY-World-Window-MS/app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
          retention-days: 30
EOF

cd "$REPO"

echo
echo "Checking generated files..."

test -f ALRADHY-World-Window-MS/settings.gradle
test -f ALRADHY-World-Window-MS/build.gradle
test -f ALRADHY-World-Window-MS/app/build.gradle
test -f ALRADHY-World-Window-MS/app/src/main/AndroidManifest.xml
test -f ALRADHY-World-Window-MS/app/src/main/java/com/alradhy/worldwindow/MainActivity.kt
test -f ALRADHY-World-Window-MS/app/src/main/java/com/alradhy/worldwindow/PlayerActivity.kt
test -f .github/workflows/world-window-apk.yml

echo
echo "All required files were created successfully."

git add ALRADHY-World-Window-MS .github/workflows/world-window-apk.yml
git commit -m "Fix and build ALRADHY World Window Android TV app" || true
git push origin main

echo
echo "=============================================="
echo "DONE: Project repaired and pushed to GitHub."
echo "Open GitHub Actions: ALRADHY World Window APK"
echo "=============================================="

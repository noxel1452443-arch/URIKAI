import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

/** Google 公式サンプル（デバッグ／ID 未設定時）。本番 ID は local.properties へ。 */
private val defaultAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
private val defaultAdmobInterstitialId = "ca-app-pub-3940256099942544/1033173712"
private val defaultAdmobRewardedId = "ca-app-pub-3940256099942544/5224354917"

val admobAppId = localProperties.getProperty("admob.app.id")?.takeIf { it.isNotBlank() }
    ?: defaultAdmobAppId
val admobInterstitialId =
    localProperties.getProperty("admob.interstitial.id")?.takeIf { it.isNotBlank() }
        ?: defaultAdmobInterstitialId
val admobRewardedId =
    localProperties.getProperty("admob.rewarded.id")?.takeIf { it.isNotBlank() }
        ?: defaultAdmobRewardedId

android {
    namespace = "com.urikai.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.urikai.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.7.4"
        // Play Console の商品 ID（作成後は変更不可）。未作成でも AAB アップロードは可能。
        buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", "\"urikai_remove_ads\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$admobInterstitialId\"")
        buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$admobRewardedId\"")
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))

    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("com.google.android.gms:play-services-ads:24.4.0")
}

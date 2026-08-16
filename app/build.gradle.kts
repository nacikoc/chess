import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// İmzalama bilgileri depoya girmez: kök dizindeki keystore.properties'ten
// okunur (bkz. RELEASING.md). Dosya yoksa release derlemesi imzasız kalır,
// debug derlemesi etkilenmez.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
val hasSigningConfig = keystoreProps.getProperty("storeFile") != null

// ABI split'leri yalnızca APK derlemelerinde. AAB zaten cihaz başına doğru
// ABI'yi kendisi ayırır; ikisi birlikte açıkken bundle görevi
// "Sequence contains more than one matching element" ile patlıyor.
val buildingBundle = gradle.startParameter.taskNames.any {
    it.contains("bundle", ignoreCase = true)
}

android {
    namespace = "com.nakitasarim.chess"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nakitasarim.chess"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
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
            if (hasSigningConfig) {
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
    splits {
        abi {
            isEnable = !buildingBundle
            reset()
            // x86_64: emülatör ve Chromebook. Stockfish binary'si yok, BuiltInEngine devreye girer.
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = false
        }
    }
    packaging {
        jniLibs {
            // Stockfish calistirilabilir dosyasinin diske cikarilmasi icin gerekli
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.chesslib)
}

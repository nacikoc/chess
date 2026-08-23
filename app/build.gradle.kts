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
    namespace = "com.hilspot.chess"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hilspot.chess"
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

// Stockfish binary'leri depoda tutulmuyor (bkz. .gitignore). Eksik olduklarinda
// uygulama COKMEZ -- ChessAi.kt sessizce cok daha zayif BuiltInEngine'e duser.
// Yani hatasiz, imzali ama motoru sakat bir yayin paketi uretilebilir ve bu
// ancak oynayinca fark edilir. Yayin derlemesini bu yuzden bilerek durduruyoruz.
// x86_64 muaf: o ABI icin zaten binary paketlenmiyor, BuiltInEngine tasarim geregi.
// Kontrol bilerek yapilandirma zamaninda, ayri bir Gradle gorevi olarak degil:
// Kotlin DSL'de doLast lambdasi betik nesnesini ortuk yakaliyor ve bu
// configuration cache ile serilestirilemiyor. Burada yapinca hem o sorun yok,
// hem de derleme hic baslamadan aninda hata veriyor.
val stockfishAbis = listOf("arm64-v8a", "armeabi-v7a")
val buildingRelease = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true) || it.contains("bundle", ignoreCase = true)
}
if (buildingRelease) {
    val missing = stockfishAbis
        .map { abi -> layout.projectDirectory.file("src/main/jniLibs/$abi/libstockfish.so").asFile }
        .filter { !it.exists() || it.length() == 0L }
    if (missing.isNotEmpty()) {
        throw GradleException(
            buildString {
                appendLine("Stockfish binary'leri eksik:")
                missing.forEach { appendLine("  - " + it.name + "  (" + it.parentFile.name + ")") }
                appendLine()
                appendLine("Bunlar depoda tutulmuyor (bkz. .gitignore). Uretmek icin:")
                appendLine("    bash tools/build-stockfish.sh")
                appendLine()
                appendLine("Bu kontrol olmasaydi derleme BASARILI olurdu, ama uygulama")
                appendLine("guclu motor yerine cok daha zayif BuiltInEngine ile yayinlanirdi")
                appendLine("-- ChessAi.kt eksik binary'de sessizce ona duser.")
            }
        )
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
    implementation(libs.billing)
    // 16 KB sayfa boyutu uyumluluğu için sürüm yükseltmesi (bkz. libs.versions.toml)
    implementation(libs.androidx.graphics.path)
}

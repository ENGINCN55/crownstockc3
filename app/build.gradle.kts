// NOT: Doküman (Bölüm 36, Faz 0) yalnızca "Jetpack Compose + Kotlin", "Hilt DI
// modülleri" ve "Firebase / Firestore bağlantısı" gerektiğini belirtiyor; kesin
// kütüphane sürümlerini belirtmiyor. Aşağıdaki sürümler bu yazının hazırlandığı
// dönemde birbiriyle uyumlu bilinen sürümlerdir — projeye eklemeden önce
// güncel/uyumlu sürümleri (Compose BOM, Hilt, AGP, Kotlin) doğrulayın.

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services") // Firebase yapılandırması (google-services.json) için
    id("kotlin-kapt")
}

android {
    namespace = "com.company.crownstock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.company.crownstock"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    // NOT: Kotlin 1.9.24 kullanıldığından (Kotlin 2.0'daki yeni Compose Compiler
    // Gradle eklentisi yerine) composeOptions.kotlinCompilerExtensionVersion
    // AÇIKÇA belirtilmelidir — aksi halde Android Studio'da Gradle Sync
    // "Compose Compiler ve Kotlin sürümü uyumsuz" hatasıyla başarısız olur.
    // Kotlin 1.9.24 ↔ Compose Compiler 1.5.14 resmi JetBrains uyumluluk
    // eşlemesidir; Kotlin sürümünü değiştirirseniz bu değeri de güncelleyin.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // NOT: AGP 8.5.0, Gradle 8.7 ve derleme için JDK 17 gerektirir (resmi Android
    // Gradle Plugin uyumluluk tablosu). kotlinOptions.jvmTarget=17 ile compileOptions
    // arasında sürüm tutarsızlığı olursa "Inconsistent JVM-target compatibility"
    // derleme hatası oluşur — bu yüzden ikisi de 17'ye sabitlendi.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    // Kaynak kod app/src/main/kotlin altında (app/src/main/java yerine).
    sourceSets {
        getByName("main") {
            kotlin.srcDirs("src/main/kotlin")
            res.srcDirs("src/main/res")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
        getByName("test") {
            kotlin.srcDirs("src/test/kotlin")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Jetpack Compose (Bölüm 36, Faz 0)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Navigation-Compose (Bölüm 12)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Material Components (XML temaları için)
    implementation("com.google.android.material:material:1.12.0")

    // Hilt (Bölüm 36, Faz 0 — \"Hilt DI modülleri\")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // Firebase / Firestore (Bölüm 26)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Coroutines - Task.await() için
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // Test (Bölüm 34, öneri 8 — hesaplama çekirdeğinin birim testleri)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

// KÖK build.gradle.kts (proje düzeyi).
// NOT: Önceki commit'te tek bir build.gradle.kts dosyası "app modülü" olarak
// paylaşılmıştı; standart Android çok-modüllü Gradle yapısında bu dosyanın
// app/build.gradle.kts altında olması gerekir (bkz. app/build.gradle.kts —
// içerik aynı, yalnızca konumu düzeltildi). Bu dosya ise KÖK projedir ve
// yalnızca plugin sürümlerini bildirir, hiçbir bağımlılık içermez.
//
// GRADLE SÜRÜMÜ: AGP 8.5.0, minimum Gradle 8.7 gerektirir (resmi AGP↔Gradle
// uyumluluk tablosu). gradle/wrapper/gradle-wrapper.properties bu nedenle
// Gradle 8.7'ye sabitlendi; derleme için JDK 17 gerekir (bkz. app/build.gradle.kts
// compileOptions/kotlinOptions).

plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

# gradle-wrapper.jar eksik — bu klasöre elle eklenmesi gerekiyor

`gradlew` ve `gradlew.bat` betikleri, bu klasördeki (`gradle/wrapper/`)
**`gradle-wrapper.jar`** adlı derlenmiş (binary) bir Java sınıf arşivini çalıştırarak
çalışır. Bu proje bir metin/kod üretim ortamında hazırlandığından, derlenmiş
bir `.jar` dosyası **üretilemez** — ayrıca bu ortamın ağ erişimi
`services.gradle.org` ve GitHub'a kapalı olduğu için resmi dosya indirilip
eklenemedi de.

`gradle-wrapper.properties` (Gradle 8.7) ve `gradlew` / `gradlew.bat` betikleri
doğru ve eksiksizdir — yalnızca bu tek ikili dosya eksiktir.

## Android Studio kullanıyorsanız (önerilen)

Projeyi **File → Open** ile bu klasörü seçerek açtığınızda, Android Studio
eksik `gradle-wrapper.jar`'ı genellikle otomatik tespit eder ve bildirim
çubuğunda "Gradle wrapper is missing" / benzeri bir uyarıyla birlikte
otomatik düzeltme seçeneği sunar (kendi paketlenmiş Gradle dağıtımını
kullanarak). Sync başarısız olursa:

- **File → Sync Project with Gradle Files**'ı tekrar deneyin, veya
- **Terminal** sekmesinden (Android Studio içi terminal) aşağıdaki komutu çalıştırın:

```bash
gradle wrapper --gradle-version 8.7
```

Android Studio'nun kendi bünyesindeki Gradle bu komutu internete ek bir
bağımlılık olmadan çalıştırabilir (yalnızca Gradle 8.7 dağıtımı henüz
makinenizde/AS önbelleğinde yoksa ilk seferde indirilir).

## Terminalden / CI'dan kullanıyorsanız

Herhangi bir Gradle kurulu makinede proje kök dizininde:

```bash
gradle wrapper --gradle-version 8.7
```

Bu komut `gradle-wrapper.jar`'ı oluşturur; mevcut `gradle-wrapper.properties`
ve `gradlew` / `gradlew.bat` dosyalarınız aynı içerikle korunur.


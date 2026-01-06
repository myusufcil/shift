# iOS Widget Setup Guide

Bu rehber, ShiftWidget'ı Xcode projesine ekleme adımlarını içerir.

## Adım 1: Widget Extension Target Ekleme

1. Xcode'da `iosApp.xcworkspace` dosyasını aç
2. Proje navigatöründe `iosApp` projesini seç
3. Sol alt köşedeki `+` butonuna tıkla
4. "Widget Extension" seç ve Next'e tıkla
5. Product Name: `ShiftWidget`
6. Bundle Identifier: `com.cil.shift.ShiftWidget`
7. "Include Configuration Intent" seçeneğini **KAPAT**
8. Finish'e tıkla
9. "Activate ShiftWidget scheme?" sorusuna **Activate** de

## Adım 2: Mevcut Dosyaları Kullan

Xcode'un otomatik oluşturduğu dosyaları sil ve bu klasördeki dosyaları kullan:

1. Xcode'un oluşturduğu `ShiftWidget` klasöründeki dosyaları sil
2. Bu klasördeki dosyaları ShiftWidget grubuna sürükle:
   - `ShiftWidget.swift`
   - `HabitEntry.swift`
   - `HabitProvider.swift`
   - `Info.plist`
   - `Assets.xcassets`

## Adım 3: App Groups Ayarı (Data Sharing için)

Widget'ın ana uygulamayla veri paylaşabilmesi için:

1. Proje ayarlarında `iosApp` target'ını seç
2. "Signing & Capabilities" tabına git
3. "+ Capability" butonuna tıkla
4. "App Groups" ekle
5. Yeni grup oluştur: `group.com.cil.shift`

6. Aynı işlemi `ShiftWidget` extension target'ı için de yap

## Adım 4: SQLite Framework Ekleme (Widget için)

1. `ShiftWidget` target'ını seç
2. "Build Phases" tabına git
3. "Link Binary With Libraries" bölümüne `libsqlite3.tbd` ekle

## Adım 5: Deployment Target Ayarı

1. `ShiftWidget` target'ını seç
2. "Build Settings" tabına git
3. iOS Deployment Target: `17.0` (veya ana uygulamayla aynı)

## Adım 6: Build ve Test

1. Scheme'i `iosApp` olarak değiştir
2. Build yap (Cmd + B)
3. Simulator veya cihazda çalıştır
4. Ana ekranda widget ekle: uzun bas -> Edit Home Screen -> + butonu -> Shift Widget

## Troubleshooting

### Widget veri göstermiyor
- App Groups düzgün yapılandırıldığından emin ol
- Ana uygulamayı bir kez aç ve en az bir habit oluştur
- Widget'ı kaldırıp tekrar ekle

### Build hatası
- Swift version kontrolü: Widget ve ana uygulama aynı Swift versiyonunu kullanmalı
- SQLite framework eklendiğinden emin ol

## Database Path Notları

Widget, veritabanını şu sırayla arar:
1. App Group container: `group.com.cil.shift/shift_v4.db`
2. Documents directory: `~/Documents/shift_v4.db`
3. Library directory: `~/Library/shift_v4.db`

Ana uygulamanın veritabanını App Group container'a taşıması için
KMP tarafında iOS database driver güncellemesi gerekebilir.

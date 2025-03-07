# HubCore

## Sunucunuz İçin Profesyonel Lobi Çözümü

![HubCore Banner](https://imgur.com/a/LlRD7BW)

**Sürüm:** 1.0.0  
**MC Sürümleri:** 1.21  
**Bağımlılıklar:** ProtocolLib  
**Geliştirici:** Cengiz1

---

## 📋 Hakkında

HubCore, Minecraft sunucunuzun lobisini profesyonel ve etkileyici hale getirmek için tasarlanmış kapsamlı bir lobi yönetim sistemidir. Çoklu lobi desteği, sunucu seçicisi, NPC sistemi, özel efektler ve daha birçok özelliği ile sunucunuzu bir üst seviyeye taşır.

## ✨ Özellikler

### 🌐 Çoklu Lobi Sistemi
- **Sınırsız lobi desteği** - Ana lobi, VIP lobi ve daha fazlası için destek
- **Özel izinler** - Her lobi için özel erişim izinleri
- **Maksimum oyuncu limitleri** - Her lobi için ayrı oyuncu kapasitesi
- **Özelleştirilebilir spawn noktaları** - Her lobi için ayrı spawn noktası

### 🧭 Sunucu Seçici
- **Dinamik sunucu listesi** - Aktif/pasif durumlar otomatik kontrol edilir
- **Özelleştirilebilir arayüz** - Sunucu görünümlerini tamamen özelleştirin
- **Oyuncu sayısı gösterimi** - Her sunucunun anlık oyuncu sayısını görün
- **Direkt bağlantı** - Tek tıkla diğer sunuculara geçiş yapın

### 👤 NPC Sistemi
- **Özelleştirilebilir NPC'ler** - İsim, görünüm ve komutlar
- **Hologram desteği** - NPC'ler için özel hologram metinleri
- **Skin sistemi** - İstediğiniz skini kullanabilme
- **Etkileşim komutları** - NPC'ye tıklandığında komut çalıştırma

### 📢 Duyuru Sistemi
- **Otomatik duyurular** - Belirli aralıklarla otomatik duyurular
- **Komut ile duyuru** - Anında duyuru yapma
- **Özelleştirilebilir format** - Duyuru görünümlerini özelleştirme
- **Ses efektleri** - Duyurular için ses efektleri

### 🛡️ Lobi Koruması
- **Blok kırma/koyma koruması** - Lobi alanında tahribatı engelleme
- **PvP koruması** - Oyuncular arası savaşı engelleme
- **Açlık koruması** - Açlık çubuğunun azalmasını engelleme
- **Hava durumu kontrolü** - Yağmur ve fırtına engelleme

### 🏆 VIP Sistemi
- **Özel giriş mesajları** - VIP oyuncular için özel giriş mesajları
- **Özel efektler** - VIP oyuncular için parçacık efektleri
- **Özel lobiler** - Sadece VIP'lerin erişebildiği alanlar

### 🎯 Ek Özellikler
- **Çift zıplama** - Lobi alanında çift zıplama
- **Parçacık efektleri** - Oyuncular için özel parçacık efektleri
- **Sohbet kilitleme** - Gerektiğinde sohbeti kilitleme
- **Oyuncu gizleme** - Diğer oyuncuları gösterme/gizleme
- **Özelleştirilebilir skorboard** - Dinamik skorboard bilgileri
- **MySQL desteği** - İstatistik ve veri saklamak için

## 📝 Komutlar

| Komut | Açıklama | İzin |
|-------|----------|------|
| `/lobby [id]` | Belirtilen lobiye ışınlanma | Yok |
| `/setlobby [id]` | Lobi spawn noktası belirleme | `multihub.setlobby` |
| `/hub` | Ana lobiye ışınlanma | Yok |
| `/spawn` | Şu anki lobinin spawn noktasına ışınlanma | Yok |
| `/multihubreload` | Eklentiyi yeniden yükleme | `hubcore.reload` |
| `/chatlock` | Sohbeti kilitleme/açma | `hubcore.chatlock` |
| `/announce <mesaj>` | Duyuru yapma | `hubcore.announce` |
| `/announce next` | Sıradaki duyuruyu gösterme | `hubcore.announce` |
| `/npc create <id> [isim]` | Yeni NPC oluşturma | `hubcore.npc` |
| `/npc remove <id>` | NPC silme | `hubcore.npc` |
| `/npc list` | Tüm NPC'leri listeleme | `hubcore.npc` |
| `/npc setskin <id> <skin>` | NPC skin'i ayarlama | `hubcore.npc` |
| `/npc setcommand <id> <komut>` | NPC komutunu ayarlama | `hubcore.npc` |
| `/npc hologram <id> <add/remove/clear> [text]` | NPC hologramını düzenleme | `hubcore.npc` |

## ⚙️ Yapılandırma

HubCore, tamamen özelleştirilebilir bir yapılandırma dosyası ile gelir. Tüm özellikler `config.yml` dosyasından ayarlanabilir:

- 🌐 **Lobi ayarları** - Özelleştirilebilir lobiler ve spawn noktaları
- 🧭 **Sunucu seçici ayarları** - Özelleştirilebilir sunucu listesi ve bağlantıları
- 📢 **Duyuru ayarları** - Otomatik duyurular ve formatlar
- 🛡️ **Koruma ayarları** - Lobi koruma özellikleri
- 👤 **NPC ayarları** - NPC özelleştirmeleri
- 📊 **Skorboard ayarları** - Özelleştirilebilir skorboard
- 🏆 **VIP ayarları** - VIP özellikleri ve mesajlar
- 💾 **MySQL ayarları** - Veritabanı bağlantı ayarları

## 💻 Kurulum

1. ProtocolLib eklentisini sunucunuza yükleyin
2. HubCore.jar dosyasını plugins klasörüne yerleştirin
3. Sunucuyu başlatın veya yeniden başlatın
4. `config.yml` dosyasını ihtiyaçlarınıza göre düzenleyin
5. `/multihubreload` komutu ile eklentiyi yeniden yükleyin

## 🔧 API

Geliştiriciler için HubCore API'si mevcuttur. API sayesinde:

- NPC'leri programlı olarak yönetebilir
- Lobi teleportlarını kontrol edebilir
- Sunucu bağlantılarını yönetebilir
- Ve daha fazlasını yapabilirsiniz

## 📚 Destek

Sorularınız ve sorunlarınız için:

- [Discord Sunucumuz](https://discord.gg/GJTX4usK)

---


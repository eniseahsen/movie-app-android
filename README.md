<p align="center">
  <img src="screenshots/logo_beyaz.png" alt="Movie App Banner" width="390" height="290"/>
</p>


# Movie App - Android Uygulaması

Kotlin diliyle geliştirilen bu Android uygulaması, kullanıcıların popüler filmleri keşfetmesine, detaylarını incelemesine ve favorilerine eklemesine olanak tanır. TMDB (The Movie Database) API'si kullanılarak dinamik veri çekimi gerçekleştirilmiştir.

## Özellikler
- Popüler filmleri listeleme
- Arama yaparak film keşfetme
- Favorilere film ekleme
- Veri yenileme (Swipe to Refresh)
- TMDB API ile canlı veri kullanımı


## Kullanılan Teknolojiler

| Teknoloji        | Açıklama                          |
|------------------|-----------------------------------|
| Kotlin           | Modern Android uygulama dili      |
| Android Jetpack  | RecyclerView, ViewModel, Drawer   |
| Retrofit         | REST API tüketimi için            |
| Glide            | Film görselleri için görsel yükleyici |
| Firebase Auth    | Google ve e-mail ile giriş sistemi |
| Firestore Database | 	Kullanıcı bilgileri (favoriler vb.) gibi dinamik verileri saklamak için |
| XML              | Android UI bileşenlerini tanımlamak için kullanılan biçim |
| Cloudinary       | 	Kullanıcı profil fotoğraflarını yüklemek ve barındırmak için bulut tabanlı medya yönetim servisi|



## Ekran Görüntüleri



<table>
  <tr>
    <th>Giriş Ekranı</th>
    <th>Ana Sayfa</th>
    <th>Profil Sayfası</th>
    <th>Navigation Bar</th>
  </tr>
  <tr>
    <td><img src="screenshots/login.jpeg" width="200"/></td>
    <td><img src="screenshots/home.jpeg" width="200"/></td>
    <td><img src="screenshots/profile.jpeg" width="200"/></td>
    <td><img src="screenshots/navbar.jpeg" width="200"/></td>
  </tr>
</table>

<br/>

<table>
  <tr>
    <th>Film Detay Sayfası</th>
    <th>Oyuncu Listesi</th>
    <th>Profil Düzenleme</th>
    <th>Favori Filmler</th>
  </tr>
  <tr>
    <td><img src="screenshots/filmdetail.jpeg" width="200"/></td>
    <td><img src="screenshots/cast.jpeg" width="200"/></td>
    <td><img src="screenshots/editprofile.jpeg" width="200"/></td>
    <td><img src="screenshots/favorites.jpeg" width="200"/></td>
  </tr>
</table>


## Kurulum

1. Bu repoyu klonlayın:

```bash
git clone https://github.com/eniseahsen/movie-app-android
```

2. Klonladığınız dizine gidin:
   
```bash
cd movie-app-android
```

3. Android Studio ile projeyi açın:
Android Studio'yu açın → Open seçeneğine tıklayın → movie-app-android klasörünü seçin

4. local.properties dosyanıza TMDB API anahtarınızı girin:
TMDB_API_KEY= sizin_api_keyiniz

5. Projeyi senkronize edin ve çalıştırın:
 Android Studio'da Sync Now butonuna tıklayın ve ardından uygulamayı emülatörde veya gerçek cihazda çalıştırın.








package eu.tutorials.filmsite.network

import eu.tutorials.filmsite.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Retrofit kütüphanesini kullanarak TMDBApi interface’inin gerçek bir “implementasyonunu” (yani çalışan halini) oluşturur.



object RetrofitClient{ //singelton object uygulama boyunca tek bir Retrofit örneği
    val api: TMDBApi by lazy { //ilk kullanıldığında başlatılır
        Retrofit.Builder()  //retrofit nesnesi oluşturulur
            .baseUrl(Constants.BASE_URL) //Constants.kt
            .addConverterFactory(GsonConverterFactory.create()) //json cevapları kotlin nesnesine çeviriliyor
            .build()
            .create(TMDBApi::class.java)
    }
}
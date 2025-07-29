package eu.tutorials.filmsite.network


import eu.tutorials.filmsite.Constants
import eu.tutorials.filmsite.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

//RetrofitClient ise Retrofit'i yapılandırıp, bu arayüzün gerçek işleyen halini oluşturarak çağrı yapmayı sağlar.

interface TMDBApi{  //Retrofit'in “nasıl çağrı yapılacağını” tarif ettiği bir arayüz
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = Constants.TMDB_API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse


    @GET("search/movie") //get isteği yaparak query parametresini gönderiyor
    suspend fun searchMovies( //API çağrıları suspend olduğu için coroutine içinde çalışır.
        @Query("api_key") apiKey: String = Constants.TMDB_API_KEY,
        @Query("query") query: String,
        @Query("language") language: String ="en-US"
    ): MovieResponse





}
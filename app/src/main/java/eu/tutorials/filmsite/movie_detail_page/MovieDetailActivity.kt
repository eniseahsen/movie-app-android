package eu.tutorials.filmsite.movie_detail_page

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.filmsite.favorites_page.FavoritesFragment
import eu.tutorials.filmsite.profile_page.ProfileActivity
import eu.tutorials.filmsite.R
import eu.tutorials.filmsite.adapter.CastAdapter
import eu.tutorials.filmsite.login_page.MainKayitOl
import eu.tutorials.filmsite.model.Movie
import eu.tutorials.filmsite.movies_page.MoviesActivity
import eu.tutorials.filmsite.network.MovieApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private var userEmail: String? = null

    @SuppressLint("MissingInflatedId") //uyarı vermesini önlemek için
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)


        drawerLayout = findViewById(R.id.drawer)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)


        setSupportActionBar(toolbar) //başlıkta logo olması için
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_logo_only)
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        //Diğer Activity'den (MoviesActivity) putExtra("movie", movie) ile gönderilen veriyi alır
        val movie = intent.getSerializableExtra("movie") as? Movie //ekranda gösterilecek filmin verisi (seriaizable nesne) ve null da olabilir

        val title = findViewById<TextView>(R.id.detailTitle)
        val date = findViewById<TextView>(R.id.detailDate)
        val image = findViewById<ImageView>(R.id.detailPoster)
        val overview = findViewById<TextView>(R.id.detailOverview)
        val castRecyclerView = findViewById<RecyclerView>(R.id.castRecyclerView)



        movie?.let { //movie null değilse
            title.text = it.title
            date.text = it.release_date
            overview.text = it.overview
            Glide.with(this).load("https://image.tmdb.org/t/p/w500${it.poster_path}").into(image)


            fetchCast(it.id, castRecyclerView)
            fetchTrailer(it.id)
        }




        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer2, FavoritesFragment())
                        .addToBackStack(null) //o anki fragment durumu backstack'e eklenir ve geri tuşuna basılınca bir önceki fragmenta döner
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_home -> {
                    val intent = Intent(this, MoviesActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_logout -> {

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, MainKayitOl::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //önceki görevdeki aktiviteleri siler böylece kullanıcı geri tuşuna bastığında çıkış yaptığı ekrana geri dönemez
                    intent.putExtra("isLogout", true) //MainKayitOl'a isLogout boolean değerini gönderir
                    startActivity(intent) //MainKayitOl başlatır

                    drawerLayout.closeDrawers()
                    true
                }

                else ->{
                    false
                }

            }

        }

    }
    private fun fetchCast(movieId: Int, recyclerView: RecyclerView){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create()) //json nesnelerini kotlin nesnesine dönüştürüyor
            .build()

        val api = retrofit.create(MovieApiService::class.java)


        //recyclerview ögelerini yatay bir şekilde sıralar
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        //filmin oyuncu kadrosunu (cast) APIden asenkron şekilde çekmek ve ardından veriyi RecyclerView'a bağlamak için
        lifecycleScope.launch{ //coroutine başlatmayı sağlar
            try{
                val response = withContext(Dispatchers.IO){ //Bu ağır işi arka planda yap, kullanıcı arayüzünü meşgul etme
                    api.getMovieCredits(movieId, "186da3962b15b4d215058c7f3309e708")

                }
                val castList = response.cast
                withContext(Dispatchers.Main){ //ana threadte çalışsın
                    recyclerView.adapter = CastAdapter(castList) { castMember ->
                        val dialogFragment = CastFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable("cast", castMember)
                            }
                        }
                        dialogFragment.show(supportFragmentManager,"cast_dialog")
                    }
                }

            }
            catch (e: Exception){
                e.printStackTrace()

            }
        }
    }

    private fun showTrailer(trailerKey: String){
        val trailerButton = findViewById<Button>(R.id.trailerButton)
        trailerButton.visibility =View.VISIBLE
        trailerButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$trailerKey"))
            startActivity(intent)
        }
    }


    private fun fetchTrailer(movieId: Int){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(MovieApiService::class.java)

        lifecycleScope.launch {
            try{
                val response = withContext(Dispatchers.IO){
                    api.getMovieVideos(movieId,"186da3962b15b4d215058c7f3309e708")
                }
                val trailers = response.results.filter { it.site == "YouTube" && it.type =="Trailer"}
                val trailerKey = trailers.firstOrNull()?.key //listenin ilk elemanını döner

                withContext(Dispatchers.Main){
                    if(trailerKey != null){
                        showTrailer(trailerKey)
                    }
                    else{

                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}

package eu.tutorials.filmsite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.filmsite.adapter.MovieAdapter
import eu.tutorials.filmsite.model.Movie
import eu.tutorials.filmsite.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoviesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieAdapter
    private lateinit var searchView: SearchView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var searchJob: Job? = null
    private var userEmail: String? = null
    private var favoriteMovieIDs = mutableSetOf<Int>()
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)

        db = FirebaseFirestore.getInstance()
        userEmail = intent.getStringExtra("email")

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchPopularMovies()
        }

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MovieAdapter(
            mutableListOf(),
            onItemClick = { movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }, false
        )

        recyclerView.adapter = adapter

        drawerLayout = findViewById(R.id.drawer)
        navigationView = findViewById(R.id.navigation_view)

        progressBar = findViewById(R.id.progressBar2)

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
                        .addToBackStack(null)
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
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("isLogout", true)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
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

        fetchPopularMovies()
        loadFavoriteMovies()
        setupSearch()
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (!newText.isNullOrEmpty() && newText.length >= 3) {
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        searchMovies(newText)
                    }
                } else if (newText.isNullOrEmpty()) {
                    fetchPopularMovies()
                }
                return true
            }
        })
    }

    private fun searchMovies(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchMovies(query = query)
                adapter.updateMovies(response.results)
            } catch (e: Exception) {
                Log.e("SEARCH_ERROR", e.message.toString())
            }
        }
    }

    private fun fetchPopularMovies() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val response = RetrofitClient.api.getPopularMovies()
                adapter.updateMovies(response.results)
            } catch (e: Exception) {
                Log.e("API_ERROR", e.message.toString())
            } finally {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadFavoriteMovies() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("favorites")
                .document(currentUser.uid)
                .collection("movies")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    favoriteMovieIDs.clear()
                    for (doc in querySnapshot.documents) {
                        val movieId = doc.getLong("id")?.toInt()
                        if (movieId != null) {
                            favoriteMovieIDs.add(movieId)
                        }
                    }
                    adapter.setFavoriteMovieIds(favoriteMovieIDs)
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Favori filmler yüklenirken hata: ${e.message}")
                }
        }
    }
}

package eu.tutorials.filmsite.favorites_page

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.filmsite.R
import eu.tutorials.filmsite.adapter.MovieAdapter
import eu.tutorials.filmsite.model.Movie
import eu.tutorials.filmsite.movie_detail_page.MovieDetailActivity

class FavoritesFragment : Fragment(R.layout.fragment_favorites){
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter: MovieAdapter
    private lateinit var db: FirebaseFirestore
    private val favoriteMovieIDs = mutableSetOf<Int>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private var favoritesMovies = mutableListOf<Movie>()


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false) //xml dosyasından view oluşturuluyor

        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout2)
        swipeRefreshLayout.setOnRefreshListener {
          loadFavoriteMovies()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) //Recyclerview dikey liste olarak ayarlanıyor
        adapter = MovieAdapter(mutableListOf(),
            { movie ->
            val intent = android.content.Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movie", movie) //MovieDetailActivity'ye
            startActivity(intent)

        },true)
        recyclerView.adapter = adapter
        //val favorite: ImageView = itemView.findViewById(R.id.favorite)











        db = FirebaseFirestore.getInstance()
        loadFavoriteMovies()


        return view



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity

        toolbar = view.findViewById(R.id.toolbar2)
        activity.setSupportActionBar(toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_logo_only)
        }



        drawerLayout = activity.findViewById(R.id.drawer)
        navigationView = activity.findViewById(R.id.navigation_view)

        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close

        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun loadFavoriteMovies() {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            db.collection("favorites")
                .document(user.uid)
                .collection("movies")
                .get()
                .addOnSuccessListener { result ->
                    val movies = mutableListOf<Movie>()
                    favoriteMovieIDs.clear()
                    for (document in result.documents){
                        val movie = document.toObject(Movie::class.java)
                        if (movie != null) {
                            movies.add(movie)
                            favoriteMovieIDs.add(movie.id)
                            favoritesMovies.add(movie)
                        }
                    }
                    adapter.updateMovies(movies)
                    adapter.setFavoriteMovieIds(favoriteMovieIDs)
                    swipeRefreshLayout.isRefreshing = false

                }
                .addOnFailureListener { e ->
                    Log.e("FavoritesFragment", "Firestore error: ${e.message}")
                    swipeRefreshLayout.isRefreshing = false

                }



        }
    }




}
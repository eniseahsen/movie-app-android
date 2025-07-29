package eu.tutorials.filmsite.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide //Poster resimlerini URL'den indirmek için
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.filmsite.R
import eu.tutorials.filmsite.model.Movie

class MovieAdapter(
    //constructor parametrleri
    private var movies: MutableList<Movie> = mutableListOf<Movie>(), //Movie bir dataclass(modelde)
    private val onItemClick: (Movie) -> Unit, //filme tıklandığında çağırılacak lambda fonksiyonu (detay sayfasına gidecek)
    private val isFavoritePage: Boolean


) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() { // RecyclerView.Adapter sınıfından miras alıyor




    private var favoriteMovieIds = mutableSetOf<Int>() // favori film idlerini tutar. set olarak tanımlandığı için aynı film tekrar eklenmez



    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //her recyclerview ögesini temsil eder. bir film kartı. ve tek bir kart içindeki tüm view bileşenlerini tutar
        val poster: ImageView = itemView.findViewById(R.id.ivPoster)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val progressVote: CircularProgressIndicator = itemView.findViewById(R.id.progressVote) //yuvarlak
        val voteAverage: TextView = itemView.findViewById(R.id.tvVoteAverage) //puan
        val favorite: ImageView = itemView.findViewById(R.id.favorite)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder { // her satır için item_movie.xml dosyasında View oluşturur
        val view = LayoutInflater.from(parent.context) //view nesnesi  //LayoutInflater, Android’de XML ile tanımlanmış bir layout dosyasını item_movie.xml bellekteki View nesnesine dönüştürmek için kullanılan bir sınıf
            .inflate(R.layout.item_movie, parent, false) //bağlamayı RecyclerView yapacak
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int = movies.size //kaç adet film ? film listesi kadar

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) { //film verilerini xml içindeki elemanlara yerleştirir (bağlamak)
        val movie = movies[position] //o pozisyondaki Movie nesnesini alır


        holder.title.text = movie.title //Film adı ve çıkış tarihi, ViewHolder içindeki TextView’lara atanıyor
        holder.date.text = movie.release_date


        Glide.with(holder.itemView).load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
            .into(holder.poster) //glide internetten resim indirme ve görüntüleme için kullanılan kütüphane

        holder.voteAverage.text = movie.vote_average.toString()

        val voteAverageFloat = movie.vote_average * 10
        val progressValue = voteAverageFloat.toInt() // ProgressBar için Int
        val displayValue = String.format("%.1f", voteAverageFloat) // Yazı için String

        holder.progressVote.progress = progressValue //CircularProgressIndicator bileşenine yüzdelik doluluk oranını verir
        holder.voteAverage.text = "$displayValue%"


        holder.itemView.setOnClickListener { //filme tıklanınca ...film detayı gelecek
            onItemClick(movie)
        }

        if (favoriteMovieIds.contains(movie.id)){
            holder.favorite.setImageResource(R.drawable.favorites_icon)

        }
        else {
            holder.favorite.setImageResource(R.drawable.favorites_icon_2)
        }



        holder.favorite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = FirebaseFirestore.getInstance()
                val favs = db.collection("favorites")
                    .document(user.uid)  //favorites koleksiyonu içinde, o anki kullanıcıya özel bir belge (document)
                    .collection("movies")
                    .document(movie.id.toString())

                favs.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        favs.delete().addOnSuccessListener {
                            favoriteMovieIds.remove(movie.id)

                            if (isFavoritePage) {
                                if(position >= 0 && position < movies.size){
                                    movies.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, movies.size - position)
                                }
                                else{
                                    notifyItemChanged(position)
                                }
                            } else {
                                notifyItemChanged(position)
                            }

                            Toast.makeText(holder.itemView.context, "Deleted from favorites", Toast.LENGTH_SHORT).show()
                        }
                    } else {  //film favori listesinde yoksa
                        val movieData = hashMapOf(
                            "id" to movie.id,
                            "title" to movie.title,
                            "poster_path" to movie.poster_path,
                            "release_date" to movie.release_date,
                            "vote_average" to movie.vote_average,
                            "userId" to user.uid,
                            "overview" to movie.overview
                        )
                        favs.set(movieData).addOnSuccessListener {
                            favoriteMovieIds.add(movie.id)
                            notifyItemChanged(position)
                            Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {

            }
        }

    }




    fun updateMovies(newMovies: List<Movie>) {
        this.movies = newMovies.toMutableList()
        notifyDataSetChanged()
    }

    fun setFavoriteMovieIds(ids: Set<Int>){
        favoriteMovieIds = ids.toMutableSet()
        notifyDataSetChanged()
    }


}
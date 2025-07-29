package eu.tutorials.filmsite.model

//TMDB API'sinden gelen JSON yanıtını Kotlin nesnesine dönüştürmek için

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    val total_pages: Int,
    val total_results: Int
)
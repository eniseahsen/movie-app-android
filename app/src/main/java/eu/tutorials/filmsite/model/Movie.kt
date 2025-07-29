package eu.tutorials.filmsite.model

import java.io.Serializable


data class Movie (
    val id: Int = 0,
    val title: String = "",
    val release_date: String = "",
    val poster_path: String = "",
    val vote_average: Double = 0.0,
    val overview: String = "",
    var userId: String? = null

) : Serializable  //nesne olarak başka bir yere aktarılabilmesini (taşınabilmesini) sağlar.
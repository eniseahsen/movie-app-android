package eu.tutorials.filmsite.model

import java.io.Serializable


data class CastMember(
    val name: String,
    val character: String,
    val profile_path: String

) : Serializable
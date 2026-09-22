package com.kkphim.model

data class Movie(
    val name: String,
    val slug: String,
    val origin_name: String = "",
    val content: String = "",
    val poster_url: String,
    val thumb_url: String,
    val year: Int,
    val episode_current: String? = "",
    val quality: String? = "",
    val lang: String? = ""
)

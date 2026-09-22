package com.kkphim.model

/**
 * Response của GET /phim/{slug} — không đổi cấu trúc so với API cũ (không có tiền tố v1/api).
 * Field được nới nullable/có default để tránh crash nếu API trả thiếu field.
 */
data class MovieDetailResponse(
    val status: Boolean = false,
    val msg: String? = null,
    val movie: MovieDetail = MovieDetail(),
    val episodes: List<EpisodeServer> = emptyList()
)

data class MovieDetail(
    val name: String = "",
    val slug: String = "",
    val origin_name: String = "",
    val content: String? = "",
    val poster_url: String = "",
    val thumb_url: String? = "",
    val year: Int = 0,
    val time: String? = null,
    val episode_current: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    val director: List<String> = emptyList(),
    val actor: List<String> = emptyList(),
    val category: List<Category> = emptyList(),
    val country: List<Category> = emptyList()
)

data class Category(
    val name: String = "",
    val slug: String = "",
    val id: String = ""
)

data class EpisodeServer(
    val server_name: String = "",
    val server_data: List<Episode> = emptyList()
)

data class Episode(
    val name: String = "",
    val slug: String = "",
    val filename: String? = "",
    val link_embed: String? = "",
    val link_m3u8: String = ""
)

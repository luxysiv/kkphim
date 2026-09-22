package com.kkphim.model

/**
 * Item phim thô trả về từ các API danh sách (home, the-loai, quoc-gia, tim-kiem, danh-sach/{type}).
 * Các endpoint không phải lúc nào cũng trả về đủ mọi field (vd: /v1/api/home không có thumb_url,
 * danh-sach/{type} không có episode_current/quality/lang) nên các field không chắc chắn được
 * khai báo nullable để tránh crash khi Gson deserialize (Gson bỏ qua default value của Kotlin).
 */
data class ApiMovieItem(
    val name: String = "",
    val slug: String = "",
    val origin_name: String? = "",
    val poster_url: String? = "",
    val thumb_url: String? = null,
    val year: Int = 0,
    val episode_current: String? = "",
    val quality: String? = "",
    val lang: String? = ""
)

data class Pagination(
    val totalItems: Int = 0,
    val totalItemsPerPage: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

data class ParamsWrapper(
    val pagination: Pagination = Pagination()
)

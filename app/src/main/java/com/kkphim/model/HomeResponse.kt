package com.kkphim.model

/**
 * Response của GET /v1/api/home
 * Toàn bộ danh sách nằm trong "data", ảnh (poster_url/thumb_url) là đường dẫn tương đối,
 * cần ghép với APP_DOMAIN_CDN_IMAGE để ra URL đầy đủ.
 */
data class HomeResponse(
    val data: HomeData = HomeData()
)

data class HomeData(
    val items: List<ApiMovieItem> = emptyList(),
    val params: ParamsWrapper = ParamsWrapper(),
    val APP_DOMAIN_CDN_IMAGE: String? = null
)

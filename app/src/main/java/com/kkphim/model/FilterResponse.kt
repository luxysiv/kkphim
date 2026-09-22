package com.kkphim.model

/**
 * Response chung cho các API v1/api/... trả kết quả dạng danh sách có bọc "data":
 * - GET /v1/api/the-loai/{slug}
 * - GET /v1/api/quoc-gia/{slug}
 * - GET /v1/api/nam/{year}
 * - GET /v1/api/tim-kiem?keyword=...
 * Ảnh có thể là URL tương đối hoặc tuyệt đối tùy endpoint, luôn xử lý qua toFullImageUrl().
 */
data class FilterResponse(
    val data: FilterData = FilterData()
)

data class FilterData(
    val items: List<ApiMovieItem> = emptyList(),
    val params: ParamsWrapper = ParamsWrapper(),
    val APP_DOMAIN_CDN_IMAGE: String? = null
)

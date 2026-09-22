package com.kkphim.api

import com.kkphim.model.HomeResponse
import com.kkphim.model.FilterResponse
import com.kkphim.model.MovieDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Trang chủ: phim mới cập nhật ---
    @GET("v1/api/home")
    suspend fun getHome(
        @Query("page") page: Int = 1
    ): HomeResponse

    // --- Chi tiết phim + danh sách tập ---
    @GET("phim/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): MovieDetailResponse

    // --- Tìm kiếm theo từ khóa ---
    @GET("v1/api/tim-kiem")
    suspend fun searchMovies(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): FilterResponse

    // --- Lọc theo Thể loại (ví dụ: hanh-dong, tinh-cam) ---
    @GET("v1/api/the-loai/{slug}")
    suspend fun getByCategory(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1
    ): FilterResponse

    // --- Lọc theo Quốc gia (ví dụ: han-quoc, au-my) ---
    @GET("v1/api/quoc-gia/{slug}")
    suspend fun getByCountry(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1
    ): FilterResponse

    // --- Lọc theo Loại phim: phim-le, phim-bo, hoat-hinh, tv-shows, phim-chieu-rap ---
    // Dùng bản v1/api (bọc trong "data") để có đầy đủ thông tin như the-loai/quoc-gia
    @GET("v1/api/danh-sach/{type}")
    suspend fun getByTypeList(
        @Path("type") type: String,
        @Query("page") page: Int = 1
    ): FilterResponse
}

package com.kkphim.utils

object TypeUtils {
    // API danh-sach/{type} chỉ hỗ trợ đúng 5 loại này
    val list = listOf(
        FilterItem("Phim Bộ", "phim-bo"),
        FilterItem("Phim Lẻ", "phim-le"),
        FilterItem("Hoạt Hình", "hoat-hinh"),
        FilterItem("TV Shows", "tv-shows"),
        FilterItem("Phim Chiếu Rạp", "phim-chieu-rap")
    )
}

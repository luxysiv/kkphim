package com.kkphim.utils

data class FilterItem(val name: String, val slug: String)

object CategoryUtils {
    val list = listOf(
        FilterItem("Hành Động", "hanh-dong"), FilterItem("Miền Tây", "mien-tay"),
        FilterItem("Trẻ Em", "tre-em"), FilterItem("Lịch Sử", "lich-su"),
        FilterItem("Cổ Trang", "co-trang"), FilterItem("Chiến Tranh", "chien-tranh"),
        FilterItem("Viễn Tưởng", "vien-tuong"), FilterItem("Kinh Dị", "kinh-di"),
        FilterItem("Tài Liệu", "tai-lieu"), FilterItem("Bí Ẩn", "bi-an"),
        FilterItem("Phim 18+", "phim-18"), FilterItem("Tình Cảm", "tinh-cam"),
        FilterItem("Tâm Lý", "tam-ly"), FilterItem("Thể Thao", "the-thao"),
        FilterItem("Phiêu Lưu", "phieu-luu"), FilterItem("Âm Nhạc", "am-nhac"),
        FilterItem("Gia Đình", "gia-dinh"), FilterItem("Học Đường", "hoc-duong"),
        FilterItem("Hài Hước", "hai-huoc"), FilterItem("Hình Sự", "hinh-su"),
        FilterItem("Võ Thuật", "vo-thuat"), FilterItem("Khoa Học", "khoa-hoc"),
        FilterItem("Thần Thoại", "than-thoai"), FilterItem("Chính Kịch", "chinh-kich"),
        FilterItem("Kinh Điển", "kinh-dien"), FilterItem("Phim Ngắn", "phim-ngan")
    )
}

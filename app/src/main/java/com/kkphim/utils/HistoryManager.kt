package com.kkphim.utils

import android.content.Context
import com.kkphim.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    
    // Model lưu trữ thông tin lịch sử
    data class HistoryItem(
        val movie: Movie,
        val serverIndex: Int,
        val serverName: String,
        val episodeIndex: Int,
        val episodeName: String,
        val episodeSlug: String, // Định danh tập phim (ví dụ: tap-01)
        val position: Long,
        val duration: Long = 0L
    )

    private const val PREF_NAME = "KKPhimHistory"
    private const val KEY_HISTORY = "history_data"

    /**
     * Lấy danh sách lịch sử từ SharedPreferences
     */
    fun getHistoryList(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return try { 
            Gson().fromJson(json, type) 
        } catch (e: Exception) { 
            emptyList() 
        }
    }

    /**
     * Lưu lịch sử xem phim
     * Cập nhật: Đã thêm tham số eSlug và sắp xếp lại để tránh lỗi Type mismatch
     */
    fun saveHistory(
        context: Context, 
        movie: Movie, 
        sIdx: Int, 
        sName: String, 
        eIdx: Int, 
        eName: String, 
        eSlug: String, // Thêm slug của tập phim vào đây
        pos: Long, 
        dur: Long
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = getHistoryList(context).toMutableList()
        
        // Loại bỏ phim này nếu đã tồn tại trong lịch sử trước đó (để đưa lên đầu)
        list.removeAll { it.movie.slug == movie.slug }
        
        // Thêm bản ghi mới nhất vào vị trí đầu tiên
        list.add(0, HistoryItem(movie, sIdx, sName, eIdx, eName, eSlug, pos, dur))
        
        // Giới hạn danh sách tối đa 50 phim
        if (list.size > 50) {
            list.removeAt(list.size - 1)
        }
        
        prefs.edit().putString(KEY_HISTORY, Gson().toJson(list)).apply()
    }

    /**
     * Xóa một phim khỏi lịch sử dựa trên movie slug
     */
    fun deleteItem(context: Context, slug: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newList = getHistoryList(context).filter { it.movie.slug != slug }
        prefs.edit().putString(KEY_HISTORY, Gson().toJson(newList)).apply()
    }

    /**
     * Xóa toàn bộ lịch sử
     */
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }

    /**
     * Lấy thông tin đã lưu của một bộ phim cụ thể
     */
    fun getSavedData(context: Context, slug: String): HistoryItem? {
        return getHistoryList(context).find { it.movie.slug == slug }
    }
}

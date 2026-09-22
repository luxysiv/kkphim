package com.kkphim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kkphim.utils.HistoryManager

// Dùng AndroidViewModel vì HistoryManager cần context để đọc SharedPreferences
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _historyList = MutableLiveData<List<HistoryManager.HistoryItem>>()
    val historyList: LiveData<List<HistoryManager.HistoryItem>> get() = _historyList

    // Load dữ liệu từ SharedPreferences
    fun loadHistory() {
        val data = HistoryManager.getHistoryList(getApplication())
        _historyList.value = data
    }

    // Xóa một item
    fun deleteItem(slug: String) {
        HistoryManager.deleteItem(getApplication(), slug)
        loadHistory() // Cập nhật lại LiveData sau khi xóa
    }

    // Xóa tất cả
    fun clearAll() {
        HistoryManager.clearAll(getApplication())
        _historyList.value = emptyList()
    }
}

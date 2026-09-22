package com.kkphim.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkphim.api.RetrofitClient
import com.kkphim.model.Movie
import com.kkphim.utils.DEFAULT_CDN
import com.kkphim.utils.toMovieList
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>(emptyList())
    val movies: LiveData<List<Movie>> get() = _movies

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private var currentPage = 1
    var isLastPage = false
        private set

    var currentKeyword: String = ""
        private set

    fun searchMovies(keyword: String, isNewSearch: Boolean = false) {
        if (keyword.isEmpty()) return
        if (_isLoading.value == true || (isLastPage && !isNewSearch)) return

        if (isNewSearch) {
            currentPage = 1
            isLastPage = false
            currentKeyword = keyword
            // Xóa list cũ khi search từ khóa mới để hiện Shimmer
            _movies.value = emptyList()
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.searchMovies(keyword, currentPage)

                val cdn = response.data.APP_DOMAIN_CDN_IMAGE ?: DEFAULT_CDN
                val totalPages = response.data.params.pagination.totalPages.takeIf { it > 0 } ?: 1

                val newList = response.data.items.toMovieList(cdn)

                val currentData = if (isNewSearch) emptyList() else _movies.value.orEmpty()
                _movies.value = currentData + newList

                // Cập nhật trạng thái phân trang
                isLastPage = currentPage >= totalPages
                if (!isLastPage) currentPage++

                if (isNewSearch && newList.isEmpty()) {
                    _error.value = "Không tìm thấy phim phù hợp"
                }

            } catch (e: Exception) {
                _error.value = "Lỗi kết nối server"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

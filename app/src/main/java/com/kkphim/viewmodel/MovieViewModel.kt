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

class MovieViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>(emptyList())
    val movies: LiveData<List<Movie>> get() = _movies

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private var currentPage = 1
    private var totalPages = 1

    // Biến này giúp Activity biết khi nào dừng load more
    var isLastPage = false
        private set

    fun loadMovies(isRefresh: Boolean = false) {
        // Nếu đang load hoặc đã hết trang (và không phải refresh) thì thoát
        if (_isLoading.value == true || (isLastPage && !isRefresh)) return

        if (isRefresh) {
            currentPage = 1
            isLastPage = false
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getHome(currentPage)
                val data = response.data
                totalPages = data.params.pagination.totalPages.takeIf { it > 0 } ?: 1
                val cdn = data.APP_DOMAIN_CDN_IMAGE ?: DEFAULT_CDN

                val newList = data.items.toMovieList(cdn)

                // Nếu refresh thì thay mới, nếu không thì cộng dồn
                val currentData = if (isRefresh) emptyList() else _movies.value.orEmpty()
                _movies.value = currentData + newList

                // Kiểm tra xem đã đến trang cuối chưa
                isLastPage = currentPage >= totalPages

                // Chỉ tăng trang nếu load thành công và chưa phải trang cuối
                if (!isLastPage) {
                    currentPage++
                }

            } catch (e: Exception) {
                _error.value = "Lỗi kết nối server"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

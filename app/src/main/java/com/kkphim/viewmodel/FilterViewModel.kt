package com.kkphim.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkphim.api.RetrofitClient
import com.kkphim.model.ApiMovieItem
import com.kkphim.model.Movie
import com.kkphim.utils.DEFAULT_CDN
import com.kkphim.utils.toMovieList
import kotlinx.coroutines.launch

class FilterViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>(emptyList())
    val movies: LiveData<List<Movie>> get() = _movies

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private var currentPage = 1
    var isLastPage = false
        private set

    private var currentAction: String? = null
    private var currentSlug: String? = null

    fun loadFilterData(action: String, slug: String, isRefresh: Boolean = false) {
        if (_isLoading.value == true || (isLastPage && !isRefresh)) return

        if (isRefresh || action != currentAction || slug != currentSlug) {
            currentPage = 1
            isLastPage = false
            currentAction = action
            currentSlug = slug
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                when (action) {
                    // Tất cả các loại đều dùng chung shape bọc trong "data",
                    // có đầy đủ episode_current/quality/lang + APP_DOMAIN_CDN_IMAGE
                    "TYPE", "CATEGORY", "COUNTRY" -> {
                        val response = when (action) {
                            "TYPE" -> RetrofitClient.api.getByTypeList(slug, currentPage)
                            "CATEGORY" -> RetrofitClient.api.getByCategory(slug, currentPage)
                            else -> RetrofitClient.api.getByCountry(slug, currentPage)
                        }
                        val cdn = response.data.APP_DOMAIN_CDN_IMAGE ?: DEFAULT_CDN
                        val totalPages = response.data.params.pagination.totalPages.takeIf { it > 0 } ?: 1
                        applyResult(response.data.items, cdn, totalPages)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối dữ liệu"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyResult(items: List<ApiMovieItem>, cdn: String, totalPages: Int) {
        val newList = items.toMovieList(cdn)

        val currentData = if (currentPage == 1) emptyList() else _movies.value.orEmpty()
        _movies.value = currentData + newList

        isLastPage = currentPage >= totalPages
        if (!isLastPage) currentPage++
    }
}

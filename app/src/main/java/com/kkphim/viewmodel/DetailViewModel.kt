package com.kkphim.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkphim.api.RetrofitClient
import com.kkphim.model.EpisodeServer
import com.kkphim.model.Movie
import com.kkphim.model.MovieDetailResponse
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val _movieDetail = MutableLiveData<MovieDetailResponse?>()
    val movieDetail: LiveData<MovieDetailResponse?> get() = _movieDetail

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Biến để kiểm soát việc tự động phát phim (chỉ chạy 1 lần duy nhất)
    var hasAutoPlayed = false

    fun fetchMovieDetail(slug: String) {
        // Nếu đã có dữ liệu của phim này rồi thì không load lại
        if (_movieDetail.value?.movie?.slug == slug) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMovieDetail(slug)
                _movieDetail.value = response
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu phim"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Chuyển đổi từ MovieDetail (API) sang Movie (Model chung)
    fun getAsMovieModel(): Movie? {
        val m = _movieDetail.value?.movie ?: return null
        return Movie(
            name = m.name,
            slug = m.slug,
            origin_name = m.origin_name,
            content = m.content ?: "",
            poster_url = m.poster_url,
            thumb_url = m.thumb_url ?: m.poster_url,
            year = m.year,
            episode_current = m.episode_current,
            quality = m.quality,
            lang = m.lang
        )
    }
}

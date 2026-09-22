package com.kkphim.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kkphim.model.ApiMovieItem
import com.kkphim.model.Movie

const val DEFAULT_CDN = "https://phimimg.com"

fun String?.toFullImageUrl(domain: String = DEFAULT_CDN): String {
    if (this.isNullOrEmpty()) return ""
    return if (this.startsWith("http://") || this.startsWith("https://")) {
        this
    } else {
        val cleanDomain = domain.trimEnd('/')
        val cleanPath = this.trimStart('/')
        "$cleanDomain/$cleanPath"
    }
}

fun List<ApiMovieItem>.toMovieList(cdn: String?): List<Movie> {
    val domain = cdn ?: DEFAULT_CDN
    return this.map { it.toMovie(domain) }
}

fun ApiMovieItem.toMovie(domain: String): Movie {
    return Movie(
        name = name,
        slug = slug,
        origin_name = origin_name ?: "",
        content = "",
        poster_url = poster_url.toFullImageUrl(domain),
        thumb_url = (thumb_url ?: poster_url).toFullImageUrl(domain),
        year = year,
        episode_current = episode_current,
        quality = quality,
        lang = lang
    )
}

fun RecyclerView.addLoadMoreListener(
    threshold: Int = 4,
    isLoading: () -> Boolean,
    canLoadMore: () -> Boolean,
    debounceTime: Long = 500,
    onLoadMore: () -> Unit
) {
    val lm = layoutManager as? GridLayoutManager ?: return
    var lastTriggerTime = 0L
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val totalItemCount = lm.itemCount
            val lastVisibleItem = lm.findLastVisibleItemPosition()
            val shouldLoadMore = !isLoading() && canLoadMore() && lastVisibleItem >= totalItemCount - threshold
            val currentTime = System.currentTimeMillis()
            if (shouldLoadMore && currentTime - lastTriggerTime > debounceTime) {
                lastTriggerTime = currentTime
                onLoadMore()
            }
        }
    })
}

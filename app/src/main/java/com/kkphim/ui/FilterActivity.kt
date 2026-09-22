package com.kkphim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.kkphim.R
import com.kkphim.adapter.MovieAdapter
import com.kkphim.utils.*
import com.kkphim.viewmodel.FilterViewModel

class FilterActivity : BaseActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var shimmerView: ShimmerFrameLayout
    private lateinit var shimmerContainer: LinearLayout
    private val movieAdapter = MovieAdapter()
    
    private val viewModel: FilterViewModel by viewModels()

    private var action = ""
    private var slug = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        action = intent.getStringExtra("ACTION") ?: ""
        slug = intent.getStringExtra("VALUE") ?: ""
        findViewById<TextView>(R.id.tvFilterTitle).text = intent.getStringExtra("TITLE") ?: "Kết quả"

        initViews()
        observeViewModel()
        updateRVLayoutManager(rv)
        
        // Load More hỏi ý kiến ViewModel
        rv.addLoadMoreListener(
            isLoading = { viewModel.isLoading.value == true },
            canLoadMore = { !viewModel.isLastPage },
            onLoadMore = { 
                if (isNetworkAvailable()) {
                    viewModel.loadFilterData(action, slug) 
                }
            }
        )

        refreshShimmer(shimmerContainer)

        if (viewModel.movies.value.isNullOrEmpty()) {
            if (isNetworkAvailable()) {
                viewModel.loadFilterData(action, slug, isRefresh = true)
            } else {
                hideShimmerCompletely()
                Toast.makeText(this, "Không có kết nối mạng!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViews() {
        rv = findViewById(R.id.rvFilterResult)
        shimmerView = findViewById(R.id.shimmerFilter)
        shimmerContainer = findViewById(R.id.shimmerFilterContainer)
        rv.adapter = movieAdapter

        findViewById<ImageButton>(R.id.btnBackFilter).setOnClickListener { finish() }

        movieAdapter.setOnItemClickListener { movie ->
            startActivity(Intent(this, DetailActivity::class.java).apply {
                putExtra("slug", movie.slug)
            })
        }
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { list ->
            movieAdapter.submitList(list)
            if (!list.isNullOrEmpty()) {
                hideShimmerCompletely()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            // Chỉ hiện Shimmer khi load trang đầu
            if (loading && movieAdapter.currentList.isEmpty()) {
                showShimmer()
            } else if (!loading) {
                if (movieAdapter.currentList.isNotEmpty()) {
                    hideShimmerCompletely()
                }
            }
        }

        viewModel.error.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (movieAdapter.currentList.isEmpty()) hideShimmerCompletely()
            }
        }
    }

    private fun showShimmer() {
        shimmerView.visibility = View.VISIBLE
        shimmerView.startShimmer()
        rv.visibility = View.GONE
    }

    private fun hideShimmerCompletely() {
        shimmerView.stopShimmer()
        shimmerView.visibility = View.GONE
        rv.visibility = View.VISIBLE
    }

    override fun onNetworkRestored() {
        if (movieAdapter.currentList.isEmpty() && viewModel.isLoading.value == false) {
            refreshShimmer(shimmerContainer)
            viewModel.loadFilterData(action, slug, isRefresh = true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateRVLayoutManager(rv)
        if (shimmerView.visibility == View.VISIBLE) {
            refreshShimmer(shimmerContainer)
        }
    }
}

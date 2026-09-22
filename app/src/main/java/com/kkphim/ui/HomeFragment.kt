package com.kkphim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kkphim.R
import com.kkphim.adapter.MovieAdapter
import com.kkphim.databinding.FragmentHomeBinding
import com.kkphim.utils.ScreenUtils
import com.kkphim.utils.addLoadMoreListener
import com.kkphim.viewmodel.MovieViewModel

class HomeFragment : Fragment(), NetworkAwareFragment {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieViewModel by viewModels()
    private val movieAdapter = MovieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeViewModel()
        setupLoadMore()

        ScreenUtils.buildShimmerRows(requireContext(), binding.shimmerContainer)

        if (viewModel.movies.value.isNullOrEmpty()) {
            if (isNetworkAvailable()) {
                viewModel.loadMovies(isRefresh = true)
            } else {
                hideShimmer()
                Toast.makeText(requireContext(), "Không có kết nối mạng!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViews() {
        binding.rvMovies.adapter = movieAdapter
        ScreenUtils.applyMovieGrid(requireContext(), binding.rvMovies)

        binding.swipeRefresh.setColorSchemeResources(R.color.accent)
        binding.swipeRefresh.setOnRefreshListener {
            if (isNetworkAvailable()) {
                viewModel.loadMovies(isRefresh = true)
            } else {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Không có kết nối mạng!", Toast.LENGTH_SHORT).show()
            }
        }

        movieAdapter.setOnItemClickListener { movie ->
            startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("slug", movie.slug)
            })
        }
    }

    private fun setupLoadMore() {
        binding.rvMovies.addLoadMoreListener(
            isLoading = { viewModel.isLoading.value == true },
            canLoadMore = { !viewModel.isLastPage },
            onLoadMore = {
                if (isNetworkAvailable()) {
                    viewModel.loadMovies()
                } else {
                    Toast.makeText(requireContext(), "Vui lòng kết nối mạng để tải thêm", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner) { list ->
            movieAdapter.submitList(list)
            if (!list.isNullOrEmpty()) {
                hideShimmer()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && movieAdapter.currentList.isEmpty()) {
                showShimmer()
            } else if (!loading) {
                if (movieAdapter.currentList.isNotEmpty()) {
                    hideShimmer()
                }
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                if (movieAdapter.currentList.isEmpty()) {
                    hideShimmer()
                }
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerView.visibility = View.VISIBLE
        binding.shimmerView.startShimmer()
        binding.rvMovies.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerView.stopShimmer()
        binding.shimmerView.visibility = View.GONE
        binding.rvMovies.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(): Boolean =
        (activity as? BaseActivity)?.isNetworkAvailable() ?: true

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenUtils.applyMovieGrid(requireContext(), binding.rvMovies)
        if (binding.shimmerView.visibility == View.VISIBLE) {
            ScreenUtils.buildShimmerRows(requireContext(), binding.shimmerContainer)
        }
    }

    override fun onNetworkRestored() {
        if (movieAdapter.currentList.isEmpty() && viewModel.isLoading.value == false) {
            viewModel.loadMovies(isRefresh = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
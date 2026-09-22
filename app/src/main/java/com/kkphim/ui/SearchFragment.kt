package com.kkphim.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kkphim.adapter.MovieAdapter
import com.kkphim.databinding.FragmentSearchBinding
import com.kkphim.utils.ScreenUtils
import com.kkphim.utils.addLoadMoreListener
import com.kkphim.viewmodel.SearchViewModel

class SearchFragment : Fragment(), NetworkAwareFragment {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val movieAdapter = MovieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSearchResult.adapter = movieAdapter
        ScreenUtils.applyMovieGrid(requireContext(), binding.rvSearchResult)

        binding.rvSearchResult.addLoadMoreListener(
            isLoading = { viewModel.isLoading.value == true },
            canLoadMore = { !viewModel.isLastPage },
            onLoadMore = {
                if (viewModel.currentKeyword.isNotEmpty() && isNetworkAvailable()) {
                    viewModel.searchMovies(viewModel.currentKeyword)
                }
            }
        )

        ScreenUtils.buildShimmerRows(requireContext(), binding.shimmerContainer)

        movieAdapter.setOnItemClickListener { movie ->
            startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("slug", movie.slug)
            })
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val keyword = query?.trim().orEmpty()
                if (keyword.isNotEmpty()) {
                    if (isNetworkAvailable()) {
                        viewModel.searchMovies(keyword, isNewSearch = true)
                        binding.searchView.clearFocus()
                    } else {
                        Toast.makeText(requireContext(), "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner) {
            movieAdapter.submitList(it)
            updateVisibility()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            updateVisibility()
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                if (movieAdapter.currentList.isEmpty()) {
                    binding.tvEmptyMessage.text = it
                } else {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
                updateVisibility()
            }
        }
    }

    private fun updateVisibility() {
        val hasData = !viewModel.movies.value.isNullOrEmpty()
        val loading = viewModel.isLoading.value == true

        binding.rvSearchResult.isVisible = hasData
        binding.shimmerView.isVisible = !hasData && loading
        binding.emptyView.isVisible = !hasData && !loading

        if (binding.shimmerView.isVisible) {
            binding.shimmerView.startShimmer()
        } else {
            binding.shimmerView.stopShimmer()
        }
    }

    private fun isNetworkAvailable(): Boolean =
        (activity as? BaseActivity)?.isNetworkAvailable() ?: true

    override fun onNetworkRestored() {
        if (viewModel.currentKeyword.isNotEmpty() && movieAdapter.currentList.isEmpty()) {
            viewModel.searchMovies(viewModel.currentKeyword, isNewSearch = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
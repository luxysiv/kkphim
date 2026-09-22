package com.kkphim.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kkphim.adapter.HistoryAdapter
import com.kkphim.databinding.FragmentHistoryBinding
import com.kkphim.utils.ScreenUtils
import com.kkphim.viewmodel.HistoryViewModel

class HistoryFragment : Fragment(), NetworkAwareFragment {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
        viewModel.loadHistory()
    }

    private fun initViews() {
        ScreenUtils.applyMovieGrid(requireContext(), binding.rvHistory)

        historyAdapter = HistoryAdapter(
            emptyList(),
            onItemClick = { item ->
                startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("slug", item.movie.slug)
                    putExtra("AUTO_PLAY", true)
                    putExtra("serverName", item.serverName)
                    putExtra("episodeSlug", item.episodeSlug)
                    putExtra("position", item.position)
                })
            },
            onDeleteClick = { item ->
                viewModel.deleteItem(item.movie.slug)
            }
        )
        binding.rvHistory.adapter = historyAdapter

        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ danh sách phim đã xem?")
                .setPositiveButton("Xóa tất cả") { _, _ -> viewModel.clearAll() }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.historyList.observe(viewLifecycleOwner) { data ->
            historyAdapter.updateData(data)
            binding.btnClearHistory.isVisible = data.isNotEmpty()
            binding.emptyView.isVisible = data.isEmpty()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistory()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.loadHistory()
        }
    }

    override fun onNetworkRestored() {
        viewModel.loadHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
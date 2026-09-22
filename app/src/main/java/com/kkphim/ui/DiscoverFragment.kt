package com.kkphim.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.kkphim.R
import com.kkphim.databinding.FragmentDiscoverBinding
import com.kkphim.utils.CategoryUtils
import com.kkphim.utils.CountryUtils
import com.kkphim.utils.FilterItem
import com.kkphim.utils.TypeUtils

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillChips(binding.chipGroupType, TypeUtils.list, "TYPE")
        fillChips(binding.chipGroupCategory, CategoryUtils.list, "CATEGORY")
        fillChips(binding.chipGroupCountry, CountryUtils.list, "COUNTRY")
    }

    private fun fillChips(group: ChipGroup, items: List<FilterItem>, action: String) {
        group.removeAllViews()
        items.forEach { item ->
            group.addView(createChip(item.name) { openFilter(action, item) })
        }
    }

    private fun createChip(text: String, onClick: () -> Unit): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            textSize = 13f
            isCheckable = false
            chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.chip_bg)
            )
            setTextColor(ContextCompat.getColor(context, R.color.chip_text))
            chipStrokeWidth = 0f
            chipCornerRadius = 8f
            setOnClickListener { onClick() }
        }
    }

    private fun openFilter(action: String, item: FilterItem) {
        startActivity(Intent(requireContext(), FilterActivity::class.java).apply {
            putExtra("ACTION", action)
            putExtra("VALUE", item.slug)
            putExtra("TITLE", item.name)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
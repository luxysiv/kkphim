package com.kkphim.utils

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kkphim.R

object ScreenUtils {
    fun getMovieSpanCount(context: Context): Int {
        val config = context.resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

        return if (isTablet) {
            if (isLandscape) 4 else 3
        } else {
            if (isLandscape) 3 else 2
        }
    }

    fun applyMovieGrid(context: Context, rv: RecyclerView) {
        val spanCount = getMovieSpanCount(context)
        val lm = rv.layoutManager as? GridLayoutManager
        if (lm == null) {
            rv.layoutManager = GridLayoutManager(context, spanCount)
        } else {
            lm.spanCount = spanCount
        }
    }

    fun buildShimmerRows(context: Context, container: LinearLayout?, rows: Int = 4) {
        container ?: return
        container.removeAllViews()
        val spanCount = getMovieSpanCount(context)
        val inflater = LayoutInflater.from(context)
        repeat(rows) {
            val row = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }
            repeat(spanCount) {
                val item = inflater.inflate(R.layout.item_movie_placeholder, row, false)
                (item.layoutParams as LinearLayout.LayoutParams).apply {
                    width = 0
                    weight = 1f
                }
                row.addView(item)
            }
            container.addView(row)
        }
    }
}

package com.kkphim.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kkphim.R
import com.kkphim.model.Movie
import com.kkphim.utils.toFullImageUrl

class MovieAdapter : ListAdapter<Movie, MovieAdapter.ViewHolder>(MovieDiffCallback()) {

    private var onItemClickListener: ((Movie) -> Unit)? = null

    fun setOnItemClickListener(listener: (Movie) -> Unit) {
        onItemClickListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val title: TextView = view.findViewById(R.id.title)
        val txtBadge: TextView = view.findViewById(R.id.txtBadge)
        val txtLang: TextView = view.findViewById(R.id.txtLang)
        val txtYear: TextView = view.findViewById(R.id.txtYear)
        val txtQuality: TextView = view.findViewById(R.id.txtQuality)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)

        holder.title.text = movie.name
        holder.txtYear.text = movie.year.toString()
        holder.txtQuality.text = movie.quality?.ifEmpty { "HD" } ?: "HD"

        if (movie.episode_current.isNullOrEmpty()) {
            holder.txtBadge.visibility = View.GONE
        } else {
            holder.txtBadge.visibility = View.VISIBLE
            holder.txtBadge.text = movie.episode_current
        }
        
        val fullLang = movie.lang?.trim()
        if (fullLang.isNullOrEmpty()) {
            holder.txtLang.visibility = View.GONE
        } else {
            holder.txtLang.visibility = View.VISIBLE
            holder.txtLang.text = fullLang
        }

        Glide.with(holder.itemView.context)
            .load(movie.poster_url.toFullImageUrl())
            .placeholder(R.drawable.bg_round)
            .error(R.drawable.bg_round)
            .centerCrop()
            .into(holder.img)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(movie)
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.slug == newItem.slug
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}

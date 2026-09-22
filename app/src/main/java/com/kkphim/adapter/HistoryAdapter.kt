package com.kkphim.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kkphim.R
import com.kkphim.utils.HistoryManager
import com.kkphim.utils.toFullImageUrl

class HistoryAdapter(
    private var list: List<HistoryManager.HistoryItem>,
    private val onItemClick: (HistoryManager.HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryManager.HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgMovie)
        val name: TextView = v.findViewById(R.id.tvMovieName)
        val tvEp: TextView = v.findViewById(R.id.tvHistoryEpisode)
        val btnDelete: View = v.findViewById(R.id.btnDeleteHistory)
        val progress: ProgressBar = v.findViewById(R.id.pbHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val movie = item.movie

        holder.name.text = movie.name

        // Ví dụ: Xem tiếp: Vietsub - Tập 10
        holder.tvEp.text = "Xem tiếp: ${item.serverName} - ${item.episodeName}"

        Glide.with(holder.itemView.context)
            .load(movie.poster_url.toFullImageUrl())
            .placeholder(R.color.bg_item)
            .error(R.color.bg_item)
            .into(holder.img)

        // Hiển thị tiến trình xem %
        if (item.duration > 0) {
            holder.progress.visibility = View.VISIBLE
            val progressPercent = ((item.position * 100) / item.duration).toInt()
            holder.progress.progress = progressPercent
        } else {
            holder.progress.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        
        // Khi click vào item, chuyển dữ liệu sang DetailActivity và kích hoạt Auto Play
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<HistoryManager.HistoryItem>) {
        this.list = newList
        notifyDataSetChanged()
    }
}

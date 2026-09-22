package com.kkphim.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.kkphim.R
import com.kkphim.model.EpisodeServer
import com.kkphim.model.MovieDetail
import com.kkphim.utils.HistoryManager
import com.kkphim.utils.toFullImageUrl
import com.kkphim.viewmodel.DetailViewModel

class DetailActivity : BaseActivity() {

    private lateinit var tvHeaderTitle: TextView
    private lateinit var spinnerServer: Spinner
    private lateinit var episodeGrid: GridLayout
    private lateinit var imgPoster: ImageView
    private lateinit var txtOriginName: TextView
    private lateinit var badgeRow: LinearLayout
    private lateinit var txtStatus: TextView
    private lateinit var categorySection: View
    private lateinit var categoryChipRow: LinearLayout
    private lateinit var txtCountry: TextView
    private lateinit var txtContent: TextView
    private lateinit var txtDirector: TextView
    private lateinit var txtActor: TextView
    private lateinit var btnPlay: Button

    private var movieSlug: String = ""
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        
        movieSlug = intent.getStringExtra("slug") ?: ""
        if (movieSlug.isEmpty()) { 
            finish()
            return 
        }

        initViews()
        observeViewModel()
        
        viewModel.fetchMovieDetail(movieSlug)
    }

    override fun onNetworkRestored() {
        if (viewModel.movieDetail.value == null) {
            viewModel.fetchMovieDetail(movieSlug)
        }
    }

    override fun onRestart() {
        super.onRestart()
        updatePlayButton()
    }

    private fun initViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        spinnerServer = findViewById(R.id.spinnerServer)
        episodeGrid = findViewById(R.id.episodeGrid)
        imgPoster = findViewById(R.id.imgPoster)
        txtOriginName = findViewById(R.id.txtOriginName)
        badgeRow = findViewById(R.id.badgeRow)
        txtStatus = findViewById(R.id.txtStatus)
        categorySection = findViewById(R.id.categorySection)
        categoryChipRow = findViewById(R.id.categoryChipRow)
        txtCountry = findViewById(R.id.txtCountry)
        txtContent = findViewById(R.id.txtContent)
        txtDirector = findViewById(R.id.txtDirector)
        txtActor = findViewById(R.id.txtActor)
        btnPlay = findViewById(R.id.btnPlay)

        findViewById<View>(R.id.btnBackDetail)?.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.movieDetail.observe(this) { response ->
            response?.let {
                val m = it.movie
                
                tvHeaderTitle.text = m.name
                txtOriginName.text = m.origin_name

                // Dùng HtmlCompat để giải mã các ký tự như &quot; thành dấu "
                txtContent.text = HtmlCompat.fromHtml(
                    m.content ?: "", 
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                
                Glide.with(this)
                    .load(m.poster_url.toFullImageUrl())
                    .placeholder(R.drawable.bg_round)
                    .into(imgPoster)

                renderBadges(m)
                renderStatus(m)
                renderCategories(m)
                renderCountry(m)
                renderCast(m)

                setupServerSpinner(it.episodes)
                updatePlayButton()
                handleAutoPlay(it.episodes)
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    // Năm / Chất lượng / Ngôn ngữ / Thời lượng - hiển thị dạng badge viền accent
    private fun renderBadges(m: MovieDetail) {
        badgeRow.removeAllViews()
        val items = listOfNotNull(
            if (m.year > 0) m.year.toString() else null,
            m.quality?.takeIf { it.isNotBlank() },
            m.lang?.takeIf { it.isNotBlank() },
            m.time?.takeIf { it.isNotBlank() }
        )
        items.forEach { text -> badgeRow.addView(createBadge(text, outline = true)) }
    }

    // Trạng thái phim: "Tập 5/12" hoặc "Hoàn Tất (12/12)" - badge nổi bật màu accent
    private fun renderStatus(m: MovieDetail) {
        val current = m.episode_current?.takeIf { it.isNotBlank() }
        if (current != null) {
            txtStatus.text = current
            txtStatus.visibility = View.VISIBLE
        } else {
            txtStatus.visibility = View.GONE
        }
    }

    private fun renderCategories(m: MovieDetail) {
        val cats = m.category.orEmpty().mapNotNull { it.name.takeIf { n -> n.isNotBlank() } }
        if (cats.isEmpty()) {
            categorySection.visibility = View.GONE
            return
        }
        categorySection.visibility = View.VISIBLE
        categoryChipRow.removeAllViews()
        cats.forEach { name -> categoryChipRow.addView(createBadge(name, outline = false)) }
    }

    private fun renderCountry(m: MovieDetail) {
        val countries = m.country.orEmpty().mapNotNull { it.name.takeIf { n -> n.isNotBlank() } }
        if (countries.isEmpty()) {
            txtCountry.visibility = View.GONE
        } else {
            txtCountry.text = "Quốc gia: ${countries.joinToString(", ")}"
            txtCountry.visibility = View.VISIBLE
        }
    }

    private fun renderCast(m: MovieDetail) {
        val directors = m.director.orEmpty().filter { it.isNotBlank() }
        val actors = m.actor.orEmpty().filter { it.isNotBlank() }

        if (directors.isNotEmpty()) {
            txtDirector.text = "Đạo diễn: ${directors.joinToString(", ")}"
            txtDirector.visibility = View.VISIBLE
        } else {
            txtDirector.visibility = View.GONE
        }

        if (actors.isNotEmpty()) {
            txtActor.text = "Diễn viên: ${actors.joinToString(", ")}"
            txtActor.visibility = View.VISIBLE
        } else {
            txtActor.visibility = View.GONE
        }
    }

    private fun createBadge(text: String, outline: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setPadding(24, 10, 24, 10)
            if (outline) {
                setBackgroundResource(R.drawable.bg_badge_outline)
                setTextColor(ContextCompat.getColor(context, R.color.accent))
            } else {
                setBackgroundResource(R.drawable.bg_chip)
                setTextColor(ContextCompat.getColor(context, R.color.chip_text))
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 8
            layoutParams = params
        }
    }

    private fun updatePlayButton() {
        val detail = viewModel.movieDetail.value ?: return
        val history = HistoryManager.getSavedData(this, movieSlug)
        
        if (history != null) {
            btnPlay.text = "Xem tiếp: ${history.serverName} - ${history.episodeName}"
            btnPlay.setOnClickListener { 
                startPlayer(detail.episodes, history.serverIndex, history.episodeIndex, history.position) 
            }
        } else {
            btnPlay.text = "Xem ngay"
            btnPlay.setOnClickListener { 
                startPlayer(detail.episodes, 0, 0, 0L) 
            }
        }
    }

    private fun handleAutoPlay(servers: List<EpisodeServer>) {
        if (viewModel.hasAutoPlayed) return
        val isAutoPlay = intent.getBooleanExtra("AUTO_PLAY", false)
        if (!isAutoPlay) return

        val targetServer = intent.getStringExtra("serverName")
        val targetEpSlug = intent.getStringExtra("episodeSlug")
        val pos = intent.getLongExtra("position", 0L)

        var sIdx = servers.indexOfFirst { it.server_name == targetServer }
        if (sIdx == -1) sIdx = 0
        val eIdx = servers[sIdx].server_data.indexOfFirst { it.slug == targetEpSlug }

        if (eIdx != -1) {
            viewModel.hasAutoPlayed = true
            startPlayer(servers, sIdx, eIdx, pos)
        }
    }

    private fun setupServerSpinner(servers: List<EpisodeServer>) {
        if (servers.isEmpty()) return
        val serverNames = servers.map { it.server_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serverNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerServer.adapter = adapter
        
        spinnerServer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                renderEpisodes(servers, pos)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun renderEpisodes(servers: List<EpisodeServer>, serverIndex: Int) {
        episodeGrid.removeAllViews()
        val server = servers.getOrNull(serverIndex) ?: return
        
        server.server_data.forEachIndexed { index, ep ->
            val btn = Button(this).apply {
                text = ep.name
                setBackgroundResource(R.drawable.bg_episode)
                setOnClickListener { startPlayer(servers, serverIndex, index, 0L) }
            }
            
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            btn.layoutParams = params
            episodeGrid.addView(btn)
        }
    }

    private fun startPlayer(servers: List<EpisodeServer>, sIdx: Int, eIdx: Int, pos: Long) {
        val server = servers.getOrNull(sIdx) ?: return
        val movie = viewModel.getAsMovieModel() ?: return
        
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("movieName", movie.name)
            putExtra("movieSlug", movie.slug)
            putExtra("movieOriginName", movie.origin_name)
            putExtra("movieContent", movie.content)
            putExtra("moviePoster", movie.poster_url)
            putExtra("movieThumb", movie.thumb_url)
            putExtra("movieYear", movie.year)
            putExtra("serverIndex", sIdx)
            putExtra("serverName", server.server_name)
            putStringArrayListExtra("links", ArrayList(server.server_data.map { it.link_m3u8 }))
            putStringArrayListExtra("names", ArrayList(server.server_data.map { it.name }))
            putStringArrayListExtra("slugs", ArrayList(server.server_data.map { it.slug }))
            putExtra("currentIndex", eIdx)
            putExtra("savedPosition", pos)
        }
        startActivity(intent)
    }
}

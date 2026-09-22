package com.kkphim.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.kkphim.R
import com.kkphim.model.Movie
import com.kkphim.utils.HistoryManager
import com.kkphim.utils.InterceptDataSource

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var tvPlayerTitle: TextView
    private lateinit var layoutHeader: View

    private val saveHandler = Handler(Looper.getMainLooper())
    private val saveRunnable = object : Runnable {
        override fun run() { saveProgress(); saveHandler.postDelayed(this, 10000) }
    }

    private var movieName = ""; private var movieSlug = ""; private var movieOriginName = ""; private var movieContent = ""
    private var moviePoster = ""; private var movieThumb = ""; private var movieYear = 0
    private var epLinks = arrayListOf<String>(); private var epNames = arrayListOf<String>()
    private var epSlugs = arrayListOf<String>() 
    private var currentIndex = 0; private var serverIndex = 0; private var serverName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // 1. Cấu hình Ẩn hoàn toàn Status Bar & Navigation Bar (Immersive Mode)
        setupFullscreenMode()

        playerView = findViewById(R.id.playerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        tvPlayerTitle = findViewById(R.id.tvPlayerTitle)
        layoutHeader = findViewById(R.id.layoutHeader)

        // 2. Xử lý đẩy Header xuống dưới vùng Camera (Notch/Cutout)
        applySafeInsets()

        layoutHeader.setOnClickListener { finish() }

        movieName = intent.getStringExtra("movieName") ?: ""
        movieSlug = intent.getStringExtra("movieSlug") ?: ""
        movieOriginName = intent.getStringExtra("movieOriginName") ?: ""
        movieContent = intent.getStringExtra("movieContent") ?: ""
        moviePoster = intent.getStringExtra("moviePoster") ?: ""
        movieThumb = intent.getStringExtra("movieThumb") ?: ""
        movieYear = intent.getIntExtra("movieYear", 0)
        
        serverIndex = intent.getIntExtra("serverIndex", 0)
        serverName = intent.getStringExtra("serverName") ?: ""
        epLinks = intent.getStringArrayListExtra("links") ?: arrayListOf()
        epNames = intent.getStringArrayListExtra("names") ?: arrayListOf()
        epSlugs = intent.getStringArrayListExtra("slugs") ?: arrayListOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)
        val savedPos = intent.getLongExtra("savedPosition", 0L)

        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { v -> 
            layoutHeader.visibility = v 
            if (v == View.VISIBLE) {
                // Khi hiện control, đảm bảo ẩn lại thanh hệ thống nếu nó vô tình hiện lên
                setupFullscreenMode()
            }
        })
        
        initializePlayer(savedPos)
    }

    private fun setupFullscreenMode() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Ẩn thanh trạng thái và thanh điều hướng
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Thiết lập chế độ: Vuốt từ mép mới hiện lại thanh hệ thống và tự ẩn ngay sau đó
        windowInsetsController.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun applySafeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(layoutHeader) { view, insets ->
            // Lấy khoảng trống của thanh trạng thái hoặc vùng đục lỗ (camera)
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val displayCutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            val topPadding = if (displayCutoutInsets.top > statusBarInsets.top) 
                displayCutoutInsets.top else statusBarInsets.top

            // Đẩy margin của header xuống để không bị che khuất
            val params = view.layoutParams as RelativeLayout.LayoutParams
            params.topMargin = topPadding + 20 // Cộng thêm 20px cho đẹp
            view.layoutParams = params
            
            insets
        }
    }

    private fun initializePlayer(savedPos: Long) {
        val dataSourceFactory = DataSource.Factory { InterceptDataSource() }
        player = ExoPlayer.Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory)).build()
        playerView.player = player
        val mediaItems = epLinks.map { MediaItem.fromUri(it) }
        player?.let { p ->
            p.setMediaItems(mediaItems)
            p.seekTo(currentIndex, savedPos)
            p.prepare()
            p.playWhenReady = true
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) { 
                    loadingProgressBar.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE 
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) { 
                    playerView.keepScreenOn = isPlaying 
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { 
                    saveProgress()
                    updateHeaderTitle() 
                }
            })
        }
        updateHeaderTitle()
        saveHandler.postDelayed(saveRunnable, 10000)
    }

    private fun updateHeaderTitle() {
        val currentIdx = player?.currentMediaItemIndex ?: currentIndex
        val epName = epNames.getOrNull(currentIdx) ?: ""
        if (epName.isEmpty()) { tvPlayerTitle.text = movieName; return }
        val separator = " - "; val fullText = "$movieName$separator$epName"
        val spannable = SpannableString(fullText)
        val lastIdx = fullText.lastIndexOf(separator)
        if (lastIdx != -1) {
            val start = lastIdx + separator.length
            if (start < fullText.length) spannable.setSpan(ForegroundColorSpan(Color.YELLOW), start, fullText.length, 0)
        }
        tvPlayerTitle.text = spannable
    }

    private fun saveProgress() {
        val p = player ?: return
        val idx = p.currentMediaItemIndex
        val pos = p.currentPosition
        val dur = p.duration
        if (pos > 1000 && dur > 0) {
            val m = Movie(movieName, movieSlug, movieOriginName, movieContent, moviePoster, movieThumb, movieYear)
            val name = epNames.getOrNull(idx) ?: ""
            val slug = epSlugs.getOrNull(idx) ?: ""
            HistoryManager.saveHistory(this, m, serverIndex, serverName, idx, name, slug, pos, dur)
        }
    }

    override fun onPause() { super.onPause(); saveProgress(); player?.pause() }
    override fun onDestroy() { saveHandler.removeCallbacks(saveRunnable); saveProgress(); player?.release(); super.onDestroy() }
}

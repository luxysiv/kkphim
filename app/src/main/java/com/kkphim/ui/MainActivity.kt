package com.kkphim.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kkphim.R

class MainActivity : BaseActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var homeFragment: HomeFragment
    private lateinit var discoverFragment: DiscoverFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var historyFragment: HistoryFragment

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)
        initFragments()
        setupBottomNav()
        if (savedInstanceState == null) {
            // Lần đầu: chỉ hiển thị Trang chủ, ẩn các tab còn lại (trạng thái show/hide
            // sẽ được FragmentManager khôi phục tự động khi xoay màn hình)
            hideNonHomeFragments()
        }
        setupBackExit()
    }

    private fun initFragments() {
        val fm = supportFragmentManager
        homeFragment = findOrCreate(TAG_HOME) { HomeFragment() }
        discoverFragment = findOrCreate(TAG_DISCOVER) { DiscoverFragment() }
        searchFragment = findOrCreate(TAG_SEARCH) { SearchFragment() }
        historyFragment = findOrCreate(TAG_HISTORY) { HistoryFragment() }
    }

    private inline fun <reified F : androidx.fragment.app.Fragment> findOrCreate(
        tag: String,
        create: () -> F
    ): F {
        supportFragmentManager.findFragmentByTag(tag)?.let { return it as F }
        return create().also {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainNavHost, it, tag)
                .commitNow()
        }
    }

    private fun hideNonHomeFragments() {
        supportFragmentManager.beginTransaction()
            .hide(discoverFragment)
            .hide(searchFragment)
            .hide(historyFragment)
            .commitNow()
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchTo(TAG_HOME)
                R.id.nav_discover -> switchTo(TAG_DISCOVER)
                R.id.nav_search -> switchTo(TAG_SEARCH)
                R.id.nav_history -> switchTo(TAG_HISTORY)
            }
            true
        }
    }

    // Fragment show/hide để giữ nguyên trạng thái cuộn, search, history khi chuyển tab
    private fun switchTo(activeTag: String) {
        val tx = supportFragmentManager.beginTransaction()
        val tabs = listOf(
            TAG_HOME to homeFragment,
            TAG_DISCOVER to discoverFragment,
            TAG_SEARCH to searchFragment,
            TAG_HISTORY to historyFragment
        )
        tabs.forEach { (tag, fragment) ->
            if (fragment.isAdded) {
                if (tag == activeTag) tx.show(fragment) else tx.hide(fragment)
            }
        }
        tx.commitNow()
    }

    private fun setupBackExit() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomNav.selectedItemId != R.id.nav_home) {
                    bottomNav.selectedItemId = R.id.nav_home
                } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Nhấn back lần nữa để thoát", Toast.LENGTH_SHORT).show()
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
    }

    // Khi mạng có lại: forward event xuống các fragment có dữ liệu cần tải lại
    override fun onNetworkRestored() {
        listOf(homeFragment, searchFragment, historyFragment)
            .forEach { it.onNetworkRestored() }
    }

    companion object {
        private const val TAG_HOME = "home"
        private const val TAG_DISCOVER = "discover"
        private const val TAG_SEARCH = "search"
        private const val TAG_HISTORY = "history"
    }
}
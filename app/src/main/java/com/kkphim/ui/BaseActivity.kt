package com.kkphim.ui

import android.content.res.Configuration
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.kkphim.utils.ScreenUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity : AppCompatActivity() {

    private var networkDisposable: Disposable? = null
    protected var isConnected = true
    private var isInitialCheck = true
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNetworkObserver()
    }

    private fun setupNetworkObserver() {
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity ->
                val currentStatus = (connectivity.state() == android.net.NetworkInfo.State.CONNECTED)
                val wasConnected = isConnected
                isConnected = currentStatus

                if (isInitialCheck) {
                    isInitialCheck = false
                } else if (!wasConnected && isConnected) {
                    onNetworkRestored()
                    Toast.makeText(this, "Đã khôi phục kết nối", Toast.LENGTH_SHORT).show()
                } else if (wasConnected && !isConnected) {
                    Toast.makeText(this, "Mất kết nối mạng!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun isNetworkAvailable(): Boolean = isConnected

    open fun onNetworkRestored() {}

    // Chỉ giữ lại các hàm tiện ích về UI
    protected fun updateRVLayoutManager(rv: RecyclerView) {
        ScreenUtils.applyMovieGrid(this, rv)
    }

    protected fun refreshShimmer(container: LinearLayout?) {
        ScreenUtils.buildShimmerRows(this, container)
    }

    protected fun setupDoubleBackToExit() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) finish()
                else {
                    Toast.makeText(this@BaseActivity, "Nhấn back lần nữa để thoát", Toast.LENGTH_SHORT).show()
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        networkDisposable?.dispose()
    }
}

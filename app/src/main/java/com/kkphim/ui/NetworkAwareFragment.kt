package com.kkphim.ui

/**
 * Fragment nằm trong MainActivity nhận sự kiện mạng được phục hồi
 * (MainActivity.onNetworkRestored() forward xuống fragment đang hiển thị).
 */
interface NetworkAwareFragment {
    fun onNetworkRestored()
}
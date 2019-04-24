package com.example.botanas.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class Network (context: Context) {
    val appContext = context

    fun isConnected(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}
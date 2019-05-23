package com.example.botanas.services
import android.bluetooth.BluetoothAdapter
import android.util.Log

class BluetoothService {
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun isEnabled(): Boolean {
        return mBluetoothAdapter.isEnabled
    }
}
package com.example.botanas

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.BluetoothScannerAdapter
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import android.view.MenuItem
import com.example.botanas.adapter.BDevice
import com.example.botanas.services.BluetoothService




class BluetoothScannerActivity : AppCompatActivity(), BluetoothScannerAdapter.ItemClickListener {

    private lateinit var bluetoothScannerAdapter: BluetoothScannerAdapter
    private lateinit var bluetoothDevicesRecycler: RecyclerView
    private val REQUEST_ENABLE_BT = 1
    private val devices: MutableSet<BluetoothDevice> = mutableSetOf()
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var devicePair: BluetoothDevice

    override fun onItemClick(item: BluetoothScannerAdapter.ViewHolder, position: Int, parentPosition: Int) {
        val device = devices.elementAt(position)
        pair(device)
        bluetoothDevicesRecycler.adapter!!.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scanner)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bluetoothScannerAdapter = BluetoothScannerAdapter(devices, this)
        bluetoothDevicesRecycler = findViewById(R.id.bluetoothDevicesRecycler)
        bluetoothDevicesRecycler.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = bluetoothScannerAdapter
        }

        if (!BluetoothService().isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        refreshListDevices()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if  (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            refreshListDevices()
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }

    private fun refreshListDevices() {

        devices.addAll(mBluetoothAdapter.bondedDevices)
        bluetoothDevicesRecycler.adapter!!.notifyDataSetChanged()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)
        if (mBluetoothAdapter.isDiscovering)
            mBluetoothAdapter.cancelDiscovery()

        mBluetoothAdapter.startDiscovery()

    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // A Bluetooth device was found
                // Getting device information from the intent
                devices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
                bluetoothDevicesRecycler.adapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun pair(device: BluetoothDevice) {
        applicationContext.registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        try {
            BDevice.device = device
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
            }
            if (mBluetoothAdapter.isDiscovering)
                mBluetoothAdapter.cancelDiscovery()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (mBluetoothAdapter.isDiscovering)
                    mBluetoothAdapter.cancelDiscovery()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val SCANNING_FOR_PRINTER = 115
    }
}

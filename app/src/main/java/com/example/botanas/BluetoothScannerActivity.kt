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
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.example.botanas.services.BluePrinter
import com.example.botanas.services.BluetoothService
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule


class BluetoothScannerActivity : AppCompatActivity(), BluetoothScannerAdapter.ItemClickListener {

    private lateinit var bluetoothScannerAdapter: BluetoothScannerAdapter
    private lateinit var bluetoothDevicesRecycler: RecyclerView
    private val REQUEST_ENABLE_BT = 1
    private val devices: MutableSet<BluetoothDevice> = mutableSetOf()
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var view: View
    private lateinit var searchingProgress: ProgressBar

    override fun onItemClick(item: BluetoothScannerAdapter.ViewHolder, position: Int, parentPosition: Int) {
        val device = devices.elementAt(position)
        pair(device)
        bluetoothDevicesRecycler.adapter!!.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scanner)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view = findViewById(R.id.bluetooth_scanner_layout)
        searchingProgress = findViewById(R.id.searching_progress)

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
        //searchingProgress.visibility = View.VISIBLE

        Timer("SettingUp", true).schedule(15000) {
            if (mBluetoothAdapter.isDiscovering)
                mBluetoothAdapter.cancelDiscovery()
        }



    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // A Bluetooth device was found
                // Getting device information from the intent
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                devices.add(device)
                bluetoothDevicesRecycler.adapter!!.notifyDataSetChanged()
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                Log.d("stop", "true")
                mBluetoothAdapter.cancelDiscovery()
                //searchingProgress.visibility = View.GONE
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(this)
                    if (mBluetoothAdapter.isDiscovering) {
                        mBluetoothAdapter.cancelDiscovery()
                        //searchingProgress.visibility = View.GONE
                        context.unregisterReceiver(this)
                    }
                    val handler = Handler()
                    handler.postDelayed({
                        if (BluePrinter(context).setPrinter(BluePrinter.device!!))
                            finish()
                        else
                            Snackbar.make(view, R.string.bluetooth_pairing_error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    }, 1000)

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {

                }
            }
        }
    }

    private fun pair(device: BluetoothDevice) {
        applicationContext.registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        try {
            BluePrinter.device = device
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
            } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
                unregisterReceiver(mReceiver)
                BluePrinter.device = device
                BluePrinter(this).setPrinter(device)
                finish()
            }
            if (mBluetoothAdapter.isDiscovering)
                mBluetoothAdapter.cancelDiscovery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (mBluetoothAdapter.isDiscovering)
                    mBluetoothAdapter.cancelDiscovery()
                unregisterReceiver(mReceiver)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
       super.onBackPressed()
       unregisterReceiver(mReceiver)
    }

    companion object {
        const val SCANNING_FOR_PRINTER = 115
    }
}

package com.example.botanas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import android.bluetooth.BluetoothDevice
import androidx.cardview.widget.CardView

class BluetoothScannerAdapter(private val samples: MutableSet<BluetoothDevice>, listener: ItemClickListener, parentPosition: Int = -1) :
    RecyclerView.Adapter<BluetoothScannerAdapter.ViewHolder>() {
    private val onItemClickListener: ItemClickListener = listener
    private val parentPosition = parentPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bluetooh_device_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples.elementAt(position)

        holder.deviceName.text = item.name
        holder.deviceAddress.text = item.address
        holder.deviceType.text = item.type.toString()

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(holder, position, parentPosition)
        }


    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val deviceName: TextView = mView.findViewById(R.id.device_name)
        val deviceAddress: TextView = mView.findViewById(R.id.device_address)
        val deviceType: TextView = mView.findViewById(R.id.device_type)
        val deviceContainer: CardView = mView.findViewById(R.id.device_container)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int, parentPosition: Int)
    }
}

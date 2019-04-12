package com.example.botanas.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.dataClasses.Requisition
import com.example.botanas.dataClasses.Storage
import java.text.NumberFormat
import java.util.*


class SalesAdapter(private val samples: ArrayList<Requisition>, listener: ItemOnPressListener) : RecyclerView.Adapter<SalesAdapter.ViewHolder>() {

    private val onItemOnPressListener: ItemOnPressListener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sales_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = NumberFormat.getCurrencyInstance()
        val item = samples[position]
        val total = item.total.toDouble()
        holder.salesClientName.text = item.client_name
        holder.salesDate.text = item.date
        holder.salesAmount.text = currency.format(total)

        holder.itemView.setOnClickListener {
                View -> onItemOnPressListener.onItemClick(holder, position)
        }
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val salesClientName: TextView = mView.findViewById(R.id.sales_client_name)
        val salesDate: TextView = mView.findViewById(R.id.sales_date)
        val salesAmount: TextView = mView.findViewById(R.id.sales_amount)
    }

    interface  ItemOnPressListener {
        fun onItemClick(item: ViewHolder, position: Int)
    }
}
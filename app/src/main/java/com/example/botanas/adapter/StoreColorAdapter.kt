package com.example.botanas.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.dataClasses.Store
import java.text.NumberFormat
import java.util.*


class StoreColorAdapter(private val samples: ArrayList<Store>) : RecyclerView.Adapter<StoreColorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.store_color_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = NumberFormat.getCurrencyInstance()
        val item = samples[position]
        val color = Color.parseColor(item.color)
        val gd = holder.color.background.mutate() as GradientDrawable
        gd.setColor(color)
        holder.storeName.text = item.name
        //holder.color.setBackgroundColor(color)
        holder.storeColorLayout.setBackgroundColor(color)
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val storeColorLayout: TableRow = mView.findViewById(R.id.store_color_layout)
        val color: TextView = mView.findViewById(R.id.store_color_color)
        val storeName: TextView = mView.findViewById(R.id.store_color_name)
    }
}
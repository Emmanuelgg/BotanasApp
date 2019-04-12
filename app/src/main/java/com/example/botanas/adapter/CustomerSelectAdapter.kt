package com.example.botanas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.dataClasses.Storage
import java.text.NumberFormat


class CustomerSelectAdapter(private val samples: ArrayList<Storage>, listener: ItemClickListener, color: Int = 0, text_color: Int = 0, parentPosition: Int = -1) :
    RecyclerView.Adapter<CustomerSelectAdapter.ViewHolder>() {
    private val onItemClickListener: ItemClickListener = listener
    private val parentPosition = parentPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.customer_product_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        var total = item.cost.toDouble() * item.quantity.toDouble()
        val currency = NumberFormat.getCurrencyInstance()

        holder.quantity.text = "${item.quantity}x"
        holder.productName.text = item.product_name
        holder.cost.text = currency.format(item.cost.toDouble())
        holder.totalCost.text = currency.format(total)

        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position, parentPosition)
        }


    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val quantity: TextView = mView.findViewById(R.id.sale_product_quantity)
        val productName: TextView = mView.findViewById(R.id.sale_product_name)
        val cost: TextView = mView.findViewById(R.id.sale_product_cost)
        val totalCost: TextView = mView.findViewById(R.id.sale_product_total_cost)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int, parentPosition: Int)
    }
}

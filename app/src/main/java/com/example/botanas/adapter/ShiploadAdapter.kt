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
import com.example.botanas.dataClasses.Storage
import java.util.ArrayList

class ShiploadAdapter(private val samples: ArrayList<Storage>, listener: ItemOnPressListener) : RecyclerView.Adapter<ShiploadAdapter.ViewHolder>() {

    private val onItemOnPressListener: ItemOnPressListener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.shipload_product_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        var quantity = item.quantity.toString()
        Log.d("quantity", quantity)
        quantity = if (quantity == "0" || quantity == "")
            ""
        else
            item.quantity.toString()

        holder.productQuantity.setText(quantity)
        holder.saleProductName.text = item.product_name
        holder.itemView.setOnLongClickListener {
                View -> onItemOnPressListener.onItemPress(holder, position)
            true
        }
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val saleProductName: TextView = mView.findViewById(R.id.shipload_product_name)
        val productQuantity: EditText = mView.findViewById(R.id.shipload_product_quantity)

        init  {
            productQuantity.addTextChangedListener(
                object : TextWatcher {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s.toString() != "")
                            samples[adapterPosition].quantity = s.toString().toInt()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {

                    }

                }
            )
        }
    }

    interface  ItemOnPressListener {
        fun onItemPress(item: ViewHolder, position: Int)
    }
}
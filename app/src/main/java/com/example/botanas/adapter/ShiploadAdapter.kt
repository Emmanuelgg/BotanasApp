package com.example.botanas.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.LoadUpFragment
import com.example.botanas.R
import com.example.botanas.dataClasses.Storage
import java.util.ArrayList


class ShiploadAdapter(private val samples: ArrayList<Storage>, listener: ItemOnPressListener, clickListener: ItemOnClickListener) : RecyclerView.Adapter<ShiploadAdapter.ViewHolder>() {

    private val onItemOnPressListener: ItemOnPressListener = listener
    private val onItemOnClickListener: ItemOnClickListener = clickListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.shipload_product_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        var quantity = item.quantity.toString()
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
        holder.itemView.setOnClickListener { View ->
            onItemOnClickListener.onItemClick(holder, position)
        }

        LoadUpFragment.changeColorByStore(holder, position)

    }


    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val saleProductName: TextView = mView.findViewById(R.id.shipload_product_name)
        val productQuantity: EditText = mView.findViewById(R.id.shipload_product_quantity)
        val shipLoadProductLayout: CardView = mView.findViewById(R.id.shipload_product_layout)

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

    interface  ItemOnClickListener {
        fun onItemClick(item: ViewHolder, position: Int)
    }
}
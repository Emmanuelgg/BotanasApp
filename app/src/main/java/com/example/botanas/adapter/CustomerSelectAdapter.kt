package com.example.botanas.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select
import java.text.NumberFormat


class CustomerSelectAdapter(private val samples: ArrayList<Storage>, listener: ItemClickListener, color: Int = 0, text_color: Int = 0, parentPosition: Int = -1) :
    RecyclerView.Adapter<CustomerSelectAdapter.ViewHolder>() {
    private val onItemClickListener: ItemClickListener = listener
    private val parentPosition = parentPosition
    private lateinit var context: Context
    private lateinit var mySqlHelper: MySqlHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        mySqlHelper = MySqlHelper(context)
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.customer_product_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        val total = item.price.toDouble() * item.quantity.toDouble()
        val currency = NumberFormat.getCurrencyInstance()
        val idStore = item.id_store
        if (idStore != 0) {
            var color = ""
            //var darkColor = ""
            mySqlHelper.use {
                select("store", "color, dark_color")
                    .whereArgs("id_store == {id_store}", "id_store" to idStore)
                    .exec {
                        this.moveToNext()
                        color = this.getString(this.getColumnIndex("color"))
                        //darkColor = this.getString(this.getColumnIndex("dark_color"))
                    }
            }
            holder.customerProductItemContainer.setBackgroundColor(Color.parseColor(color))
            //holder.productName.setTextColor(Color.parseColor(darkColor))
        }

        holder.quantity.text = "${item.quantity}x"
        holder.productName.text = item.product_name
        holder.price.text = currency.format(item.price.toDouble())
        holder.totalCost.text = currency.format(total)

        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position, parentPosition)
        }


    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val quantity: TextView = mView.findViewById(R.id.sale_product_quantity)
        val productName: TextView = mView.findViewById(R.id.sale_product_name)
        val price: TextView = mView.findViewById(R.id.sale_product_price)
        val totalCost: TextView = mView.findViewById(R.id.sale_product_total_cost)
        val customerProductItemContainer: TableLayout = mView.findViewById(R.id.customer_product_item_container)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int, parentPosition: Int)
    }
}

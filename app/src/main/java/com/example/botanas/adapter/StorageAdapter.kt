package com.example.botanas.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.SaleFragment
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import java.lang.Exception

class ProductTypeAdapter(private val samples: ArrayList<ProductType>, listener: ItemClickListener, saleFragment: SaleFragment? = null) : RecyclerView.Adapter<ProductTypeAdapter.ViewHolder>(), StorageAdapter.ItemClickListener {
    private val onItemClickListener: ItemClickListener = listener
    private val viewPool = RecyclerView.RecycledViewPool()
    private lateinit var mySqlHelper: MySqlHelper
    private val saleFragment = saleFragment
    override fun onItemClick(item: StorageAdapter.ViewHolder, position: Int, parentPosition: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("click", item.s_product_name.text.toString())
        Log.d("parentPosition", parentPosition.toString())
        Log.d("position", position.toString())
        if (saleFragment != null)
            saleFragment.onProductClick(item,position,parentPosition)
    }



    override fun onBindViewHolder(holder: ProductTypeAdapter.ViewHolder, position: Int) {
        val item = samples[position]
        holder.s_header.text = item.description
        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position)
        }
        var new_color = item.color
        if (new_color == "null" || new_color == "")
            new_color = "#2ECC71"
        val color = Color.parseColor(new_color)

        var new_text_color = item.text_color
        if (new_text_color == "null" || new_text_color == "")
            new_text_color = "#186A3B"
        val textColor = Color.parseColor(new_text_color)

        holder.s_header.setBackgroundColor(color)
        holder.s_header.setTextColor(textColor)


        mySqlHelper = MySqlHelper(holder.products_recycler.context)
        val query = "SELECT dgi.id_driver_general_inventory, p.id_product, dgi.product_name, dgi.quantity, dgi.unit_measurement " +
                "FROM driver_general_inventory AS dgi " +
                "INNER JOIN product AS p ON dgi.id_product = p.id_product " +
                "WHERE p.id_product_type = ${item.id_product_type} " +
                "AND dgi.quantity != 0"
        try {
            item.products?.clear()
            mySqlHelper.use {
                val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
                if (cursor.count > 0)
                    for (i in 0 until cursor.count) {
                        cursor.moveToNext()
                        item.products.add(
                            Storage(
                                cursor.getInt(cursor.getColumnIndex("id_driver_general_inventory")),
                                cursor.getInt(cursor.getColumnIndex("id_product")),
                                cursor.getString(cursor.getColumnIndex("product_name")),
                                cursor.getInt(cursor.getColumnIndex("quantity")),
                                cursor.getInt(cursor.getColumnIndex("unit_measurement"))
                            )
                        )
                    }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.products_recycler.apply {
            this.layoutManager = LinearLayoutManager(holder.products_recycler.context)
            this.adapter = StorageAdapter(item.products, this@ProductTypeAdapter, color, textColor, position)
            setRecycledViewPool(viewPool)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.storage_header, parent, false))
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val s_header: TextView = mView.findViewById(R.id.s_header)
        val products_recycler: RecyclerView = mView.findViewById(R.id.products_recycler)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int)
    }
}


class StorageAdapter(private val samples: ArrayList<Storage>, listener: ItemClickListener, color: Int = 0, text_color: Int = 0, parentPosition: Int = -1) :
    RecyclerView.Adapter<StorageAdapter.ViewHolder>() {
    private val color = color
    private val text_color = text_color
    private val onItemClickListener: ItemClickListener = listener
    private val parentPosition = parentPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.storage_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        holder.s_quantity.text = item.quantity.toString()
        if (color != 0 && text_color != 0){
            val drawable= holder.s_quantity.background.mutate() as GradientDrawable
            drawable.setColor(color)
            holder.s_quantity.setTextColor(text_color)
        }
        holder.s_product_name.text = item.product_name
        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position, parentPosition)
        }
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val s_quantity: TextView = mView.findViewById(R.id.s_quantity)
        val s_product_name: TextView = mView.findViewById(R.id.s_name_product)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int, parentPosition: Int)
    }
}
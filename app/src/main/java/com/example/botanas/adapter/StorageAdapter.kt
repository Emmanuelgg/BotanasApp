package com.example.botanas.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.LoadUpFragment
import com.example.botanas.R
import com.example.botanas.SellFragment
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import java.lang.Exception
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class ProductTypeAdapter(private val samples: ArrayList<ProductType>, listener: ItemClickListener, sellFragment: SellFragment? = null, loadUpFragment: LoadUpFragment? = null) :
    RecyclerView.Adapter<ProductTypeAdapter.ViewHolder>(), StorageAdapter.ItemClickListener, Filterable {
    private val onItemClickListener: ItemClickListener = listener
    private val viewPool = RecyclerView.RecycledViewPool()
    private lateinit var mySqlHelper: MySqlHelper
    private val saleFragment = sellFragment
    private val driverShiploadFragment = loadUpFragment
    private var productTypeSearchList: List<ProductType>? = null

    override fun onItemClick(item: StorageAdapter.ViewHolder, position: Int, parentPosition: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("click", item.sProductName.text.toString())
        Log.d("parentPosition", parentPosition.toString())
        Log.d("position", position.toString())
        if (saleFragment != null)
            saleFragment.onProductClick(item,position,parentPosition)
        else if(driverShiploadFragment != null)
            driverShiploadFragment.onProductClick(item,position,parentPosition)
    }

    init {
        this.productTypeSearchList = samples
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = productTypeSearchList!![position]
        holder.s_header.text = item.description
        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position)
        }
        var new_color = item.color
        if (new_color == "null" || new_color == "")
            new_color = "#FFEBEE"
        val color = Color.parseColor(new_color)

        var new_text_color = item.text_color
        if (new_text_color == "null" || new_text_color == "")
            new_text_color = "#B71C1C"
        val textColor = Color.parseColor(new_text_color)

        holder.s_header.setBackgroundColor(color)
        holder.s_header.setTextColor(textColor)
        holder.products_recycler.setBackgroundColor(color)


        mySqlHelper = MySqlHelper(holder.products_recycler.context)
        val query =  if (!item.all) {
            "SELECT dgi.id_driver_general_inventory, p.id_product, dgi.product_name, p.short_name as short_name, dgi.quantity, dgi.unit_measurement, p.cost, pp.price " +
                    "FROM driver_general_inventory AS dgi " +
                    "INNER JOIN product AS p ON dgi.id_product = p.id_product " +
                    "INNER JOIN product_price AS pp ON pp.id_product = p.id_product " +
                    "WHERE p.id_product_type = ${item.id_product_type} " +
                    "AND dgi.quantity != 0 " +
                    "AND pp.id_price = 5"
        } else {
            "SELECT 0 as id_driver_general_inventory, 0 as quantity, p.id_product, p.name as product_name, p.short_name as short_name, p.quantity_unit_measurement as unit_measurement, p.cost, pp.price " +
                    "FROM product AS p " +
                    "INNER JOIN product_price AS pp ON pp.id_product = p.id_product " +
                    "WHERE p.id_product_type = ${item.id_product_type} " +
                    "AND pp.id_price = 5"
        }

        try {
            item.products?.clear()
            mySqlHelper.use {
                val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
                if (cursor.count > 0)
                    for (i in 0 until cursor.count) {
                        cursor.moveToNext()
                        var productName = cursor.getString(cursor.getColumnIndex("short_name"))
                        productName = if (productName == "null") cursor.getString(cursor.getColumnIndex("product_name")) else productName
                        item.products.add(
                            Storage(
                                cursor.getInt(cursor.getColumnIndex("id_driver_general_inventory")),
                                cursor.getInt(cursor.getColumnIndex("id_product")),
                                productName,
                                cursor.getInt(cursor.getColumnIndex("quantity")),
                                cursor.getInt(cursor.getColumnIndex("unit_measurement")),
                                cursor.getString(cursor.getColumnIndex("cost")),
                                "",
                                "",
                                cursor.getString(cursor.getColumnIndex("price"))
                            )
                        )
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val pAdapter = StorageAdapter(item.products, this@ProductTypeAdapter, color, textColor, position, item.description)
        holder.products_recycler.apply {
            this.layoutManager = LinearLayoutManager(holder.products_recycler.context)
            this.adapter = pAdapter
            setRecycledViewPool(viewPool)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.storage_header, parent, false))
    }

    override fun getItemCount() = productTypeSearchList!!.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase(Locale.forLanguageTag("la"))
                if (charString.isEmpty()) {
                    productTypeSearchList = samples
                } else {
                    val filteredList = ArrayList<ProductType>()
                    var contain: Boolean
                    for (row in samples) {
                        contain = false
                        for (item in row.products) {
                            if (row.description.toLowerCase().contains(charString) || item.product_name.toLowerCase().contains(charString)) {
                                contain = true
                            }
                        }
                        if (contain) filteredList.add(row)
                    }
                    productTypeSearchList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = productTypeSearchList
                return filterResults
            }
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                productTypeSearchList = filterResults.values as ArrayList<ProductType>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val s_header: TextView = mView.findViewById(R.id.s_header)
        val products_recycler: RecyclerView = mView.findViewById(R.id.products_recycler)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int)
    }
}

class StorageAdapter(private val samples: ArrayList<Storage>, listener: ItemClickListener, color: Int = 0, text_color: Int = 0, parentPosition: Int = -1, productType:String = "") :
    RecyclerView.Adapter<StorageAdapter.ViewHolder>() {
    private val color = color
    private val textColor = text_color
    private val onItemClickListener: ItemClickListener = listener
    private val parentPosition = parentPosition
    private val productType = productType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.storage_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        val currency = NumberFormat.getCurrencyInstance()
        holder.sQuantity.text = item.quantity.toString()
        holder.sProductPrice.text = currency.format(item.price.toDouble())
        holder.sProductName.text = item.product_name

        val imageResoruce = when(productType) {
            "Bulto Anita" -> R.drawable.fritura
            "Churro" -> R.drawable.churro
            "Cheto" -> R.drawable.cheto
            "Chicharron" -> R.drawable.chicharron
            "Churrico" -> R.drawable.churrico
            "Churrihuekito" -> R.drawable.churri_huekitos
            "Granel" -> R.drawable.fritura
            "Otro ingreso" -> R.drawable.ic_snack
            "Paloma Maiz" -> R.drawable.paloma
            "Papa" -> R.drawable.papa
            "Tostianita" -> R.drawable.nachos
            "Tostada" -> R.drawable.tostada
            "Cheto Torcido" -> R.drawable.cheto
            else -> R.drawable.ic_snack
        }

        holder.sProductImage.setImageResource(imageResoruce)
        if (color != 0 && textColor != 0){
            //val drawable= holder.s_quantity.background.mutate() as GradientDrawable
            //drawable.setColor(color)
            //holder.s_quantity.setTextColor(textColor)
            //holder.cardView.setCardBackgroundColor(color)
            //holder.s_product_name.setTextColor(textColor)
        }

        holder.itemView.setOnClickListener {
                View -> onItemClickListener.onItemClick(holder, position, parentPosition)
        }
    }

    override fun getItemCount() = samples.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val sQuantity: TextView = mView.findViewById(R.id.s_quantity)
        val sProductName: TextView = mView.findViewById(R.id.s_name_product)
        val sProductPrice: TextView = mView.findViewById(R.id.s_product_price)
        val sProductImage: ImageView = mView.findViewById(R.id.s_product_image)
        //val cardView: CardView = mView.findViewById(R.id.s_product_container)
    }

    interface ItemClickListener {
        fun onItemClick(item: ViewHolder, position: Int, parentPosition: Int)
    }
}
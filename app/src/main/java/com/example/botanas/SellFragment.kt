package com.example.botanas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.ProductListSale
import com.example.botanas.adapter.ProductTypeAdapter
import com.example.botanas.adapter.StorageAdapter
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SellFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SellFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class SellFragment : Fragment(), ProductTypeAdapter.ItemClickListener, ProductListSale.ItemOnPressListener {


    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    // private val allProductList: ArrayList<Storage> = ArrayList()
    private val categoryList: ArrayList<ProductType> = ArrayList()
    private val productListSale: ArrayList<Storage> = ArrayList()
    private lateinit var productListAdapter: ProductTypeAdapter
    private lateinit var productListSaleAdapter: ProductListSale
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context
    private lateinit var productListRecycler: RecyclerView
    private lateinit var productListSaleRecycler: RecyclerView

    override fun onItemClick(item: ProductTypeAdapter.ViewHolder, position: Int) {
        // Toast.makeText(this.context, "Item numero: $position, Tipo de producto: ${item.s_header.text}", Toast.LENGTH_SHORT).show()
    }


    fun onProductClick(item: StorageAdapter.ViewHolder, position: Int, parentPosition: Int){
        //Toast.makeText(this.context, "Item numero: $position, Producto: ${item.s_product_name.text}", Toast.LENGTH_SHORT).show()
        Log.d("parentPosition:", parentPosition.toString())
        Log.d("position:", position.toString())
        if (parentPosition != -1) {
            val item = categoryList[parentPosition].products[position]
            val exist = productListSale.find {
                storage ->  storage.id_product == categoryList[parentPosition].products[position].id_product
            }
            Log.d("producto:", exist.toString())
            if (exist == null){
                productListSale.add(
                    Storage(
                        item.id_driver_general_inventory,
                        item.id_product,
                        item.product_name,
                        0,
                        item.quantity_unit_measurement
                    )
                )
                productListSaleRecycler.adapter!!.notifyDataSetChanged()
            } else {
                Snackbar.make(this.view!!, R.string.product_exist, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            }

            //categoryList[parentPosition].products.remove(categoryList[parentPosition].products[position])
            //productListRecycler.adapter!!.notifyDataSetChanged()
            //Snackbar.make(this.view!!, R.string.product_added, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }



    /*override fun onItemClick(item: StorageAdapter.ViewHolder, position: Int, parentPosition: Int) {
        Toast.makeText(this.context, "Item numero: $position, Producto: ${item.s_product_name.text}", Toast.LENGTH_SHORT).show()
        Log.d("parentPosition:", parentPosition.toString())
        Log.d("position:", position.toString())
        if (parentPosition != 0) {
            val item = categoryList[parentPosition].products[position]

            productListSale.add(
                Storage(
                    item.id_driver_general_inventory,
                    item.id_product,
                    item.product_name,
                    0,
                    item.unit_measurement
                )
            )
            productListSaleRecycler.adapter!!.notifyDataSetChanged()
            categoryList.remove(categoryList[position])
            productListRecycler.adapter!!.notifyDataSetChanged()
            Snackbar.make(this.view!!, R.string.product_added, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }

    }*/

    override fun onItemPress(item: ProductListSale.ViewHolder, position: Int) {
        productListSale.remove(productListSale[position])
        productListSaleRecycler.adapter!!.notifyDataSetChanged()
        Snackbar.make(this.view!!, R.string.product_deleted, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        initRecycleView()
        productListAdapter = ProductTypeAdapter(categoryList,this,this)
        productListSaleAdapter = ProductListSale(productListSale,this)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sell, container, false)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.menu_sell)
        productListRecycler = view.findViewById(R.id.productListRecycle) as RecyclerView
        productListRecycler.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = productListAdapter
        }

        productListSaleRecycler = view.findViewById(R.id.produtListSaleRecycle) as RecyclerView
        productListSaleRecycler.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = productListSaleAdapter
        }

        //val itemTouchHelper = ItemTouchHelper(SwipeController())
        //itemTouchHelper.attachToRecyclerView(productListSaleRecycler)


        val btnCustomerSelecction = view.findViewById<FloatingActionButton>(R.id.btnCustomerSeleccion)
        btnCustomerSelecction.setOnClickListener {
            var isSuccess = true
            val hasProducts = productListSale.size > 0
            if (hasProducts){
                for (product in productListSale) {
                    if (product.quantity.toInt() <= 0) {
                        isSuccess = false
                    }
                }
                if (isSuccess){
                    val intent = Intent(context, CustomerSelectionActivity::class.java)
                    intent.putExtra("product_list", productListSale as Serializable)
                    startActivity(intent)
                } else {
                    Snackbar.make(this.view!!, "Las cantidades de los productos deben ser mayores a 0", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
                }
            } else {
                Snackbar.make(this.view!!, "Debe contener almenos un producto en la lista de venta", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            }


        }

        val btnShowAll = view.findViewById<Button>(R.id.showAll)
        var showAll = false
        btnShowAll.setOnClickListener {
            showAll = !showAll
            btnShowAll.text = if (showAll) getString(R.string.show_existing) else getString(R.string.show_all)
            initRecycleView(showAll)
        }
            return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SellFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    fun initRecycleView(all: Boolean = false) {
        categoryList.clear()
        val query = if (!all) {
            "SELECT pt.id_product_type, pt.name, pt.description, pt.color, pt.text_color, SUM(dgi.quantity) quantity " +
                    "FROM product AS p " +
                    "INNER JOIN driver_general_inventory AS dgi ON dgi.id_product = p.id_product " +
                    "INNER JOIN product_type AS pt ON p.id_product_type = pt.id_product_type " +
                    "WHERE dgi.quantity > 0 " +
                    "GROUP BY pt.id_product_type"
        } else {
            "SELECT id_product_type, name, description, color, text_color " +
                    "FROM product_type"
        }
        mySqlHelper.use {
            val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
            while (cursor.moveToNext()) {
                categoryList.add(
                    ProductType(
                        cursor.getInt(cursor.getColumnIndex("id_product_type")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("description")),
                        cursor.getString(cursor.getColumnIndex("color")),
                        cursor.getString(cursor.getColumnIndex("text_color")),
                        all
                    )
                )
            }
            if (cursor.count <= 0) {
                categoryList.add(
                    ProductType(
                        0,
                        "",
                        "No se encontraron productos en el inventario, favor de sincronizar.",
                        "",
                        ""
                    )
                )
            }
        }
        try {
            productListRecycler.adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {

        }

        /*mySqlHelper.use {
            select("driver_general_inventory")
                .whereArgs("(quantity != {quantity})",
                    "quantity" to 0).exec {
                    if (this.count > 0)
                        for (i in 0 until this.count) {
                            this.moveToNext()
                            productList.add(
                                    Storage(
                                    this.getInt(this.getColumnIndex("id_driver_general_inventory")),
                                    this.getInt(this.getColumnIndex("id_product")),
                                    this.getString(this.getColumnIndex("product_name")),
                                    this.getInt(this.getColumnIndex("quantity")),
                                    this.getInt(this.getColumnIndex("unit_measurement"))
                                )
                            )
                            allProductList.add(
                                Storage(
                                    this.getInt(this.getColumnIndex("id_driver_general_inventory")),
                                    this.getInt(this.getColumnIndex("id_product")),
                                    this.getString(this.getColumnIndex("product_name")),
                                    this.getInt(this.getColumnIndex("quantity")),
                                    this.getInt(this.getColumnIndex("unit_measurement"))
                                )
                            )
                        }
                    else
                        productList.add(
                            Storage (
                                0,
                                0,
                                "No cuenta con inventario, sincronice para verificar",
                                0,
                                0
                            )
                        )
                    allProductList.add(
                            Storage (
                                0,
                                0,
                                "No cuenta con inventario, sincronice para verificar",
                                0,
                                0
                            )
                        )


                }
        }
        productList.sortBy {
                storage -> storage.product_name
        }*/
    }
}
package com.example.botanas

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.ProductTypeAdapter
import com.example.botanas.adapter.ShiploadAdapter
import com.example.botanas.adapter.StorageAdapter
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.example.botanas.utils.StorageColorDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.select
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DriverShiploadFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DriverShiploadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DriverShiploadFragment : Fragment(), ProductTypeAdapter.ItemClickListener, ShiploadAdapter.ItemOnPressListener, ShiploadAdapter.ItemOnClickListener {
    //override fun onItemClick(item: StoreColorAdapter.ViewHolder, position: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    //}


    override fun onItemPress(item: ShiploadAdapter.ViewHolder, position: Int) {
        shiploadList.remove(shiploadList[position])
        driverShiploadRecycler.adapter!!.notifyDataSetChanged()
        driverShiploadRecycler.adapter!!.notifyItemChanged(position)
        Snackbar.make(this.view!!, R.string.product_deleted, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }

    override fun onItemClick(item: ShiploadAdapter.ViewHolder, position: Int) {
        val actualItem = shiploadList[item.adapterPosition]
        actualItem.id_store++
        Log.d("clickpo", position.toString())
        changeColorByStore(item, position,true)
    }

    override fun onItemClick(item: ProductTypeAdapter.ViewHolder, position: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Rename and change types of parameters
    // private val allProductList: ArrayList<Storage> = ArrayList()
    private val categoryList: ArrayList<ProductType> = ArrayList()
    private lateinit var productListAdapter: ProductTypeAdapter
    private lateinit var shiploadAdapter: ShiploadAdapter

    private lateinit var appContext: Context
    private lateinit var onlyProductListRecycler: RecyclerView
    private lateinit var showStoreColorList: FloatingActionButton
    private lateinit var storageColorDialog: StorageColorDialog

    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null

    fun onProductClick(item: StorageAdapter.ViewHolder, position: Int, parentPosition: Int){
        //Toast.makeText(this.context, "Item numero: $position, Producto: ${item.s_product_name.text}", Toast.LENGTH_SHORT).show()
        if (parentPosition != -1) {
            val item = categoryList[parentPosition].products[position]
            val exist = shiploadList.find {
                    storage ->  storage.id_product == categoryList[parentPosition].products[position].id_product
            }
            if (exist == null){

                val idStore = if (shiploadList.isNotEmpty())  shiploadList.last().id_store else 1

                shiploadList.add(
                    Storage(
                        item.id_driver_general_inventory,
                        item.id_product,
                        item.product_name,
                        0,
                        item.quantity_unit_measurement,
                        id_store = idStore
                    )
                )

                driverShiploadRecycler.adapter!!.notifyDataSetChanged()
            } else {
                Snackbar.make(this.view!!, R.string.product_exist, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            }

            //categoryList[parentPosition].products.remove(categoryList[parentPosition].products[position])
            //onlyProductListRecycler.adapter!!.notifyDataSetChanged()
            //Snackbar.make(this.view!!, R.string.product_added, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        initRecycleView()
        productListAdapter = ProductTypeAdapter(categoryList,this,null, this)
        shiploadAdapter = ShiploadAdapter(shiploadList,this, this)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_driver_shipload, container, false)
        val toolbar = (activity as AppCompatActivity).supportActionBar!!
        toolbar.title = getString(R.string.driver_shipload_title)
        onlyProductListRecycler = view.findViewById(R.id.onlyProductListRecycler) as RecyclerView
        onlyProductListRecycler.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = productListAdapter
        }

        driverShiploadRecycler = view.findViewById(R.id.driverShiploadRecycler) as RecyclerView
        driverShiploadRecycler.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = shiploadAdapter
        }

        driverShiploadRecycler.adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                super.onChanged()
                val count = driverShiploadRecycler.layoutManager!!.itemCount
                for (i in 0..count) {
                    //val refresh = driverShiploadRecycler.layoutManager!!.findViewByPosition(i)
                    //changeColorByStore(refresh)
                }
            }
        })

        val btnReviewShipload = view.findViewById<FloatingActionButton>(R.id.btnReviewShipload)
        btnReviewShipload.setOnClickListener {
            var isSuccess = true
            val hasProducts = shiploadList.size > 0
            if (hasProducts){
                for (product in shiploadList) {
                    if (product.quantity.toInt() <= 0) {
                        isSuccess = false
                    }
                }
                if (isSuccess){
                    //val intent = Intent(context, ReviewShiploadActivity::class.java)
                    //intent.putExtra("shipload_list", shiploadList as Serializable)
                    //startActivity(intent)
                    Log.d("array", shiploadList.toString())
                } else {
                    Snackbar.make(this.view!!, "Las cantidades de los productos deben ser mayores a 0", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
                }
            } else {
                Snackbar.make(this.view!!, "Debe contener almenos un producto en la lista de orden de carga", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
            }


        }
        storageColorDialog = StorageColorDialog(appContext)
        showStoreColorList = view.findViewById(R.id.showStoreColorList)
        showStoreColorList.setOnClickListener {
            storageColorDialog.showDialog()
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DriverShiploadFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            DriverShiploadFragment().apply {
                arguments = Bundle().apply {

                }
            }
        private lateinit var mySqlHelper: MySqlHelper
        private lateinit var driverShiploadRecycler: RecyclerView
        private val shiploadList: ArrayList<Storage> = ArrayList()
        fun changeColorByStore(item: ShiploadAdapter.ViewHolder, position: Int, click:Boolean = false) {
            try {
                val actualItem = shiploadList[position]
                Log.d("try to change color to", actualItem.product_name)
                mySqlHelper.use {
                    if  (!click) {
                        select("store", "MAX(id_store) id_store")
                            .exec {
                                this.moveToNext()
                                val idStore = this.getInt(this.getColumnIndex("id_store"))
                                if (actualItem.id_store > idStore) {
                                    actualItem.id_store = 1
                                }
                            }
                    }

                    select("store")
                        .whereArgs("id_store == {id_store} AND color != {color}", "id_store" to actualItem.id_store, "color" to "null")
                        .exec {
                            this.moveToNext()
                            if  (this.count > 0) {
                                val color = Color.parseColor(this.getString(this.getColumnIndex("color")))
                                item.productQuantity.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                                item.shipLoadProductLayout.setBackgroundColor(color)
                                Log.d("change color to", actualItem.product_name)
                            } else {
                                actualItem.id_store++
                                changeColorByStore(item, position)
                            }


                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun initRecycleView() {
        categoryList.clear()
        val query =
            "SELECT id_product_type, name, description, color, text_color " +
                    "FROM product_type"

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
                        true
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
            cursor.close()
        }
        try {
            onlyProductListRecycler.adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {

        }
    }
}

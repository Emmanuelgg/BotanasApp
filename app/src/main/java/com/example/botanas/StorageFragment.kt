package com.example.botanas

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.ProductTypeAdapter
import com.example.botanas.api.StorageApi
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.collections.ArrayList
import androidx.appcompat.app.AppCompatActivity



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [StorageFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [StorageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

/**
 *StorageFragment class with click listener and implements StorageAdapter to fill StorageRecycleView
 */
class StorageFragment : Fragment(), ProductTypeAdapter.ItemClickListener {
    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    private val categoryList: ArrayList<ProductType> = ArrayList()
    private lateinit var adapterData: ProductTypeAdapter
    private lateinit var storageRecycler: RecyclerView
    private lateinit var apiClient: StorageApi
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context
    private lateinit var mainActivity: MainActivity


    override fun onItemClick(item: ProductTypeAdapter.ViewHolder, position: Int) {
        //Toast.makeText(this.context, "Item numero: $position, Tipo de producto: ${item.s_header.text}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        initRecycleView()
        adapterData = ProductTypeAdapter(categoryList,this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Set the adapter
        val view = inflater.inflate(R.layout.fragment_storage, container, false)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.menu_inventory)
        storageRecycler = view.findViewById(R.id.storage_recycler) as RecyclerView
        storageRecycler.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = adapterData
        }
        val btn_sync: FloatingActionButton = view.findViewById(R.id.btn_sync) as FloatingActionButton
        btn_sync.setOnClickListener{
            apiClient.requestGetInventory(Admin.idAdmin, categoryList, storageRecycler)
        }
        apiClient = StorageApi(appContext, mainActivity)

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
        fun newInstance(activity: MainActivity) =
            StorageFragment().apply {
                arguments = Bundle().apply {
                     mainActivity = activity
                }
            }
    }

    fun initRecycleView() {

        val query = "SELECT pt.id_product_type, pt.name, pt.description, pt.color, pt.text_color, SUM(dgi.quantity) quantity " +
                "FROM product AS p " +
                "INNER JOIN driver_general_inventory AS dgi ON dgi.id_product = p.id_product " +
                "INNER JOIN product_type AS pt ON p.id_product_type = pt.id_product_type " +
                "WHERE dgi.quantity > 0 " +
                "GROUP BY pt.id_product_type"
        mySqlHelper.use {
            val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
            while (cursor.moveToNext()) {
                categoryList.add(
                    ProductType(
                        cursor.getInt(cursor.getColumnIndex("id_product_type")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("description")),
                        cursor.getString(cursor.getColumnIndex("color")),
                        cursor.getString(cursor.getColumnIndex("text_color"))
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

    }
}
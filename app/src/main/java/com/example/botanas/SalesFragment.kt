package com.example.botanas

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.SalesAdapter
import com.example.botanas.api.SalesApi
import com.example.botanas.dataClasses.Requisition
import com.example.botanas.db.MySqlHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.select
import java.lang.Exception
import java.text.NumberFormat

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SalesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SalesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SalesFragment : Fragment(), SalesAdapter.ItemOnPressListener {


    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mySqlHelper: MySqlHelper
    private val requisitionList = ArrayList<Requisition>()
    private lateinit var appContext: Context
    private lateinit var salesRecyclerView: RecyclerView
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var salesApi: SalesApi
    private lateinit var mainActivity: MainActivity
    private lateinit var actualView: View

    override fun onItemClick(item: SalesAdapter.ViewHolder, position: Int) {
        try {
            val intent = Intent(appContext, SaleDetail::class.java)
            intent.putExtra("id_requisition", requisitionList[position].id_requisition)
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(actualView, R.string.sale_not_found, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        initRecycleView()
        salesAdapter = SalesAdapter(requisitionList,this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        actualView = inflater.inflate(R.layout.fragment_sales, container, false)
        val toolbar = (activity as AppCompatActivity).supportActionBar!!
        toolbar.title = getString(R.string.menu_sales)
        toolbar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FF303F9F")))
        salesApi = SalesApi(appContext, mainActivity)

        val btnSalesSync = actualView.findViewById<FloatingActionButton>(R.id.btn_sales_sync)

        salesRecyclerView = actualView.findViewById(R.id.sales_recycler_view)
        salesRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = salesAdapter
        }

        val builder = AlertDialog.Builder(appContext)
        initAlertDialog(builder)

        btnSalesSync.setOnClickListener {
            builder.show()
        }

        val currency = NumberFormat.getCurrencyInstance()
        var totalSales = 0.0
        requisitionList.forEach { item -> totalSales += item.total.toDouble() }
        (actualView.findViewById<TextView>(R.id.textTotalSales)).text = currency.format(totalSales)


            return actualView
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }*/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.sales_sync_confirm_title)
        builder.setMessage(R.string.sales_sync_confirm_body)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { _, _ ->
            salesApi.run {
                if (requestPostSyncSales()){
                    requisitionList.clear()
                    requisitionList.add(
                        Requisition(
                            0,
                            0,
                            "No se han realizado ventas",
                            "",
                            "0",
                            ""
                        )
                    )
                    salesRecyclerView.adapter!!.notifyDataSetChanged()
                }
            }

        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(actualView, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(activity: MainActivity) =
            SalesFragment().apply {
                arguments = Bundle().apply {
                    mainActivity = activity
                }
            }
    }

    private fun initRecycleView(){
        requisitionList.clear()
        mySqlHelper.use {
            select("requisition")
                .exec {
                    if  (this.count > 0)
                        while(this.moveToNext()) {
                            var clientName = ""
                            val idClient = this.getInt(this.getColumnIndex("id_client"))
                            select("client", "name, municipality")
                                .whereArgs("id_client == {id_client}",
                                    "id_client" to idClient
                                ).exec {
                                    this.moveToNext()
                                    clientName = this.getString(this.getColumnIndex("name")) + " - " + this.getString(this.getColumnIndex("municipality"))

                                }
                            var total = this.getDouble(this.getColumnIndex("total"))
                            total -= (total*this.getDouble(this.getColumnIndex("discount"))/100)
                            requisitionList.add(
                                Requisition(
                                    this.getInt(this.getColumnIndex("id_requisition")),
                                    idClient,
                                    clientName,
                                    this.getString(this.getColumnIndex("created_at")),
                                    total.toString(),
                                    this.getString(this.getColumnIndex("discount"))
                                )
                            )
                        }
                    else
                        requisitionList.add(
                            Requisition(
                                0,
                                0,
                                "No se han realizado ventas",
                                "",
                                "0",
                                ""
                            )
                        )
                }
        }
        try {
            salesRecyclerView.adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {

        }
    }
}

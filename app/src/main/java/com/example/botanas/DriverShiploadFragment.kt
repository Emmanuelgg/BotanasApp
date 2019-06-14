package com.example.botanas

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.DriverShiploadAdapter
import com.example.botanas.api.SalesApi
import com.example.botanas.api.ShiploadApi
import com.example.botanas.dataClasses.DriverShipload
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.select
import java.lang.Exception
import java.text.NumberFormat

class DriverShiploadFragment : Fragment(), DriverShiploadAdapter.ItemOnPressListener {

    override fun onItemClick(item: DriverShiploadAdapter.ViewHolder, position: Int) {
        try {
            val intent = Intent(appContext, SaleDetail::class.java)
            intent.putExtra("id_driver_shipload", driverShiploadList[position].id_driver_shipload)
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(actualView, R.string.sale_not_found, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }


    private var listener: OnFragmentInteractionListener? = null

    private lateinit var mySqlHelper: MySqlHelper
    private val driverShiploadList = ArrayList<DriverShipload>()
    private lateinit var appContext: Context
    private lateinit var driverShiploadRecyclerView: RecyclerView
    private lateinit var driverShiploadAdapter: DriverShiploadAdapter
    private lateinit var shiploadApi: ShiploadApi
    private lateinit var mainActivity: MainActivity
    private lateinit var actualView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        driverShiploadList.clear()
        initRecycleView()
        driverShiploadAdapter = DriverShiploadAdapter(driverShiploadList,this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actualView = inflater.inflate(R.layout.fragment_driver_shipload, container, false)
        val toolbar = (activity as AppCompatActivity).supportActionBar!!
        toolbar.title = getString(R.string.shipload_title)
        toolbar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FF303F9F")))
        shiploadApi = ShiploadApi(appContext, mainActivity)

        val btnDriverShiploadSync = actualView.findViewById<FloatingActionButton>(R.id.btnDriverShiploadSync)

        driverShiploadRecyclerView = actualView.findViewById(R.id.driverShiploadRecyclerView)
        driverShiploadRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = driverShiploadAdapter
        }

        val builder = AlertDialog.Builder(appContext)
        initAlertDialog(builder)

        btnDriverShiploadSync.setOnClickListener {
            builder.show()
        }

        val currency = NumberFormat.getCurrencyInstance()
        var totalSales = 0.0
        driverShiploadList.forEach { item -> totalSales += item.total.toDouble() }
        (actualView.findViewById<TextView>(R.id.textTotalShipload)).text = currency.format(totalSales)

        return actualView
    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.sales_sync_confirm_title)
        builder.setMessage(R.string.sales_sync_confirm_body)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { _, _ ->
            shiploadApi.run {
                if (requestPostSyncShipload()){
                    driverShiploadList.clear()
                    driverShiploadList.add(
                        DriverShipload(
                            0,
                            0,
                            0,
                            "No se han realizado cargamentos de chofer",
                            0,
                            "",
                            "",
                            ""
                        )
                    )
                    driverShiploadRecyclerView.adapter!!.notifyDataSetChanged()
                }
            }

        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(actualView, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }

    private fun initRecycleView() {
        driverShiploadList.clear()
        mySqlHelper.use {
            select("driver_shipload")
                .whereArgs("status != 4")
                .exec {
                    if  (this.count > 0)
                        while(this.moveToNext()) {
                            var clientName = ""
                            val idClient = this.getInt(this.getColumnIndex("id_client"))
                            select("client", "name, municipality")
                                .whereArgs("id_client == {id_client}",
                                    "id_client" to idClient
                                ).exec {
                                    clientName = if  (this.count > 0) {
                                        this.moveToNext()
                                        this.getString(this.getColumnIndex("name")) + " - " + this.getString(this.getColumnIndex("municipality"))
                                    } else {
                                        "Sin cliente"
                                    }

                                }
                            driverShiploadList.add(
                                DriverShipload(
                                    this.getInt(this.getColumnIndex("id_driver_shipload")),
                                    Admin.idAdmin,
                                    idClient,
                                    clientName,
                                    this.getInt(this.getColumnIndex("status")),
                                    this.getString(this.getColumnIndex("total")),
                                    this.getString(this.getColumnIndex("created_at")),
                                    this.getString(this.getColumnIndex("updated_at"))

                                )
                            )
                        }
                    else
                        driverShiploadList.add(
                            DriverShipload(
                                0,
                                0,
                                0,
                                "No se han realizado ventas",
                                0,
                                "",
                                "",
                                ""
                            )
                        )
                }
        }
        try {
            driverShiploadRecyclerView.adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {

        }
    }

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

        @JvmStatic
        fun newInstance() =
            DriverShiploadFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

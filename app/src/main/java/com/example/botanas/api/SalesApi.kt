package com.example.botanas.api

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.botanas.MainActivity
import com.example.botanas.services.Network
import com.example.botanas.R
import com.example.botanas.api.Server.Companion.api_key
import com.example.botanas.dataClasses.Requisition
import com.example.botanas.dataClasses.ServerResponse
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.json.JSONArray


class SalesApi(context: Context, mainActivity: MainActivity) {

    private var url = ""
    private val serverUrl = Server.url

    private var appContext: Context = context
    private val mySqlHelper: MySqlHelper = MySqlHelper(appContext)
    private val progressBar : ProgressBar = mainActivity.findViewById(R.id.progressBar)
    private val view: View = (appContext as Activity).findViewById(R.id.containerFragments)

    fun requestPostSyncSales(list :ArrayList<Requisition>, recyclerView: RecyclerView) {

        val network = Network(appContext)
        if (!network.isConnected()) {
            Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return
        }
        if (list.isEmpty() || (list.size == 1 && list[0].id_requisition == 0)) {
            Snackbar.make(view, R.string.sync_error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return
        }
        val route = "sales_add.php"

        var jsonArrayRequisition = JSONArray()
        mySqlHelper.use {
            for (item in list){
                val jsonRequisition = JSONObject()
                jsonRequisition.put("id_requisition", item.id_requisition)
                jsonRequisition.put("id_admin", Admin.idAdmin)
                jsonRequisition.put("id_client", item.id_client)
                jsonRequisition.put("client_name", item.client_name)
                jsonRequisition.put("date", item.date)
                jsonRequisition.put("total", item.total)
                jsonRequisition.put("discount", item.discount)
                val jsonArrayDescription = JSONArray()
                select("requisition_description")
                    .whereArgs("id_requisition == {id_requisition}", "id_requisition" to item.id_requisition)
                    .exec {
                        if (this.count > 0) {
                            while (this.moveToNext()) {
                                val jsonDescription = JSONObject()
                                jsonDescription.put("id_requisition_description", this.getInt(this.getColumnIndex("id_requisition_description")))
                                jsonDescription.put("id_requisition", this.getInt(this.getColumnIndex("id_requisition")))
                                jsonDescription.put("id_product", this.getInt(this.getColumnIndex("id_product")))
                                jsonDescription.put("price", this.getString(this.getColumnIndex("price")))
                                jsonDescription.put("quantity", this.getInt(this.getColumnIndex("quantity")))
                                jsonDescription.put("weight", this.getString(this.getColumnIndex("weight")))
                                jsonDescription.put("cost", this.getString(this.getColumnIndex("cost")))
                                jsonDescription.put("total", this.getString(this.getColumnIndex("total")))
                                jsonDescription.put("description", this.getString(this.getColumnIndex("description")))
                                jsonDescription.put("quantity_unit_measure", this.getInt(this.getColumnIndex("quantity_unit_measure")))
                                jsonDescription.put("status", this.getInt(this.getColumnIndex("status")))
                                jsonArrayDescription.put(jsonDescription)
                            }
                        }
                    }
                jsonRequisition.put("description", jsonArrayDescription)
                jsonArrayRequisition.put(jsonRequisition)
            }
        }



        progressBar.visibility = View.VISIBLE
        url = "$serverUrl$route?api_key=$api_key&requisitions=$jsonArrayRequisition"

        val jsonArrayRequest = StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val serverResponse = Gson().fromJson(response, ServerResponse::class.java)
                    if (serverResponse.status != 0) {
                        mySqlHelper.use {
                            delete("requisition")
                            delete("requisition_description")
                        }
                        list.clear()
                        list.add(
                            Requisition(
                                0,
                                0,
                                "No se han realizado ventas",
                                "",
                                "0",
                                ""
                            )
                        )
                        recyclerView.adapter!!.notifyDataSetChanged()
                    }
                    Snackbar.make(view, serverResponse.messages, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    progressBar.visibility = View.GONE
                    Log.d("error", serverResponse.error)
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBar.visibility = View.GONE
                }
            },
            Response.ErrorListener { error: VolleyError ->
                println("Error ${error.message}")
                println("Result ${error}")
                Snackbar.make(view, R.string.sync_error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                progressBar.visibility = View.GONE
            }
        )
        val queue = Volley.newRequestQueue(appContext)
        queue.add(jsonArrayRequest)

    }

}
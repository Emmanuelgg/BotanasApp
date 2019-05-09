package com.example.botanas.api

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import org.json.JSONObject
import java.lang.Exception
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.botanas.MainActivity
import com.example.botanas.services.Network
import com.example.botanas.R
import com.example.botanas.api.Server.Companion.api_key
import com.example.botanas.dataClasses.ServerResponse
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.json.JSONArray


class SalesApi(context: Context, mainActivity: MainActivity? = null) {

    private var url = ""
    private val serverUrl = Server.url

    private var appContext: Context = context
    private val mySqlHelper: MySqlHelper = MySqlHelper(appContext)
    private lateinit var progressBar: ProgressBar
    private lateinit var view: View
    private var isBackground: Boolean = false
    private val messagingService = MyFirebaseMessagingService()
    init {
        if (mainActivity != null) {
            progressBar = mainActivity.findViewById(R.id.progressBar)
            view = (appContext as Activity).findViewById(R.id.containerFragments)
        }
        else
            isBackground = true
    }


    fun requestPostSyncSales(): Boolean {

        val network = Network(appContext)
        if (!network.isConnected()) {
            if (!isBackground)
                Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return false
        }
        val route = "sales_add.php"

        val jsonArrayRequisition = JSONArray()
        var hasRows = false
        mySqlHelper.use {
            select("requisition").exec {
                if (this.count > 0) {
                    hasRows = true
                    while (this.moveToNext()) {
                        val jsonRequisition = JSONObject()
                        val idRequisition = this.getInt(this.getColumnIndex("id_requisition"))
                        jsonRequisition.put("id_requisition", idRequisition)
                        jsonRequisition.put("id_admin", Admin.idAdmin)
                        jsonRequisition.put("id_client", this.getInt(this.getColumnIndex("id_client")))
                        jsonRequisition.put("date", this.getString(this.getColumnIndex("created_at")))
                        jsonRequisition.put("total", this.getString(this.getColumnIndex("total")))
                        jsonRequisition.put("discount", this.getString(this.getColumnIndex("discount")))
                        val jsonArrayDescription = JSONArray()
                        select("requisition_description")
                            .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
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
            }
        }
        if (!hasRows) {
            if (!isBackground)
                Snackbar.make(view, R.string.sync_error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return false
        }


        if (!isBackground)
            progressBar.visibility = View.VISIBLE
        else
            messagingService.doInBackgroundNotificationStart(
                appContext.getString(R.string.notification_title_sync_available),
                appContext.getString(R.string.notification_description_sync_available),
                appContext
            )
        var isSuccess = false
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
                        isSuccess = true
                    }
                    if (!isBackground){
                        Snackbar.make(view, serverResponse.messages, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        progressBar.visibility = View.GONE
                    } else {
                        messagingService.sendCustomMessage(
                            appContext.getString(R.string.sync_success),
                            serverResponse.messages,
                            appContext
                        )
                        messagingService.doInBackgroundNotificationStop(appContext)

                    }


                    Log.d("error", serverResponse.error)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (!isBackground)
                        progressBar.visibility = View.GONE
                    else
                        messagingService.doInBackgroundNotificationStop(appContext)
                }
            },
            Response.ErrorListener { error: VolleyError ->
                println("Error ${error.message}")
                println("Result $error")
                if (!isBackground) {
                    Snackbar.make(view, R.string.sync_error, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    progressBar.visibility = View.GONE
                } else
                    messagingService.doInBackgroundNotificationStop(appContext)

            }
        )
        val queue = Volley.newRequestQueue(appContext)
        queue.add(jsonArrayRequest)
        return isSuccess
    }

}
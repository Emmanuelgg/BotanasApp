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
import org.jetbrains.anko.db.update
import org.json.JSONArray


class ShiploadApi(context: Context, mainActivity: MainActivity? = null) {

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


    fun requestPostSyncShipload(): Boolean {

        val network = Network(appContext)
        if (!network.isConnected()) {
            if (!isBackground)
                Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return false
        }
        val route = "driver_shipload_add.php"

        val jsonArrayDriverShipload = JSONArray()
        var hasRows = false
        mySqlHelper.use {
            select("driver_shipload").exec {
                if (this.count > 0) {
                    hasRows = true
                    while (this.moveToNext()) {
                        val jsonDriverShipload = JSONObject()
                        val idDriverShipload = this.getInt(this.getColumnIndex("id_driver_shipload"))
                        val total = this.getDouble(this.getColumnIndex("total"))
                        jsonDriverShipload.put("driver_shipload", idDriverShipload)
                        jsonDriverShipload.put("id_driver", Admin.idAdmin)
                        jsonDriverShipload.put("id_client", this.getInt(this.getColumnIndex("id_client")))
                        jsonDriverShipload.put("status", this.getInt(this.getColumnIndex("id_client")))
                        jsonDriverShipload.put("total", total.toString())
                        jsonDriverShipload.put("created_at", this.getString(this.getColumnIndex("created_at")))
                        jsonDriverShipload.put("updated_at", this.getString(this.getColumnIndex("updated_at")))
                        val jsonArrayDescription = JSONArray()
                        select("driver_inventory")
                            .whereArgs("id_driver_shipload == {id_driver_shipload}", "id_driver_shipload" to idDriverShipload)
                            .exec {
                                if (this.count > 0) {
                                    while (this.moveToNext()) {
                                        val jsonDescription = JSONObject()
                                        jsonDescription.put("id_driver_inventory", this.getInt(this.getColumnIndex("id_driver_inventory")))
                                        jsonDescription.put("id_driver_shipload", this.getInt(this.getColumnIndex("id_driver_shipload")))
                                        jsonDescription.put("id_product", this.getInt(this.getColumnIndex("id_product")))
                                        jsonDescription.put("id_store", this.getInt(this.getColumnIndex("id_store")))
                                        jsonDescription.put("barcode", this.getString(this.getColumnIndex("barcode")))
                                        jsonDescription.put("description", this.getString(this.getColumnIndex("description")))
                                        jsonDescription.put("price", this.getString(this.getColumnIndex("price")))
                                        jsonDescription.put("quantity", this.getInt(this.getColumnIndex("quantity")))
                                        jsonDescription.put("quantity_unit_measurement", this.getInt(this.getColumnIndex("quantity_unit_measurement")))
                                        jsonDescription.put("total", this.getString(this.getColumnIndex("total")))
                                        jsonDescription.put("status", this.getInt(this.getColumnIndex("status")))
                                        jsonDescription.put("unit_measurement_description", this.getString(this.getColumnIndex("unit_measurement_description")))
                                        jsonArrayDescription.put(jsonDescription)
                                    }
                                }
                            }
                        jsonDriverShipload.put("description", jsonArrayDescription)
                        jsonArrayDriverShipload.put(jsonDriverShipload)
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
        url = "$serverUrl$route?api_key=$api_key&driver_shipload=$jsonArrayDriverShipload"

        val jsonArrayRequest = StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val serverResponse = Gson().fromJson(response, ServerResponse::class.java)
                    if (serverResponse.status != 0) {
                        mySqlHelper.use {
                            update("driver_shipload", "status" to "4").exec()
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
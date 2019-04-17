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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.botanas.MainActivity
import com.example.botanas.services.Network
import com.example.botanas.R
import com.example.botanas.api.Server.Companion.api_key
import com.example.botanas.dataClasses.ProductType
import com.example.botanas.db.MySqlHelper
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.transaction
import org.json.JSONArray


class StorageApi(context: Context, mainActivity: MainActivity) {

    private var url = ""
    private val serverUrl = Server.url

    private var appContext: Context = context
    private val mySqlHelper: MySqlHelper = MySqlHelper(appContext)
    private val progressBar : ProgressBar = mainActivity.findViewById(R.id.progressBar)
    private val view: View = (appContext as Activity).findViewById(R.id.containerFragments)

    fun requestGetInventory(idAmin: Int, list :ArrayList<ProductType>, recyclerView: RecyclerView) {
        val route = "driver_inventory_get.php"
        progressBar.visibility = View.VISIBLE
        url = "$serverUrl$route?api_key=$api_key&id=$idAmin"

        val network = Network(appContext)
        if (!network.isConnected()) {
            Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return
        }

        var responseData = ""
        val jsonDATA = JSONObject()
        jsonDATA.put("id",idAmin)
        jsonDATA.put("api_key", api_key)
        val jsonArray = JSONArray()

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.POST, url, jsonArray,
            Response.Listener {
                    response ->
                try {
                    responseData = response.toString()
                    updateStorageTable(responseData)
                    requestGetProducts(list, recyclerView)
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseData = ""
                    progressBar.visibility = View.GONE
                }

            },
            Response.ErrorListener { error: VolleyError ->
                println("Error ${error.message}")
                println("Result ${error}")
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                progressBar.visibility = View.GONE
            }
        )

        val queue = Volley.newRequestQueue(appContext)
        queue.add(jsonArrayRequest)

    }

    fun requestGetProducts(list: ArrayList<ProductType>, recyclerView: RecyclerView) {
        val route = "products_get.php"
        progressBar.visibility = View.VISIBLE
        url = "$serverUrl$route?api_key=$api_key"

        val network = Network(appContext)
        if (!network.isConnected()) {
            Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return
        }

        var jsonProduct = ""
        var jsonProductType = ""
        var jsonProductBase = ""
        var jsonClient = ""
        val jsonDATA = JSONObject()
        jsonDATA.put("api_key", api_key)

        val jsonArrayRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonDATA,
            Response.Listener {
                    response ->
                try {

                    jsonProduct = response.getJSONArray("products").toString()
                    updateProductTable(jsonProduct)

                    jsonProductType = response.getJSONArray("product_type").toString()
                    updateProductTypeTable(jsonProductType, list, recyclerView)

                    jsonProductBase = response.getJSONArray("product_base").toString()
                    updateProductBaseTable(jsonProductBase)

                    jsonClient = response.getJSONArray("client").toString()
                    updateClientTable(jsonClient)

                    progressBar.visibility = View.GONE
                    Snackbar.make(view, "Inventario sincronizado con exito.", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    jsonProduct = ""
                    jsonProductType = ""
                    jsonProductBase = ""
                    jsonClient = ""
                }

            },
            Response.ErrorListener { error: VolleyError ->
                println("Error ${error.message}")
                println("Result ${error}")
                //Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        )

        val queue = Volley.newRequestQueue(appContext)
        queue.add(jsonArrayRequest)

    }


    private fun updateProductTable(result: String?) {
        if  (result != "") {
            try {
                val jsonArray = JSONArray(result)
                var i = 0
                mySqlHelper.use {
                    transaction {
                        delete(
                            "product")
                        while (i < jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            insert(
                                "product",
                                "id_product" to jsonObject.getInt("id_product"),
                                "id_product_type" to jsonObject.getInt("id_product_type"),
                                "id_product_base" to jsonObject.getInt("id_product_base"),
                                "id_product_unit_measurement" to jsonObject.getInt("id_product_unit_measurement"),
                                "barcode" to jsonObject.getString("barcode"),
                                "barcode_unit" to jsonObject.getString("barcode_unit"),
                                "name" to jsonObject.getString("name"),
                                "cost" to jsonObject.getString("cost"),
                                "cost_foreign" to jsonObject.getString("cost_foreign"),
                                "cost_export" to jsonObject.getString("cost_export"),
                                "weight" to jsonObject.getString("weight"),
                                "quantity_unit_measurement" to jsonObject.getInt("quantity_unit_measurement"),
                                "shot_name" to jsonObject.getString("shot_name")
                            )
                            i++
                        }
                    }
                }



            } catch (e: Exception){
                Log.d("Exception: ", e.toString())
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
    }

    private fun updateProductTypeTable(result: String?, list: ArrayList<ProductType>, recyclerView: RecyclerView) {
        if  (result != "") {
            try {
                val jsonArray = JSONArray(result)
                var i = 0

                mySqlHelper.use {
                    transaction {
                        delete(
                            "product_type")
                        while (i < jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            insert(
                                "product_type",
                                "id_product_type" to jsonObject.getInt("id_product_type"),
                                "name" to jsonObject.getString("name"),
                                "description" to jsonObject.getString("description"),
                                "color" to jsonObject.getString("color"),
                                "text_color" to jsonObject.getString("text_color")
                            )
                            i++
                        }
                    }
                }



                list.clear()
                val query = "SELECT pt.id_product_type, pt.name, pt.description, pt.color, pt.text_color, SUM(dgi.quantity) quantity " +
                        "FROM product AS p " +
                        "INNER JOIN driver_general_inventory AS dgi ON dgi.id_product = p.id_product " +
                        "INNER JOIN product_type AS pt ON p.id_product_type = pt.id_product_type " +
                        "WHERE dgi.quantity > 0 " +
                        "GROUP BY pt.id_product_type"
                mySqlHelper.use {
                    val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
                    while (cursor.moveToNext()) {
                        list.add(
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
                        list.add(
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
                recyclerView.adapter!!.notifyDataSetChanged()

            } catch (e: Exception){
                Log.d("Exception: ", e.toString())
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
    }

    private fun updateProductBaseTable(result: String?) {
        if  (result != "") {
            try {
                val jsonArray = JSONArray(result)
                var i = 0
                    mySqlHelper.use {
                        transaction {
                            delete(
                                "product_base")
                            while (i < jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                insert(
                                    "product_base",
                                    "id_product_base" to jsonObject.getInt("id_product_base"),
                                    "description" to jsonObject.getString("description")
                                )
                                        i++
                            }
                        }
                    }
            } catch (e: Exception){
                Log.d("Exception: ", e.toString())
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
    }

    private fun updateClientTable(result: String?) {
        if (result != "") {
            try {
                val jsonArray = JSONArray(result)
                var i = 0
                mySqlHelper.use {
                    delete("client")
                    while (i < jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        insert(
                            "client",
                            "id_client" to jsonObject.getInt("id_client"),
                            "name" to jsonObject.getString("name"),
                            "rfc" to jsonObject.getString("rfc"),
                            "country" to jsonObject.getString("country"),
                            "state" to jsonObject.getString("state"),
                            "municipality" to jsonObject.getString("municipality"),
                            "locality" to jsonObject.getString("locality"),
                            "suburb" to jsonObject.getString("suburb"),
                            "street" to jsonObject.getString("street"),
                            "num_ext" to jsonObject.getString("num_ext"),
                            "num_int" to jsonObject.getString("num_int"),
                            "postal_code" to jsonObject.getString("postal_code"),
                            "email" to jsonObject.getString("email"),
                            "phone" to jsonObject.getString("phone"),
                            "company_name" to jsonObject.getString("company_name"),
                            "company_phone" to jsonObject.getString("company_phone"),
                            "company_latitude" to jsonObject.getString("company_latitude"),
                            "company_longitude" to jsonObject.getString("company_longitude"),
                            "company_contact" to jsonObject.getString("company_contact"),
                            "id_price" to jsonObject.getInt("id_price"),
                            "percentage" to jsonObject.getInt("percentage"),
                            "status" to jsonObject.getInt("status"),
                            "created_at" to jsonObject.getString("created_at"),
                            "updated_at" to jsonObject.getString("updated_at")
                        )
                        i++
                    }
                }
            } catch (e: Exception){
                Log.d("Exception: ", e.toString())
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
    }


    private fun updateStorageTable(result: String?) {
        if  (result != "") {
            try {
                val jsonArray = JSONArray(result)
                var i = 0
                mySqlHelper.use {
                    transaction {
                        delete(
                            "driver_general_inventory")
                        while (i < jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            insert(
                                "driver_general_inventory",
                                "id_driver_general_inventory" to jsonObject.getInt("id_driver_general_inventory"),
                                "id_product" to jsonObject.getInt("id_product"),
                                "product_name" to jsonObject.getString("product_name"),
                                "quantity" to jsonObject.getInt("quantity"),
                                "unit_measurement" to jsonObject.getInt("unit_measurement")
                            )
                            i++
                        }
                    }
                }
            } catch (e: Exception){
                Log.d("Exception: ", e.toString())
                Snackbar.make(view, "Error al sincronizar el inventario", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
    }

}
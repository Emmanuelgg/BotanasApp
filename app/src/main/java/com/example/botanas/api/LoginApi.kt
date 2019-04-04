package com.example.botanas.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.botanas.MainActivity
import com.example.botanas.services.Network
import com.example.botanas.R
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.insert
import org.json.JSONObject
import java.lang.Exception

class LoginApi (context: Context) {

    private var appContext: Context = context
    private lateinit var mySqlHelper: MySqlHelper
    private var url = ""
    private val api_key = "8b1134b2-230d-42ba-9d0c-b8eb2e5520ca"
    private val progressBar : ProgressBar = (context as Activity).findViewById(R.id.progressBarLogin)
    private val view: View = (appContext as Activity).findViewById(R.id.container)



    fun login(user:String, pass: String) {
        url = "https://testbotanas.emkode.net/test/android_login.php?api_key=$api_key&user_name=$user&password=$pass"
        requestPost(user, pass)
    }

    fun requestPost(user:String, pass: String) {

        val network = Network(appContext)
        if (!network.isConnected()) {
            Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return
        }


            var responseData = ""
            val JsonDATA = JSONObject()
            JsonDATA.put("user_name",user)
            JsonDATA.put("password",pass)
            JsonDATA.put("api_key", api_key)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, JsonDATA,
                Response.Listener {
                    response ->
                        try {
                            progressBar.visibility = View.VISIBLE
                            responseData = response.toString()
                            isSuccess(responseData)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            responseData = ""
                            progressBar.visibility = View.GONE
                        }

                },
                Response.ErrorListener { error: VolleyError ->
                    println("Error ${error.message}")
                    Snackbar.make(view, R.string.invalid_login, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    progressBar.visibility = View.GONE
                }
            )

            val queue = Volley.newRequestQueue(appContext)
            queue.add(jsonObjectRequest)
        }

        fun isSuccess(result: String) {
                try {
                    mySqlHelper = MySqlHelper(appContext)
                    val jsonObject = JSONObject(result)
                    if (jsonObject.getBoolean("success")) {
                        mySqlHelper.use {
                            insert(
                                "admin",
                                "id_admin" to jsonObject.getInt("id_admin"),
                                "user_name" to jsonObject.getString("user_name"),
                                "name" to jsonObject.getString("name"),
                                "email" to jsonObject.getString("email"),
                                "status" to jsonObject.getInt("status"),
                                "id_role" to jsonObject.getInt("id_role")
                            )
                        }
                        Admin.idAdmin = jsonObject.getInt("id_admin")
                        Admin.userName = jsonObject.getString("user_name")
                        Admin.name = jsonObject.getString("name")
                        Admin.email = jsonObject.getString("email")
                        Admin.idRole = jsonObject.getInt("id_role")
                        Admin.status = jsonObject.getInt("status")
                        loginSuccess(jsonObject.getString("name"))
                    } else {
                        progressBar.visibility = View.GONE
                        Snackbar.make(view, R.string.invalid_login, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    }
                } catch (e: Exception){
                    Log.d("Exception: ", e.toString())
                    progressBar.visibility = View.GONE
                    Snackbar.make(view, R.string.invalid_login, Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }

        }

    fun loginSuccess(name: String) {
        val mainActivity = MainActivity()
        val intent = Intent(appContext, mainActivity::class.java)
        intent.putExtra("user_name", name)
        appContext.startActivity(intent)
    }
}
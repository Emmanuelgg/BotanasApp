package com.example.botanas.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

import com.example.botanas.R
import com.example.botanas.api.LoginApi
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select

class LoginActivity : AppCompatActivity() {

    private lateinit var loginApi: LoginApi
    private lateinit var mySqlHelper: MySqlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginApi = LoginApi(this)


        mySqlHelper = MySqlHelper(this)
        mySqlHelper.use {
            select("admin")
                .limit(1)
                .exec {
                    if (this.count > 0) {
                        this.moveToNext()
                        val name = this.getString(this.getColumnIndex("name"))
                        loginApi.loginSuccess(name)
                        /*Admin(
                            this.getInt(this.getColumnIndex("id_admin")),
                            this.getString(this.getColumnIndex("user_name")),
                            this.getString(this.getColumnIndex("name")),
                            this.getString(this.getColumnIndex("email")),
                            this.getInt(this.getColumnIndex("id_role")),
                            this.getInt(this.getColumnIndex("status"))
                        )*/
                        Admin.idAdmin = this.getInt(this.getColumnIndex("id_admin"))
                        Admin.userName = this.getString(this.getColumnIndex("user_name"))
                        Admin.name = this.getString(this.getColumnIndex("name"))
                        Admin.email = this.getString(this.getColumnIndex("email"))
                        Admin.idRole = this.getInt(this.getColumnIndex("id_role"))
                        Admin.status = this.getInt(this.getColumnIndex("status"))
                    }
                }
        }

        val userName = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btn_login)



        btnLogin.setOnClickListener {
            loginApi.login(userName.text.toString(),password.text.toString())
        }

    }
}

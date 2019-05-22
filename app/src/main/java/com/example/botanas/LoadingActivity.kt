package com.example.botanas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.botanas.api.LoginApi
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.example.botanas.ui.login.LoginActivity
import org.jetbrains.anko.db.select
import androidx.core.os.HandlerCompat.postDelayed



class LoadingActivity : AppCompatActivity() {

    private lateinit var loginApi: LoginApi
    private lateinit var mySqlHelper: MySqlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loginApi = LoginApi(this, true)
        mySqlHelper = MySqlHelper(this)

        mySqlHelper.use {
            select("admin")
                .limit(1)
                .exec {
                    if (this.count > 0) {
                        this.moveToNext()
                        val name = this.getString(this.getColumnIndex("name"))
                        loginApi.loginSuccess(name)
                        Admin.idAdmin = this.getInt(this.getColumnIndex("id_admin"))
                        Admin.userName = this.getString(this.getColumnIndex("user_name"))
                        Admin.name = this.getString(this.getColumnIndex("name"))
                        Admin.email = this.getString(this.getColumnIndex("email"))
                        Admin.idRole = this.getInt(this.getColumnIndex("id_role"))
                        Admin.status = this.getInt(this.getColumnIndex("status"))
                    } else {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    }
                }
        }



    }
}

package com.example.botanas.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

import com.example.botanas.R
import com.example.botanas.api.LoginApi
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select
import android.R.attr.start
import androidx.core.view.ViewCompat.setScaleY
import android.R.attr.colorPrimaryDark
import android.R.attr.colorPrimary
import com.scwang.wave.MultiWaveHeader



class LoginActivity : AppCompatActivity() {

    private lateinit var loginApi: LoginApi
    private lateinit var mySqlHelper: MySqlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loginApi = LoginApi(this)


        mySqlHelper = MySqlHelper(this)

        val userName = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btn_login)



        btnLogin.setOnClickListener {
            loginApi.login(userName.text.toString(),password.text.toString())
        }

    }
}

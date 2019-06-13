package com.example.botanas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.text.NumberFormat

class SuccessShiploadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_shipload)

        val currency = NumberFormat.getCurrencyInstance()

        val total = intent.getDoubleExtra("total", 0.00)
        val clientName= intent.getStringExtra("client")
        val date = intent.getStringExtra("date")

        val clientNameView = findViewById<TextView>(R.id.shipload_client_name)
        val totalView = findViewById<TextView>(R.id.shipload_total)
        val dateView = findViewById<TextView>(R.id.shipload_date)
        val btnShiploadConfirm = findViewById<Button>(R.id.btn_shipload_confirm)

        clientNameView.text = clientName
        totalView.text = currency.format(total)
        dateView.text = date

        btnShiploadConfirm.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}

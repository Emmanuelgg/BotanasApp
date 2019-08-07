package com.example.botanas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.botanas.services.BluePrinter
import com.example.botanas.services.BluetoothService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat

class SuccessfulSale : AppCompatActivity() {

    private lateinit var bluePrinter: BluePrinter
    private var idRequisition: Long = 0
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successful_sale)

        val currency = NumberFormat.getCurrencyInstance()

        val total = intent.getDoubleExtra("total", 0.00)
        val clientName= intent.getStringExtra("client")
        val date = intent.getStringExtra("date")
        idRequisition = intent.getLongExtra("idRequisition", 0)
        view = findViewById(R.id.success_sale_layout)

        val clientNameView = findViewById<TextView>(R.id.sold_client_name)
        val totalView = findViewById<TextView>(R.id.sold_total)
        val dateView = findViewById<TextView>(R.id.sold_date)
        val btnSoldConfirm = findViewById<Button>(R.id.btn_sold_confirm)
        val btnPrintSale = findViewById<FloatingActionButton>(R.id.btn_print_success_sale)

        clientNameView.text = clientName
        totalView.text = currency.format(total)
        dateView.text = date

        btnSoldConfirm.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        bluePrinter = BluePrinter(applicationContext)
        val builder = AlertDialog.Builder(this)
        initAlertDialog(builder)

        btnPrintSale.setOnClickListener {
            builder.show()
        }
    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.printing_title)
        builder.setMessage(R.string.printing_message)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { _, _ ->
            if (!BluetoothService().isEnabled() || !bluePrinter.connected())
                startActivityForResult(Intent(applicationContext, BluetoothScannerActivity::class.java), BluetoothScannerActivity.SCANNING_FOR_PRINTER)
            else {
                bluePrinter.printSale(idRequisition.toInt())
            }


        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(view, R.string.printing_cancel, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }
}

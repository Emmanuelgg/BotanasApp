package com.example.botanas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.CustomerSelectAdapter
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.example.botanas.services.BluePrinter
import com.example.botanas.services.BluetoothService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.select
import java.text.NumberFormat
import java.util.ArrayList

class SaleDetail : AppCompatActivity(), CustomerSelectAdapter.ItemClickListener {

    private var soldProductList: ArrayList<Storage> = ArrayList()
    private lateinit var soldProducts: RecyclerView
    private lateinit var customerSelectionAdapter: CustomerSelectAdapter
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context
    private lateinit var soldDiscountView: TextView
    private lateinit var soldTotalView: TextView
    private lateinit var soldClientNameView: TextView
    private lateinit var soldDateView: TextView
    private lateinit var view: View
    private lateinit var bluePrinter: BluePrinter
    private var idRequisition: Int = 0
    private var idDriverShipload: Int = 0

    override fun onItemClick(item: CustomerSelectAdapter.ViewHolder, position: Int, parentPosition: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_detail)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        soldDiscountView = findViewById(R.id.sold_discount)
        soldTotalView = findViewById(R.id.sold_total)
        soldClientNameView = findViewById(R.id.sold_client_name)
        soldDateView = findViewById(R.id.sold_date)
        view = findViewById(R.id.sale_detail_layout)
        val imageSaleDetail = findViewById<ImageView>(R.id.imageSaleDetail)
        val soldDiscountTitle = findViewById<TextView>(R.id.sold_discount_title)
        val btnPrint = findViewById<FloatingActionButton>(R.id.btn_print)


        idRequisition = intent.getIntExtra("id_requisition", 0)
        idDriverShipload = intent.getIntExtra("id_driver_shipload", 0)

        appContext = baseContext
        mySqlHelper = MySqlHelper(this)

        if(idRequisition != 0) {
            getSale(idRequisition)
            title = getString(R.string.sale_detail)
            btnPrint.show()
            imageSaleDetail.setImageResource(R.drawable.ic_money)
        } else if(idDriverShipload != 0) {
            title = getString(R.string.shipload_detail)
            soldDiscountTitle.text = ""
            soldDiscountView.text = ""
            btnPrint.hide()
            imageSaleDetail.setImageResource(R.drawable.ic_shipped)
            getDriverShipload(idDriverShipload)
        }


        customerSelectionAdapter = CustomerSelectAdapter(soldProductList, this)
        soldProducts = findViewById(R.id.soldProductsRecycler)
        soldProducts.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = customerSelectionAdapter
        }

        bluePrinter = BluePrinter(applicationContext)
        val builder = AlertDialog.Builder(this)
        initAlertDialog(builder)


        btnPrint.setOnClickListener {
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
                bluePrinter.printSale(idRequisition)
            }


        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(view, R.string.printing_cancel, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun getSale(idRequisition: Int){
        val currency = NumberFormat.getCurrencyInstance()
        if (idRequisition == 0)
            return

        mySqlHelper.use {
            select("requisition")
                .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                .exec {
                    this.moveToNext()
                    val discount = this.getString(this.getColumnIndex("discount")) + "%"
                    var total = this.getDouble(this.getColumnIndex("total"))
                    total -= (total * (this.getDouble(this.getColumnIndex("discount"))/100))
                    val totalString = currency.format(total)
                    soldDiscountView.text = discount
                    soldTotalView.text = totalString
                    soldDateView.text = this.getString(this.getColumnIndex("created_at"))

                    val idClient = this.getInt(this.getColumnIndex("id_client"))
                    select("client")
                        .whereArgs("id_client == {id_client}", "id_client" to idClient)
                        .exec {
                            this.moveToNext()
                            val clientName = this.getString(this.getColumnIndex("name")) + " - " + this.getString(this.getColumnIndex("municipality"))
                            soldClientNameView.text = clientName
                        }

                    select("requisition_description")
                        .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                        .exec {
                            while (this.moveToNext()) {

                                soldProductList.add(
                                    Storage(
                                        0,
                                        this.getInt(this.getColumnIndex("id_product")),
                                        this.getString(this.getColumnIndex("description")),
                                        this.getInt(this.getColumnIndex("quantity")),
                                        this.getInt(this.getColumnIndex("quantity_unit_measure")),
                                        this.getString(this.getColumnIndex("cost")),
                                        "0",
                                        this.getString(this.getColumnIndex("weight")),
                                        this.getString(this.getColumnIndex("price"))
                                    )
                                )
                            }
                        }
                }
        }
    }

    private fun getDriverShipload(idDriverShipload: Int){
        val currency = NumberFormat.getCurrencyInstance()
        if (idDriverShipload == 0)
            return

        mySqlHelper.use {
            select("driver_shipload")
                .whereArgs("id_driver_shipload == {id_driver_shipload}", "id_driver_shipload" to idDriverShipload)
                .exec {
                    this.moveToNext()
                    var total = this.getDouble(this.getColumnIndex("total"))
                    val totalString = currency.format(total)
                    soldTotalView.text = totalString
                    soldDateView.text = this.getString(this.getColumnIndex("created_at"))

                    val idClient = this.getInt(this.getColumnIndex("id_client"))
                    select("client")
                        .whereArgs("id_client == {id_client}", "id_client" to idClient)
                        .exec {
                            val clientName = if (this.count > 0) {
                                this.moveToNext()
                                this.getString(this.getColumnIndex("name")) + " - " + this.getString(this.getColumnIndex("municipality"))
                            } else {
                                "Sin cliente"
                            }
                            soldClientNameView.text = clientName
                        }

                    select("driver_inventory")
                        .whereArgs("id_driver_shipload == {id_driver_shipload}", "id_driver_shipload" to idDriverShipload)
                        .exec {
                            while (this.moveToNext()) {
                                soldProductList.add(
                                    Storage(
                                        0,
                                        this.getInt(this.getColumnIndex("id_product")),
                                        this.getString(this.getColumnIndex("description")),
                                        this.getInt(this.getColumnIndex("quantity")),
                                        this.getInt(this.getColumnIndex("quantity_unit_measurement")),
                                        "0",
                                        "0",
                                        "0",
                                        this.getString(this.getColumnIndex("price")),
                                        id_store = this.getInt(this.getColumnIndex("id_store"))
                                    )
                                )
                            }
                        }
                }
        }
    }
}

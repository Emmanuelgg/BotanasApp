package com.example.botanas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.CustomerSelectAdapter
import com.example.botanas.dataClasses.Client
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import com.example.botanas.utils.StorageColorDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.fragment_load_up.*
import org.jetbrains.anko.db.*
import java.lang.Exception
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReviewShiploadActivity : AppCompatActivity(), CustomerSelectAdapter.ItemClickListener {

    override fun onItemClick(item: CustomerSelectAdapter.ViewHolder, position: Int, parentPosition: Int) {

    }

    private lateinit var shiploadList: ArrayList<Storage>
    private lateinit var reviewShiploadRecycler: RecyclerView
    private lateinit var customerSelectionAdapter: CustomerSelectAdapter
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context
    private lateinit var view: View
    private lateinit var clientSpinner: SearchableSpinner
    private var totalAmount: Double = 0.00
    private val clientArray = ArrayList<Client>()
    private val currency = NumberFormat.getCurrencyInstance()
    private lateinit var storageColorDialog: StorageColorDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_shipload)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        appContext = applicationContext
        view = findViewById(R.id.review_shipload_layout)
        mySqlHelper = MySqlHelper(this)
        clientSpinner = this.findViewById(R.id.review_shipload_client_spinner)
        val totalAmountTextView = findViewById<TextView>(R.id.review_shipload_total)
        val btnFinishSale = findViewById<FloatingActionButton>(R.id.btn_finish_review)

        shiploadList = intent.getSerializableExtra("shipload_list") as ArrayList<Storage>
        totalAmount = 0.00
        for (item in shiploadList) {
            mySqlHelper.use {
                select("product", "cost", "weight")
                    .whereArgs("id_product == {id_product}", "id_product"  to item.id_product)
                    .exec {
                        while (this.moveToNext()){
                            item.cost = this.getString(this.getColumnIndex("cost"))
                            item.trueCost = this.getString(this.getColumnIndex("cost"))
                            item.weight = this.getString(this.getColumnIndex("weight"))
                        }
                    }
                select("product_price", "price")
                    .whereArgs("(id_product == {id_product}) and (id_price == {id_price})", "id_product"  to item.id_product, "id_price" to 5)
                    .exec {
                        while (this.moveToNext()){
                            item.price = this.getString(this.getColumnIndex("price"))
                        }
                    }

                totalAmount += item.price.toDouble() * item.quantity.toDouble()
            }
        }
        totalAmountTextView.text = currency.format(totalAmount)

        customerSelectionAdapter = CustomerSelectAdapter(shiploadList, this)

        reviewShiploadRecycler = findViewById(R.id.reviewShiploadRecycler)
        reviewShiploadRecycler.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = customerSelectionAdapter
        }


        val textCustomerSelect = resources.getString(R.string.select_customer)
        val textClose = resources.getString(R.string.close)
        clientSpinner.setTitle(textCustomerSelect)
        clientSpinner.setPositiveButton(textClose)

        clientArray.add(
            Client(
                0,
                "Sin cliente"
            )
        )

        mySqlHelper.use {
            select("client").whereArgs("status != {status}",
                "status" to -1)
                .orderBy("name", SqlOrderDirection.ASC)
                .exec {
                    while (this.moveToNext()){
                        clientArray.add(
                            Client(
                                this.getInt(this.getColumnIndex("id_client")),
                                this.getString(this.getColumnIndex("name")) + " - " + this.getString(this.getColumnIndex("municipality"))
                            )

                        )
                    }
                    clientSpinner.adapter = ArrayAdapter<Client>(appContext, android.R.layout.simple_spinner_dropdown_item, clientArray)
                }
        }

        val builder = AlertDialog.Builder(this)
        initAlertDialog(builder)

        btnFinishSale.setOnClickListener {
            builder.show()
        }

        storageColorDialog = StorageColorDialog(this)
        val btnShowStoreColorReview: FloatingActionButton = findViewById(R.id.btn_show_store_color_review)
        btnShowStoreColorReview.setOnClickListener {
            storageColorDialog.showDialog()
        }
    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.shipload_confirm_title)
        builder.setMessage(R.string.sale_confirm_body)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { _, _ ->
            driverShiploadSave()
        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(view, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }

    private fun driverShiploadSave() {
        val clientID = clientArray[clientSpinner.selectedItemId.toInt()].id_client
        Log.d("id_client", clientID.toString())
        val currentTime: Date = Calendar.getInstance().time
        val dateTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currentTime)
        var driverShiploadId: Long

        try {
            mySqlHelper.use{
                transaction {
                    driverShiploadId = insert(
                        "driver_shipload",
                        "id_driver" to Admin.idAdmin,
                        "id_client" to clientID,
                        "status" to 4,
                        "total" to totalAmount.toString(),
                        "created_at" to dateTime,
                        "updated_at" to dateTime
                    )
                    for (product in shiploadList) {
                        insert(
                            "driver_inventory",
                            "id_driver_shipload" to driverShiploadId,
                            "id_product" to product.id_product,
                            "id_store" to product.id_store,
                            "barcode" to product.quantity,
                            "description" to product.product_name,
                            "price" to product.price,
                            "quantity" to product.quantity,
                            "quantity_unit_measurement" to product.quantity_unit_measurement,
                            "total" to (product.quantity.toDouble()*product.price.toDouble()).toString(),
                            "status" to 1,
                            "unit_measurement_description" to ""
                        )
                    }
                }
            }
            finishAffinity()
            val intent = Intent(appContext, SuccessShiploadActivity::class.java)
            //intent.putExtra("saleSuccessful", true)
            intent.putExtra("total", totalAmount)
            intent.putExtra("client", clientSpinner.selectedItem.toString())
            intent.putExtra("date", dateTime)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(view, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

    }
}

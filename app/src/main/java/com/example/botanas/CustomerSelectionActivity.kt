package com.example.botanas

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.CustomerSelectAdapter
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import com.example.botanas.dataClasses.Client
import com.example.botanas.ui.login.Admin
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.*
import java.lang.Exception
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@Suppress("UNCHECKED_CAST")
class CustomerSelectionActivity : AppCompatActivity(), CustomerSelectAdapter.ItemClickListener {

    private lateinit var productListSale: ArrayList<Storage>
    private lateinit var custProductListRecycler: RecyclerView
    private lateinit var customerSelectionAdapter: CustomerSelectAdapter
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context
    private lateinit var view: View
    private lateinit var clientSpinner: SearchableSpinner
    private var totalAmount: Double = 0.00
    private var totalAmountDisc: Double = 0.00
    private val clientArray = ArrayList<Client>()
    private lateinit var saleDiscount: EditText
    private val currency = NumberFormat.getCurrencyInstance()


    override fun onItemClick(item: CustomerSelectAdapter.ViewHolder, position: Int, parentPosition: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val discountListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val result = s.toString()
            val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
            totalAmount = 0.00
            Log.d("count", count.toString())
            Log.d("result", result)
            if (count == 0 || result == "." || result == ",") {
                for (product in productListSale){
                    //val newCost = product.trueCost.toDouble()
                    //product.cost = "%.2f".format(newCost)
                    totalAmount += product.price.toDouble() * product.quantity.toDouble()
                    custProductListRecycler.adapter!!.notifyDataSetChanged()
                }
                totalAmountDisc = totalAmount

                totalAmountTextView.text = currency.format(totalAmount)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val result = s.toString()
            val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
            val saleDiscount = findViewById<TextView>(R.id.sale_discount)
            if (result != "" && result != "." && result != ",") {
                totalAmount = 0.00
                val discount = (saleDiscount.text.toString().toDouble() / 100)
                for (product in productListSale){
                //    val newCost = product.trueCost.toDouble() - (product.trueCost.toDouble() * discount)
                  //  product.cost = "%.2f".format(newCost)
                    totalAmount += product.price.toDouble() * product.quantity.toDouble()
                }
                val currency = NumberFormat.getCurrencyInstance()
                totalAmountDisc = totalAmount
                totalAmountDisc -= (totalAmount * discount)
                totalAmountTextView.text = currency.format(totalAmountDisc)
                custProductListRecycler.adapter!!.notifyDataSetChanged()
            } else {

            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_selection)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        appContext = baseContext
        view = findViewById(R.id.customer_select_layout)
        mySqlHelper = MySqlHelper(this)
        clientSpinner = findViewById(R.id.client_spinner)
        val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
        saleDiscount = findViewById(R.id.sale_discount)
        val btnFinishSale = findViewById<FloatingActionButton>(R.id.btn_finish_sale)

        productListSale = intent.getSerializableExtra("product_list") as ArrayList<Storage>
        totalAmount = 0.00

        totalAmountTextView.text = currency.format(totalAmount)

        customerSelectionAdapter = CustomerSelectAdapter(productListSale, this)

        custProductListRecycler = findViewById(R.id.custProductListRecycler)
        custProductListRecycler.apply {
                this.layoutManager = LinearLayoutManager(context)
                this.adapter = customerSelectionAdapter
        }


        val textCustomerSelect = resources.getString(R.string.select_customer)
        val textClose = resources.getString(R.string.close)
        clientSpinner.setTitle(textCustomerSelect)
        clientSpinner.setPositiveButton(textClose)


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

        saleDiscount.addTextChangedListener(discountListener)

        clientSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val idClient = clientArray[position].id_client
                var idPrice = 0
                mySqlHelper.use {
                    select("client", "id_price")
                        .whereArgs("id_client = {id_client}", "id_client" to idClient)
                        .exec {
                            this.moveToNext()
                            idPrice = this.getInt(this.getColumnIndex("id_price"))
                        }
                }
                getProductPrice(idPrice)
            }
        }

        val builder = AlertDialog.Builder(this)
        initAlertDialog(builder)

        btnFinishSale.setOnClickListener {
            builder.show()
        }

        getProductPrice(5)

    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.sale_confirm_title)
        builder.setMessage(R.string.sale_confirm_body)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { _, _ ->
            clientSaleSave()
        }

        builder.setNegativeButton(R.string.no
        ) { _, _ ->
            Snackbar.make(view, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
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

    private fun getProductPrice(idPrice: Int) {
        val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
        totalAmount = 0.00
        for (item in productListSale) {
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
                    .whereArgs("(id_product == {id_product}) and (id_price == {id_price})", "id_product"  to item.id_product, "id_price" to idPrice)
                    .exec {
                        while (this.moveToNext()){
                            item.price = this.getString(this.getColumnIndex("price"))
                        }
                    }
                totalAmount += item.price.toDouble() * item.quantity.toDouble()
                totalAmountDisc = totalAmount
            }
        }
        val saleDiscount = findViewById<TextView>(R.id.sale_discount)
        val result = saleDiscount.text.toString()
        var discount = 0.00
        if (result != "" && result != "." && result != ",") {
            discount = (saleDiscount.text.toString().toDouble() / 100)
            totalAmountDisc = totalAmount
            totalAmountDisc -= (totalAmount * discount)
        }
        totalAmountTextView.text = if (discount != 0.00)
            currency.format(totalAmountDisc)
        else
            currency.format(totalAmount)

        custProductListRecycler.adapter!!.notifyDataSetChanged()
    }

    @SuppressLint("SimpleDateFormat")
    private fun clientSaleSave() {
        val clientID = clientArray[clientSpinner.selectedItemId.toInt()].id_client
        val currentTime: Date = Calendar.getInstance().time
        val date = SimpleDateFormat("yyyy-MM-dd").format(currentTime)
        val dateTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currentTime)
        var requisitionID: Long = 0
        var discount = saleDiscount.text.toString()
        if (discount == "" || discount == "." || discount == ",")
            discount = "0"

        try {
            mySqlHelper.use{
                transaction {
                    requisitionID = insert(
                        "requisition",
                        "id_driver" to Admin.idAdmin,
                        "id_client" to clientID,
                        "total" to totalAmount.toString(),
                        "discount" to discount,
                        "status" to 2,
                        "requisition_date" to date,
                        "type" to 1,
                        "created_at" to dateTime,
                        "updated_at" to dateTime
                    )
                    for (product in productListSale) {
                        insert(
                            "requisition_description",
                            "id_requisition" to requisitionID,
                            "id_product" to product.id_product,
                            "price" to product.price,
                            "quantity" to product.quantity,
                            "weight" to product.weight,
                            "cost" to product.trueCost,
                            "total" to (product.quantity.toDouble()*product.price.toDouble()).toString(),
                            "description" to product.product_name,
                            "quantity_unit_measure" to product.quantity_unit_measurement,
                            "status" to 2
                        )
                        select("driver_general_inventory", "quantity")
                            .whereArgs("id_product == {id_product}", "id_product" to product.id_product)
                            .exec {
                                if  (this.count > 0) {
                                    this.moveToNext()
                                    val newQuantity = this.getInt(this.getColumnIndex("quantity"))-product.quantity.toInt()
                                    update("driver_general_inventory", "quantity" to newQuantity)
                                        .whereArgs("id_product == {id_product}", "id_product" to product.id_product)
                                        .exec()
                                }

                            }
                    }
                }
            }
            finishAffinity()
            val intent = Intent(appContext,SuccessfulSale::class.java)
            //intent.putExtra("saleSuccessful", true)
            intent.putExtra("total", totalAmountDisc)
            intent.putExtra("client", clientSpinner.selectedItem.toString())
            intent.putExtra("date", dateTime)
            intent.putExtra("idRequisition", requisitionID)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(view, R.string.no_changes, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

    }
}

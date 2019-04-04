package com.example.botanas

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.CustomerSelectAdapter
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.select
import android.widget.Toast
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog


class CustomerSelectionActivity : AppCompatActivity(), CustomerSelectAdapter.ItemClickListener {

    private lateinit var productListSale: ArrayList<Storage>
    private lateinit var custProductListRecycler: RecyclerView
    private lateinit var customerSelectionAdapter: CustomerSelectAdapter
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context


    override fun onItemClick(item: CustomerSelectAdapter.ViewHolder, position: Int, parentPosition: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val discountListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val result = s.toString()
            val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
            var totalAmount = 0.00
            Log.d("count", count.toString())
            Log.d("result", result)
            if (count == 0 || result == "." || result == ",") {
                for (product in productListSale){
                    val newCost = product.trueCost.toDouble()
                    product.cost = "${"%.2f".format(newCost)}"
                    totalAmount += product.cost.toDouble() * product.quantity.toDouble()
                    custProductListRecycler.adapter!!.notifyDataSetChanged()
                }
                totalAmountTextView.text = "$${"%.2f".format(totalAmount)}"
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val result = s.toString()
            val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
            val saleDiscount = findViewById<TextView>(R.id.sale_discount)
            var totalAmount = 0.00
            if (result != "" && result != "." && result != ",") {
                val discount = (saleDiscount.text.toString().toDouble() / 100)
                for (product in productListSale){
                    val newCost = product.trueCost.toDouble() - (product.trueCost.toDouble() * discount)
                    product.cost = "${"%.2f".format(newCost)}"
                    totalAmount += product.cost.toDouble() * product.quantity.toDouble()
                }
                totalAmountTextView.text = "$${"%.2f".format(totalAmount)}"
                custProductListRecycler.adapter!!.notifyDataSetChanged()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_selection)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        appContext = baseContext
        mySqlHelper = MySqlHelper(this)
        val clientSpinner = findViewById<SearchableSpinner>(R.id.client_spinner)
        val totalAmountTextView = findViewById<TextView>(R.id.total_amunt)
        val saleDiscount = findViewById<EditText>(R.id.sale_discount)
        val btnFinishSale = findViewById<FloatingActionButton>(R.id.btn_finish_sale)

        productListSale = intent.getSerializableExtra("product_list") as ArrayList<Storage>
        var totalAmount = 0.00
        for (item in productListSale) {
            mySqlHelper.use {
                select("product", "cost")
                    .whereArgs("id_product == {id_product}", "id_product"  to item.id_product)
                    .exec {
                        while (this.moveToNext()){
                            item.cost = this.getString(this.getColumnIndex("cost"))
                            item.trueCost = this.getString(this.getColumnIndex("cost"))
                            totalAmount += item.cost.toDouble() * item.quantity.toDouble()
                        }
                    }
            }
        }
        totalAmountTextView.text = "$${"%.2f".format(totalAmount)}"

        customerSelectionAdapter = CustomerSelectAdapter(productListSale, this)

        custProductListRecycler = findViewById(R.id.custProductListRecycler)
        custProductListRecycler.apply {
                this.layoutManager = LinearLayoutManager(context)
                this.adapter = customerSelectionAdapter
        }


        val clientArray = ArrayList<String>()
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
                    clientArray.add(this.getString(this.getColumnIndex("name")))
                }
                clientSpinner.adapter = ArrayAdapter<String>(appContext, android.R.layout.simple_spinner_dropdown_item, clientArray)


            }
        }

        saleDiscount.addTextChangedListener(discountListener)

        val builder = AlertDialog.Builder(this)
        initAlertDialog(builder)

        btnFinishSale.setOnClickListener {
            builder.show()
        }

    }

    private fun initAlertDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.sale_confirm_title)
        builder.setMessage(R.string.sale_confirm_body)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm,
            DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    "You've choosen to delete all records",
                    Toast.LENGTH_SHORT
                ).show()
            })

        builder.setNegativeButton(R.string.no,
            DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    "You've changed your mind to delete all records",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
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
}

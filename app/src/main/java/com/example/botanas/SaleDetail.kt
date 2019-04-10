package com.example.botanas

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.CustomerSelectAdapter
import com.example.botanas.dataClasses.Storage
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select
import org.w3c.dom.Text
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

        val idRequisition = intent.getIntExtra("id_requisition", 0)

        appContext = baseContext
        mySqlHelper = MySqlHelper(this)

        getSale(idRequisition)

        customerSelectionAdapter = CustomerSelectAdapter(soldProductList, this)
        soldProducts = findViewById(R.id.soldProductsRecycler)
        soldProducts.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = customerSelectionAdapter
        }
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

    private fun getSale(idRequisition: Int){
        if (idRequisition == 0)
            return

        mySqlHelper.use {
            select("requisition")
                .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                .exec {
                    this.moveToNext()
                    val discount = this.getString(this.getColumnIndex("discount")) + "%"
                    val total = "$%.2f".format(this.getDouble(this.getColumnIndex("total")))
                    soldDiscountView.text = discount
                    soldTotalView.text = total
                    soldDateView.text = this.getString(this.getColumnIndex("created_at"))

                    val idClient = this.getInt(this.getColumnIndex("id_client"))
                    select("client")
                        .whereArgs("id_client == {id_client}", "id_client" to idClient)
                        .exec {
                            this.moveToNext()
                            val clientName = this.getString(this.getColumnIndex("name"))
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
                                        this.getString(this.getColumnIndex("weight"))
                                    )
                                )
                            }
                        }
                }
        }
    }
}

package com.example.botanas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Switch
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class SettingsActivity : AppCompatActivity() {

    private lateinit var mySqlHelper: MySqlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        mySqlHelper = MySqlHelper(applicationContext)
        var autoSaleSync = false
        var serverNotifications = false
        mySqlHelper.use {
            select("settings")
                .whereArgs("id_admin == {id_admin}", "id_admin" to Admin.idAdmin)
                .exec {
                    this.moveToNext()
                    if (this.getInt(this.getColumnIndex("auto_sales_sync")) == 1)
                        autoSaleSync = true
                    if (this.getInt(this.getColumnIndex("server_notifications")) == 1)
                        serverNotifications = true
                }
        }

        changerSwitch(findViewById(R.id.switchAutoSalesSync), autoSaleSync, "auto_sales_sync")
        changerSwitch(findViewById(R.id.switchServerNotifications), serverNotifications, "server_notifications")



    }

    private fun changerSwitch(switch: Switch, value: Boolean, column: String) {
        //switch.setText(if (value) R.string.on else R.string.off)
        switch.isChecked = value

        switch.setOnCheckedChangeListener { button: CompoundButton, check: Boolean ->
            val isChecked = if (check) 1 else 0
            //button.setText(if (check) R.string.on else R.string.off)
            mySqlHelper.use {
                update("settings", column to isChecked)
                    .whereArgs("id_admin == {id_admin}", "id_admin" to Admin.idAdmin).exec()
            }
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
}

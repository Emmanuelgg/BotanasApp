package com.example.botanas.db

import android.content.Context
import com.example.botanas.ui.login.Admin
import org.jetbrains.anko.db.select

class Permission(context: Context) {
    val mySqlHelper = MySqlHelper(context)

    fun autoSaleSync(): Boolean {
        return query("auto_sales_sync") == 1
    }

    fun serverNotification(): Boolean {
        return query("server_notifications") == 1
    }

    private fun query(column: String): Int {
        var value = 0
        mySqlHelper.use {
            select("settings").whereArgs(
                "id_admin == {id_admin}", "id_admin" to Admin.idAdmin
            ).exec {
                this.moveToNext()
                value = this.getInt(this.getColumnIndex(column))
            }
        }
        return value
    }

}
package com.example.botanas.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.R
import com.example.botanas.adapter.StoreColorAdapter
import com.example.botanas.dataClasses.Store
import com.example.botanas.db.MySqlHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.Exception

class StorageColorDialog(context: Context){

    private var storeColorDialog: BottomSheetDialog
    private var storeColorAdapter: StoreColorAdapter
    val storeColorList: ArrayList<Store> = ArrayList()
    var appContext: Context = context
    private  var mySqlHelper: MySqlHelper = MySqlHelper(context)
    private  var storeColorRecycler: RecyclerView

    init {
        storeColorDialog = BottomSheetDialog(appContext)
        val storeColorDialogView = LayoutInflater.from(appContext).inflate(R.layout.store_color_dialog, null)
        storeColorDialog.setContentView(storeColorDialogView)

        initStoreColorRecycleView()
        storeColorAdapter = StoreColorAdapter(storeColorList)
        with(storeColorDialogView) {
            storeColorRecycler = findViewById(R.id.storeColorRecycler)
            storeColorRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            storeColorRecycler.adapter = storeColorAdapter
        }

    }

    private fun initStoreColorRecycleView() {
        storeColorList.clear()
        val query = "SELECT * FROM store WHERE color != 'null'"

        mySqlHelper.use {
            val cursor = mySqlHelper.writableDatabase.rawQuery(query, null)
            while (cursor.moveToNext()) {
                storeColorList.add(
                    Store(
                        cursor.getInt(cursor.getColumnIndex("id_store")),
                        cursor.getString(cursor.getColumnIndex("code")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getInt(cursor.getColumnIndex("status")),
                        cursor.getString(cursor.getColumnIndex("color"))
                    )
                )
            }
            if (cursor.count <= 0) {
                storeColorList.add(
                    Store(
                        0,
                        "",
                        "No se encontraron plantas, favor de sincronizar.",
                        0,
                        "#FF303F9F"
                    )
                )
            }
            cursor.close()
        }
        try {
            storeColorRecycler.adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {

        }
    }

    fun showDialog() {
        storeColorDialog.show()
    }
}
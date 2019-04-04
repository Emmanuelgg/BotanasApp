package com.example.botanas.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
private const val DATABASE_VERSION = 7
class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "botanas_db", null, DATABASE_VERSION) {

    companion object {
        private var instance: MySqlHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        /* Table admin */
        db.createTable("admin", true,
            "id_admin" to INTEGER + PRIMARY_KEY,
            "user_name" to TEXT,
            "name" to TEXT,
            "email" to TEXT,
            "id_role" to INTEGER,
            "status" to INTEGER
        )
        /* Table product */
        db.createTable("product", true,
            "id_product" to INTEGER + PRIMARY_KEY,
            "id_product_type" to INTEGER,
            "id_product_base" to INTEGER,
            "id_product_unit_measurement" to INTEGER,
            "barcode" to TEXT,
            "barcode_unit" to TEXT,
            "name" to TEXT,
            "cost" to TEXT,
            "cost_foreign" to TEXT,
            "cost_export" to TEXT,
            "weight" to TEXT,
            "quantity_unit_measurement" to INTEGER,
            "shot_name" to TEXT
        )
        /* Table product_type */
        db.createTable("product_type", true,
            "id_product_type" to INTEGER + PRIMARY_KEY,
            "name" to TEXT,
            "description" to TEXT,
            "color" to TEXT,
            "text_color" to TEXT
        )
        /* Table product_base */
        db.createTable("product_base", true,
            "id_product_base" to INTEGER + PRIMARY_KEY,
            "description" to TEXT
        )
        /* Table driver_general_inventory */
        db.createTable("driver_general_inventory", true,
            "id_driver_general_inventory" to INTEGER + PRIMARY_KEY,
            "id_product" to INTEGER,
            "product_name" to TEXT,
            "quantity" to INTEGER,
            "unit_measurement" to INTEGER
        )
        /* Table client */
        db.createTable("client", true,
            "id_client" to INTEGER + PRIMARY_KEY,
            "name" to TEXT,
            "rfc" to TEXT,
            "country" to TEXT,
            "state" to TEXT,
            "municipality" to TEXT,
            "locality" to TEXT,
            "suburb" to TEXT,
            "street" to TEXT,
            "num_ext" to TEXT,
            "num_int" to TEXT,
            "postal_code" to TEXT,
            "email" to TEXT,
            "phone" to TEXT,
            "company_name" to TEXT,
            "company_phone" to TEXT,
            "company_latitude" to TEXT,
            "company_longitude" to TEXT,
            "company_contact" to TEXT,
            "id_price" to INTEGER,
            "percentage" to INTEGER,
            "status" to INTEGER,
            "created_at" to TEXT,
            "updated_at" to TEXT
        )


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable("admin",true)
        db.dropTable("product",true)
        db.dropTable("product_type",true)
        db.dropTable("product_base",true)
        db.dropTable("driver_general_inventory",true)
        db.dropTable("client",true)
        onCreate(db)
    }

}
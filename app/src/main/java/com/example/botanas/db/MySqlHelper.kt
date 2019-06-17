package com.example.botanas.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

private const val DATABASE_VERSION = 16
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
        /* Table settings */
        db.createTable("settings", true,
            "id_settings" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            "id_admin" to INTEGER,
            "auto_sales_sync" to INTEGER,
            "server_notifications" to INTEGER,
            "visits" to INTEGER
        )

        /* Table store */
        db.createTable("store", true,
            "id_store" to INTEGER + PRIMARY_KEY,
            "code" to TEXT,
            "name" to TEXT,
            "status" to INTEGER,
            "color" to TEXT,
            "dark_color" to TEXT
        )

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
        /* Table price */
        db.createTable("price", true,
            "id_price" to INTEGER + PRIMARY_KEY,
            "name" to TEXT,
            "description" to TEXT,
            "status" to INTEGER
        )
        /* Table product_price */
        db.createTable("product_price", true,
            "id_product_price" to INTEGER + PRIMARY_KEY,
            "id_product" to INTEGER,
            "id_price" to INTEGER,
            "price" to TEXT,
            "profit" to TEXT,
            "quantity" to TEXT
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

        /* Table requisition */
        db.createTable("requisition", true,
            "id_requisition" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            "id_driver" to INTEGER,
            "id_client" to INTEGER,
            "number" to TEXT,
            "total" to TEXT,
            "discount" to TEXT,
            "status" to INTEGER,
            "requisition_date" to TEXT,
            "type" to INTEGER,
            "created_at" to TEXT,
            "updated_at" to TEXT
        )

        /* Table requisition description */
        db.createTable("requisition_description", true,
            "id_requisition_description" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            "id_requisition" to INTEGER,
            "id_store" to INTEGER,
            "id_product" to TEXT,
            "price" to TEXT,
            "quantity" to INTEGER,
            "weight" to INTEGER,
            "cost" to TEXT,
            "total" to TEXT,
            "description" to TEXT,
            "quantity_unit_measure" to INTEGER,
            "unit_measurement" to TEXT,
            "status" to INTEGER

        )

        /* Table driver shipload */
        db.createTable("driver_shipload", true,
            "id_driver_shipload" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            "id_driver" to INTEGER,
            "id_client" to INTEGER,
            "status" to INTEGER,
            "total" to TEXT,
            "created_at" to TEXT,
            "updated_at" to TEXT
        )

        /* Table driver inventory */
        db.createTable("driver_inventory", true,
            "id_driver_inventory" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            "id_driver_shipload" to INTEGER,
            "id_product" to INTEGER,
            "id_store" to INTEGER,
            "barcode" to TEXT,
            "description" to TEXT,
            "price" to TEXT,
            "quantity" to INTEGER,
            "quantity_unit_measurement" to INTEGER,
            "total" to TEXT,
            "status" to INTEGER,
            "unit_measurement_description" to TEXT
        )


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable("settings",true)
        db.dropTable("store",true)
        db.dropTable("admin",true)
        db.dropTable("product",true)
        db.dropTable("price",true)
        db.dropTable("product_price",true)
        db.dropTable("product_type",true)
        db.dropTable("product_base",true)
        db.dropTable("driver_general_inventory",true)
        db.dropTable("client",true)
        db.dropTable("requisition",true)
        db.dropTable("requisition_description",true)
        db.dropTable("driver_shipload",true)
        db.dropTable("driver_inventory",true)
        onCreate(db)
    }

}
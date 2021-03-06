package com.example.botanas.dataClasses

import java.io.Serializable

/*Data class from table of DB*/
data class Storage(
    val id_driver_general_inventory: Number,
    val id_product: Number,
    val product_name: String,
    var quantity: Number,
    val quantity_unit_measurement: Number,
    var cost: String = "0",
    var trueCost: String = "0",
    var weight: String = "",
    var price: String = "",
    var id_store: Int = 0
) : Serializable
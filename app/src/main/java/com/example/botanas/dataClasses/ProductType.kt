package com.example.botanas.dataClasses

import java.io.Serializable

/*Data class from table of DB*/
data class ProductType(
    val id_product_type: Int,
    val name: String,
    val description: String,
    val color: String,
    val text_color: String,
    var all: Boolean = false,
    val products: ArrayList<Storage> = ArrayList()

) : Serializable
package com.example.botanas.dataClasses

import java.io.Serializable

/*Data class from table of DB*/
data class Store(
    val id_store: Int,
    val code: String,
    val name: String ,
    val status: Int,
    val color: String,
    val dark_color: String
) : Serializable
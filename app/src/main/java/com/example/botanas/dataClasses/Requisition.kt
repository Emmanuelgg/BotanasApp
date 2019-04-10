package com.example.botanas.dataClasses

import java.io.Serializable

data class Requisition (
    val id_requisition: Int,
    val client_name: String,
    val date: String,
    val total: String
): Serializable
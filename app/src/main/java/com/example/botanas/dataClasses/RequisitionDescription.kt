package com.example.botanas.dataClasses

import java.io.Serializable

data class RequisitionDescription(
    val id_requisition_description: Int,
    val id_requisition : Int,
    var id_store: Int = 0,
    val id_product: String,
    val price: String,
    val quantity: Int,
    val weight: Int,
    val cost: String,
    val total: String,
    val description: String,
    val quantity_unit_measure: Int,
    var unit_measurement: String = "",
    val status: Int
):Serializable
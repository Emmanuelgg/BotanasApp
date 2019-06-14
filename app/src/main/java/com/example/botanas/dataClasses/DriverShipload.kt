package com.example.botanas.dataClasses

import java.io.Serializable

data class DriverShipload (
    val id_driver_shipload: Int,
    val id_driver: Int,
    val id_client: Int,
    val client_name: String,
    val status: Int,
    val total: String,
    val created_at: String,
    val updated_at: String
): Serializable
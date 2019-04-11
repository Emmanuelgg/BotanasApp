package com.example.botanas.dataClasses

import android.accounts.AuthenticatorDescription
import java.io.Serializable

data class Requisition (
    val id_requisition: Int,
    val id_client: Int,
    val client_name: String,
    val date: String,
    val total: String,
    val discount: String,
    val description: ArrayList<RequisitionDescription> = ArrayList()
): Serializable
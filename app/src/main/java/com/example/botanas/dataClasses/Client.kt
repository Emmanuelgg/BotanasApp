package com.example.botanas.dataClasses

import java.io.Serializable

data class Client(
    val id_client: Int, val name: String
): Serializable {
    override fun toString(): String {
        return name
    }
}
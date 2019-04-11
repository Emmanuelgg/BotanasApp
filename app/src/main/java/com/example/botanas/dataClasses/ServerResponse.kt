package com.example.botanas.dataClasses

import java.io.Serializable

data class ServerResponse (
    val status: Int,
    val response: String,
    val messages: String,
    var error: String = ""
): Serializable
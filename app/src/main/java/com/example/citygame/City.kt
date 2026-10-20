package com.example.citygame

import java.io.Serializable

data class City(
    val name: String,
    var description: String,
    var imageResId: Int = R.mipmap.ic_launcher,
    var imageUri: String? = null, // Путь к пользовательскому фото
    val latitude: Double,
    val longitude: Double
) : Serializable
package com.example.stickynotes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.graphics.scale

fun uriToBitmap(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        bitmap?.scale(targetSizePx, targetSizePx, true)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 5..12 -> "Bom dia! ☀️\nVamos criar um widget?"
        in 13..18 -> "Boa tarde! ☁️\nVamos criar um widget?"
        else -> "Boa noite! 🌙\nVamos criar um widget?"
    }
}
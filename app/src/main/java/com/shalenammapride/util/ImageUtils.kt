package com.shalenammapride.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun compress(context: Context, uri: Uri, maxDimension: Int = 1024, quality: Int = 80): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val scaled = if (original.width > maxDimension || original.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(original.width, original.height)
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true
            )
        } else original

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        if (scaled != original) scaled.recycle()
        return out.toByteArray()
    }
}

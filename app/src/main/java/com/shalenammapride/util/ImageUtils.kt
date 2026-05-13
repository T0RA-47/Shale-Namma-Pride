package com.shalenammapride.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun compress(context: Context, uri: Uri, maxDimension: Int = 1024, quality: Int = 80): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val exifStream = context.contentResolver.openInputStream(uri)
        val rotation = exifStream?.let {
            val exif = ExifInterface(it)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        exifStream?.close()

        val rotated = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        } else original

        val scaled = if (rotated.width > maxDimension || rotated.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(rotated.width, rotated.height)
            Bitmap.createScaledBitmap(rotated, (rotated.width * scale).toInt(), (rotated.height * scale).toInt(), true)
        } else rotated

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        if (scaled != rotated) scaled.recycle()
        if (rotated != original) rotated.recycle()
        return out.toByteArray()
    }
}

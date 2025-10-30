package com.amos_tech_code.smartattend.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrCodeSharingService(private val context: Context) {

    private val imageLoader = ImageLoader(context)

    suspend fun shareQrCodeImage(qrCodeUrl: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                // Download the QR code image
                val bitmap = downloadQrCodeImage(qrCodeUrl)

                // Save the image to cache
                val imageFile = saveBitmapToCache(bitmap)

                // Share the image
                shareImageFile(imageFile)

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun downloadQrCodeImage(qrCodeUrl: String): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(qrCodeUrl)
            .allowHardware(false) // Important for sharing
            .build()

        val result = imageLoader.execute(request)

        return when (result) {
            is SuccessResult -> result.drawable.toBitmap()
            else -> throw Exception("Failed to download QR code image")
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): File {
        // Create a unique file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "qr_code_$timeStamp.png"

        // Create cache file
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val file = File(cacheDir, fileName)

        // Compress and save bitmap
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return file
    }

    private fun shareImageFile(imageFile: File) {
        // Get URI using FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        // Create share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Attendance QR Code")
            putExtra(
                Intent.EXTRA_TEXT,
                "Scan this QR code with ClassTrack App to join attendance session"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start share activity
        context.startActivity(
            Intent.createChooser(shareIntent, "Share QR Code")
        )
    }

    // Clean up cached files (call this periodically)
    fun cleanupCache() {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("qr_code_") && file.name.endsWith(".png")) {
                // Delete files older than 24 hours
                if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                    file.delete()
                }
            }
        }
    }
}
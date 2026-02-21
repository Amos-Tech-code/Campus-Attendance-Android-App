package com.amos_tech_code.smartattend.services

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object FileDownloadManager {
    private const val TAG = "FileDownloadManager"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
        onProgress: suspend (Float) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // Validate URL
            if (url.isBlank()) {
                return@withContext Result.failure(Exception("Invalid URL: URL is empty"))
            }

            Log.d(TAG, "Downloading file from: $url")
            Log.d(TAG, "File name: $fileName")

            // Create request with proper headers
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/octet-stream")
                .addHeader("User-Agent", "SmartAttend-App")
                .build()

            // Execute request with better error handling
            val response = try {
                client.newCall(request).execute()
            } catch (e: SocketTimeoutException) {
                return@withContext Result.failure(Exception("Connection timeout. Please check your internet connection."))
            } catch (e: UnknownHostException) {
                return@withContext Result.failure(Exception("Network error. Please check your internet connection."))
            } catch (e: IOException) {
                return@withContext Result.failure(Exception("Network error: ${e.message}"))
            }

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error body"
                Log.e(TAG, "Download failed with code: ${response.code}, body: $errorBody")
                return@withContext Result.failure(
                    IOException("Download failed (${response.code}): ${response.message}")
                )
            }

            // Get content length for progress
            val contentLength = response.body?.contentLength() ?: -1L
            Log.d(TAG, "Content length: $contentLength")

            // Get input stream
            response.body?.byteStream()?.use { inputStream ->
                // Determine MIME type
                val mimeType = getMimeType(fileName)
                Log.d(TAG, "MIME type: $mimeType")

                // Save file based on Android version
                val uri = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveToMediaStore(
                            context = context,
                            inputStream = inputStream,
                            fileName = fileName,
                            mimeType = mimeType,
                            contentLength = contentLength,
                            onProgress = onProgress  // Directly pass the suspend function
                        )
                    } else {
                        saveToExternalStorage(
                            context = context,
                            inputStream = inputStream,
                            fileName = fileName,
                            mimeType = mimeType,
                            contentLength = contentLength,
                            onProgress = onProgress  // Directly pass the suspend function
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving file", e)
                    return@withContext Result.failure(e)
                }

                Log.d(TAG, "File saved successfully: $uri")
                Result.success(uri)
            } ?: Result.failure(Exception("Empty response body from server"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during download", e)
            Result.failure(e)
        }
    }

    // Make save functions suspend and accept suspend lambda
    private suspend fun saveToMediaStore(
        context: Context,
        inputStream: java.io.InputStream,
        fileName: String,
        mimeType: String,
        contentLength: Long,
        onProgress: suspend (Float) -> Unit
    ): Uri {
        val resolver = context.contentResolver

        // Create content values
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/SmartAttend")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        // Insert into MediaStore
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, contentValues)
            ?: throw Exception("Failed to create file in Downloads")

        // Write file content
        resolver.openOutputStream(uri)?.use { outputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    // Call suspend function directly - we're already in a suspend context
                    onProgress(totalBytesRead.toFloat() / contentLength)
                }
            }
            outputStream.flush()
        } ?: throw Exception("Failed to open output stream")

        // Clear IS_PENDING flag for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }

    private suspend fun saveToExternalStorage(
        context: Context,
        inputStream: java.io.InputStream,
        fileName: String,
        mimeType: String,
        contentLength: Long,
        onProgress: suspend (Float) -> Unit
    ): Uri {
        // For Android 9 and below, save to external storage
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "SmartAttend")

        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val file = File(appDir, fileName)

        // Delete if exists
        if (file.exists()) {
            file.delete()
        }

        // Write file
        FileOutputStream(file).use { outputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    // Call suspend function directly - we're already in a suspend context
                    onProgress(totalBytesRead.toFloat() / contentLength)
                }
            }
            outputStream.flush()
        }

        // Notify media scanner
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

        // Return FileProvider URI for sharing/viewing
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".csv", ignoreCase = true) -> "text/csv"
            fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
            fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            else -> {
                val extension = fileName.substringAfterLast(".", "").lowercase()
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
            }
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share File"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }

    fun getFileUri(context: Context, filePath: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, filePath might be a content URI
            if (filePath.startsWith("content://")) {
                Uri.parse(filePath)
            } else {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(filePath)
                )
            }
        } else {
            // For older versions
            Uri.fromFile(File(filePath))
        }
    }
}
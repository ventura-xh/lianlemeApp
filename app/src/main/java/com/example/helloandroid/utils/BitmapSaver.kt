package com.example.helloandroid.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapSaver {

    /**
     * 保存 Bitmap 到相册
     * @param format 图片格式，默认 JPEG
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG  // ✅ 默认 JPEG
    ): Boolean {
        return try {
            saveBitmapUsingMediaStore(context, bitmap, fileName, format)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveBitmapUsingMediaStore(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: Bitmap.CompressFormat
    ): Boolean {
        val mimeType = if (format == Bitmap.CompressFormat.JPEG) "image/jpeg" else "image/png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(format, 90, outputStream)  // ✅ JPEG 使用 90% 质量
                outputStream.flush()
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
            return true
        }
        return false
    }

    private fun saveBitmapLegacy(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: Bitmap.CompressFormat
    ): Boolean {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appDir = File(picturesDir, "FitApp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val file = File(appDir, fileName)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(format, 90, outputStream)  // ✅ JPEG 使用 90% 质量
            outputStream.flush()
        }

        val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = android.net.Uri.fromFile(file)
        context.sendBroadcast(mediaScanIntent)

        return true
    }

    /**
     * 生成文件名（带时间戳）
     * @param extension 文件扩展名，默认 .jpg
     */
    fun generateFileName(planName: String, extension: String = ".jpg"): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "训练记录_${planName}_$timestamp$extension"
    }
}
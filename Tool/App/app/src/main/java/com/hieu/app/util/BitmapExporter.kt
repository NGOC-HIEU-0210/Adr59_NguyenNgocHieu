package com.hieu.app.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ALBUM = "VietCongThuc"
private const val ONE_DAY_MS = 24L * 60 * 60 * 1000

private fun fileName(): String =
    "do_thi_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.png"

/**
 * Lưu PNG vào thư viện ảnh (Pictures/VietCongThuc).
 * Android 10+ dùng MediaStore, không cần quyền. Android 9 trở xuống cần WRITE_EXTERNAL_STORAGE.
 */
@Suppress("DEPRECATION")
fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean = runCatching {
    val name = fileName()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Không tạo được mục ảnh")
        try {
            val ok = resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            check(ok == true) { "Ghi PNG thất bại" }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    } else {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
        check(dir.exists() || dir.mkdirs()) { "Không tạo được thư mục" }
        val file = File(dir, name)
        FileOutputStream(file).use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
    }
    true
}.getOrDefault(false)

/** Mở hộp thoại chia sẻ ảnh PNG (Zalo, Messenger, Gmail...) qua FileProvider. */
fun shareBitmap(context: Context, bitmap: Bitmap, chooserTitle: String): Boolean = runCatching {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    // Dọn các ảnh chia sẻ cũ hơn 1 ngày
    dir.listFiles()?.forEach { if (System.currentTimeMillis() - it.lastModified() > ONE_DAY_MS) it.delete() }

    val file = File(dir, fileName())
    FileOutputStream(file).use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, chooserTitle).apply {
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
    true
}.getOrDefault(false)

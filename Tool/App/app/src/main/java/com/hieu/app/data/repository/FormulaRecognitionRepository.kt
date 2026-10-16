package com.hieu.app.data.repository

import android.graphics.Bitmap
import com.google.gson.Gson
import com.hieu.app.data.model.RecognizeResponse
import com.hieu.app.data.network.FormulaApi
import com.hieu.app.data.network.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class FormulaRecognitionRepository(
    private val api: FormulaApi = RetrofitProvider.api
) {

    private fun downscaleIfNeeded(bitmap: Bitmap, maxDimension: Int = 1280): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / largestSide
        val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    suspend fun recognize(bitmap: Bitmap): Result<RecognizeResponse> = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = downscaleIfNeeded(bitmap)
            val bytes = ByteArrayOutputStream().use { stream ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
            val requestBody = bytes.toRequestBody("image/png".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "formula.png", requestBody)

            val response = api.recognize(part)
            val body = response.body()

            when {
                response.isSuccessful && body != null && body.status != "error" ->
                    Result.success(body)

                // Server tra ve 200 nhung status = "error"
                response.isSuccessful ->
                    Result.failure(Exception(body?.message ?: "Server tra ve ket qua rong"))

                // 4xx / 5xx: doc message that trong body loi thay vi chi bao "HTTP 400"
                else -> {
                    val raw = runCatching { response.errorBody()?.string() }.getOrNull().orEmpty()
                    val serverMessage = runCatching {
                        Gson().fromJson(raw, RecognizeResponse::class.java)?.message
                    }.getOrNull()
                    Result.failure(
                        Exception("HTTP ${response.code()} - ${serverMessage ?: raw.take(300)}")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
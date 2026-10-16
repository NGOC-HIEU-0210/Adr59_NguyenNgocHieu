package com.hieu.app.data.network

import com.hieu.app.data.model.RecognizeResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FormulaApi {
    @Multipart
    @POST("api/formulas/recognize")
    suspend fun recognize(@Part image: MultipartBody.Part): Response<RecognizeResponse>
}

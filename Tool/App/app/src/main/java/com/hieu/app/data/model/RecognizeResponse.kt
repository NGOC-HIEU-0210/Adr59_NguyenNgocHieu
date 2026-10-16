package com.hieu.app.data.model

import com.google.gson.annotations.SerializedName

data class RecognizeResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("equations")
    val equations: List<String>?,

    @SerializedName("filename")
    val filename: String?,

    // Server tra ve message khi status = "error"
    @SerializedName("message")
    val message: String? = null
)
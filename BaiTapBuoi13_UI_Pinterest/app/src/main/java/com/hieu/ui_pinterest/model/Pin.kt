package com.hieu.ui_pinterest.model

data class Pin(
    val id: Int,
    val imageRes: Int,
    val heightDp: Int,
    val title: String? = null
)
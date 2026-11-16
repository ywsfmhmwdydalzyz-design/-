package com.example.data.models

data class DuaItem(
    val id: String,
    val title: String,
    val text: String,
    val translationOrFadl: String,
    val category: String,
    val reference: String
)

data class DuaCategory(
    val id: String,
    val titleArabic: String,
    val count: Int
)

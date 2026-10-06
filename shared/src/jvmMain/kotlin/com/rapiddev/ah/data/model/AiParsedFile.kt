package com.rapiddev.ah.data.model

data class AiParsedFile(
    val path: String,
    val content: String,
    val isDirectory: Boolean = false
)
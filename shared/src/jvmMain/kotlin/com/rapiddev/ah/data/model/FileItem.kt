package com.rapiddev.ah.data.model

data class FileItem(
    val name: String,
    val fullPath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String
)
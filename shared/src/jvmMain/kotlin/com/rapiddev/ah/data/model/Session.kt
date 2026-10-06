package com.rapiddev.ah.data.model

data class Session(
    val id: String,
    val name: String,
    val path: String,
    val type: SessionType,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpened: Long = System.currentTimeMillis(),
    val note: String = ""
)

enum class SessionType(val displayName: String, val emoji: String) {
    ANDROID("Android", "\uD83D\uDCF1"),
    WEB("موقع ويب", "\uD83C\uDF10"),
    TREE("من شجرة", "\uD83C\uDF33"),
    IMPORT_AI("مستورد AI", "\uD83E\uDD16"),
    CUSTOM("مخصص", "\uD83D\uDCC1")
}
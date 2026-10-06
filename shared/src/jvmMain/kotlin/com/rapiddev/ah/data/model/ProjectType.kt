package com.rapiddev.ah.data.model

enum class ProjectType(
    val displayName: String,
    val description: String,
    val emoji: String,
    val needsLanguage: Boolean = false
) {
    ANDROID(
        "مشروع تطبيق Android",
        "Java أو Kotlin — مطابق لـ Android IDE PRO",
        "\uD83D\uDCF1",
        needsLanguage = true
    ),
    WEB(
        "موقع ويب",
        "HTML + CSS + JavaScript",
        "\uD83C\uDF10"
    ),
    FROM_TREE(
        "من شجرة",
        "الصق شجرة مشروع (مع أو بدون أكواد) لإنشائه",
        "\uD83C\uDF33"
    );

    enum class AndroidLanguage(val displayName: String) {
        JAVA("Java"),
        KOTLIN("Kotlin")
    }
}
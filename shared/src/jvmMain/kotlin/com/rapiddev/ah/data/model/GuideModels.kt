package com.rapiddev.ah.data.model

data class GuideSection(
    val id: String,
    val title: String,
    val emoji: String,
    val steps: List<GuideStep>
)

data class GuideStep(
    val title: String,
    val body: String,
    val prompt: String? = null,
    val showCopy: Boolean = false
)
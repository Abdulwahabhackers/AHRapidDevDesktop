package com.rapiddev.ah.core.templates

import com.rapiddev.ah.data.model.AiParsedFile
import com.rapiddev.ah.data.model.ProjectType

object ProjectTemplates {

    fun getFiles(
        type: ProjectType,
        appName: String,
        packageName: String,
        androidLanguage: ProjectType.AndroidLanguage = ProjectType.AndroidLanguage.KOTLIN
    ): List<AiParsedFile> = when (type) {
        ProjectType.ANDROID -> AndroidTemplates.getFiles(androidLanguage, appName, packageName)
        ProjectType.WEB -> webStatic(appName)
        ProjectType.FROM_TREE -> emptyList()
    }

    private fun webStatic(appName: String): List<AiParsedFile> = listOf(
        AiParsedFile("index.html", """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$appName</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <header><h1>$appName</h1></header>
    <main><p>مرحباً بك في موقعك الجديد!</p></main>
    <script src="script.js"></script>
</body>
</html>
""".trimIndent()),

        AiParsedFile("style.css", """
* { margin: 0; padding: 0; box-sizing: border-box; }

body {
    font-family: 'Segoe UI', sans-serif;
    background: #0B0F1A;
    color: #F1F5F9;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

header h1 {
    font-size: 3rem;
    color: #22D3EE;
    margin-bottom: 1rem;
}

main p { font-size: 1.2rem; color: #94A3B8; }
""".trimIndent()),

        AiParsedFile("script.js", "console.log('$appName جاهز!');")
    )
}
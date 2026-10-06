package com.rapiddev.ah.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

enum class CodeLanguage(val displayName: String) {
    KOTLIN("Kotlin"),
    JAVA("Java"),
    XML("XML"),
    JSON("JSON"),
    GROOVY("Groovy"),
    MARKDOWN("Markdown"),
    PLAIN("نص عادي");

    companion object {
        fun fromExtension(ext: String): CodeLanguage = when (ext.lowercase()) {
            "kt", "kts" -> KOTLIN
            "java" -> JAVA
            "xml", "html", "htm", "svg" -> XML
            "json" -> JSON
            "gradle" -> GROOVY
            "md", "markdown" -> MARKDOWN
            else -> PLAIN
        }
    }
}

/**
 * ألوان التلوين — مطابقة لثيم التطبيق الداكن
 */
data class SyntaxColors(
    val keyword: Color = Color(0xFFF472B6),      // وردي - كلمات مفتاحية
    val string: Color = Color(0xFFF0C674),       // ذهبي - نصوص
    val comment: Color = Color(0xFF6B7280),      // رمادي - تعليقات
    val number: Color = Color(0xFFA78BFA),       // بنفسجي - أرقام
    val annotation: Color = Color(0xFF22D3EE),   // سماوي - annotations
    val type: Color = Color(0xFF7DD3FC),         // أزرق فاتح - أنواع
    val function: Color = Color(0xFF93C5FD),     // أزرق - دوال
    val tag: Color = Color(0xFFF472B6),          // وسم XML
    val attr: Color = Color(0xFFFBBF24),         // خاصية XML
    val plain: Color = Color(0xFFE5E7EB),        // عادي
    val keywordBold: Boolean = true
)

object CodeHighlighter {

    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "object", "interface", "enum", "fun",
        "val", "var", "vararg", "const", "lateinit", "by",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "try", "catch", "finally", "throw", "is", "as", "in", "!in", "out",
        "null", "true", "false", "this", "super", "it",
        "public", "private", "protected", "internal", "open", "final", "abstract",
        "override", "sealed", "data", "inline", "noinline", "crossinline", "reified",
        "suspend", "operator", "infix", "external", "companion", "init", "constructor",
        "typealias", "where", "actual", "expect", "annotation", "tailrec",
        "get", "set", "field", "value", "fun"
    )

    private val JAVA_KEYWORDS = setOf(
        "package", "import", "class", "interface", "enum", "extends", "implements",
        "public", "private", "protected", "static", "final", "abstract", "synchronized",
        "volatile", "transient", "native", "strictfp",
        "if", "else", "switch", "case", "default", "for", "while", "do",
        "return", "break", "continue", "try", "catch", "finally", "throw", "throws",
        "new", "this", "super", "null", "true", "false", "instanceof", "assert",
        "void", "int", "long", "short", "byte", "char", "boolean", "float", "double",
        "String", "Object", "List", "Map", "Set"
    )

    private val GROOVY_KEYWORDS = KOTLIN_KEYWORDS + setOf(
        "def", "println", "printf", "apply", "plugins", "repositories", "dependencies",
        "task", "buildscript", "allprojects", "subprojects", "ext", "rootProject"
    )

    fun highlight(
        code: String,
        language: CodeLanguage,
        colors: SyntaxColors = SyntaxColors()
    ): AnnotatedString = when (language) {
        CodeLanguage.KOTLIN -> highlightKotlin(code, colors, KOTLIN_KEYWORDS)
        CodeLanguage.JAVA -> highlightKotlin(code, colors, JAVA_KEYWORDS)
        CodeLanguage.GROOVY -> highlightKotlin(code, colors, GROOVY_KEYWORDS)
        CodeLanguage.XML -> highlightXml(code, colors)
        CodeLanguage.JSON -> highlightJson(code, colors)
        CodeLanguage.MARKDOWN -> highlightMarkdown(code, colors)
        CodeLanguage.PLAIN -> AnnotatedString(code)
    }

    // ============ Kotlin / Java / Groovy ============
    private fun highlightKotlin(
        code: String,
        colors: SyntaxColors,
        keywords: Set<String>
    ): AnnotatedString = buildAnnotatedString {
        var i = 0
        val n = code.length

        while (i < n) {
            val c = code[i]

            // تعليق سطري
            if (c == '/' && i + 1 < n && code[i + 1] == '/') {
                val end = code.indexOf('\n', i).let { if (it < 0) n else it }
                withStyle(SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // تعليق متعدد الأسطر
            if (c == '/' && i + 1 < n && code[i + 1] == '*') {
                val end = code.indexOf("*/", i + 2).let { if (it < 0) n else it + 2 }
                withStyle(SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // نص (""" و " و ')
            if (c == '"') {
                val triple = i + 2 < n && code[i + 1] == '"' && code[i + 2] == '"'
                val quote = if (triple) "\"\"\"" else "\""
                val closeIdx = if (triple) {
                    code.indexOf("\"\"\"", i + 3).let { if (it < 0) n else it + 3 }
                } else {
                    var j = i + 1
                    while (j < n) {
                        if (code[j] == '\\') { j += 2; continue }
                        if (code[j] == '"' || code[j] == '\n') break
                        j++
                    }
                    if (j < n && code[j] == '"') j + 1 else n
                }
                withStyle(SpanStyle(color = colors.string)) {
                    append(code.substring(i, closeIdx))
                }
                i = closeIdx
                continue
            }

            if (c == '\'') {
                var j = i + 1
                while (j < n) {
                    if (code[j] == '\\') { j += 2; continue }
                    if (code[j] == '\'' || code[j] == '\n') break
                    j++
                }
                val closeIdx = if (j < n && code[j] == '\'') j + 1 else n
                withStyle(SpanStyle(color = colors.string)) {
                    append(code.substring(i, closeIdx))
                }
                i = closeIdx
                continue
            }

            // annotation (@Override, @Composable)
            if (c == '@' && i + 1 < n && (code[i + 1].isLetter() || code[i + 1] == '_')) {
                var j = i + 1
                while (j < n && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                withStyle(SpanStyle(color = colors.annotation, fontWeight = FontWeight.Bold)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // رقم
            if (c.isDigit() && (i == 0 || !code[i - 1].isLetterOrDigit() && code[i - 1] != '_')) {
                var j = i
                while (j < n && (code[j].isDigit() || code[j] == '.' || code[j] == '_' ||
                            code[j] == 'x' || code[j] == 'X' || code[j] == 'L' || code[j] == 'l' ||
                            code[j] == 'F' || code[j] == 'f' || code[j] == 'D' || code[j] == 'd' ||
                            (code[j] in 'a'..'f') || (code[j] in 'A'..'F'))) j++
                withStyle(SpanStyle(color = colors.number)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // معرّف (كلمة)
            if (c.isLetter() || c == '_') {
                var j = i
                while (j < n && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                val word = code.substring(i, j)

                when {
                    word in keywords -> withStyle(
                        SpanStyle(
                            color = colors.keyword,
                            fontWeight = if (colors.keywordBold) FontWeight.Bold else FontWeight.Normal
                        )
                    ) { append(word) }
                    // نوع يبدأ بحرف كبير
                    word.firstOrNull()?.isUpperCase() == true &&
                        j < n && (code[j] == '.' || code[j] == '(' || code[j].isWhitespace() || code[j] == '<') ->
                        withStyle(SpanStyle(color = colors.type)) { append(word) }
                    // دالة: كلمة متبوعة بـ (
                    j < n && code[j] == '(' ->
                        withStyle(SpanStyle(color = colors.function)) { append(word) }
                    else -> withStyle(SpanStyle(color = colors.plain)) { append(word) }
                }
                i = j
                continue
            }

            // أي رمز آخر
            withStyle(SpanStyle(color = colors.plain)) { append(c) }
            i++
        }
    }

    // ============ XML ============
    private fun highlightXml(code: String, colors: SyntaxColors): AnnotatedString = buildAnnotatedString {
        var i = 0
        val n = code.length

        while (i < n) {
            val c = code[i]

            // تعليق <!-- ... -->
            if (c == '<' && i + 3 < n && code.substring(i, i + 4) == "<!--") {
                val end = code.indexOf("-->", i + 4).let { if (it < 0) n else it + 3 }
                withStyle(SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // وسم <...>
            if (c == '<') {
                withStyle(SpanStyle(color = colors.tag)) { append(c) }
                i++
                if (i < n && code[i] == '/') { withStyle(SpanStyle(color = colors.tag)) { append('/') }; i++ }
                // اسم الوسم
                while (i < n && (code[i].isLetterOrDigit() || code[i] == '_' || code[i] == '-' || code[i] == ':' || code[i] == '.')) {
                    withStyle(SpanStyle(color = colors.tag, fontWeight = FontWeight.Bold)) { append(code[i]) }
                    i++
                }
                // داخل الوسم
                while (i < n && code[i] != '>' && code[i] != '<') {
                    val ch = code[i]
                    if (ch == '"') {
                        val end = code.indexOf('"', i + 1).let { if (it < 0) n else it + 1 }
                        withStyle(SpanStyle(color = colors.string)) { append(code.substring(i, end)) }
                        i = end
                        continue
                    }
                    if (ch.isLetter() || ch == '_' || ch == '-' || ch == ':') {
                        var j = i
                        while (j < n && (code[j].isLetterOrDigit() || code[j] == '_' || code[j] == '-' || code[j] == ':')) j++
                        withStyle(SpanStyle(color = colors.attr)) { append(code.substring(i, j)) }
                        i = j
                        continue
                    }
                    withStyle(SpanStyle(color = colors.plain)) { append(ch) }
                    i++
                }
                if (i < n && code[i] == '>') {
                    withStyle(SpanStyle(color = colors.tag)) { append('>') }
                    i++
                }
                continue
            }

            withStyle(SpanStyle(color = colors.plain)) { append(c) }
            i++
        }
    }

    // ============ JSON ============
    private fun highlightJson(code: String, colors: SyntaxColors): AnnotatedString = buildAnnotatedString {
        var i = 0
        val n = code.length

        while (i < n) {
            val c = code[i]

            if (c == '"') {
                var j = i + 1
                while (j < n) {
                    if (code[j] == '\\') { j += 2; continue }
                    if (code[j] == '"') break
                    j++
                }
                val end = if (j < n) j + 1 else n

                // هل هو مفتاح (يتبعه :)
                var k = end
                while (k < n && code[k].isWhitespace()) k++
                val isKey = k < n && code[k] == ':'

                withStyle(SpanStyle(color = if (isKey) colors.attr else colors.string)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            if (c.isDigit() || (c == '-' && i + 1 < n && code[i + 1].isDigit())) {
                var j = i + 1
                while (j < n && (code[j].isDigit() || code[j] == '.' || code[j] == 'e' || code[j] == 'E' || code[j] == '+' || code[j] == '-')) j++
                withStyle(SpanStyle(color = colors.number)) { append(code.substring(i, j)) }
                i = j
                continue
            }

            if (code.startsWith("true", i) || code.startsWith("false", i) || code.startsWith("null", i)) {
                val word = when {
                    code.startsWith("true", i) -> "true"
                    code.startsWith("false", i) -> "false"
                    else -> "null"
                }
                withStyle(SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold)) { append(word) }
                i += word.length
                continue
            }

            withStyle(SpanStyle(color = colors.plain)) { append(c) }
            i++
        }
    }

    // ============ Markdown ============
    private fun highlightMarkdown(code: String, colors: SyntaxColors): AnnotatedString = buildAnnotatedString {
        code.lines().forEachIndexed { idx, line ->
            if (idx > 0) append('\n')
            when {
                line.startsWith("#") -> withStyle(SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold)) { append(line) }
                line.startsWith("```") -> withStyle(SpanStyle(color = colors.comment)) { append(line) }
                line.startsWith("- ") || line.startsWith("* ") ->
                    withStyle(SpanStyle(color = colors.annotation)) { append(line) }
                line.startsWith("> ") ->
                    withStyle(SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)) { append(line) }
                else -> withStyle(SpanStyle(color = colors.plain)) { append(line) }
            }
        }
    }
}
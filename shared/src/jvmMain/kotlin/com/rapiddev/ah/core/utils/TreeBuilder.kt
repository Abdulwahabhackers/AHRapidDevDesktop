package com.rapiddev.ah.core.utils

import java.io.File
import java.util.Locale

object TreeBuilder {

    private val DEFAULT_IGNORE = setOf(
        ".git", ".gradle", ".idea", "build", "node_modules",
        ".androidide", ".cxx", "__pycache__", ".dart_tool",
        ".vscode", "dist", "out", "target", ".cache", ".kotlin"
    )

    data class TreeNode(
        val name: String,
        val isDirectory: Boolean,
        val children: List<TreeNode>
    )

    fun buildTree(
        root: File,
        ignoreDirs: Set<String> = DEFAULT_IGNORE,
        maxDepth: Int = Int.MAX_VALUE,
        showHidden: Boolean = false
    ): TreeNode? {
        if (!root.exists() || !root.isDirectory) return null
        return buildRecursive(root, ignoreDirs, maxDepth, showHidden, 0)
    }

    private fun buildRecursive(
        file: File,
        ignoreDirs: Set<String>,
        maxDepth: Int,
        showHidden: Boolean,
        depth: Int
    ): TreeNode {
        val children = mutableListOf<TreeNode>()

        if (depth < maxDepth && file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                val filtered = files.filter { f ->
                    if (!showHidden && f.name.startsWith(".")) return@filter false
                    if (f.isDirectory && ignoreDirs.contains(f.name)) return@filter false
                    true
                }
                val sorted = filtered.sortedWith(
                    compareByDescending<File> { it.isDirectory }
                        .thenBy { it.name.lowercase(Locale.ROOT) }
                )
                for (child in sorted) {
                    children.add(
                        buildRecursive(child, ignoreDirs, maxDepth, showHidden, depth + 1)
                    )
                }
            }
        }

        return TreeNode(
            name = file.name,
            isDirectory = file.isDirectory,
            children = children
        )
    }

    fun toTreeString(node: TreeNode): String {
        val sb = StringBuilder()
        sb.append(node.name)
        if (node.isDirectory) sb.append("/")
        sb.append("\n")
        renderChildren(node.children, "", sb)
        return sb.toString()
    }

    private fun renderChildren(children: List<TreeNode>, prefix: String, sb: StringBuilder) {
        for ((i, child) in children.withIndex()) {
            val isLast = i == children.size - 1
            val connector = if (isLast) "└── " else "├── "
            sb.append(prefix).append(connector).append(child.name)
            if (child.isDirectory) sb.append("/")
            sb.append("\n")

            if (child.isDirectory && child.children.isNotEmpty()) {
                val nextPrefix = prefix + (if (isLast) "    " else "│   ")
                renderChildren(child.children, nextPrefix, sb)
            }
        }
    }

    fun countEntries(node: TreeNode): Pair<Int, Int> {
        var dirs = 0
        var files = 0
        if (node.isDirectory) dirs++ else files++
        for (child in node.children) {
            val counts = countEntries(child)
            dirs += counts.first
            files += counts.second
        }
        return Pair(dirs, files)
    }
}
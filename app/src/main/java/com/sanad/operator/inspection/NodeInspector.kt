package com.sanad.operator.inspection

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object NodeInspector {
    private const val MAX_DEPTH = 12
    private const val MAX_NODES = 250

    fun inspect(root: AccessibilityNodeInfo?): List<String> {
        if (root == null) return listOf("root=null")
        val output = mutableListOf<String>()
        var visited = 0

        fun walk(node: AccessibilityNodeInfo, depth: Int) {
            if (depth > MAX_DEPTH || visited >= MAX_NODES) return
            visited++

            val bounds = Rect().also(node::getBoundsInScreen)
            val text = node.text?.toString()?.take(120).orEmpty()
            val desc = node.contentDescription?.toString()?.take(120).orEmpty()
            val viewId = node.viewIdResourceName.orEmpty()
            val className = node.className?.toString().orEmpty()

            val interesting = text.isNotBlank() || desc.isNotBlank() || viewId.isNotBlank() ||
                node.isClickable || node.isEditable || node.isFocusable

            if (interesting) {
                output += buildString {
                    append("depth=").append(depth)
                    append(" class=").append(className)
                    if (viewId.isNotBlank()) append(" id=").append(viewId)
                    if (text.isNotBlank()) append(" text=\"").append(text).append('"')
                    if (desc.isNotBlank()) append(" desc=\"").append(desc).append('"')
                    append(" clickable=").append(node.isClickable)
                    append(" editable=").append(node.isEditable)
                    append(" focusable=").append(node.isFocusable)
                    append(" bounds=").append(bounds.toShortString())
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    try {
                        walk(child, depth + 1)
                    } finally {
                        child.recycle()
                    }
                }
            }
        }

        walk(root, 0)
        output += "nodes_visited=$visited"
        return output
    }
}

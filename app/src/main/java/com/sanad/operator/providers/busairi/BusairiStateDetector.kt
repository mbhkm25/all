package com.sanad.operator.providers.busairi

import android.view.accessibility.AccessibilityNodeInfo

enum class BusairiState {
    UNKNOWN,
    LOGIN,
    HOME,
    TRANSFER_FORM,
    CONFIRMATION,
    AUTHENTICATION_REQUIRED
}

object BusairiStateDetector {
    fun detect(root: AccessibilityNodeInfo?): BusairiState {
        if (root == null) return BusairiState.UNKNOWN

        if (findById(root, BusairiContract.Id.ACCOUNT_NUMBER) != null &&
            findById(root, BusairiContract.Id.AMOUNT) != null
        ) {
            return BusairiState.TRANSFER_FORM
        }

        if (findText(root, "تأكيد كلمة المرور") != null ||
            (findById(root, "${BusairiContract.PACKAGE}:id/imgFinger") != null &&
                findById(root, "${BusairiContract.PACKAGE}:id/btnOk") != null)
        ) {
            return BusairiState.AUTHENTICATION_REQUIRED
        }

        if (findText(root, BusairiContract.Text.CONFIRM_OPERATION) != null) {
            return BusairiState.CONFIRMATION
        }

        if (findText(root, BusairiContract.Text.TRANSFER_TO_ACCOUNT) != null) {
            return BusairiState.HOME
        }

        val allText = collectText(root)
        if (allText.any { it.contains("اسم المستخدم") } &&
            allText.any { it.contains("كلمة المرور") }
        ) {
            return BusairiState.LOGIN
        }

        return BusairiState.UNKNOWN
    }

    fun findById(root: AccessibilityNodeInfo?, id: String): AccessibilityNodeInfo? =
        root?.findAccessibilityNodeInfosByViewId(id)?.firstOrNull()

    fun findText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? =
        root?.findAccessibilityNodeInfosByText(text)?.firstOrNull { node ->
            node.text?.toString()?.contains(text) == true ||
                node.contentDescription?.toString()?.contains(text) == true
        }

    private fun collectText(root: AccessibilityNodeInfo): List<String> {
        val out = mutableListOf<String>()
        fun walk(node: AccessibilityNodeInfo, depth: Int) {
            if (depth > 12) return
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let { child ->
                    try {
                        walk(child, depth + 1)
                    } finally {
                        child.recycle()
                    }
                }
            }
        }
        walk(root, 0)
        return out
    }
}

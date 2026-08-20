package com.sanad.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.sanad.operator.inspection.InspectionLog
import com.sanad.operator.inspection.NodeInspector

class SanadAccessibilityService : AccessibilityService() {

    private var lastInspectedPackage: String = ""
    private var lastInspectionAtMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        InspectionLog.add("Accessibility service connected")
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isBlank() || packageName in IGNORED_PACKAGES) return

        val className = event.className?.toString().orEmpty()
        val eventName = AccessibilityEvent.eventTypeToString(event.eventType)
        InspectionLog.add("EVENT $eventName package=$packageName class=$className")

        val shouldInspect = when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> true
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val now = SystemClock.elapsedRealtime()
                val packageChanged = packageName != lastInspectedPackage
                val enoughTimePassed = now - lastInspectionAtMs >= CONTENT_INSPECTION_THROTTLE_MS
                packageChanged || enoughTimePassed
            }
            else -> false
        }

        if (!shouldInspect) return

        lastInspectedPackage = packageName
        lastInspectionAtMs = SystemClock.elapsedRealtime()

        val root = rootInActiveWindow
        val rootPackage = root?.packageName?.toString().orEmpty()
        InspectionLog.add("SNAPSHOT package=$packageName rootPackage=$rootPackage")
        NodeInspector.inspect(root).forEach { line ->
            InspectionLog.add("NODE $line")
            Log.d(TAG, "$packageName $line")
        }
    }

    override fun onInterrupt() {
        InspectionLog.add("Accessibility service interrupted")
    }

    override fun onDestroy() {
        InspectionLog.add("Accessibility service destroyed")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SanadInspector"
        private const val CONTENT_INSPECTION_THROTTLE_MS = 700L

        private val IGNORED_PACKAGES = setOf(
            "com.sanad.operator",
            "com.sec.android.app.launcher",
            "com.android.systemui",
            "com.samsung.android.app.cocktailbarservice"
        )
    }
}

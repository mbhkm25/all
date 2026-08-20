package com.sanad.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.sanad.operator.inspection.InspectionLog
import com.sanad.operator.inspection.NodeInspector
import com.sanad.operator.providers.busairi.BusairiContract
import com.sanad.operator.providers.busairi.BusairiStateDetector

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
        if (packageName.isBlank()) return

        // PoC 0.1 is intentionally scoped to Al Busairi only. This prevents
        // unrelated high-frequency apps (ChatGPT, Android chooser, keyboard, etc.)
        // from flooding the bounded inspection log and evicting the evidence we need.
        if (packageName != BusairiContract.PACKAGE) return

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

        val state = BusairiStateDetector.detect(root)
        InspectionLog.add("BUSAIRI_STATE $state")

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
    }
}

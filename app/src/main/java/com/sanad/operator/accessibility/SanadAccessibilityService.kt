package com.sanad.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.sanad.operator.inspection.InspectionLog
import com.sanad.operator.inspection.NodeInspector

class SanadAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        InspectionLog.add("Accessibility service connected")
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString().orEmpty()
        val className = event.className?.toString().orEmpty()
        val eventName = AccessibilityEvent.eventTypeToString(event.eventType)

        InspectionLog.add("EVENT $eventName package=$packageName class=$className")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val root = rootInActiveWindow
            NodeInspector.inspect(root).forEach { line ->
                InspectionLog.add("NODE $line")
                Log.d(TAG, line)
            }
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
    }
}

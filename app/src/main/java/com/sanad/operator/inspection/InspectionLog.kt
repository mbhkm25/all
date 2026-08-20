package com.sanad.operator.inspection

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

object InspectionLog {
    private const val MAX_ENTRIES = 1500
    private val entries = CopyOnWriteArrayList<String>()
    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun add(message: String) {
        val line = "${formatter.format(Date())}  $message"
        entries.add(line)
        while (entries.size > MAX_ENTRIES) {
            entries.removeAt(0)
        }
    }

    fun snapshot(): List<String> = entries.toList()

    fun clear() = entries.clear()
}

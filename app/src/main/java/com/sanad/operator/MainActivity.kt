package com.sanad.operator

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sanad.operator.inspection.InspectionLog

class MainActivity : AppCompatActivity() {

    private lateinit var statusView: TextView
    private lateinit var logView: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshUi()
            handler.postDelayed(this, 800)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "SANAD Operator PoC"
        setContentView(buildUi())
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshRunnable)
        super.onPause()
    }

    private fun buildUi(): ScrollView {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            setPadding(dp(20), dp(24), dp(20), dp(24))
            layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "SANAD Financial Operator — PoC 0.1"
            textSize = 22f
            gravity = Gravity.END
        }

        val explanation = TextView(this).apply {
            text = "هذه النسخة لا تنفذ أي تحويل مالي. وظيفتها الحالية هي فحص Accessibility Tree للتطبيق النشط بعد تفعيل الخدمة يدويًا من إعدادات أندرويد."
            textSize = 16f
            gravity = Gravity.END
            setPadding(0, dp(12), 0, dp(16))
        }

        statusView = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.END
            setPadding(0, 0, 0, dp(12))
        }

        val openSettings = Button(this).apply {
            text = "فتح إعدادات إمكانية الوصول"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val clearLog = Button(this).apply {
            text = "مسح سجل الفحص"
            setOnClickListener {
                InspectionLog.clear()
                refreshUi()
            }
        }

        val instructions = TextView(this).apply {
            text = "طريقة الاختبار:\n1) فعّل SANAD Operator من إعدادات إمكانية الوصول.\n2) اخرج من التطبيق وافتح البسيري يدويًا.\n3) تنقل بين الرئيسية والخدمات المالية وتسجيل الدخول وتحويل لحساب بدون تنفيذ عملية فعلية.\n4) ارجع إلى SANAD Operator واقرأ السجل أدناه."
            textSize = 15f
            gravity = Gravity.END
            setPadding(0, dp(16), 0, dp(12))
        }

        logView = TextView(this).apply {
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.START
            textDirection = android.view.View.TEXT_DIRECTION_LTR
            movementMethod = ScrollingMovementMethod()
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        listOf<ViewGroup.LayoutParams>(
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        root.addView(title)
        root.addView(explanation)
        root.addView(statusView)
        root.addView(openSettings, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(clearLog, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(8)
        })
        root.addView(instructions)
        root.addView(logView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(620)))

        scroll.addView(root)
        return scroll
    }

    private fun refreshUi() {
        val enabled = isAccessibilityServiceEnabled()
        statusView.text = if (enabled) {
            "الحالة: خدمة SANAD مفعلة ✓"
        } else {
            "الحالة: خدمة SANAD غير مفعلة"
        }

        val lines = InspectionLog.snapshot()
        logView.text = if (lines.isEmpty()) {
            "لا توجد أحداث بعد."
        } else {
            lines.takeLast(180).joinToString("\n")
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        return enabledServices.contains("$packageName/${com.sanad.operator.accessibility.SanadAccessibilityService::class.java.name}", ignoreCase = true)
    }
}

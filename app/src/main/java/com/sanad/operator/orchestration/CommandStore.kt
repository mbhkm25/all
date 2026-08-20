package com.sanad.operator.orchestration

import com.sanad.operator.domain.TransferToAccountIntent
import java.util.concurrent.atomic.AtomicReference

object CommandStore {
    enum class Stage {
        IDLE,
        READY,
        WAITING_FOR_LOGIN,
        NAVIGATING,
        FILLING_FORM,
        WAITING_FOR_USER_CONFIRMATION,
        COMPLETED,
        FAILED
    }

    data class Snapshot(
        val command: TransferToAccountIntent? = null,
        val stage: Stage = Stage.IDLE,
        val message: String = "لا يوجد أمر نشط"
    )

    private val state = AtomicReference(Snapshot())

    fun snapshot(): Snapshot = state.get()

    fun submit(command: TransferToAccountIntent) {
        state.set(Snapshot(command, Stage.READY, "الأمر جاهز للتنفيذ"))
    }

    fun update(stage: Stage, message: String) {
        val current = state.get()
        state.set(current.copy(stage = stage, message = message))
    }

    fun clear() {
        state.set(Snapshot())
    }
}

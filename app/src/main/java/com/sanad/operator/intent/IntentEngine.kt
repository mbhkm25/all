package com.sanad.operator.intent

import com.sanad.operator.domain.TransferToAccountIntent

interface IntentEngine {
    suspend fun parse(input: String): Result<TransferToAccountIntent>
}

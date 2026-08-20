package com.sanad.operator.domain

data class TransferToAccountIntent(
    val provider: String = "albusairi",
    val accountNumber: String,
    val amount: String,
    val currency: String = "YER",
    val notes: String = ""
)

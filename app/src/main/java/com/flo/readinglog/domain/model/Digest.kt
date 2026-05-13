package com.flo.readinglog.domain.model

import java.time.LocalDate

data class Digest(
    val id: Long,
    val weekStart: LocalDate,
    val message: String,
    val sentAt: Long,
)

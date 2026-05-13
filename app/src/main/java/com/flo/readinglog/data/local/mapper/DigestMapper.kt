package com.flo.readinglog.data.local.mapper

import com.flo.readinglog.data.local.entity.DigestEntity
import com.flo.readinglog.domain.model.Digest
import java.time.LocalDate

fun DigestEntity.toDomain() = Digest(
    id = id,
    weekStart = LocalDate.ofEpochDay(weekStartEpochDay),
    message = message,
    sentAt = sentAt,
)

fun Digest.toEntity() = DigestEntity(
    id = id,
    weekStartEpochDay = weekStart.toEpochDay(),
    message = message,
    sentAt = sentAt,
)

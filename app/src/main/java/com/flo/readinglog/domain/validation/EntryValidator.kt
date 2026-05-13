package com.flo.readinglog.domain.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

object EntryValidator {
    fun validatePageRange(pageFrom: Int, pageTo: Int): ValidationResult = when {
        pageFrom <= 0 -> ValidationResult.Invalid("Page from must be greater than 0")
        pageTo <= 0 -> ValidationResult.Invalid("Page to must be greater than 0")
        pageTo < pageFrom -> ValidationResult.Invalid("Page to must be >= page from")
        else -> ValidationResult.Valid
    }

    fun validatePageRange(pageFromStr: String, pageToStr: String): ValidationResult {
        val from = pageFromStr.toIntOrNull()
            ?: return ValidationResult.Invalid("Page from must be a number")
        val to = pageToStr.toIntOrNull()
            ?: return ValidationResult.Invalid("Page to must be a number")
        return validatePageRange(from, to)
    }
}

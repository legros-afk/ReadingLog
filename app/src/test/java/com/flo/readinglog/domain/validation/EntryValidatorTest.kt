package com.flo.readinglog.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryValidatorTest {

    // --- Int overload ---

    @Test
    fun `valid range returns Valid`() {
        val result = EntryValidator.validatePageRange(1, 50)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `single page returns Valid`() {
        val result = EntryValidator.validatePageRange(42, 42)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `pageTo less than pageFrom returns Invalid`() {
        val result = EntryValidator.validatePageRange(10, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `pageFrom zero returns Invalid`() {
        val result = EntryValidator.validatePageRange(0, 10)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `pageFrom negative returns Invalid`() {
        val result = EntryValidator.validatePageRange(-1, 10)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `pageTo zero returns Invalid`() {
        val result = EntryValidator.validatePageRange(1, 0)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `pageTo negative returns Invalid`() {
        val result = EntryValidator.validatePageRange(1, -5)
        assertTrue(result is ValidationResult.Invalid)
    }

    // --- String overload ---

    @Test
    fun `valid string range returns Valid`() {
        val result = EntryValidator.validatePageRange("1", "50")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `non-numeric pageFrom string returns Invalid`() {
        val result = EntryValidator.validatePageRange("abc", "50")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `non-numeric pageTo string returns Invalid`() {
        val result = EntryValidator.validatePageRange("1", "xyz")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `empty string pageFrom returns Invalid`() {
        val result = EntryValidator.validatePageRange("", "50")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `string pageTo less than pageFrom returns Invalid`() {
        val result = EntryValidator.validatePageRange("20", "10")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `string pageFrom zero returns Invalid`() {
        val result = EntryValidator.validatePageRange("0", "10")
        assertTrue(result is ValidationResult.Invalid)
    }
}

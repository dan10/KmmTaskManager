package com.danioliveira.taskmanager.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileValidatorTest {

    @Test
    fun `isValidMimeType should return true for allowed MIME types`() {
        // Test all allowed MIME types
        assertTrue(FileValidator.isValidMimeType("image/jpeg"))
        assertTrue(FileValidator.isValidMimeType("image/jpg"))
        assertTrue(FileValidator.isValidMimeType("image/png"))
        assertTrue(FileValidator.isValidMimeType("image/gif"))
        assertTrue(FileValidator.isValidMimeType("application/pdf"))

        // Test case insensitivity
        assertTrue(FileValidator.isValidMimeType("IMAGE/JPEG"))
        assertTrue(FileValidator.isValidMimeType("Application/Pdf"))
    }

    @Test
    fun `isValidMimeType should return false for disallowed MIME types`() {
        assertFalse(FileValidator.isValidMimeType("text/plain"))
        assertFalse(FileValidator.isValidMimeType("application/json"))
        assertFalse(FileValidator.isValidMimeType("application/zip"))
        assertFalse(FileValidator.isValidMimeType("video/mp4"))
        assertFalse(FileValidator.isValidMimeType("audio/mpeg"))
    }

    @Test
    fun `getAllowedMimeTypesAsString should return a comma-separated list of allowed MIME types`() {
        val allowedMimeTypes = FileValidator.getAllowedMimeTypesAsString()
        assertTrue(allowedMimeTypes.contains("image/jpeg"))
        assertTrue(allowedMimeTypes.contains("image/jpg"))
        assertTrue(allowedMimeTypes.contains("image/png"))
        assertTrue(allowedMimeTypes.contains("image/gif"))
        assertTrue(allowedMimeTypes.contains("application/pdf"))
    }
}
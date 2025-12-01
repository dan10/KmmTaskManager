package com.danioliveira.taskmanager.utils

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extension function to generate a UUIDv7.
 * UUIDv7 is time-ordered, which provides better database indexing performance
 * compared to UUIDv4 (random).
 *
 * Benefits of UUIDv7:
 * - Time-ordered: Sequential nature improves B-tree index performance
 * - Sortable: Can be sorted by creation time
 * - Unique: Still maintains UUID uniqueness guarantees
 * - Database-friendly: Reduces index fragmentation
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.randomV7(): Uuid {
    val value = random().toByteArray()
    val timestamp = Clock.System.now().toEpochMilliseconds()
    
    // Set version to 7 (0111 in binary)
    value[6] = (value[6].toInt() and 0x0F or 0x70).toByte()
    
    // Set variant to RFC 4122 (10 in binary)
    value[8] = (value[8].toInt() and 0x3F or 0x80).toByte()
    
    // Set the timestamp in the first 48 bits
    repeat(6) { value[it] = ((timestamp shr (40 - 8 * it)) and 0xFF).toByte() }
    
    return fromByteArray(value)
}

/**
 * Extension function to convert a String to Kotlin UUID.
 * Throws IllegalArgumentException if the string is not a valid UUID.
 */
@OptIn(ExperimentalUuidApi::class)
fun String.toUuid(): Uuid {
    return Uuid.parse(this)
}

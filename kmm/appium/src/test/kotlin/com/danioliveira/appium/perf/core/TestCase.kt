package com.danioliveira.appium.perf.core

/**
 * Represents a single performance test case.
 *
 * @property beforeTest Setup actions to run before the test (e.g., login, navigation)
 * @property run The actual test actions to measure
 * @property durationMs Optional duration hint for the test (used for planning, not enforcement)
 */
data class TestCase(
    val beforeTest: suspend () -> Unit = {},
    val run: suspend () -> Unit,
    val durationMs: Long? = null
)





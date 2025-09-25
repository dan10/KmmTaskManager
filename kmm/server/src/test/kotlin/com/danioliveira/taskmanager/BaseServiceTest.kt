package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

abstract class BaseServiceTest : KoinTest {

    @Before
    fun setUpBase() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()

        // Start Koin with the test module
        startKoin {
            modules(getTestModule())
        }

        // Initialize JwtConfig with test configuration
        JwtConfig.init(
            com.danioliveira.taskmanager.domain.JwtConfig(
                secret = "test-secret",
                issuer = "test-issuer",
                audience = "test-audience",
                realm = "test-realm",
                validityMs = 36_000_00 * 10 // 10 hours
            )
        )

        // Call additional setup if needed by subclasses
        additionalSetup()
    }

    @After
    fun tearDownBase() = runBlocking {
        // Call additional teardown if needed by subclasses
        additionalTeardown()

        // Clear the database after each test
        TestDatabase.clearDatabase()

        // Stop Koin
        stopKoin()
    }

    /**
     * Override this method in subclasses to add additional setup logic
     */
    protected open suspend fun additionalSetup() {
        // Default implementation does nothing
    }

    /**
     * Override this method in subclasses to add additional teardown logic
     */
    protected open suspend fun additionalTeardown() {
        // Default implementation does nothing
    }
}
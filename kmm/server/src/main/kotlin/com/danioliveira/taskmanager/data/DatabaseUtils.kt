package com.danioliveira.taskmanager.data

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Extension function to execute database operations in a suspended transaction with IO dispatcher.
 * This centralizes the transaction handling logic and ensures consistent use of the IO dispatcher.
 *
 * @param block The database operation to execute within the transaction.
 * @return The result of the database operation.
 */
suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
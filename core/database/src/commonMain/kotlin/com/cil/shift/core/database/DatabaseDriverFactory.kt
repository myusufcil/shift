package com.cil.shift.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult

/**
 * Platform-specific factory for creating SQLDelight database drivers.
 * Implementations are provided in androidMain and iosMain.
 */
expect class DatabaseDriverFactory {
    /**
     * Creates a platform-specific SQL driver without schema.
     * Schema will be passed when creating the database instance.
     *
     * @param dbName The name of the database file
     * @return A platform-specific SqlDriver instance
     */
    fun createDriver(dbName: String): SqlDriver

    /**
     * Creates a platform-specific SQL driver with schema.
     *
     * @param schema The database schema
     * @param dbName The name of the database file
     * @return A platform-specific SqlDriver instance
     */
    fun createDriverWithSchema(schema: SqlSchema<QueryResult.Value<Unit>>, dbName: String): SqlDriver
}

package com.cil.shift.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult

/**
 * iOS implementation of DatabaseDriverFactory using NativeSqliteDriver.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(dbName: String): SqlDriver {
        // Driver will be created with schema when initializing the database
        throw IllegalStateException("Use createDriver(schema, dbName) instead")
    }

    actual fun createDriverWithSchema(schema: SqlSchema<QueryResult.Value<Unit>>, dbName: String): SqlDriver {
        return NativeSqliteDriver(
            schema = schema,
            name = dbName
        )
    }
}

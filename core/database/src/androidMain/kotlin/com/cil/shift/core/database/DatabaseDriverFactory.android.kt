package com.cil.shift.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult

/**
 * Android implementation of DatabaseDriverFactory using AndroidSqliteDriver.
 *
 * @param context Android context required for database creation
 */
actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(dbName: String): SqlDriver {
        // Driver will be created with schema when initializing the database
        throw IllegalStateException("Use createDriver(schema, dbName) instead")
    }

    actual fun createDriverWithSchema(schema: SqlSchema<QueryResult.Value<Unit>>, dbName: String): SqlDriver {
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = dbName
        )
    }
}

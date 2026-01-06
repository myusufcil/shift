package com.cil.shift.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

/**
 * iOS implementation of DatabaseDriverFactory using NativeSqliteDriver.
 * Uses App Group container for widget data sharing.
 */
actual class DatabaseDriverFactory {

    private val appGroupIdentifier = "group.com.cil.shift"

    actual fun createDriver(dbName: String): SqlDriver {
        // Driver will be created with schema when initializing the database
        throw IllegalStateException("Use createDriver(schema, dbName) instead")
    }

    actual fun createDriverWithSchema(schema: SqlSchema<QueryResult.Value<Unit>>, dbName: String): SqlDriver {
        // Try to use App Group container for widget sharing
        val containerUrl = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(appGroupIdentifier)

        return if (containerUrl != null) {
            // Use App Group container path
            val dbPath = containerUrl.path + "/$dbName"
            NativeSqliteDriver(
                schema = schema,
                name = dbPath
            )
        } else {
            // Fallback to default location
            NativeSqliteDriver(
                schema = schema,
                name = dbName
            )
        }
    }
}

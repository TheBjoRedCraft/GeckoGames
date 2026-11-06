package dev.thebjoredcraft.gecko.server.database

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.thebjoredcraft.gecko.server.database.table.*
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventDataTable
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventsTable

object GeckoDatabaseManager {
    private lateinit var database: R2dbcDatabase

    suspend fun create() {
        database = R2dbcDatabase.connect {
            connectionFactoryOptions {

            }
        }


        suspendTransaction {
            SchemaUtils.create(
                GeckoGamesTable,
                GeckoGameStatsTable,
                GeckoPunishmentsTable,
                GeckoPlayerNamesTable,
                GeckoPlayerLanguagesTable,
                GeckoEventsTable,
                GeckoEventDataTable
            )
        }
    }

    fun shutdown() {
        if (::databaseApi.isInitialized) {
            databaseApi.shutdown()
        }
    }
}
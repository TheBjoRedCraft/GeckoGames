package dev.thebjoredcraft.gecko.server.database

import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.thebjoredcraft.gecko.server.database.table.*
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventDataTable
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventsTable
import kotlin.io.path.Path

object GeckoDatabaseManager {
    private lateinit var databaseApi: DatabaseApi

    suspend fun create() {
        databaseApi = DatabaseApi.create(Path("."))

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
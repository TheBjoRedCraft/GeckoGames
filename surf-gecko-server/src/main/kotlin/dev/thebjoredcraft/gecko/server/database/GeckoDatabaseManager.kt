package dev.thebjoredcraft.gecko.server.database

import dev.thebjoredcraft.gecko.server.database.table.*
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventDataTable
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventsTable
import dev.thebjoredcraft.railway.database.DatabaseConnection
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.io.path.Path

object GeckoDatabaseManager {
    suspend fun create() {
        DatabaseConnection.create(Path("."))


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
        DatabaseConnection.disconnect()
    }
}
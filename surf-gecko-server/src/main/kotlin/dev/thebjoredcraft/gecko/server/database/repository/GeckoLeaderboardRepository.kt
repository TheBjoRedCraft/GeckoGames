package dev.thebjoredcraft.gecko.server.database.repository

import dev.thebjoredcraft.gecko.server.database.table.GeckoGameStatsTable
import dev.thebjoredcraft.gecko.server.gecko.lobby.leaderbord.data.LeaderboardSummary
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object GeckoLeaderboardRepository {
    suspend fun fetchWinTotals(): List<LeaderboardSummary> = suspendTransaction {
        val wins = GeckoGameStatsTable.id.count()

        GeckoGameStatsTable
            .select(GeckoGameStatsTable.playerUuid, wins)
            .where(GeckoGameStatsTable.win eq true)
            .groupBy(GeckoGameStatsTable.playerUuid)
            .orderBy(wins, SortOrder.DESC)
            .map { LeaderboardSummary(it[GeckoGameStatsTable.playerUuid], it[wins]) }
            .toList()
    }

    suspend fun fetchGamesPlayedTotals(): List<LeaderboardSummary> = suspendTransaction {
        val gamesPlayed = GeckoGameStatsTable.id.count()

        GeckoGameStatsTable
            .select(GeckoGameStatsTable.playerUuid, gamesPlayed)
            .groupBy(GeckoGameStatsTable.playerUuid)
            .orderBy(gamesPlayed, SortOrder.DESC)
            .map { LeaderboardSummary(it[GeckoGameStatsTable.playerUuid], it[gamesPlayed]) }
            .toList()
    }
}

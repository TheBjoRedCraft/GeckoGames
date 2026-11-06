package dev.thebjoredcraft.gecko.server.database.repository

import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventDataTable
import dev.thebjoredcraft.gecko.server.database.table.events.GeckoEventsTable
import dev.thebjoredcraft.gecko.server.gecko.events.GeckoEvent
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

object GeckoEventsRepository {
    private val zone = ZoneId.of("Europe/Berlin")
    suspend fun logEvent(event: GeckoEvent) =
        suspendTransaction {
            val data = event.mapData()
            val eventId = GeckoEventsTable.insertAndGetId {
                it[eventType] = event.type
            }.value

            GeckoEventDataTable.batchInsert(data.entries) { (key, value) ->
                this[GeckoEventDataTable.eventId] = eventId
                this[GeckoEventDataTable.key] = key
                this[GeckoEventDataTable.value] = value
            }
        }

    suspend fun fetchTodayJoinMeCount(playerUuid: UUID) = suspendTransaction {
        val now = OffsetDateTime.now(zone.rules.getOffset(Instant.now()))
        val startOfDay = now.toLocalDate().atStartOfDay(zone).toOffsetDateTime()
        val startOfTomorrow = startOfDay.plusDays(1)

        GeckoEventsTable
            .join(
                GeckoEventDataTable,
                JoinType.INNER,
                GeckoEventsTable.id,
                GeckoEventDataTable.eventId
            )
            .select(GeckoEventsTable.id)
            .where {
                (GeckoEventsTable.eventType eq GeckoEvent.Type.JOIN_ME) and
                        (GeckoEventDataTable.key eq "sender_uuid") and
                        (GeckoEventDataTable.value eq playerUuid.toString()) and
                        (GeckoEventsTable.createdAt greaterEq startOfDay) and
                        (GeckoEventsTable.createdAt less startOfTomorrow)
            }
            .count()
    }

    suspend fun fetchTodayGamesCreatedCount(playerUuid: UUID) = suspendTransaction {
        val now = OffsetDateTime.now(zone.rules.getOffset(Instant.now()))
        val startOfDay = now.toLocalDate().atStartOfDay(zone).toOffsetDateTime()
        val startOfTomorrow = startOfDay.plusDays(1)

        GeckoEventsTable
            .join(
                GeckoEventDataTable,
                JoinType.INNER,
                GeckoEventsTable.id,
                GeckoEventDataTable.eventId
            )
            .select(GeckoEventsTable.id)
            .where {
                (GeckoEventsTable.eventType eq GeckoEvent.Type.CREATE_GAME) and
                        (GeckoEventDataTable.key eq "sender_uuid") and
                        (GeckoEventDataTable.value eq playerUuid.toString()) and
                        (GeckoEventsTable.createdAt greaterEq startOfDay) and
                        (GeckoEventsTable.createdAt less startOfTomorrow)
            }
            .count()
    }
}
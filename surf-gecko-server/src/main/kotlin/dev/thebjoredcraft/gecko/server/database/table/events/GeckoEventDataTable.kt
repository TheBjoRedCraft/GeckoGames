package dev.thebjoredcraft.gecko.server.database.table.events

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object GeckoEventDataTable : LongIdTable("gecko_event_data") {
    val eventId = long("event_id").references(GeckoEventsTable.id)
    val key = varchar("key", 255)
    val value = largeText("value")
}
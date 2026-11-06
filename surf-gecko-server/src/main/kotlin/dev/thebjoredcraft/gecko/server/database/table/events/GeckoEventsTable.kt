package dev.thebjoredcraft.gecko.server.database.table.events

import dev.thebjoredcraft.gecko.server.gecko.events.GeckoEvent
import dev.thebjoredcraft.railway.database.table.auditable.AuditableLongIdTable

object GeckoEventsTable : AuditableLongIdTable("gecko_events") {
    val eventType = enumerationByName<GeckoEvent.Type>(
        "event_type",
        50
    )
}
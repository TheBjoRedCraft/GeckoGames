package dev.thebjoredcraft.gecko.server.database.table

import dev.thebjoredcraft.gecko.server.gecko.state.GeckoGameEndReason
import dev.thebjoredcraft.railway.database.table.auditable.AuditableLongIdTable
import dev.thebjoredcraft.railway.database.table.column.stringUuid

object GeckoGamesTable : AuditableLongIdTable("gecko_games") {
    val map = stringUuid("map_uuid")
    val endReason = enumerationByName<GeckoGameEndReason>("end_reason", 50).default(
        GeckoGameEndReason.UNSET
    )
}
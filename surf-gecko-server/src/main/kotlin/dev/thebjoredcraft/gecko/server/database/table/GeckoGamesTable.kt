package dev.thebjoredcraft.gecko.server.database.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.thebjoredcraft.gecko.server.gecko.state.GeckoGameEndReason

object GeckoGamesTable : AuditableLongIdTable("gecko_games") {
    val map = nativeUuid("map_uuid")
    val endReason = enumerationByName<GeckoGameEndReason>("end_reason", 50).default(
        GeckoGameEndReason.UNSET
    )
}
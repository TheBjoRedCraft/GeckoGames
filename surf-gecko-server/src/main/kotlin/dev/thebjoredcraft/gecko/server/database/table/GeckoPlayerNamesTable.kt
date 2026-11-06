package dev.thebjoredcraft.gecko.server.database.table

import dev.thebjoredcraft.railway.database.table.auditable.AuditableTable
import dev.thebjoredcraft.railway.database.table.column.stringUuid

object GeckoPlayerNamesTable : AuditableTable("gecko_player_names") {
    val playerUuid = stringUuid("player_uuid").uniqueIndex()
    val name = varchar("name", 16)
}

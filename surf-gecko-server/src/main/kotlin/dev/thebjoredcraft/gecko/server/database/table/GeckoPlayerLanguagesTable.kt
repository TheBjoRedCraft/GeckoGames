package dev.thebjoredcraft.gecko.server.database.table

import dev.thebjoredcraft.railway.database.table.auditable.AuditableTable
import dev.thebjoredcraft.railway.database.table.column.stringUuid

object GeckoPlayerLanguagesTable : AuditableTable("gecko_player_languages") {
    val playerUuid = stringUuid("player_uuid").uniqueIndex()
    val language = varchar("language", 16)
}

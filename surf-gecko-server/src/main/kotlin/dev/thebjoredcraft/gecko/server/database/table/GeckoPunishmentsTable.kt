package dev.thebjoredcraft.gecko.server.database.table

import dev.thebjoredcraft.railway.database.table.auditable.AuditableLongIdTable
import dev.thebjoredcraft.railway.database.table.column.offsetDateTime
import dev.thebjoredcraft.railway.database.table.column.stringUuid

object GeckoPunishmentsTable : AuditableLongIdTable("gecko_punishments") {
    val playerUuid = stringUuid("player_uuid")
    val expiresAt = offsetDateTime("expires_at").nullable()
    val unpunished = bool("unpunished").default(false)
    val punisherUuid = stringUuid("punisher_uuid").nullable()
    val reason = varchar("reason", 255)
}
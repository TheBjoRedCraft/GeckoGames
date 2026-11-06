package dev.thebjoredcraft.gecko.server.database.table

import dev.thebjoredcraft.railway.database.table.auditable.AuditableLongIdTable
import dev.thebjoredcraft.railway.database.table.column.stringUuid

object GeckoGameStatsTable : AuditableLongIdTable("gecko_game_stats") {
    val playerUuid = stringUuid("player_uuid")
    val gameId = ulong("game_id")

    val seeker = bool("seeker")
    val finalSeeker = bool("final_seeker")
    val win = bool("win")
    val kills = integer("kills")
    val foundAfter = long("found_after").nullable()
}
package dev.thebjoredcraft.gecko.server.gecko.stats

import java.util.*

data class GeckoGameStats(
    val playerUuid: UUID,
    val gameId: Long,
    val seeker: Boolean,
    val finalSeeker: Boolean,
    val win: Boolean,
    val kills: Int,
    val foundAfter: Long?
)

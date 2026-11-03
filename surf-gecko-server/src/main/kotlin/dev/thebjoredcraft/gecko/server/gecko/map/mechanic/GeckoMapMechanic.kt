package dev.thebjoredcraft.gecko.server.gecko.map.mechanic

import dev.thebjoredcraft.gecko.server.event.MinestomListener
import dev.thebjoredcraft.gecko.server.gecko.GeckoGame
import dev.thebjoredcraft.gecko.server.gecko.GeckoGameManager
import dev.thebjoredcraft.gecko.server.gecko.map.GeckoMap
import net.minestom.server.entity.Player

interface GeckoMapMechanic {
    val listeners: List<MinestomListener>

    suspend fun start() = Unit
    suspend fun stop() = Unit


    fun hasThisMechanic(map: GeckoMap) = map.mechanics.any { it::class == this::class }
    fun hasThisMechanic(game: GeckoGame) =
        game.settings.map.mechanics.any { it::class == this::class }

    fun hasThisMechanic(player: Player) =
        GeckoGameManager.findGame(player.uuid)?.settings?.map?.mechanics?.any { it::class == this::class }
            ?: false
}
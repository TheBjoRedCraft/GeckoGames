package dev.thebjoredcraft.gecko.server.gecko.lobby.leaderbord

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.thebjoredcraft.gecko.server.database.repository.GeckoPlayerNameRepository
import dev.thebjoredcraft.gecko.server.event.EventHandler
import dev.thebjoredcraft.gecko.server.event.MinestomListener
import kotlinx.coroutines.launch
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

object LeaderboardListener : MinestomListener {
    @EventHandler
    fun onSpawn(event: PlayerSpawnEvent) {
        val player = event.player

        minestomAsyncScope.launch {
            if (event.isFirstSpawn) {
                GeckoPlayerNameRepository.saveName(player.uuid, player.username)
            }

            LeaderboardManager.show(player)
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerDisconnectEvent) {
        LeaderboardManager.invalidate(event.player.uuid)
    }
}

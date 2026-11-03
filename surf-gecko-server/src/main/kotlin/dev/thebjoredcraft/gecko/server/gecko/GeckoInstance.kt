package dev.thebjoredcraft.gecko.server.gecko

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.thebjoredcraft.gecko.server.antiesp.PlayerCulling
import dev.thebjoredcraft.gecko.server.antimap.AntiMap
import dev.thebjoredcraft.gecko.server.database.GeckoDatabaseManager
import dev.thebjoredcraft.gecko.server.event.MinestomListenerRegistry
import dev.thebjoredcraft.gecko.server.event.register
import dev.thebjoredcraft.gecko.server.gecko.command.geckoCommand
import dev.thebjoredcraft.gecko.server.gecko.command.joinMeCommand
import dev.thebjoredcraft.gecko.server.gecko.command.lobbyCommand
import dev.thebjoredcraft.gecko.server.gecko.command.skipCommand
import dev.thebjoredcraft.gecko.server.gecko.display.tablist.GeckoGameTablistManager
import dev.thebjoredcraft.gecko.server.gecko.display.tablist.GeckoTablistRenderer
import dev.thebjoredcraft.gecko.server.gecko.lobby.GeckoLobby
import dev.thebjoredcraft.gecko.server.gecko.lobby.elytra.ElytraBoostListener
import dev.thebjoredcraft.gecko.server.gecko.lobby.leaderbord.LeaderboardManager
import dev.thebjoredcraft.gecko.server.gecko.lobby.mascot.DuckMascotListener
import dev.thebjoredcraft.gecko.server.gecko.lobby.mascot.DuckMascotManager
import dev.thebjoredcraft.gecko.server.gecko.lobby.npc.LobbyNpcManager
import dev.thebjoredcraft.gecko.server.gecko.lobby.riseup.RiseUpListener
import dev.thebjoredcraft.gecko.server.gecko.map.GeckoMapManager
import dev.thebjoredcraft.gecko.server.gecko.punishment.GeckoPunishmentService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val geckoLogger: Logger = LoggerFactory.getLogger("GeckoGames")

object GeckoInstance {
    suspend fun enable() {
        geckoLogger.info("Enabling GeckoInstance...")
        GeckoDatabaseManager.create()
        MinestomListenerRegistry.create()
        GeckoLobby.createLobby()
        GeckoGameManager.init()
        GeckoMapManager.registerMechanics()
        GeckoGameTablistManager.init()
        GeckoTablistRenderer.init()
        GeckoPunishmentService.init()
        PlayerCulling.init()
        AntiMap.init()
        LobbyNpcManager.create()
        DuckMascotManager.create()
        LeaderboardManager.init()

        RiseUpListener.register()
        ElytraBoostListener.register()
        DuckMascotListener.register()

        geckoCommand()
        skipCommand()

        CommandAPI.unregister("lobby")
        lobbyCommand()
        joinMeCommand()

        geckoLogger.info("Enabled GeckoInstance.")
    }

    suspend fun shutdown() {
        geckoLogger.info("Stopping GeckoInstance...")
        GeckoGameManager.shutdown()
        GeckoMapManager.unregisterMechanics()
        GeckoGameTablistManager.shutdown()
        GeckoTablistRenderer.shutdown()
        GeckoPunishmentService.shutdown()
        LeaderboardManager.shutdown()
        PlayerCulling.shutdown()
        GeckoDatabaseManager.shutdown()

        geckoLogger.info("Stopped GeckoInstance.")
    }
}
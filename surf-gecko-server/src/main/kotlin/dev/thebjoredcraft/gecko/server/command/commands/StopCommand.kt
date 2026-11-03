package dev.thebjoredcraft.gecko.server.command.commands

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.thebjoredcraft.gecko.server.GeckoServer
import dev.thebjoredcraft.gecko.server.i18n.sendTranslated
import dev.thebjoredcraft.gecko.server.permission.PermissionList

fun stopCommand() = commandTree("stop") {
    withAliases("exit", "shutdown")
    withPermission(PermissionList.COMMAND_STOP)

    anyExecutor { sender, _ ->
        sender.sendTranslated("command.stop.stopping")

        GeckoServer.beginShutdown()
    }
}
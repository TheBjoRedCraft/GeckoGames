package dev.thebjoredcraft.gecko.server.command

import dev.thebjoredcraft.gecko.server.command.commands.*
import dev.thebjoredcraft.gecko.server.i18n.command.i18nCommand
import dev.thebjoredcraft.gecko.server.i18n.command.languageCommand

object ServerGeckoCommandRegistrar {
    fun registerAll() {
        stopCommand()
        gameModeCommand()
        miniMessageCommand()
        teleportCommand()
        statusBarCommand()
        i18nCommand()
        languageCommand()
    }
}

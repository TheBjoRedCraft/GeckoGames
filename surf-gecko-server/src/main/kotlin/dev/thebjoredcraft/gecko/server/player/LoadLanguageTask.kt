package dev.thebjoredcraft.gecko.server.player

import dev.slne.surf.api.minestom.server.configuration.ConfigurationContext
import dev.slne.surf.api.minestom.server.configuration.ConfigurationTask
import dev.slne.surf.api.minestom.server.configuration.ConfigurationTaskId
import dev.thebjoredcraft.gecko.server.i18n.PlayerLanguages
import kotlinx.coroutines.runBlocking

object LoadLanguageTask : ConfigurationTask {
    val ID = ConfigurationTaskId("gecko_load_language")

    override fun run(context: ConfigurationContext) {
        if (!context.isFirstConfig) return

        runBlocking { PlayerLanguages.load(context.player) }
    }
}

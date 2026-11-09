package dev.thebjoredcraft.gecko.server.resourcepack

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.server.configuration.ConfigurationContext
import dev.slne.surf.api.minestom.server.configuration.ConfigurationTask
import dev.slne.surf.api.minestom.server.configuration.ConfigurationTaskId
import dev.thebjoredcraft.gecko.server.GeckoBootstrap
import kotlinx.coroutines.launch
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import java.net.URI
import java.util.*

object ResourcePackConfigurationTask : ConfigurationTask {
    val ID = ConfigurationTaskId("gecko_send_resourcepack")

    override fun run(context: ConfigurationContext) {
        if (!context.isFirstConfig) {
            return
        }

        if (!GeckoBootstrap.geckoServerConfig.packServer.enabled) {
            return
        }

        val url = GeckoBootstrap.geckoServerConfig.packServer.resourcePackUrl()

        minestomAsyncScope.launch {
            val sha1 = ResourcePackServer.sha1()

            val pack = ResourcePackInfo.resourcePackInfo()
                .id(UUID.nameUUIDFromBytes(url.toByteArray()))
                .uri(URI.create(url))
                .hash(sha1)
                .build()

            context.player.sendResourcePacks(
                ResourcePackRequest.resourcePackRequest()
                    .required(true)
                    .packs(pack)
                    .build()
            )
        }
    }
}
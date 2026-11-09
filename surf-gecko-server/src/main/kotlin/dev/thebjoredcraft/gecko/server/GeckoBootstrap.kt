package dev.thebjoredcraft.gecko.server

import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.api.minestom.server.chat.withSignedChat
import dev.slne.surf.api.minestom.server.configuration.ConfigurationTasks
import dev.slne.surf.api.minestom.server.console.withConsole
import dev.slne.surf.api.minestom.server.luckperms.withLuckPerms
import dev.slne.surf.api.minestom.server.npc.withNpcLib
import dev.slne.surf.api.minestom.server.plugins.withPlugins
import dev.slne.surf.api.minestom.server.spark.withSpark
import dev.slne.surf.api.minestom.server.surfMinestomServer
import dev.thebjoredcraft.gecko.server.chat.GeckoChatListener
import dev.thebjoredcraft.gecko.server.combat.BowCombatListener
import dev.thebjoredcraft.gecko.server.combat.MeleeCombatListener
import dev.thebjoredcraft.gecko.server.command.ServerGeckoCommandRegistrar
import dev.thebjoredcraft.gecko.server.config.GeckoServerConfig
import dev.thebjoredcraft.gecko.server.gecko.GeckoGameJoinService
import dev.thebjoredcraft.gecko.server.gecko.death.GeckoDamageListener
import dev.thebjoredcraft.gecko.server.gecko.display.GeckoDisplayListener
import dev.thebjoredcraft.gecko.server.gecko.hotbar.GeckoHotbarListener
import dev.thebjoredcraft.gecko.server.gecko.lobby.listener.GeckoLobbyListener
import dev.thebjoredcraft.gecko.server.gecko.lobby.view.geckoGamesView
import dev.thebjoredcraft.gecko.server.gecko.orbs.GeckoOrbListener
import dev.thebjoredcraft.gecko.server.gecko.player.listener.GeckoPlayerListener
import dev.thebjoredcraft.gecko.server.gecko.shop.ShopItemListener
import dev.thebjoredcraft.gecko.server.gecko.shop.type.shops.hiderShopView
import dev.thebjoredcraft.gecko.server.gecko.shop.type.shops.seekerShopView
import dev.thebjoredcraft.gecko.server.i18n.GeckoTranslations
import dev.thebjoredcraft.gecko.server.i18n.view.languageView
import dev.thebjoredcraft.gecko.server.performance.EntityTickFilter
import dev.thebjoredcraft.gecko.server.performance.monitor.StatusBarService
import dev.thebjoredcraft.gecko.server.player.LoadLanguageTask
import dev.thebjoredcraft.gecko.server.player.PlayerConnectionService
import dev.thebjoredcraft.gecko.server.resourcepack.ResourcePackConfigurationTask
import dev.thebjoredcraft.railway.config.RailwayConfigFormat
import dev.thebjoredcraft.railway.config.manager.ConfigManager
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EntityTypeKeys
import kotlin.io.path.Path

val bootstrapLogger: ComponentLogger = ComponentLogger.logger("GeckoBootstrap")

object GeckoBootstrap {
    val geckoServerConfig by lazy {
        ConfigManager.create(Path("config.yml"), RailwayConfigFormat.YAML, GeckoServerConfig())
            .load()
    }

    fun boot() {
        val startupStartedAt = System.nanoTime()

        bootstrapLogger.info("Booting server...")

        GremlinRuntimeDownloader.resolve()

        applyTickDispatcherThreads()
        applyChunkViewDistance()
        applyKeepAliveDelay()

        val minecraftServer = initMinecraftServer()

        EntityTickFilter.configure(EntityTypeKeys.ARMOR_STAND.key())

        bootSurfApiMinestom()

        runBlocking {
            GeckoTranslations.configure(geckoServerConfig.translations)
            GeckoTranslations.reload()
        }

        registerViews()

        ServerGeckoCommandRegistrar.registerAll()
        GeckoChatListener.register()
        StatusBarService.start()

        runBlocking {
            GeckoServer.start(minecraftServer, geckoServerConfig, startupStartedAt)
        }
    }

    private fun initMinecraftServer(): MinecraftServer {
        bootstrapLogger.info(
            "Initializing gecko server for {}:{}.",
            geckoServerConfig.address.host,
            geckoServerConfig.address.port,
        )

        val minecraftServer = MinecraftServer.init(geckoServerConfig.velocity.createAuth())

        MinecraftServer.setCompressionThreshold(0)
        return minecraftServer
    }

    private fun bootSurfApiMinestom() {
        surfMinestomServer {
            maxPlayers = geckoServerConfig.maxPlayers

            withConfigurationPhase {
                after(ConfigurationTasks.AWAIT_SETTINGS, LoadLanguageTask.ID, LoadLanguageTask)
                before(
                    ConfigurationTasks.RESOURCE_PACK, ResourcePackConfigurationTask.ID,
                    ResourcePackConfigurationTask
                )
            }

            withLuckPerms()
            withSignedChat {
                enforceSecureProfile = geckoServerConfig.chat.enforceSecureProfile
                chatSpamThresholdSeconds = geckoServerConfig.chat.chatSpamThresholdSeconds
                commandSpamThresholdSeconds = geckoServerConfig.chat.commandSpamThresholdSeconds
            }
            withSpark {
                profileOnStartup = geckoServerConfig.performance.spark.profileOnStartup
            }
            withNpcLib()
            withConsole {
                threadName = "surf-gecko-console"
                onShutdown = { GeckoServer.shutdownAndExit("console") }
            }
            withPlugins()

            listeners(
                PlayerConnectionService,
                StatusBarService,
                GeckoGameJoinService(),
                GeckoPlayerListener(),
                MeleeCombatListener(),
                BowCombatListener(),
                GeckoDamageListener(),
                GeckoLobbyListener(),
                ShopItemListener(),
                GeckoHotbarListener(),
                GeckoOrbListener(),
                GeckoDisplayListener(),
            )
        }
    }

    private fun registerViews() {
        seekerShopView.register()
        hiderShopView.register()
        geckoGamesView.register()
        languageView.register()
    }

    private const val DISPATCHER_THREADS_PROPERTY = "minestom.dispatcher-threads"
    private const val KEEP_ALIVE_DELAY_PROPERTY = "minestom.keep-alive-delay"
    private const val KEEP_ALIVE_DELAY_MILLIS = 2_000L

    private fun applyKeepAliveDelay() {
        val existing = System.getProperty(KEEP_ALIVE_DELAY_PROPERTY)
        if (existing != null) {
            bootstrapLogger.info(
                "Keep alive delay pinned via -D{}={}; keeping it.",
                KEEP_ALIVE_DELAY_PROPERTY,
                existing
            )
            return
        }

        System.setProperty(KEEP_ALIVE_DELAY_PROPERTY, KEEP_ALIVE_DELAY_MILLIS.toString())
        bootstrapLogger.info("Sending keep alives every {}ms.", KEEP_ALIVE_DELAY_MILLIS)
    }

    private fun applyTickDispatcherThreads() {
        val existing = System.getProperty(DISPATCHER_THREADS_PROPERTY)
        if (existing != null) {
            bootstrapLogger.info(
                "Tick dispatcher threads pinned via -D{}={}; keeping it.",
                DISPATCHER_THREADS_PROPERTY,
                existing
            )
            return
        }

        val threads =
            if (geckoServerConfig.performance.tickThreads <= 0) Runtime.getRuntime()
                .availableProcessors() else geckoServerConfig.performance.tickThreads

        System.setProperty(DISPATCHER_THREADS_PROPERTY, threads.toString())
        bootstrapLogger.info("Using {} tick dispatcher thread(s).", threads)
    }

    private fun applyChunkViewDistance() {
        val existing = System.getProperty("minestom.chunk-view-distance")
        if (existing != null) {
            bootstrapLogger.info(
                "Chunk view distance pinned via -Dminestom.chunk-view-distance={}; keeping it.",
                existing
            )
            return
        }

        System.setProperty(
            "minestom.chunk-view-distance",
            geckoServerConfig.performance.viewDistance.toString()
        )
        bootstrapLogger.info(
            "Using a chunk view distance of {} chunks.",
            geckoServerConfig.performance.viewDistance
        )
    }
}

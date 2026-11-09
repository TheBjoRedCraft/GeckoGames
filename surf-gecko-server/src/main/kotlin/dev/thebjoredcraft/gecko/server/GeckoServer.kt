package dev.thebjoredcraft.gecko.server

import dev.thebjoredcraft.gecko.server.config.GeckoServerConfig
import dev.thebjoredcraft.gecko.server.gecko.GeckoInstance
import dev.thebjoredcraft.gecko.server.performance.monitor.StatusBarService
import dev.thebjoredcraft.gecko.server.resourcepack.ResourcePackServer
import kotlinx.coroutines.runBlocking
import net.minestom.server.MinecraftServer
import net.minestom.server.MinecraftServer.LOGGER
import org.jetbrains.annotations.Blocking
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

object GeckoServer {
    private val started = AtomicBoolean()
    private val stopped = AtomicBoolean()

    suspend fun start(
        minecraftServer: MinecraftServer,
        geckoServerConfig: GeckoServerConfig,
        startupStartedAt: Long
    ) {
        check(started.compareAndSet(false, true)) {
            "Gecko server has already been started"
        }

        try {
            installShutdownHook()

            LOGGER.info(
                "Binding server to {}:{}.",
                geckoServerConfig.address.host,
                geckoServerConfig.address.port,
            )
            minecraftServer.start(geckoServerConfig.address.host, geckoServerConfig.address.port)

            GeckoInstance.enable()
            ResourcePackServer.start()

            val startupDuration =
                (System.nanoTime() - startupStartedAt).nanoseconds.inWholeMilliseconds.milliseconds

            LOGGER.info(
                "gecko server is ready in {}.",
                startupDuration,
            )
        } catch (startupFailure: Throwable) {
            LOGGER.error(
                "Failed to start gecko server.",
                startupFailure,
            )

            runCatching { stop() }.onFailure { failure ->
                startupFailure.addSuppressed(failure)
                LOGGER.error("Failed to stop gecko server after startup failure.", failure)
            }

            throw startupFailure
        }
    }

    fun beginShutdown() {
        if (stopped.get()) {
            return
        }

        thread(isDaemon = false, name = "shutdown-thread") {
            shutdownAndExit("command")
        }
    }

    @Blocking
    fun shutdownAndExit(source: String) {
        runBlocking {
            runCatching {
                stop()
            }.onFailure {
                LOGGER.error(
                    "Failed to stop gecko server from {}.",
                    source,
                    it,
                )
            }
        }

        exitProcess(0)
    }

    suspend fun stop() {
        if (!stopped.compareAndSet(false, true)) {
            return
        }

        LOGGER.info("Stopping gecko server.")

        var failure: Throwable? = null

        try {
            GeckoInstance.shutdown()
            ResourcePackServer.stop()
        } catch (currentFailure: Throwable) {
            LOGGER.error("Failed to stop the gecko games.", currentFailure)
            failure = currentFailure
        }

        try {
            //   RedisService.disconnect()
        } catch (currentFailure: Throwable) {
            LOGGER.error("Failed to disconnect from redis.", currentFailure)
            failure = failure.alsoSuppress(currentFailure)
        }

        try {
            StatusBarService.stop()
        } catch (currentFailure: Throwable) {
            LOGGER.error("Failed to stop the status bar.", currentFailure)
            failure = failure.alsoSuppress(currentFailure)
        }

        if (MinecraftServer.isStarted() && !MinecraftServer.isStopping()) {
            try {
                MinecraftServer.stopCleanly()
            } catch (currentFailure: Throwable) {
                LOGGER.error("Failed to stop the gecko server cleanly.", currentFailure)
                failure = failure.alsoSuppress(currentFailure)
            }
        }

        if (failure == null) {
            LOGGER.info("Gecko server stopped successfully.")
        } else {
            throw failure
        }
    }

    private fun Throwable?.alsoSuppress(next: Throwable): Throwable {
        if (this == null) return next
        addSuppressed(next)
        return this
    }

    private fun installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            Thread(
                {
                    runBlocking {
                        runCatching {
                            stop()
                        }.onFailure(Throwable::printStackTrace)
                    }
                },
                "gecko-server-shutdown",
            )
        )
    }
}

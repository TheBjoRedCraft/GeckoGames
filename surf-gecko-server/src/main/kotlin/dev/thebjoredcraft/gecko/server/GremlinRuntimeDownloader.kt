package dev.thebjoredcraft.gecko.server

import dev.thebjoredcraft.gecko.server.instrumentation.GeckoInstrumentation
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import xyz.jpenilla.gremlin.runtime.DependencyCache
import xyz.jpenilla.gremlin.runtime.DependencyResolver
import xyz.jpenilla.gremlin.runtime.DependencySet
import xyz.jpenilla.gremlin.runtime.logging.Slf4jGremlinLogger
import java.nio.file.Path
import java.util.jar.JarFile

private val downloaderLogger: ComponentLogger = ComponentLogger.logger("GremlinRuntimeDownloader")

object GremlinRuntimeDownloader {

    fun resolve() {
        downloaderLogger.info("Resolving runtime dependencies...")

        val deps = DependencySet.readDefault(GremlinRuntimeDownloader::class.java.classLoader)
        val cache = DependencyCache(Path.of("runtime-deps"))

        try {
            DependencyResolver(Slf4jGremlinLogger(downloaderLogger)).use { resolver ->
                val jars = resolver.resolve(deps, cache).jarFiles()
                val instrumentation = GeckoInstrumentation.get()

                for (jar in jars) {
                    instrumentation.appendToSystemClassLoaderSearch(JarFile(jar.toFile()))
                }

                downloaderLogger.info("Resolved {} runtime dependency jar(s).", jars.size)
            }
        } finally {
            cache.cleanup()
        }
    }

}

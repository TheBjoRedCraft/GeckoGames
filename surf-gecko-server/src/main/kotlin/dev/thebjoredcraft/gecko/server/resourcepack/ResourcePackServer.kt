package dev.thebjoredcraft.gecko.server.resourcepack

import dev.thebjoredcraft.gecko.server.GeckoBootstrap
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.io.path.Path

object ResourcePackServer {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    private val hashLock = Mutex()
    private var cachedModified = -1L
    private var cachedSha1 = ""

    @Synchronized
    fun start() {
        if (server != null) {
            return
        }

        val packFile = GeckoBootstrap.config.packServer.packLocation.let { path ->
            Path(".").resolve(path)
        }.toFile()

        server = embeddedServer(
            Netty,
            GeckoBootstrap.config.packServer.port,
            GeckoBootstrap.config.packServer.bindAddress
        ) {
            install(AutoHeadResponse)

            routing {
                get("/resourcepack.zip") {
                    if (!packFile.exists() || !packFile.isFile) {
                        call.respondText(
                            "ResourcePack not found",
                            status = HttpStatusCode.NotFound
                        )
                        return@get
                    }

                    call.respond(LocalFileContent(packFile, ContentType.Application.Zip))
                }
                
                get("/sha1") {
                    if (!packFile.exists() || !packFile.isFile) {
                        call.respondText(
                            "ResourcePack not found",
                            status = HttpStatusCode.NotFound
                        )
                        return@get
                    }

                    call.respondText(sha1(packFile))
                }
            }
        }

        server?.start(wait = false)
    }

    @Synchronized
    fun stop() {
        server?.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
        server = null
    }

    private suspend fun sha1(packFile: File): String = hashLock.withLock {
        val modified = packFile.lastModified()
        if (modified != cachedModified) {
            cachedSha1 = withContext(Dispatchers.IO) {
                val digest = MessageDigest.getInstance("SHA-1")

                packFile.inputStream().use { input ->
                    val buffer = ByteArray(65536)

                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                    }
                }

                digest.digest().joinToString("") { "%02x".format(it) }
            }

            cachedModified = modified
        }

        cachedSha1
    }
}
package dev.thebjoredcraft.gecko.server.resourcepack

import dev.thebjoredcraft.gecko.server.GeckoBootstrap
import dev.thebjoredcraft.gecko.server.bootstrapLogger
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
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.walk

object ResourcePackServer {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    private val hashLock = Mutex()
    private var cachedModified = -1L
    private var cachedSha1 = ""

    fun mergeDirectory(dir: Path) {
        val packPath = Path(".").resolve(GeckoBootstrap.config.packServer.packLocation)
        val packFile = packPath.toFile()
        if (!packFile.exists() || !packFile.isFile) return

        val tempFile = File(packFile.parentFile, packFile.name + ".tmp")

        ZipFile(packFile).use { existingZip ->
            ZipOutputStream(tempFile.outputStream().buffered()).use { zos ->
                val existingEntries = mutableSetOf<String>()

                for (entry in existingZip.entries()) {
                    existingEntries.add(entry.name)
                    zos.putNextEntry(ZipEntry(entry.name))
                    existingZip.getInputStream(entry).use { it.copyTo(zos) }
                    zos.closeEntry()
                }

                dir.walk().forEach { file ->
                    if (file.toFile().isFile) {
                        val entryName = dir.parent.relativize(file).toString().replace('\\', '/')
                        if (entryName !in existingEntries) {
                            zos.putNextEntry(ZipEntry(entryName))
                            file.toFile().inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            }
        }

        tempFile.renameTo(packFile)
        cachedModified = -1L
    }

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

        bootstrapLogger.info("ResourcePackServer started on ${GeckoBootstrap.config.packServer.bindAddress}:${GeckoBootstrap.config.packServer.port}")
    }

    @Synchronized
    fun stop() {
        server?.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
        server = null
    }

    suspend fun sha1(): String {
        val packFile = GeckoBootstrap.config.packServer.packLocation.let { path ->
            Path(".").resolve(path)
        }.toFile()
        return sha1(packFile)
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
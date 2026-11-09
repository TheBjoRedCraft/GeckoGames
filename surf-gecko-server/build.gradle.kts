plugins {
    id("dev.slne.surf.api.gradle.minestom-server")
    id("xyz.jpenilla.gremlin-gradle")
    id("io.ktor.plugin") version "3.0.1"
}

repositories {
    maven("https://reposilite.slne.dev/releases") { name = "slne-repository-releases" }
    maven("https://reposilite.atlasengine.ca/public") { name = "atlasengine-repository-public" }
    maven("https://jitpack.io")
}

surfMinestomServerApi {
    mainClass("dev.thebjoredcraft.gecko.server.MainKt")

    withSignedChat()
    withLuckPerms()
    withSpark()
    withNpcLib()
    withConsole()
    withPlugins()
}

dependencies {
    implementation(projects.surfGeckoCommon)

    implementation("xyz.jpenilla:gremlin-runtime:0.0.9")
    runtimeDownload("com.github.TheBjoRedCraft:railway:1.0.7")

    runtimeDownload(libs.adventure.text.minimessage)
    runtimeDownload(libs.wsee)
    compileOnly(libs.flare.fastutil)

    annotationProcessor(libs.fabric.mixin)
    annotationProcessor(libs.mixin.extra)

    runtimeDownload("io.ktor:ktor-server-core")
    runtimeDownload("io.ktor:ktor-server-netty")
    runtimeDownload("io.ktor:ktor-server-auto-head-response")
}

configurations {
    runtimeDownload {
        extendsFrom(configurations.annotationProcessor.get())
    }

    compileOnly {
        extendsFrom(configurations.runtimeDownload.get())
    }

}

val bundleTranslations by tasks.registering {
    val source = rootProject.layout.projectDirectory.dir("surf-gecko-translations/lang").asFile
    val output = layout.buildDirectory.dir("generated/translations").get().asFile
    inputs.dir(source)
    outputs.dir(output)

    doLast {
        val target = output.resolve("lang")
        target.deleteRecursively()
        source.copyRecursively(target)

        val index = target.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .map { it.relativeTo(target).invariantSeparatorsPath }
            .sorted()
            .joinToString("\n")
        target.resolve("index.txt").writeText(index)
    }
}

sourceSets.main {
    resources.srcDir(bundleTranslations)
}

tasks.jar {
    archiveClassifier = "thin"
}

afterEvaluate {
    tasks.shadowJar {
        dependsOn(zipResourcepack)
        archiveFileName = "gecko-server.jar"
        isZip64 = true

        manifest {
            attributes["Surf-Delegate-Agent-Classes"] =
                "dev.thebjoredcraft.gecko.server.instrumentation.GeckoRunner"
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"

            attributes(
                mapOf("Implementation-Version" to libs.versions.asm.get()),
                "org/objectweb/asm/"
            )
        }
    }
}

val zipResourcepack by tasks.registering(Zip::class) {
    from(rootProject.layout.projectDirectory.dir("surf-gecko-resourcepack"))
    archiveFileName = "resourcepack.zip"
    destinationDirectory = layout.buildDirectory.dir("libs")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

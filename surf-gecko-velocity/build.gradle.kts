plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withCoreCommon()
    withSurfRedis()
}

group = "dev.thebjoredcraft.gecko.velocity"

velocityPluginFile {
    main = "dev.thebjoredcraft.gecko.velocity.VelocityMain"
    authors = listOf("red")

    pluginDependencies {
        register("surf-rabbitmq-velocity")
    }
}

dependencies {
    implementation(projects.surfGeckoCommon)
}

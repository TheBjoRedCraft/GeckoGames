plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

group = "dev.thebjoredcraft.gecko.map.creator"

surfPaperPluginApi {
    mainClass("dev.thebjoredcraft.gecko.map.creator.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")
}

dependencies {
    implementation(projects.surfGeckoCommon)
}

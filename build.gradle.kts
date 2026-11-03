buildscript {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://reposilite.slne.dev/releases")
    }
    dependencies {
        classpath("dev.slne.surf.api:surf-api-gradle-plugin:+")
    }
}

allprojects {
    group = "dev.thebjoredcraft.gecko"
    version = findProperty("version") as String

    repositories {
        mavenLocal()
    }
}

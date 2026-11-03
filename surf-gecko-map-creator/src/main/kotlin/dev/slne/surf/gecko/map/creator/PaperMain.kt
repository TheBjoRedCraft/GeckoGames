package dev.thebjoredcraft.gecko.map.creator

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.thebjoredcraft.gecko.map.creator.command.geckoMapCommand
import dev.thebjoredcraft.gecko.map.creator.listener.MarkerToolListener
import dev.thebjoredcraft.gecko.map.creator.render.MarkerRenderer
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        geckoMapCommand()

        MarkerToolListener().register(this)
        MarkerRenderer.start()
    }

    override suspend fun onDisableAsync() {
        MarkerRenderer.stop()
    }
}

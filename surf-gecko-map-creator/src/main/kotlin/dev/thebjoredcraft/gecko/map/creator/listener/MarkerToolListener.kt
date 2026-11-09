package dev.thebjoredcraft.gecko.map.creator.listener

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.thebjoredcraft.gecko.map.creator.PaperGeckoMapManager
import dev.thebjoredcraft.gecko.map.creator.command.sendDraftOverview
import dev.thebjoredcraft.gecko.map.creator.draft.DraftPos
import dev.thebjoredcraft.gecko.map.creator.draft.GeckoMapDraft
import dev.thebjoredcraft.gecko.map.creator.draft.GeckoPoiType
import dev.thebjoredcraft.gecko.map.creator.item.MarkerItems
import dev.thebjoredcraft.gecko.map.creator.render.MarkerRenderer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class MarkerToolListener : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = event.item ?: return

        if (MarkerItems.isToolkit(item)) {
            event.isCancelled = true
            if (event.action.isRightClick) handleToolkit(player)
            return
        }

        val type = MarkerItems.markerType(item) ?: return
        event.isCancelled = true

        val draft = PaperGeckoMapManager.draft(player.uniqueId)
        if (draft == null) {
            player.sendText {
                appendInfoPrefix()
                info("Du hast keine aktive Map. Starte mit ")
                white("/geckomap create <name>")
                info(".")
            }
            return
        }

        if (player.world.name != draft.worldName) {
            player.sendText {
                appendInfoPrefix()
                info("Deine Map gehört zur Welt ")
                white(draft.worldName)
                info(".")
            }
            return
        }

        when {
            event.action.isRightClick -> place(player, draft, type, event)
            event.action.isLeftClick -> remove(player, draft, type, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (isTool(event.player)) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (isTool(event.player)) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onDropItem(event: PlayerDropItemEvent) {
        val item = event.itemDrop.itemStack
        if (MarkerItems.markerType(item) != null || MarkerItems.isToolkit(item)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (PaperGeckoMapManager.draft(player.uniqueId) != null) {
            MarkerRenderer.enable(player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        MarkerRenderer.forget(event.player)
    }

    private fun isTool(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        return MarkerItems.markerType(item) != null || MarkerItems.isToolkit(item)
    }

    private fun handleToolkit(player: Player) {
        if (player.isSneaking) {
            player.performCommand("geckomap export")
        } else {
            player.sendDraftOverview()
        }
    }

    private fun place(
        player: Player,
        draft: GeckoMapDraft,
        type: GeckoPoiType,
        event: PlayerInteractEvent,
    ) {
        val clickedBlock = event.clickedBlock
        val blockFace = event.blockFace

        val pos = if (player.isSneaking || clickedBlock == null) {
            DraftPos.of(player.location)
        } else {
            DraftPos.ofBlockCenter(
                clickedBlock.getRelative(blockFace).location,
                player.location.yaw,
                player.location.pitch,
            )
        }

        val count = draft.place(type, pos)
        MarkerRenderer.refresh(player)

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.6f)
        player.sendActionBar(buildText {
            primary(type.displayName)
            info(" gesetzt ")
            white(readable(pos))
            if (type.multiple) {
                info(" (")
                white(count)
                info(")")
            }
        })
    }

    private fun remove(
        player: Player,
        draft: GeckoMapDraft,
        type: GeckoPoiType,
        event: PlayerInteractEvent,
    ) {
        val reference = event.clickedBlock?.location?.add(0.5, 0.0, 0.5) ?: player.eyeLocation
        val removed = draft.remove(type, reference, 4.0)

        MarkerRenderer.refresh(player)

        if (removed == null) {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f)
            player.sendActionBar(buildText {
                info("Kein ")
                white(type.displayName)
                info(" in Reichweite")
            })
            return
        }

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 0.9f)
        player.sendActionBar(buildText {
            primary(type.displayName)
            info(" entfernt ")
            white(readable(removed))
        })
    }

    private fun readable(pos: DraftPos) =
        "%.1f / %.1f / %.1f".format(Locale.ROOT, pos.x, pos.y, pos.z)
}

package dev.thebjoredcraft.gecko.server.gecko.lobby.npc.internal

import codes.bed.minestom.npc.api.NpcInteraction
import dev.thebjoredcraft.gecko.server.gecko.lobby.npc.EquipableMannequinNpc
import dev.thebjoredcraft.gecko.server.i18n.LocalizedComponent
import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.network.player.ResolvableProfile

class EquipableMannequinNpcBuilder(private val name: String) {

    var displayName: LocalizedComponent = LocalizedComponent { Component.empty() }
    var profile: ResolvableProfile? = null

    var scale: Double = 1.0
    var displayNameOffset: Vec? = null
    var description: Component = Component.empty()

    private val equipment = mutableMapOf<EquipmentSlot, ItemStack>()
    private val interactHandlers = mutableListOf<(NpcInteraction) -> Unit>()

    fun onInteract(handler: (NpcInteraction) -> Unit) {
        interactHandlers += handler
    }

    fun withEquipment(slot: EquipmentSlot, item: ItemStack) {
        equipment[slot] = item
    }

    @PublishedApi
    internal fun spawn(instance: Instance, position: Pos): EquipableMannequinNpc {
        val npc = EquipableMannequinNpc(
            name = name,
            hologramText = displayName,
            profile = profile,
            scale = scale,
            hologramOffset = displayNameOffset ?: Vec(0.0, 2.1 * scale, 0.0),
            description = description,
        )

        npc.setEquipmentMap(equipment)

        interactHandlers.forEach { handler ->
            npc.onInteract { interaction -> handler(interaction) }
        }

        npc.spawn()
        npc.setInstance(instance, position)

        return npc
    }
}

inline fun equipableMannequinNpc(
    name: String,
    instance: Instance,
    position: Pos,
    block: EquipableMannequinNpcBuilder.() -> Unit,
): EquipableMannequinNpc = EquipableMannequinNpcBuilder(name).apply(block).spawn(instance, position)
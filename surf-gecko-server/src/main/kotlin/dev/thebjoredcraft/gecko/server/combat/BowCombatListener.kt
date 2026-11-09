package dev.thebjoredcraft.gecko.server.combat

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sound
import dev.slne.surf.api.minestom.event.EventRegistrar
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.event.item.PlayerCancelItemUseEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.enchant.Enchantment
import net.minestom.server.property.ServerProperties
import net.minestom.server.tag.Tag
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil

private val arrowDamageTag = Tag.Double("surf_gecko_arrow_damage").defaultValue(2.0)
private val arrowPunchTag = Tag.Integer("surf_gecko_arrow_punch").defaultValue(0)
private val arrowFixedDamageTag = Tag.Float("surf_gecko_arrow_fixed_damage")

val bowFixedDamageTag: Tag<Float> = Tag.Float("surf_gecko_bow_fixed_damage")
val bowConsumeOnShotTag: Tag<Boolean> = Tag.Boolean("surf_gecko_bow_consume_on_shot")

class BowCombatListener : EventRegistrar {
    private val maxPower = 1f
    private val spread = 0.0075

    override fun register(node: EventNode<Event>) {
        node.addListener(PlayerCancelItemUseEvent::class.java, ::handleBowRelease)
        node.addListener(ProjectileCollideWithEntityEvent::class.java, ::handleArrowHit)
        node.addListener(ProjectileCollideWithBlockEvent::class.java, ::handleArrowLanding)
    }

    private fun handleBowRelease(event: PlayerCancelItemUseEvent) {
        val bow = event.itemStack

        if (bow.material() != Material.BOW) {
            return
        }

        val player = event.player
        val instance = player.instance ?: return
        val power = bowPower(event.useDuration)

        if (power < 0.1f || !player.consumeArrow(bow)) {
            return
        }

        val powerLevel = bow.enchantmentLevel(Enchantment.POWER)
        val arrow = CombatArrow(player)

        (arrow.entityMeta as AbstractArrowMeta).isCritical = power >= maxPower
        arrow.setTag(
            arrowDamageTag,
            2.0 + if (powerLevel > 0) powerLevel * 0.5 + 0.5 else 0.0
        )
        arrow.setTag(arrowPunchTag, bow.enchantmentLevel(Enchantment.PUNCH))
        bow.getTag(bowFixedDamageTag)?.let { arrow.setTag(arrowFixedDamageTag, it) }

        val origin = player.position.add(0.0, player.eyeHeight - 0.1, 0.0)

        arrow.velocity = player.position.direction().withSpread()
            .mul((power * 3 * ServerProperties.SERVER_TICKS_PER_SECOND.get()).toDouble())
        arrow.setInstance(instance, origin)
        arrow.scheduleRemove(Duration.ofSeconds(60))

        player.playBowSound(power)

        if (bow.hasTag(bowConsumeOnShotTag)) {
            player.setItemInHand(event.hand, bow.consume(1))
        }
    }

    private fun handleArrowHit(event: ProjectileCollideWithEntityEvent) {
        val arrow = event.entity as? CombatArrow ?: return
        val target = event.target as? LivingEntity ?: return

        if (target === arrow.shooter && !arrow.hasLeftShooter) {
            event.isCancelled = true
            return
        }

        if (target.isDead || target.isInvulnerable) {
            event.isCancelled = true
            return
        }

        val shooter = arrow.shooter
        val amount = arrow.getTag(arrowFixedDamageTag) ?: run {
            val blocksPerTick =
                arrow.velocity.length() / ServerProperties.SERVER_TICKS_PER_SECOND.get()
            val base = ceil(blocksPerTick * arrow.getTag(arrowDamageTag)).toInt().coerceAtLeast(0)
            val critical = (arrow.entityMeta as AbstractArrowMeta).isCritical

            if (critical) {
                (base + ThreadLocalRandom.current().nextInt(base / 2 + 2)).toFloat()
            } else {
                base.toFloat()
            }
        }

        val applied = target.damageWithInvulnerability(
            Damage(DamageType.ARROW, arrow, shooter, arrow.position, amount)
        )

        if (applied) {
            target.takeKnockbackFrom(
                arrow.velocity.normalize(),
                0.4f + arrow.getTag(arrowPunchTag) * KNOCKBACK_PER_LEVEL
            )

            (shooter as? Player)?.playSound(true) {
                type(key("entity.arrow.hit_player"))
                source(Sound.Source.PLAYER)
            }
        }

        arrow.remove()
    }

    private fun handleArrowLanding(event: ProjectileCollideWithBlockEvent) {
        val arrow = event.entity as? CombatArrow ?: return

        arrow.remove()
    }

    private fun bowPower(useDurationTicks: Long): Float {
        val seconds = useDurationTicks.toFloat() / ServerProperties.SERVER_TICKS_PER_SECOND.get()

        return ((seconds * seconds + seconds * 2f) / 3f).coerceAtMost(maxPower)
    }

    private fun Vec.withSpread(): Vec {
        val random = ThreadLocalRandom.current()

        return Vec(
            x() + random.nextGaussian() * spread,
            y() + random.nextGaussian() * spread,
            z() + random.nextGaussian() * spread
        )
    }

    private fun Player.consumeArrow(bow: ItemStack): Boolean {
        if (gameMode == GameMode.CREATIVE || bow.enchantmentLevel(Enchantment.INFINITY) > 0) {
            return true
        }

        val slot = (0 until inventory.size)
            .firstOrNull { inventory.getItemStack(it).material() == Material.ARROW } ?: return false

        inventory.setItemStack(slot, inventory.getItemStack(slot).consume(1))

        return true
    }

    private fun Player.playBowSound(power: Float) {
        val instance = instance ?: return
        val random = ThreadLocalRandom.current()

        instance.playSound(
            sound {
                type(key("entity.arrow.shoot"))
                source(Sound.Source.PLAYER)
                pitch(1f / (random.nextFloat() * 0.4f + 1.2f) + power * 0.5f)
            },
            position.x(),
            position.y(),
            position.z()
        )
    }
}

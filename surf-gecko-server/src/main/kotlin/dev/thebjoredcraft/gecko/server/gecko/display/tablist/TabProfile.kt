package dev.thebjoredcraft.gecko.server.gecko.display.tablist

import dev.thebjoredcraft.gecko.server.gecko.social.SocialGroup
import dev.thebjoredcraft.gecko.server.i18n.GeckoLanguage
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

class TabProfile(
    val player: Player,
    val language: GeckoLanguage,
    val group: SocialGroup,
    val gameId: Long?,
    val rankedName: Component,
    val rankOrder: Int,
    val roleName: Map<GeckoLanguage, Component>?,
    val grayName: Component
)
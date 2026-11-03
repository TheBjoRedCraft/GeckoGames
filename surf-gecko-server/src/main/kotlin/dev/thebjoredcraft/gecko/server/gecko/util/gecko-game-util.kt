package dev.thebjoredcraft.gecko.server.gecko.util

import dev.thebjoredcraft.gecko.server.gecko.player.game.GeckoGamePlayer

val Iterable<GeckoGamePlayer>.asPlayers get() = mapNotNull { it.playerOrNull }
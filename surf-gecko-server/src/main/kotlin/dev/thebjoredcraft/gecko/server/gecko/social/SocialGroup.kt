package dev.thebjoredcraft.gecko.server.gecko.social

import dev.thebjoredcraft.gecko.server.i18n.translatable

enum class SocialGroup(
    val gameScoped: Boolean = false
) {
    LOBBY,
    GAME_ALL(gameScoped = true),
    GAME_HIDER(gameScoped = true),
    GAME_SEEKER(gameScoped = true),
    GAME_SPECTATOR(gameScoped = true);

    val displayText get() = translatable("social.group.${name.lowercase().replace('_', '-')}")
}
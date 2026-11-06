package dev.thebjoredcraft.gecko.server.gecko.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.bitmap.common.head.composeHead
import dev.slne.surf.bitmap.common.head.renderHead
import dev.thebjoredcraft.gecko.server.gecko.GeckoGameManager
import dev.thebjoredcraft.gecko.server.i18n.GeckoLanguage
import dev.thebjoredcraft.gecko.server.i18n.GeckoTranslations
import dev.thebjoredcraft.gecko.server.i18n.sendTranslated
import dev.thebjoredcraft.gecko.server.permission.PermissionList
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component

fun joinMeCommand() = commandTree("joinme") {
    withPermission(PermissionList.COMMAND_JOINME)

    playerExecutor { player, _ ->
        val game = GeckoGameManager.findGame(player.uuid)

        if (game == null) {
            player.sendTranslated("command.not-in-game")
            return@playerExecutor
        }

        minestomAsyncScope.launch {
            val playerName = player.displayName ?: Component.text(player.username)
            val texture = renderHead(player.skin?.textures ?: "", 1)

            val messages = GeckoLanguage.entries.associate { language ->
                val head = composeHead(
                    texture, listOf(
                        Component.empty(),
                        Component.empty(),
                        buildText {
                            appendSpace()
                            appendSpace()
                            append(
                                GeckoTranslations.render(
                                    language,
                                    "joinme.playing",
                                    "player" to playerName
                                )
                            )
                        },
                        buildText {
                            appendSpace()
                            appendSpace()
                            append(
                                GeckoTranslations.render(
                                    language,
                                    "joinme.game",
                                    "map" to game.settings.map.displayName
                                )
                            )
                        },
                        buildText {
                            appendSpace()
                            appendSpace()
                            white("👥 ")
                            spacer("${game.players.size}/${game.settings.maxPlayers}")
                            appendSpace()
                            white("📍")
                            appendSpace()
                            //     spacer(SurfServer.current().displayName)
                        },
                        buildText {
                            appendSpace()
                            appendSpace()
                            append(GeckoTranslations.render(language, "joinme.click"))
                        },
                        Component.empty(),
                        Component.empty(),
                    )
                )

                language.id to buildText {
                    appendNewline()
                    append(head)
                    appendNewline()
                }
            }

//            redisApi.publishEvent(
//                PublishJoinMeRedisEvent(
//                    SurfServer.current(),
//                    playerName,
//                    game.internalId,
//                    messages
//                )
//            )

            player.sendTranslated("joinme.sent")
        }
    }
}
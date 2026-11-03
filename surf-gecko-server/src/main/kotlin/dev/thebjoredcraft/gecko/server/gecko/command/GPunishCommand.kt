package dev.thebjoredcraft.gecko.server.gecko.command

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.thebjoredcraft.gecko.server.permission.PermissionList

fun gPunishCommand() = commandTree("gpunish") {
    withPermission(PermissionList.COMMAND_PUNISH)

    literalArgument("ban") {

    }

    literalArgument("unban") {

    }

    literalArgument("check") {

    }
}
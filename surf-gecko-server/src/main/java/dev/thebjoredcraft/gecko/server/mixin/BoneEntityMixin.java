package dev.thebjoredcraft.gecko.server.mixin;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.worldseed.multipart.model_bones.BoneEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes NPE in WSEE: {@code Entity.getMetadataPacket()} returns {@code null} in Minestom 26.x
 * when no metadata entries are present, but {@code BoneEntity.updateNewViewer} passes the result
 * directly to {@code Player.sendPacket}, which rejects null via the JCTools queue.
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(BoneEntity.class)
abstract class BoneEntityMixin {

    @Redirect(
        method = "updateNewViewer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minestom/server/entity/Player;sendPacket(Lnet/minestom/server/network/packet/server/SendablePacket;)V",
            remap = false
        ),
        remap = false,
        require = 2
    )
    private void surf$skipNullPacket(Player player, SendablePacket packet) {
        if (packet != null) {
            player.sendPacket(packet);
        }
    }
}

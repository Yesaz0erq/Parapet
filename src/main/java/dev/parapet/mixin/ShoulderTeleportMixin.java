package dev.parapet.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.parapet.entity.PetParrotEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
abstract class ShoulderTeleportMixin {
    @WrapMethod(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;")
    private ServerPlayer parapetCarryShoulder(TeleportTransition transition, Operation<ServerPlayer> original) {
        ServerPlayer owner = (ServerPlayer)(Object)this;
        if (transition.newLevel() == owner.level() || !(owner.getFirstPassenger() instanceof PetParrotEntity bird)
            || !bird.isOwnedBy(owner)) return original.call(transition);
        bird.stopRiding();
        ServerPlayer arrived = original.call(transition);
        if (arrived == null) {
            bird.startRiding(owner, true, true);
        } else if (bird.teleport(new TeleportTransition(arrived.level(), arrived.position(), Vec3.ZERO,
            arrived.getYRot(), 0, TeleportTransition.DO_NOTHING)) instanceof PetParrotEntity moved) {
            moved.startRiding(arrived, true, true);
        }
        return arrived;
    }
}

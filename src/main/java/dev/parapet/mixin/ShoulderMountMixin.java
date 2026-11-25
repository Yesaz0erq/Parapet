package dev.parapet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.parapet.entity.PetParrotEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class ShoulderMountMixin {
    @WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean parapetAllowShoulder(EntityType<?> type, Operation<Boolean> original,
                                         Entity vehicle, boolean force, boolean sendEvent) {
        return original.call(type) || force && vehicle instanceof Player player
            && (Object)this instanceof PetParrotEntity bird && bird.isOwnedBy(player) && bird.canPerchOnShoulder();
    }
}

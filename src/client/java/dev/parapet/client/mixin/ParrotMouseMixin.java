package dev.parapet.client.mixin;

import dev.parapet.entity.PetParrotEntity;
import dev.parapet.interaction.ParrotInteractionMenu.Inspect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.phys.EntityHitResult;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class ParrotMouseMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void parapetInspect(long window, MouseButtonInfo button, int action, CallbackInfo callback) {
        Minecraft mc = Minecraft.getInstance();
        if (button.button() == InputConstants.MOUSE_BUTTON_MIDDLE && action == InputConstants.PRESS
            && (window == 0 || window == mc.getWindow().handle()) && mc.gui.screen() == null && mc.mouseHandler.isMouseGrabbed() && mc.player != null
            && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof PetParrotEntity bird
            && bird.isOwnedBy(mc.player) && ClientPlayNetworking.canSend(Inspect.TYPE)) {
            ClientPlayNetworking.send(new Inspect(bird.getId()));
            callback.cancel();
        }
    }
}

package dev.parapet.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
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
        if ((window == 0 || window == mc.getWindow().handle()) &&
            dev.parapet.client.ParrotKeys.input(mc, InputConstants.Type.MOUSE.getOrCreate(button.button()), action)) callback.cancel();
    }
}

package dev.parapet.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class ParrotKeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void parapetInspect(long window, int action, KeyEvent key, CallbackInfo callback) {
        Minecraft mc = Minecraft.getInstance();
        if ((window == 0 || window == mc.getWindow().handle()) &&
            dev.parapet.client.ParrotKeys.input(mc, InputConstants.getKey(key), action)) callback.cancel();
    }
}

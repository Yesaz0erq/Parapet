package dev.parapet.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.parapet.Parapet;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.interaction.ParrotInteractionMenu.Inspect;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.phys.EntityHitResult;

public final class ParrotKeys {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Parapet.id("controls"));
    private static KeyMapping bind(String name, InputConstants.Type type, int code) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.parapet." + name, type, code, CATEGORY));
    }
    static final KeyMapping INTERACT = bind("interact", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE);
    static final KeyMapping MODIFIER = bind("modifier", InputConstants.Type.KEYBOARD, InputConstants.KEY_LSHIFT);
    public static void initialize() { Guide.values(); }

    enum Guide {
        INDEX(InputConstants.KEY_I),
        FILTER(InputConstants.KEY_F),
        NOTES(InputConstants.KEY_H),
        PREV(InputConstants.KEY_LEFT),
        NEXT(InputConstants.KEY_RIGHT),
        UP(InputConstants.KEY_UP),
        DOWN(InputConstants.KEY_DOWN),
        SELECT(InputConstants.KEY_RETURN),
        SKIP(InputConstants.KEY_SPACE),
        HOME(InputConstants.KEY_HOME),
        PGUP(InputConstants.KEY_PAGEUP),
        PGDOWN(InputConstants.KEY_PAGEDOWN),
        ENTER(InputConstants.KEY_NUMPADENTER);
        final int code;
        final KeyMapping binding;
        Guide(int code) {
            this.code = code;
            binding = bind(name().toLowerCase(java.util.Locale.ROOT), InputConstants.Type.KEYBOARD, code);
        }
    }

    private static boolean modifierHeld;
    public static boolean input(Minecraft mc, InputConstants.Key key, int action) {
        if (MODIFIER.matches(key)) modifierHeld = action != InputConstants.RELEASE;
        if (action != InputConstants.PRESS || !INTERACT.matches(key) || mc.gui.screen() != null
            || !mc.mouseHandler.isMouseGrabbed() || mc.player == null || !ClientPlayNetworking.canSend(Inspect.TYPE)) return false;
        if (modifierHeld && mc.player.hasPassenger(e -> e instanceof PetParrotEntity)) {
            ClientPlayNetworking.send(new Inspect(-1));
        } else if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof PetParrotEntity bird && bird.isOwnedBy(mc.player)) {
            ClientPlayNetworking.send(new Inspect(bird.getId()));
        } else return false;
        return true;
    }
}

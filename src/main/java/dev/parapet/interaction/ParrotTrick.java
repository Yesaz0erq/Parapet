package dev.parapet.interaction;

import dev.parapet.entity.PetParrotActivity;
import java.util.Locale;
import net.minecraft.network.chat.Component;

public enum ParrotTrick {
    GREET(0, PetParrotActivity.GREET),
    STROKE(0, PetParrotActivity.STROKE),
    TARGET(25, PetParrotActivity.TOUCH_TARGET),
    STRETCH(45, PetParrotActivity.STRETCH),
    HIGH_FIVE(65, PetParrotActivity.HIGH_FIVE),
    TURN(85, PetParrotActivity.TURN);

    private final String key = "gui.parapet.interaction." + name().toLowerCase(Locale.ROOT);
    public final int affinity;
    public final PetParrotActivity activity;

    ParrotTrick(int affinity, PetParrotActivity activity) {
        this.affinity = affinity;
        this.activity = activity;
    }

    public Component label() {
        return Component.translatable(key);
    }

    public Component description() {
        return Component.translatable(key + ".hint");
    }
}

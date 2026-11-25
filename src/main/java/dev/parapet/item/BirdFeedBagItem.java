package dev.parapet.item;

import com.mojang.serialization.Codec;
import dev.parapet.Parapet;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class BirdFeedBagItem extends Item {
    public static final int CAPACITY = 8;
    public static final DataComponentType<Integer> SERVINGS = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE, Parapet.id("feed_servings"),
        DataComponentType.<Integer>builder().persistent(Codec.intRange(1, CAPACITY)).build());

    public BirdFeedBagItem(Properties properties) {
        super(properties.stacksTo(1).component(SERVINGS, CAPACITY));
    }

    private static int remaining(ItemStack stack) {
        return stack.getOrDefault(SERVINGS, CAPACITY);
    }

    public static void serve(Player player, InteractionHand hand, ItemStack stack) {
        if (player.hasInfiniteMaterials()) return;
        int portions = remaining(stack);
        if (portions == 1) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.PAPER)));
        } else {
            stack.set(SERVINGS, portions - 1);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) { return Math.round(13F * remaining(stack) / CAPACITY); }

    @Override
    public int getBarColor(ItemStack stack) { return 0xCDA65C; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tooltip.parapet.feed_bag.servings", remaining(stack), CAPACITY)
            .withStyle(ChatFormatting.GRAY));
    }
}

package dev.parapet.cage;

import dev.parapet.block.ParrotCageBlock;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public final class ParrotCageItem extends BlockItem {
    public ParrotCageItem(ParrotCageBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return ((ParrotCageBlock) getBlock()).placeCage(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        CageBirdCargo.birdName(stack).ifPresent(name -> lines.accept(Component.translatable(
            CageBirdCargo.isAdult(stack) ? "tooltip.parapet.cage.adult" : "tooltip.parapet.cage.juvenile", name
        ).withStyle(ChatFormatting.AQUA)));
        // Minecraft displays the item names and counts of modern container components.
        if (!stack.has(DataComponents.CONTAINER)) {
            for (ItemStack fitting : CageDecorations.contents(stack)) {
                lines.accept(fitting.getHoverName());
            }
        }
    }
}

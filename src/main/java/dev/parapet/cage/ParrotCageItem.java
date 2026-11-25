package dev.parapet.cage;

import dev.parapet.block.ParrotCageBlock;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
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
    public net.minecraft.world.InteractionResult place(BlockPlaceContext context) {
        // Wait for authoritative multipart placement before consuming a cage or predicting its blocks.
        if (context.getLevel().isClientSide()) return net.minecraft.world.InteractionResult.SUCCESS;
        return super.place(context);
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
        int birds = CageBirdCargo.birdCount(stack);
        if (birds > 1) lines.accept(Component.translatable("tooltip.parapet.cage.flock", birds));
        var size = ((ParrotCageBlock) getBlock()).size();
        var fittings = CageDecorations.contents(stack);
        int used = CageToy.installedCount(fittings);
        lines.accept(Component.translatable("tooltip.parapet.cage.toy_slots", used, CageToy.capacity(size))
            .withStyle(used > CageToy.capacity(size) ? ChatFormatting.GOLD : ChatFormatting.AQUA));
        appendNames(lines, "tooltip.parapet.cage.supported_toys",
            CageToy.supported(size).stream().map(toy -> toy.item().getDefaultInstance().getHoverName()).toList());
        if (used > 0 && display.shows(DataComponents.CONTAINER)) {
            // The vanilla inventory preview truncates after five stacks, including food and cups.
            var installed = fittings.stream().filter(item -> CageToy.forItem(item).isPresent()).map(item ->
                item.getCount() == 1 ? item.getHoverName() : Component.translatable("item.container.item_count", item.getHoverName(), item.getCount())
            ).toList();
            appendNames(lines, "tooltip.parapet.cage.installed_toys", installed);
            lines.accept(Component.translatable("tooltip.parapet.cage.remove_toys_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
        // Old cages recorded their garland and bell only as flags.
        if (!stack.has(DataComponents.CONTAINER)) {
            for (ItemStack fitting : fittings) {
                lines.accept(fitting.getHoverName());
            }
        }
    }

    private static void appendNames(Consumer<Component> lines, String label, List<Component> names) {
        for (int i = 0; i < names.size(); i += 2) {
            var pair = ComponentUtils.formatList(names.subList(i, Math.min(i + 2, names.size())), name -> name);
            lines.accept((i == 0 ? Component.translatable(label, pair) : Component.literal("  ").append(pair))
                .withStyle(ChatFormatting.GRAY));
        }
    }
}

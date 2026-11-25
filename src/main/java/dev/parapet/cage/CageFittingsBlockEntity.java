package dev.parapet.cage;

import dev.parapet.ParapetFurniture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CageFittingsBlockEntity extends BlockEntity {
    private ItemContainerContents fittings = ItemContainerContents.EMPTY;

    public CageFittingsBlockEntity(BlockPos pos, BlockState state) {
        super(ParapetFurniture.CAGE_FITTINGS, pos, state);
    }

    public ItemContainerContents fittings() { return fittings; }

    public void setFittings(ItemContainerContents fittings) {
        this.fittings = fittings;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    // Player mining asks for loot after removing the whole structure. The broken
    // member survives as the loot context's block entity, even when it was not the base.
    public void captureForDrops(CageFittingsBlockEntity base) {
        this.fittings = base.fittings;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        fittings = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        // These are handled when restoring a packed bird. Mark them consumed so
        // vanilla does not copy the redeemed ticket or its stack limit onto the block.
        components.get(DataComponents.CUSTOM_DATA);
        components.get(DataComponents.MAX_STACK_SIZE);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fittings = input.read("Fittings", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (fittings.size() > 0) output.store("Fittings", ItemContainerContents.CODEC, fittings);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveCustomOnly(registries); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}

package dev.parapet.cage;

import dev.parapet.ParapetFurniture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CageFittingsBlockEntity extends BlockEntity {
    private static final int SECONDS_UNTIL_DIRTY = 120;
    private static final String PACKED_TRAY_SECONDS = "parapet:tray_seconds";
    private ItemContainerContents fittings = ItemContainerContents.EMPTY;
    private int occupiedSeconds;

    public CageFittingsBlockEntity(BlockPos pos, BlockState state) {
        super(ParapetFurniture.CAGE_FITTINGS, pos, state);
    }

    public ItemContainerContents fittings() { return fittings; }

    public boolean needsCleaning() { return occupiedSeconds == SECONDS_UNTIL_DIRTY; }

    public void recordOccupiedSecond() {
        if (needsCleaning()) return;
        occupiedSeconds++;
        setChanged();
        if (needsCleaning()) syncAppearance();
    }

    public void cleanTray() {
        occupiedSeconds = 0;
        setChanged();
        syncAppearance();
    }

    private void syncAppearance() {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setFittings(ItemContainerContents fittings) {
        this.fittings = fittings;
        setChanged();
        syncAppearance();
    }

    // Player mining asks for loot after removing the whole structure. The broken
    // member survives as the loot context's block entity, even when it was not the base.
    public void captureForDrops(CageFittingsBlockEntity base) {
        this.fittings = base.fittings;
        this.occupiedSeconds = base.occupiedSeconds;
    }

    public void copyTrayTo(ItemStack shell) {
        if (occupiedSeconds > 0) {
            CustomData.update(DataComponents.CUSTOM_DATA, shell, tag -> tag.putInt(PACKED_TRAY_SECONDS, occupiedSeconds));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        fittings = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        // Read only the tray. A redeemed bird ticket and its stack limit belong to the item.
        var packed = components.get(DataComponents.CUSTOM_DATA);
        if (packed != null) {
            occupiedSeconds = Math.clamp(packed.copyTag().getIntOr(PACKED_TRAY_SECONDS, occupiedSeconds), 0, SECONDS_UNTIL_DIRTY);
            if (needsCleaning()) syncAppearance();
        }
        components.get(DataComponents.MAX_STACK_SIZE);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fittings = input.read("Fittings", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY);
        occupiedSeconds = Math.clamp(input.getIntOr("OccupiedSeconds", 0), 0, SECONDS_UNTIL_DIRTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (fittings.size() > 0) output.store("Fittings", ItemContainerContents.CODEC, fittings);
        if (occupiedSeconds > 0) output.putInt("OccupiedSeconds", occupiedSeconds);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveCustomOnly(registries); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}

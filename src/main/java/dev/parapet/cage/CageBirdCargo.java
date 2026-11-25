package dev.parapet.cage;

import dev.parapet.Parapet;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.entity.ParrotEcology;
import dev.parapet.entity.PetParrotEntity;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Transfers one existing pet between its entity and a single-use, world-issued cage item. */
public final class CageBirdCargo {
    private static final String KEY = "parapet:caged_bird";

    private CageBirdCargo() {}

    private static final class CargoLedger extends SavedData {
        static final SavedDataType<CargoLedger> TYPE = new SavedDataType<>(Parapet.id("caged_birds"),
            () -> new CargoLedger(new CompoundTag()), CompoundTag.CODEC.xmap(CargoLedger::new, ledger -> ledger.records.copy()),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        final CompoundTag records;

        CargoLedger(CompoundTag records) {
            this.records = records;
        }

        void add(CompoundTag entry) {
            records.put(key(entry), entry.copy());
            setDirty();
        }

        void remove(CompoundTag entry) {
            records.remove(key(entry));
            setDirty();
        }
    }

    private static CargoLedger ledger(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(CargoLedger.TYPE);
    }

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity dropped && isOccupied(dropped.getItem())
                && authenticated(level, dropped.getItem(), false).isPresent()) {
                protectDrop(dropped);
            }
        });
    }

    private static void protectDrop(ItemEntity item) {
        item.setUnlimitedLifetime();
        item.setPermanentlyInvulnerable(true);
    }

    private static Optional<CompoundTag> contents(ItemStack cage) {
        if (cage.isEmpty() || !(cage.getItem() instanceof ParrotCageItem)) return Optional.empty();
        CustomData data = cage.get(DataComponents.CUSTOM_DATA);
        return data == null ? Optional.empty() : data.copyTag().getCompound(KEY);
    }

    public static boolean isOccupied(ItemStack cage) {
        return contents(cage).isPresent();
    }

    public static boolean hasBird(ItemStack cage) {
        return isOccupied(cage);
    }

    public static boolean isAdult(ItemStack cage) {
        return contents(cage).map(tag -> tag.getIntOr("Age", -1) >= 0).orElse(false);
    }

    public static Optional<Identifier> entityTypeId(ItemStack cage) {
        return contents(cage).flatMap(tag -> tag.getString("Type")).map(Identifier::tryParse);
    }

    public static boolean isOwnedBy(ItemStack cage, Player player) {
        return contents(cage).flatMap(tag -> tag.read("Owner", UUIDUtil.CODEC)).filter(player.getUUID()::equals).isPresent();
    }

    public static Optional<Identifier> verifiedOwnedType(ServerLevel level, ItemStack cage, Player owner) {
        if (!isOwnedBy(cage, owner)) return Optional.empty();
        return authenticated(level, cage, false).flatMap(tag -> tag.getString("Type")).map(Identifier::tryParse);
    }

    public static Optional<String> birdName(ItemStack cage) {
        return contents(cage).flatMap(tag -> tag.getString("Name"));
    }

    public static ItemStack emptyCage(ItemStack cage) {
        ItemStack empty = cage.copyWithCount(1);
        clearContents(empty);
        return empty;
    }

    private static void clearContents(ItemStack cage) {
        Optional<CompoundTag> entry = contents(cage);
        if (entry.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, cage, tag -> tag.remove(KEY));
        if (cage.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty()) cage.remove(DataComponents.CUSTOM_DATA);
        cage.set(DataComponents.MAX_STACK_SIZE, Math.clamp(entry.get().getIntOr("EmptyStackLimit", 16), 1, 99));
    }

    private static String key(CompoundTag entry) {
        return entry.read("Bird", UUIDUtil.CODEC).map(UUID::toString).orElse("");
    }

    private static Optional<CompoundTag> authenticated(ServerLevel level, ItemStack cage, boolean requireStored) {
        if (cage.getCount() != 1 || cage.getMaxStackSize() != 1) return Optional.empty();
        return contents(cage).filter(entry -> {
            Optional<UUID> bird = entry.read("Bird", UUIDUtil.CODEC);
            if (bird.isEmpty() || requireStored && level.getEntityInAnyDimension(bird.get()) != null) return false;
            return ledger(level).records.getCompound(bird.get().toString()).filter(entry::equals).isPresent();
        });
    }

    private static Optional<CompoundTag> serialize(ServerLevel level, ItemStack empty, PetParrotEntity bird, Player owner) {
        if (!bird.isAlive() || bird.isPassenger() || bird.isVehicle() || !bird.isOwnedBy(owner) || bird.getCageRoost().isAssigned()) return Optional.empty();
        CargoLedger ledger = ledger(level);
        if (ledger.records.contains(bird.getStringUUID())) return Optional.empty();
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        if (!bird.save(output)) return Optional.empty();
        CompoundTag entry = new CompoundTag();
        entry.store("Ticket", UUIDUtil.CODEC, UUID.randomUUID());
        entry.store("Bird", UUIDUtil.CODEC, bird.getUUID());
        entry.store("Owner", UUIDUtil.CODEC, owner.getUUID());
        entry.putString("Type", BuiltInRegistries.ENTITY_TYPE.getKey(bird.getType()).toString());
        entry.putInt("Age", bird.getAge());
        entry.putString("Name", bird.getName().getString());
        entry.putInt("EmptyStackLimit", empty.getMaxStackSize());
        entry.put("Entity", output.buildResult());

        return Optional.of(entry);
    }

    private static ItemStack filled(ItemStack empty, CompoundTag entry) {
        ItemStack occupied = empty.copyWithCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, occupied, tag -> tag.put(KEY, entry.copy()));
        occupied.set(DataComponents.MAX_STACK_SIZE, 1);
        return occupied;
    }

    public static void capture(ServerLevel level, Player owner, net.minecraft.world.InteractionHand hand,
                               PetParrotEntity bird, ParrotCageBlock cage) {
        ItemStack empty = owner.getItemInHand(hand);
        if (isOccupied(empty) || !bird.isOwnedBy(owner) || bird.isLeashed() || bird.getCageRoost().isAssigned()) return;
        if (ParrotEcology.forBird(bird).size().compareTo(cage.size().maxBirdSize) > 0) {
            owner.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.parapet.cage.too_large"));
            return;
        }
        serialize(level, empty, bird, owner).ifPresent(entry -> {
            ItemStack occupied = filled(empty, entry);
            ledger(level).add(entry);
            empty.shrink(1);
            owner.setItemInHand(hand, occupied);
            bird.discard();
            if (!empty.isEmpty()) owner.getInventory().placeItemBackInInventory(empty, net.minecraft.util.Prediction.SERVER_ONLY);
        });
    }

    public static boolean dropFromCage(ServerLevel level, BlockPos pos, ItemStack empty, PetParrotEntity bird, Player owner) {
        var saved = serialize(level, empty, bird, owner);
        if (saved.isEmpty()) return false;
        CompoundTag entry = saved.get();
        CargoLedger ledger = ledger(level);
        ItemStack occupied = filled(empty, entry);
        ItemEntity dropped = new ItemEntity(level, pos.getX() + .5, pos.getY() + .35, pos.getZ() + .5, occupied);
        dropped.setDefaultPickUpDelay();
        dropped.setTarget(owner.getUUID());
        protectDrop(dropped);
        ledger.add(entry);
        if (!level.addFreshEntity(dropped)) {
            ledger.remove(entry);
            return false;
        }
        bird.discard();
        return true;
    }

    /** Called after all structure cells have been placed. A failed return rolls the placement back. */
    public static boolean restore(ServerLevel level, ItemStack stack, Player owner, ParrotCageBlock cage, BlockPos root, Direction facing) {
        if (owner == null || !isOwnedBy(stack, owner)) return false;
        Optional<CompoundTag> checked = authenticated(level, stack, true);
        if (checked.isEmpty()) return false;
        CompoundTag entry = checked.get();
        var type = entityTypeId(stack).flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional);
        if (type.isEmpty() || !(type.get().create(level, EntitySpawnReason.LOAD) instanceof PetParrotEntity bird)) return false;
        bird.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), entry.getCompoundOrEmpty("Entity")));
        if (!bird.isAlive() || !bird.isOwnedBy(owner) || !bird.getStringUUID().equals(key(entry))
            || ParrotEcology.forBird(bird).size().compareTo(cage.size().maxBirdSize) > 0) return false;
        bird.getCageRoost().settle(level, bird, cage, root, facing);
        if (!level.addFreshEntity(bird)) return false;
        ledger(level).remove(entry);
        if (owner.hasInfiniteMaterials()) clearContents(stack);
        return true;
    }

    /** Redeems a confirmed sale, once. Quotes and previews must not call this. */
    public static boolean redeem(ServerLevel level, ItemStack cage, Player seller) {
        if (!isOwnedBy(cage, seller) || !isAdult(cage)) return false;
        Optional<CompoundTag> checked = authenticated(level, cage, true);
        if (checked.isEmpty()) return false;
        ledger(level).remove(checked.get());
        return true;
    }
}

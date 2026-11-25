package dev.parapet.item;

import dev.parapet.Parapet;
import dev.parapet.block.ParrotBathBlock;
import dev.parapet.entity.PetParrotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ParrotHatchingEggItem extends Item {
    private static final double SURFACE_TOLERANCE = 0.0001;
    private final EntityType<? extends PetParrotEntity> species;

    private ParrotHatchingEggItem(Properties properties, EntityType<? extends PetParrotEntity> species) {
        super(properties);
        this.species = species;
    }

    public static void register(String birdId, EntityType<? extends PetParrotEntity> species) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Parapet.id(birdId + "_hatching_egg"));
        Registry.register(BuiltInRegistries.ITEM, key,
            new ParrotHatchingEggItem(new Item.Properties().setId(key).stacksTo(16), species));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        BlockPos support = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Vec3 hit = context.getClickLocation();
        BlockPos feet = BlockPos.containing(hit.add(0, SURFACE_TOLERANCE, 0));
        if (context.getClickedFace() != Direction.UP || !level.mayInteract(player, support)
            || !level.mayInteract(player, feet) || !player.mayUseItemAt(feet, Direction.UP, stack)) {
            return cannotHatch(player);
        }
        BlockState state = level.getBlockState(support);
        if (!state.getFluidState().isEmpty() || state.getValueOrElse(ParrotBathBlock.FILLED, false)
            || state.is(Blocks.WATER_CAULDRON) || state.is(Blocks.LAVA_CAULDRON) || state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            return cannotHatch(player);
        }

        // A slab or stair can support a chick at a fractional height. A flower's outline cannot.
        Vec3 local = hit.subtract(support.getX(), support.getY(), support.getZ());
        double top = Double.NaN;
        for (AABB box : state.getCollisionShape(level, support).toAabbs()) {
            if (local.x >= box.minX && local.x <= box.maxX && local.z >= box.minZ && local.z <= box.maxZ
                && Math.abs(local.y - box.maxY) <= SURFACE_TOLERANCE) {
                top = support.getY() + box.maxY;
                break;
            }
        }
        if (Double.isNaN(top)) {
            return cannotHatch(player);
        }

        PetParrotEntity chick = species.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (chick == null) {
            return cannotHatch(player);
        }
        chick.snapTo(hit.x, top, hit.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        chick.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), EntitySpawnReason.SPAWN_ITEM_USE, null);
        chick.setBaby(true);
        AABB bounds = chick.getBoundingBox().deflate(0.000001);
        if (level.isOutsideBuildHeight(Mth.floor(bounds.minY)) || level.isOutsideBuildHeight(Mth.floor(bounds.maxY))
            || !level.getWorldBorder().isWithinBounds(bounds) || !level.noCollision(chick, bounds)
            || level.containsAnyLiquid(bounds)
            || !level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> entity.isAlive() && !entity.isSpectator()).isEmpty()
            || !level.addFreshEntity(chick)) {
            return cannotHatch(player);
        }
        stack.consume(1, player);
        level.playSound(null, feet, SoundEvents.TURTLE_EGG_HATCH, SoundSource.NEUTRAL, 0.62F, 1.2F);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, chick.position());
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult cannotHatch(Player player) {
        player.sendOverlayMessage(Component.translatable("message.parapet.hatching_egg_space"));
        return InteractionResult.FAIL;
    }
}

package dev.parapet.entity;

import dev.parapet.block.BirdFeedPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A feeding block and a spot from which a grounded bird can reach it. */
record ParrotForagePatch(BlockPos pos, Vec3 feet, boolean nectar, BirdFeedPileBlock feedPile) {
    static ParrotForagePatch nearbyFeed(ServerLevel level, PetParrotEntity bird) {
        for (BlockPos pos : BlockPos.withinManhattan(bird.blockPosition(), 4)) {
            if (pos.getY() != bird.blockPosition().getY() || !level.hasChunkAt(pos)
                || !(level.getBlockState(pos).getBlock() instanceof BirdFeedPileBlock pile)) continue;
            Vec3 center = Vec3.atBottomCenterOf(pos);
            if (level.noCollision(bird, bird.getBoundingBox().move(center.subtract(bird.position())).move(0, 0.005, 0))
                && level.clip(new ClipContext(bird.getEyePosition(), center.add(0, 0.12, 0),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bird)).getType() == HitResult.Type.MISS) {
                return new ParrotForagePatch(pos.immutable(), center, false, pile);
            }
        }
        return null;
    }

    static ParrotForagePatch nearby(ServerLevel level, PetParrotEntity bird) {
        boolean nectar = bird instanceof CompanionParrotEntity companion && companion.getSpecies().eatsFruit();
        if (!nectar && !(bird instanceof BudgerigarEntity)) return null;
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasFixedTime()
            || ParrotNightRoostGoal.isRoostTime(level)) return null;
        for (BlockPos pos : BlockPos.withinManhattan(bird.blockPosition(), 4)) {
            if (pos.getY() != bird.blockPosition().getY() || !level.hasChunkAt(pos)
                || !matches(level.getBlockState(pos), nectar)) continue;
            Vec3 base = Vec3.atBottomCenterOf(pos);
            Vec3 towardsBird = bird.position().subtract(base).horizontal();
            if (towardsBird.lengthSqr() < 0.001) towardsBird = Vec3.directionFromRotation(0, bird.getYRot()).scale(-1);
            Vec3 feet = base.add(towardsBird.normalize().scale(0.62));
            BlockPos support = BlockPos.containing(feet).below();
            if (level.hasChunkAt(support) && level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)
                && level.noCollision(bird, bird.getBoundingBox().move(feet.subtract(bird.position())).move(0, 0.005, 0))) {
                return new ParrotForagePatch(pos.immutable(), feet, nectar, null);
            }
        }
        return null;
    }

    private static boolean matches(BlockState state, boolean nectar) {
        return nectar ? state.is(BlockTags.SMALL_FLOWERS) && !state.is(Blocks.WITHER_ROSE)
            : state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS);
    }

    boolean available(ServerLevel level) {
        if (!level.hasChunkAt(pos)) return false;
        if (feedPile != null) return level.getGameRules().get(GameRules.MOB_GRIEFING) && level.getBlockState(pos).is(feedPile);
        return matches(level.getBlockState(pos), nectar) && !ParrotNightRoostGoal.isRoostTime(level);
    }

    boolean finish(ServerLevel level) {
        if (!available(level)) return false;
        if (feedPile != null) return feedPile.eatOne(level, pos);
        if (nectar) {
            level.sendParticles(ParticleTypes.FALLING_NECTAR, pos.getX() + 0.5, pos.getY() + 0.6,
                pos.getZ() + 0.5, 2, 0.09, 0.04, 0.09, 0.0);
        }
        return true;
    }
}

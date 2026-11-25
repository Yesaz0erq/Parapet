package dev.parapet.cage;

import dev.parapet.Parapet;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.entity.AfricanGreyEntity;
import dev.parapet.entity.ParrotEcology;
import dev.parapet.entity.PetParrotActivity;
import dev.parapet.entity.PetParrotEntity;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A perch belongs to a live entity. No cage item or cage block stores a second copy of a bird. */
public final class CageRoost {
    private static final Identifier SCALE = Parapet.id("cage_perch_scale");
    private BlockPos anchor;
    private String dimension = "";
    private String sizeName = "";
    private int seat;
    private ParrotCageBlock.Size cachedCage;
    private int cachedLength;
    private double cachedNatural, cachedFactor;
    private int bellCooldown;
    private int provisionSeconds;
    private BlockPos breakingPart;
    private long breakTick;

    public boolean isAssigned() {
        return anchor != null;
    }

    public void save(ValueOutput output) {
        if (anchor != null) {
            output.store("CagePerch", BlockPos.CODEC, anchor);
            output.putString("CageDimension", dimension);
            output.putString("CageSize", sizeName);
            output.putInt("CageSeat", seat);
            if (provisionSeconds > 0) output.putInt("CageProvisionSeconds", provisionSeconds);
        }
    }

    public void load(ValueInput input, PetParrotEntity bird) {
        anchor = input.read("CagePerch", BlockPos.CODEC).orElse(null);
        dimension = input.getStringOr("CageDimension", "");
        sizeName = input.getStringOr("CageSize", "");
        seat = Math.clamp(input.getIntOr("CageSeat", 0), 0, 3);
        provisionSeconds = Math.clamp(input.getIntOr("CageProvisionSeconds", 0), 0, 59);
        bellCooldown = 240 + bird.getRandom().nextInt(360);
        if (anchor != null && dimension.equals(bird.level().dimension().identifier().toString())) {
            for (ParrotCageBlock.Size size : ParrotCageBlock.Size.values()) {
                if (size.name().equals(sizeName)) {
                    bird.setCageSeat(seat);
                    setPerchedScale(bird, size);
                    bird.setInSittingPose(true);
                    return;
                }
            }
        }
        anchor = null;
        bird.setCageSize(null);
        if (bird.getAttribute(Attributes.SCALE).removeModifier(SCALE)) {
            bird.refreshDimensions();
            bird.setInSittingPose(bird.isOrderedToSit());
        }
    }

    private boolean at(ServerLevel level, BlockPos pos) {
        return pos.equals(anchor) && dimension.equals(level.dimension().identifier().toString());
    }

    public static void registerInteractions() {
        var depositPhase = Parapet.id("deposit_bird");
        UseBlockCallback.EVENT.addPhaseOrdering(depositPhase, net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
        boolean carryOn = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("carryon");
        UseBlockCallback.EVENT.register(depositPhase, (player, level, hand, hit) -> {
            if (player.isSpectator()) return InteractionResult.PASS;
            var pos = hit.getBlockPos();
            var state = level.getBlockState(pos);
            boolean stand = state.is(dev.parapet.ParapetFurniture.PARROT_PLAY_STAND)
                || state.is(dev.parapet.ParapetFurniture.LARGE_PARROT_PLAY_STAND);
            if (!stand && !(state.getBlock() instanceof ParrotCageBlock)) return InteractionResult.PASS;
            var held = player.getItemInHand(hand);
            boolean carried = carryOn && hand == InteractionHand.MAIN_HAND && CarryOnBird.isHeld(player);
            if (!carried && !CageBirdCargo.isOccupied(held)) return InteractionResult.PASS;
            if (level instanceof ServerLevel server) {
                if (!level.mayInteract(player, pos)) return InteractionResult.FAIL;
                for (BlockPos candidate : BlockPos.withinManhattan(pos, stand ? 4 : 0)) {
                    if (candidate.distSqr(pos) > 16 || !server.hasChunkAt(candidate)) continue;
                    var cell = server.getBlockState(candidate);
                    if (!(cell.getBlock() instanceof ParrotCageBlock dest) || !cell.getValue(ParrotCageBlock.OPEN)) continue;
                    var root = dest.anchor(candidate, cell);
                    var facing = cell.getValue(ParrotCageBlock.FACING);
                    if ((stand && !candidate.equals(root)) || !server.mayInteract(player, root)
                        || !dest.loaded(server, root, facing) || !dest.complete(server, root, facing)) continue;
                    if (carried) {
                        if (CarryOnBird.deposit(server, player, dest, root, facing)) return InteractionResult.SUCCESS;
                    } else {
                        var empty = CageBirdCargo.emptyCage(held);
                        if (CageBirdCargo.restore(server, held, player, dest, root, facing)) {
                            player.setItemInHand(hand, empty);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                message(player, "need_open_space");
            }
            return InteractionResult.SUCCESS;
        });
        // Vanilla skips the block interaction when a sneaking player holds an item in either hand.
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !player.isSecondaryUseActive()
                || player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ParrotCageBlock cage && cage.isEnabled(level.enabledFeatures())) {
                return cage.transferBird(state, level, pos, player);
            }
            return InteractionResult.PASS;
        });
    }

    public static PetParrotEntity occupantAt(ServerLevel level, BlockPos anchor) {
        return residents(level, anchor).stream().findFirst().orElse(null);
    }

    static List<PetParrotEntity> residents(ServerLevel level, BlockPos anchor) {
        return level.getEntitiesOfClass(PetParrotEntity.class, new AABB(anchor).inflate(4),
            bird -> bird.isAlive() && bird.getCageRoost().at(level, anchor));
    }

    static int units(ParrotEcology.Size size) {
        return switch (size) { case SMALL -> 1; case MEDIUM -> 2; case LARGE -> 4; };
    }

    private Vec3 seatPosition(ParrotCageBlock cage, BlockPos root, Direction facing) {
        // Space the birds along the main wooden perch, keeping seat zero at the feeding branch.
        return cage.perch(root, facing).relative(facing.getClockWise(), CageLayout.seatOffset(cage.size(), seat));
    }

    public static void transfer(ServerLevel level, ParrotCageBlock cage, BlockPos anchor, Direction facing, Player player) {
        Vec3 perch = cage.perch(anchor, facing);
        if (!level.areEntitiesLoaded(ChunkPos.pack(BlockPos.containing(perch)))) return;
        var flock = residents(level, anchor);
        PetParrotEntity resident = flock.isEmpty() ? null : flock.getLast();
        if (resident != null) {
            if (!resident.isOwnedBy(player)) {
                message(player, "occupied");
                return;
            }
        }
        if (resident != null && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
            Vec3 exit = findExit(level, resident, cage, anchor, facing);
            if (exit == null) {
                message(player, "exit_blocked");
                return;
            }
            resident.getCageRoost().release(resident, exit);
            message(player, "released", resident.getDisplayName());
            return;
        }
        Vec3 doorway = perch.relative(facing, cage.size().depth * 0.5 + 0.04).add(0, 0.16, 0);
        List<PetParrotEntity> nearby = level.getEntitiesOfClass(PetParrotEntity.class, AABB.ofSize(doorway, 10, 10, 10),
            bird -> bird.isAlive() && bird.isTame() && bird.isOwnedBy(player) && !bird.isLeashed()
                && !bird.isPassenger() && !bird.isVehicle() && !bird.getCageRoost().isAssigned()
                && bird.distanceToSqr(doorway) <= 25 && bird.hasLineOfSight(player)
                && clearPath(level, bird, doorway, bird.getEyePosition()));
        nearby.sort(Comparator.comparingDouble(bird -> bird.distanceToSqr(doorway)));
        int used = flock.stream().mapToInt(b -> units(ParrotEcology.forBird(b).size())).sum();
        for (PetParrotEntity bird : nearby) {
            if (used + units(ParrotEcology.forBird(bird).size()) > units(cage.size().maxBirdSize)) continue;
            bird.getCageRoost().settle(level, bird, cage, anchor, facing);
            message(player, "entered", bird.getDisplayName());
            return;
        }
        message(player, nearby.isEmpty() ? "no_bird" : "too_large");
    }

    void settle(ServerLevel level, PetParrotEntity bird, ParrotCageBlock cage, BlockPos root, Direction facing) {
        boolean[] taken = new boolean[4];
        for (var other : residents(level, root)) taken[other.getCageRoost().seat] = true;
        seat = 0;
        while (seat < 3 && taken[seat]) seat++;
        anchor = root.immutable();
        dimension = level.dimension().identifier().toString();
        sizeName = cage.size().name();
        bird.setCageSeat(seat);
        provisionSeconds = seat * 7;
        bellCooldown = 240 + bird.getRandom().nextInt(360);
        bird.getNavigation().stop();
        setPerchedScale(bird, cage.size());
        position(bird, seatPosition(cage, root, facing), facing);
    }

    public static void markForPacking(ServerLevel level, Player player, BlockPos root, BlockPos brokenPart) {
        for (PetParrotEntity bird : level.getEntitiesOfClass(PetParrotEntity.class, new AABB(root).inflate(4),
            pet -> pet.isAlive() && pet.isOwnedBy(player) && pet.getCageRoost().at(level, root))) {
            bird.getCageRoost().breakingPart = brokenPart.immutable();
            bird.getCageRoost().breakTick = level.getGameTime();
        }
    }

    public static List<PetParrotEntity> takePackingCandidates(ServerLevel level, Player player, BlockPos brokenPart) {
        var result = new java.util.ArrayList<PetParrotEntity>();
        for (PetParrotEntity bird : level.getEntitiesOfClass(PetParrotEntity.class, new AABB(brokenPart).inflate(9),
            pet -> pet.isAlive() && pet.isOwnedBy(player))) {
            CageRoost roost = bird.getCageRoost();
            if (!roost.isAssigned() && brokenPart.equals(roost.breakingPart) && roost.breakTick == level.getGameTime()) {
                roost.breakingPart = null;
                result.add(bird);
            }
        }
        return result;
    }

    private static void message(Player player, String key, Object... arguments) {
        player.sendOverlayMessage(Component.translatable("message.parapet.cage." + key, arguments));
    }

    private static boolean clearPath(ServerLevel level, PetParrotEntity bird, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bird))
            .getType() == HitResult.Type.MISS;
    }

    private double currentFactor(PetParrotEntity bird) {
        AttributeModifier current = bird.getAttribute(Attributes.SCALE).getModifier(SCALE);
        return current == null ? 1 : 1 + current.amount();
    }

    private EntityDimensions outsideDimensions(PetParrotEntity bird) {
        return bird.getDimensions(bird.getPose()).scale((float) (1 / currentFactor(bird)));
    }

    private static Vec3 findExit(ServerLevel level, PetParrotEntity bird, ParrotCageBlock cage,
                                 BlockPos root, Direction facing) {
        Vec3 inside = cage.perch(root, facing);
        EntityDimensions dimensions = bird.getCageRoost().outsideDimensions(bird);
        Vec3 doorway = inside.relative(facing, cage.size().depth * 0.5 + 0.03);
        Vec3 front = doorway.relative(facing, dimensions.width() * 0.5 + 0.12);
        for (double lateral : new double[]{0, -0.65, 0.65}) {
            for (double lift : new double[]{0.07, 0.55, 1.05}) {
                Vec3 exit = front.relative(facing.getClockWise(), lateral).with(Direction.Axis.Y, root.getY() + lift);
                if (clearPath(level, bird, doorway.add(0, 0.10, 0), exit.add(0, 0.1, 0))
                    && safeSpace(level, bird, dimensions, exit)) return exit;
            }
        }
        return null;
    }

    private static boolean safeSpace(ServerLevel level, PetParrotEntity bird, EntityDimensions dimensions, Vec3 feet) {
        AABB box = dimensions.makeBoundingBox(feet);
        return level.getWorldBorder().isWithinBounds(box) && level.noCollision(bird, box) && !level.containsAnyLiquid(box);
    }

    public static void onBroken(ServerLevel level, BlockPos root) {
        for (PetParrotEntity bird : level.getEntitiesOfClass(PetParrotEntity.class, new AABB(root).inflate(4),
            pet -> pet.getCageRoost().at(level, root))) {
            bird.getCageRoost().clearBrokenRoost(level, bird);
        }
    }

    private void clearBrokenRoost(ServerLevel level, PetParrotEntity bird) {
        EntityDimensions restored = outsideDimensions(bird);
        Vec3 feet = bird.position();
        if (!safeSpace(level, bird, restored, feet)) {
            for (BlockPos pos : BlockPos.withinManhattan(bird.blockPosition(), 3)) {
                Vec3 candidate = Vec3.atBottomCenterOf(pos);
                if (safeSpace(level, bird, restored, candidate)) {
                    feet = candidate;
                    break;
                }
            }
        }
        release(bird, feet);
    }

    private void release(PetParrotEntity bird, Vec3 feet) {
        anchor = null;
        bird.setCageSize(null);
        dimension = "";
        sizeName = "";
        bird.snapTo(feet);
        if (bird.getAttribute(Attributes.SCALE).removeModifier(SCALE)) bird.refreshDimensions();
        bird.snapTo(feet);
        bird.setDeltaMovement(Vec3.ZERO);
        bird.setInSittingPose(bird.isOrderedToSit());
    }

    public void tick(ServerLevel level, PetParrotEntity bird) {
        if (anchor == null) return;
        if (!at(level, anchor) || bird.distanceToSqr(Vec3.atCenterOf(anchor)) > 36 || bird.isPassenger()) {
            release(bird, bird.position());
            return;
        }
        if (!level.hasChunkAt(anchor)) return;
        BlockState state = level.getBlockState(anchor);
        if (!(state.getBlock() instanceof ParrotCageBlock cage) || state.getValue(ParrotCageBlock.PART) != ParrotCageBlock.Part.BASE
            || !cage.size().name().equals(sizeName)) {
            clearBrokenRoost(level, bird);
            return;
        }
        Direction facing = state.getValue(ParrotCageBlock.FACING);
        if (bird.tickCount % 20 == 0 && cage.loaded(level, anchor, facing) && !cage.complete(level, anchor, facing)) {
            clearBrokenRoost(level, bird);
            return;
        }
        setPerchedScale(bird, cage.size());
        position(bird, seatPosition(cage, anchor, facing), facing);
        if (bird.tickCount % 20 == 0 && level.getBlockEntity(anchor) instanceof CageFittingsBlockEntity tray) {
            tray.recordOccupiedSecond();
            provisionSeconds = (provisionSeconds + 1) % 60;
            if (provisionSeconds == 30 || provisionSeconds == 0) {
                CageSupplies.offerMeal(tray, bird, provisionSeconds == 30);
            }
        }
        if (bird.getActivity() == PetParrotActivity.FORAGE && bird.getActivityTicks(0) == 10F) {
            Vec3 dish = CageLayout.mealPosition(cage.size(), bird.getCageMeal());
            CageSupplies.playMealContact(level, bird, CageLayout.toWorld(anchor, facing, dish));
        }
        if (state.getValue(ParrotCageBlock.BELL) && --bellCooldown <= 0) {
            bellCooldown = 240 + bird.getRandom().nextInt(360);
            level.playSound(null, anchor, SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 0.20F, 1.65F);
            level.sendParticles(ParticleTypes.NOTE, bird.getX(), bird.getEyeY() + 0.08, bird.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private void position(PetParrotEntity bird, Vec3 perch, Direction facing) {
        bird.getMoveControl().setWait();
        bird.setDeltaMovement(Vec3.ZERO);
        bird.setJumping(false);
        bird.setXxa(0);
        bird.setYya(0);
        bird.setZza(0);
        if (bird.distanceToSqr(perch) > 0.00001) bird.snapTo(perch);
        float yaw = facing.getClockWise().toYRot();
        bird.setYRot(yaw);
        bird.setYBodyRot(yaw);
        bird.setYHeadRot(yaw);
        bird.setXRot(0);
        bird.setOnGround(true);
        bird.setInSittingPose(true);
    }

    private void setPerchedScale(PetParrotEntity bird, ParrotCageBlock.Size cage) {
        bird.setCageSize(cage);
        AttributeInstance scale = bird.getAttribute(Attributes.SCALE);
        double oldFactor = currentFactor(bird);
        double naturalScale = scale.getValue() / oldFactor;
        // Fit the adult silhouette. Chicks then keep their usual proportion to an adult in the same cage.
        int cm = lengthCm(bird);
        if (cage == cachedCage && cm == cachedLength && Math.abs(cachedNatural - naturalScale) < 1e-9
            && Math.abs(cachedFactor - oldFactor) < 1e-9) return;
        double length = cm * naturalScale / 16.0;
        EntityDimensions outside = bird.getType().getDimensions().scale((float) naturalScale);
        // Each tail extends behind the feet. The perch is offset along the long axis to give it more room.
        double displayScale = switch (cage) {
            case STANDARD -> 0.54;
            case LARGE -> 0.62;
            case DELUXE -> 0.68;
            case SUPER_DELUXE -> 0.74;
            case GIANT -> 0.80;
        };
        double factor = Math.min(displayScale, 0.80 * (cage.width - 0.20) / length);
        double crossRoom = Math.min(cage.depth - 0.18, cage.width * 0.72 - 0.14);
        factor = Math.min(factor, crossRoom / outside.width());
        factor = Math.min(factor, (cage.height - cage.perchHeight - 0.12) / outside.height());
        cachedFactor = factor;
        cachedNatural = naturalScale;
        cachedLength = cm;
        cachedCage = cage;
        if (Math.abs(oldFactor - factor) < 0.00001) return;
        if (factor >= 1) scale.removeModifier(SCALE);
        else scale.addOrUpdateTransientModifier(new AttributeModifier(SCALE, factor - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        bird.refreshDimensions();
    }

    private static int lengthCm(PetParrotEntity bird) {
        var length = ParrotEcology.forBird(bird).adultLengthCm();
        return bird instanceof AfricanGreyEntity && bird.getPlumageId().equals("timneh") ? length.minCm() : length.maxCm();
    }
}

package dev.parapet.entity;

import dev.parapet.ParapetFurniture;
import dev.parapet.guide.ParrotFieldGuide;
import dev.parapet.cage.CageRoost;
import dev.parapet.cage.CageRoostGoal;
import dev.parapet.interaction.ParrotInteractionMenu;
import dev.parapet.interaction.ParrotInteractionMenu.Availability;
import dev.parapet.interaction.ParrotTrick;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class PetParrotEntity extends TamableAnimal {
    protected static final EntityDataAccessor<Integer> PLUMAGE =
        SynchedEntityData.defineId(PetParrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTIVITY =
        SynchedEntityData.defineId(PetParrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTIVITY_START =
        SynchedEntityData.defineId(PetParrotEntity.class, EntityDataSerializers.LONG);
    public static final int MAX_AFFINITY = 100;
    private static final EntityDataAccessor<Integer> AFFINITY =
        SynchedEntityData.defineId(PetParrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PERSONALITY =
        SynchedEntityData.defineId(PetParrotEntity.class, EntityDataSerializers.INT);
    private static final DustParticleOptions FEATHER_POWDER = new DustParticleOptions(0xFFF9F1, 0.48F);
    private static final int BREEDING_TIME_FACTOR = 5;
    private static final Ingredient SEEDS = Ingredient.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS,
        Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);

    private final List<String> colors;
    private String carriedPlumage;
    private WildParrotApproach wildApproach;
    private final CageRoost cageRoost = new CageRoost();
    final ParrotFlockContacts flockContacts = new ParrotFlockContacts();
    private final ParrotPerchRoutine perchRoutine = new ParrotPerchRoutine();
    private int nextPreenTick;
    private long nextInteractionTime;
    private int nextDisplayTick;
    private Player displayAudience;
    private int ignoreFoodUntil;
    private boolean ownerWasAway;
    private long nextOwnerGreeting;
    private float previousFlightAmount;
    private float flightAmount;

    protected PetParrotEntity(EntityType<? extends PetParrotEntity> type, Level level, List<String> colors) {
        super(type, level);
        this.colors = colors;
        if (!level.isClientSide()) {
            entityData.set(PERSONALITY, ParrotPersonality.forUuid(getUUID()).ordinal());
        }
        nextPreenTick = 160 + random.nextInt(280);
        moveControl = new FlyingMoveControl<>(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
            .add(Attributes.MAX_HEALTH, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.20)
            .add(Attributes.FLYING_SPEED, 0.38);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new CageRoostGoal(this));
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        wildApproach = new WildParrotApproach(this);
        goalSelector.addGoal(2, wildApproach);
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new ParrotBreedGoal(this));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1, 5.0F, 2.0F));
        goalSelector.addGoal(5, new TemptGoal(this, 1.0, stack -> isTame() && !cageRoost.isAssigned() && acceptsFood(stack), false));
        goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        goalSelector.addGoal(7, new ParrotNightRoostGoal(this));
        goalSelector.addGoal(7, new StillActivityGoal());
        goalSelector.addGoal(8, new GroundForageGoal());
        goalSelector.addGoal(9, new UseParrotFurnitureGoal(this));
        goalSelector.addGoal(10, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected boolean canFlyToOwner() {
        return true;
    }

    @Override
    protected boolean omnidirectionalAirMover() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() && isBaby() && (tickCount + getId()) % 15 == 0) {
            level().addParticle(FEATHER_POWDER, getRandomX(0.75), getY(0.25 + random.nextDouble() * 0.55),
                getRandomZ(0.75), 0.0, -0.009, 0.0);
        }
        if (level() instanceof ServerLevel server && cageRoost.isAssigned()) {
            cageRoost.tick(server, this);
        }
        previousFlightAmount = flightAmount;
        boolean flying = !onGround() && !isInWater() && !isPassenger();
        flightAmount = flying ? Math.min(1.0F, flightAmount + 0.20F) : Math.max(0.0F, flightAmount - 0.16F);
        Vec3 velocity = getDeltaMovement();
        if (!onGround() && !isInWater() && velocity.y < 0.0) {
            setDeltaMovement(velocity.multiply(1.0, 0.65, 1.0));
        }
    }

    @Override
    protected int getBabyStartAge() {
        return super.getBabyStartAge() * BREEDING_TIME_FACTOR;
    }

    @Override
    public float getAgeScale() {
        if (!isBaby()) {
            return 1.0F;
        }
        return switch (ParrotEcology.forBird(this).size()) {
            case SMALL -> 1.0F;
            case MEDIUM -> 0.65F;
            case LARGE -> 0.50F;
        };
    }

    @Override
    public void finalizeSpawnChildFromBreeding(ServerLevel level, Animal mate, AgeableMob child) {
        super.finalizeSpawnChildFromBreeding(level, mate, child);
        setAge(PARENT_AGE_AFTER_BREEDING * BREEDING_TIME_FACTOR);
        mate.setAge(PARENT_AGE_AFTER_BREEDING * BREEDING_TIME_FACTOR);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damage, float amount) {
        if (cageRoost.isAssigned() && damage.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return super.hurtServer(level, damage, amount);
    }

    public final float getFlightAmount(float partialTick) {
        return previousFlightAmount + partialTick * (flightAmount - previousFlightAmount);
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLUMAGE, 0);
        builder.define(AFFINITY, 0);
        builder.define(PERSONALITY, ParrotPersonality.CALM.ordinal());
        builder.define(ACTIVITY, PetParrotActivity.IDLE.ordinal());
        builder.define(ACTIVITY_START, 0L);
    }

    public final CageRoost getCageRoost() {
        return cageRoost;
    }

    public final int getAffinity() {
        return entityData.get(AFFINITY);
    }

    public final ParrotPersonality getPersonality() {
        return ParrotPersonality.fromIndex(entityData.get(PERSONALITY));
    }

    public final String getCarriedPlumage() {
        return carriedPlumage == null ? getPlumageId() : carriedPlumage;
    }

    protected boolean hasPlumageGenes() {
        return colors.size() > 1;
    }

    public final PetParrotActivity getActivity() {
        return PetParrotActivity.byIndex(entityData.get(ACTIVITY));
    }

    public final float getActivityTicks(float partialTick) {
        PetParrotActivity activity = getActivity();
        long elapsed = level().getGameTime() - entityData.get(ACTIVITY_START);
        return Math.max(0.0F, Math.min(activity.durationTicks(), elapsed + partialTick));
    }

    final void setActivity(PetParrotActivity activity) {
        if (getActivity() != activity) {
            if (activity != PetParrotActivity.BEG && activity != PetParrotActivity.RAISE_CREST) {
                displayAudience = null;
            }
            entityData.set(ACTIVITY_START, level().getGameTime());
            entityData.set(ACTIVITY, activity.ordinal());
        }
    }

    boolean calmOnGround() {
        return onGround() && !isInWater() && !isInLava() && !isOnFire() && !isPassenger() && !isInLove()
            && hurtTime == 0 && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() >= 100);
    }

    boolean isStartled() {
        return wildApproach != null && wildApproach.frightened();
    }

    boolean canStartNaturalDetail() {
        return !cageRoost.isAssigned() && !isLeashed() && getActivity() == PetParrotActivity.IDLE
            && navigation.isDone() && getDeltaMovement().horizontalDistanceSqr() < 0.0025
            && calmOnGround() && !needsToFollowOwner() && !isStartled() && tickCount >= ignoreFoodUntil
            && !hasPriorityActivity();
    }

    boolean canUseFurniture() {
        return isTame() && !cageRoost.isAssigned() && tickCount >= ignoreFoodUntil && !isOrderedToSit()
            && !isInSittingPose() && !isLeashed() && !isPassenger() && !isInLove()
            && !isInWater() && !isInLava() && !isOnFire() && hurtTime == 0 && !needsToFollowOwner()
            && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() >= 100);
    }

    private boolean needsToFollowOwner() {
        LivingEntity owner = getOwner();
        return !isOrderedToSit() && owner != null && (owner.level() != level() || distanceToSqr(owner) > 25.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        perchRoutine.tick(level, this, tickCount >= ignoreFoodUntil);
        if (cageRoost.isAssigned()) {
            return;
        }
        boolean still = navigation.isDone() && getDeltaMovement().horizontalDistanceSqr() < 0.0025;
        greetReturningOwner(level, still);
        flockContacts.tick(level, this);
        PetParrotActivity activity = getActivity();
        if (isStationaryActivity(activity)) {
            float elapsed = getActivityTicks(0.0F);
            boolean moving = !still && (activity == PetParrotActivity.PREEN || elapsed > 4.0F);
            if (moving || !calmOnGround() || needsToFollowOwner() || elapsed >= activity.durationTicks()) {
                setActivity(PetParrotActivity.IDLE);
            } else if (displayAudience != null) {
                if (!displayAudience.isAlive() || displayAudience.isSpectator() || displayAudience.level() != level
                    || !isDisplayAudienceInRange(displayAudience) || tickCount % 10 == 0 && !hasLineOfSight(displayAudience)) {
                    setActivity(PetParrotActivity.IDLE);
                } else {
                    getLookControl().setLookAt(displayAudience, 40.0F, 30.0F);
                }
            } else if (activity == PetParrotActivity.GREET || activity.isOwnerTrick()) {
                LivingEntity owner = getOwner();
                if (owner != null && owner.level() == level && distanceToSqr(owner) <= 20.25) {
                    if (activity == PetParrotActivity.TOUCH_TARGET) {
                        getLookControl().setLookAt(owner.getX(), owner.getEyeY() - 0.45, owner.getZ(), 55F, 40F);
                    } else {
                        getLookControl().setLookAt(owner, 40.0F, 30.0F);
                    }
                }
            }
        } else if (activity == PetParrotActivity.IDLE && tickCount >= nextPreenTick) {
            nextPreenTick = tickCount + getPersonality().preenDelay(random);
            if (still && calmOnGround() && !needsToFollowOwner() && tickCount >= ignoreFoodUntil && !hasPriorityActivity()) {
                setActivity(random.nextInt(3) == 0 ? PetParrotActivity.LISTEN : PetParrotActivity.PREEN);
            }
        }
    }

    private void greetReturningOwner(ServerLevel level, boolean still) {
        if (!isTame()) {
            return;
        }
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != level || distanceToSqr(owner) > 64.0) {
            ownerWasAway = true;
            return;
        }
        if (!ownerWasAway || owner.isSpectator() || distanceToSqr(owner) > 9.0) {
            return;
        }
        if (level.getGameTime() < nextOwnerGreeting) {
            ownerWasAway = false;
            return;
        }
        if (!still || getActivity() != PetParrotActivity.IDLE || !calmOnGround() || !hasLineOfSight(owner)) {
            return;
        }
        if (hasPriorityActivity()) {
            return;
        }
        beginGreeting(owner);
        playSound(SoundEvents.PARROT_AMBIENT, 0.55F, 1.25F);
    }

    private boolean hasPriorityActivity() {
        for (var scheduled : goalSelector.getAvailableGoals()) {
            Goal goal = scheduled.getGoal();
            if (scheduled.isRunning() && (goal instanceof PanicGoal || goal instanceof BreedGoal
                || goal instanceof GroundForageGoal || goal instanceof UseParrotFurnitureGoal || goal instanceof WildParrotApproach
                || goal instanceof TemptGoal || goal instanceof FollowParentGoal || goal instanceof ParrotNightRoostGoal)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStationaryActivity(PetParrotActivity activity) {
        return activity == PetParrotActivity.PREEN || activity == PetParrotActivity.GREET
            || activity == PetParrotActivity.BEG || activity == PetParrotActivity.RAISE_CREST || activity.isOwnerTrick()
            || activity == PetParrotActivity.LISTEN || activity == PetParrotActivity.CONTACT_CALL || activity == PetParrotActivity.RUFFLE;
    }

    protected boolean isDisplayAudienceInRange(Player player) {
        return distanceToSqr(player) <= 25.0;
    }

    protected final void tryStartDisplay(PetParrotActivity activity, Player audience, int cooldown) {
        if (cageRoost.isAssigned() || tickCount < nextDisplayTick || tickCount < ignoreFoodUntil || getActivity() != PetParrotActivity.IDLE
            || !navigation.isDone() || getDeltaMovement().horizontalDistanceSqr() >= 0.0025 || !calmOnGround()
            || needsToFollowOwner() || hasPriorityActivity() || !hasLineOfSight(audience)) {
            return;
        }
        nextDisplayTick = tickCount + cooldown;
        displayAudience = audience;
        getLookControl().setLookAt(audience, 40.0F, 30.0F);
        setActivity(activity);
    }

    private void beginGreeting(LivingEntity owner) {
        ownerWasAway = false;
        nextOwnerGreeting = level().getGameTime() + 600L;
        nextPreenTick = Math.max(nextPreenTick, tickCount + 160);
        getNavigation().stop();
        getLookControl().setLookAt(owner, 40.0F, 30.0F);
        setActivity(PetParrotActivity.GREET);
    }

    public final int ownerInteractionCooldown() {
        return Math.clamp(nextInteractionTime - level().getGameTime(), 0, 80);
    }

    public final Availability ownerInteractionAvailability() {
        if (cageRoost.isAssigned()) return Availability.CAGED;
        if (ownerInteractionCooldown() > 0) return Availability.COOLING_DOWN;
        if ((getActivity() != PetParrotActivity.IDLE && !getActivity().isNaturalDetail()) || !calmOnGround() || needsToFollowOwner()
            || hasPriorityActivity()) return Availability.BUSY;
        return Availability.READY;
    }

    public final boolean tryOwnerInteraction(ServerPlayer owner, ParrotTrick trick) {
        if (!isOwnedBy(owner) || !isAlive() || !owner.isAlive() || owner.isSpectator() || owner.level() != level()
            || distanceToSqr(owner) > 20.25 || !hasLineOfSight(owner) || getAffinity() < trick.affinity
            || ownerInteractionAvailability() != Availability.READY) return false;
        nextInteractionTime = level().getGameTime() + 80;
        ownerWasAway = false;
        nextOwnerGreeting = Math.max(nextOwnerGreeting, level().getGameTime() + 600L);
        nextPreenTick = Math.max(nextPreenTick, tickCount + 160);
        pauseForaging();
        getNavigation().stop();
        getMoveControl().setWait();
        getLookControl().setLookAt(owner, 55F, 35F);
        setActivity(trick.activity);
        ServerLevel server = owner.level();
        switch (trick) {
            case GREET -> {
                playSound(SoundEvents.PARROT_AMBIENT, 0.6F, 1.24F);
                server.sendParticles(ParticleTypes.NOTE, getX(), getEyeY() + 0.1, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            case STROKE -> server.sendParticles(ParticleTypes.HEART, getX(), getEyeY() + 0.12, getZ(), 3, 0.15, 0.08, 0.15, 0.01);
            case TARGET -> {
                Vec3 direction = owner.position().subtract(position()).horizontal().normalize();
                Vec3 target = getEyePosition().add(direction.scale(getBbWidth() * 0.53));
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.x, target.y, target.z, 2, 0.02, 0.01, 0.02, 0.0);
            }
            case STRETCH -> playSound(SoundEvents.PARROT_FLY, 0.32F, 0.95F);
            case HIGH_FIVE -> {
                owner.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                playSound(SoundEvents.PARROT_STEP, 0.60F, 1.2F);
            }
            case TURN -> playSound(SoundEvents.PARROT_STEP, 0.45F, 1.35F);
        }
        return true;
    }

    public final String getPlumageId() {
        return colors.get(entityData.get(PLUMAGE));
    }

    public String getVisualPlumageId() {
        return getPlumageId();
    }

    public final void setPlumageId(String id) {
        entityData.set(PLUMAGE, Math.max(0, colors.indexOf(id)));
        carriedPlumage = null;
    }

    protected void randomizePlumage() {
        entityData.set(PLUMAGE, random.nextInt(colors.size()));
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return ParrotEcology.forBird(this).maxFlock();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, SpawnGroupData spawnData) {
        randomizePlumage();
        ParrotEcology.assignWildPlumage(this, level, reason);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Plumage", getPlumageId());
        output.putInt("Affinity", getAffinity());
        output.putString("Personality", getPersonality().id());
        if (ownerInteractionCooldown() > 0) output.putLong("NextOwnerInteraction", nextInteractionTime);
        if (hasPlumageGenes()) {
            output.putString("CarriedPlumage", getCarriedPlumage());
        }
        output.putInt("BreedingClockVersion", 1);
        cageRoost.save(output);
        if (ownerWasAway) {
            output.putBoolean("OwnerWasAway", true);
        }
        if (nextOwnerGreeting > level().getGameTime()) {
            output.putLong("NextOwnerGreeting", nextOwnerGreeting);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Plumage").ifPresent(this::setPlumageId);
        if (hasPlumageGenes()) {
            carriedPlumage = input.getString("CarriedPlumage").filter(colors::contains).orElse(null);
        }
        ParrotPersonality nature = ParrotPersonality.fromId(input.getStringOr("Personality", ""), getUUID());
        entityData.set(PERSONALITY, nature.ordinal());
        // Complete legacy saves contain ForcedAge and Plumage. A short /summon {Age:...} keeps its explicit ticks.
        if (input.getIntOr("BreedingClockVersion", 0) == 0 && input.getInt("ForcedAge").isPresent()
            && input.getString("Plumage").isPresent()) {
            setAge(Math.clamp((long) getAge() * BREEDING_TIME_FACTOR, Integer.MIN_VALUE, Integer.MAX_VALUE));
            forcedAge = Math.clamp((long) forcedAge * BREEDING_TIME_FACTOR, 0, Integer.MAX_VALUE);
        }
        input.getInt("Affinity").ifPresent(value -> entityData.set(AFFINITY, Math.clamp(value, 0, MAX_AFFINITY)));
        cageRoost.load(input, this);
        ownerWasAway = input.getBooleanOr("OwnerWasAway", false);
        nextOwnerGreeting = input.getLongOr("NextOwnerGreeting", 0L);
        nextInteractionTime = Math.min(input.getLongOr("NextOwnerInteraction", 0L), level().getGameTime() + 80);
    }

    protected boolean acceptsFood(ItemStack stack) {
        return SEEDS.test(stack);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return isTame() && !cageRoost.isAssigned() && !isOrderedToSit() && acceptsFood(stack);
    }

    protected boolean isSameSpecies(PetParrotEntity other) {
        return getType() == other.getType();
    }

    @Override
    public boolean canMate(Animal other) {
        return other instanceof PetParrotEntity partner && isSameSpecies(partner)
            && !cageRoost.isAssigned() && !partner.cageRoost.isAssigned()
            && isTame() && partner.isTame()
            && !isOrderedToSit() && !partner.isOrderedToSit() && super.canMate(partner);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame()) {
            WildParrotApproach.Lure lure = WildParrotApproach.Lure.of(this, stack);
            if (lure != WildParrotApproach.Lure.NONE || SEEDS.test(stack) || stack.is(Items.COCOA_BEANS)
                || stack.is(Items.SWEET_BERRIES) || stack.is(Items.MELON_SLICE)) {
                if (lure == WildParrotApproach.Lure.NONE && hand == InteractionHand.MAIN_HAND
                    && WildParrotApproach.Lure.of(this, player.getOffhandItem()) != WildParrotApproach.Lure.NONE) {
                    return InteractionResult.PASS;
                }
                if (level() instanceof ServerLevel server) {
                    offerTamingItem(server, player, hand, stack, lure);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (stack.is(ParapetFurniture.BIRD_FEED) || stack.is(ParapetFurniture.PREMIUM_BIRD_FEED)) {
            if (level() instanceof ServerLevel server) {
                if (!isOwnedBy(player)) {
                    player.sendOverlayMessage(Component.translatable("message.parapet.feed_owner_only"));
                } else if (getAffinity() == MAX_AFFINITY) {
                    showAffinity(player, "message.parapet.affinity_full");
                } else {
                    int gain = stack.is(ParapetFurniture.PREMIUM_BIRD_FEED) ? 15 : 5;
                    if (getActivity().isNaturalDetail()) {
                        setActivity(PetParrotActivity.IDLE);
                        pauseForaging();
                    }
                    entityData.set(AFFINITY, Math.min(MAX_AFFINITY, getAffinity() + gain));
                    usePlayerItem(player, hand, stack);
                    playSound(SoundEvents.PARROT_EAT, 0.75F, 1.1F);
                    server.sendParticles(ParticleTypes.HEART, getX(), getEyeY() + 0.12, getZ(), 3, 0.15, 0.08, 0.15, 0.01);
                    showAffinity(player, "message.parapet.affinity");
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!level().isClientSide() && acceptsFood(stack)) {
            pauseForaging();
            setActivity(PetParrotActivity.IDLE);
        }
        if (isOwnedBy(player) && stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            ItemStack offhand = player.getOffhandItem();
            if (acceptsFood(offhand) || offhand.is(ParapetFurniture.BIRD_FEED) || offhand.is(ParapetFurniture.PREMIUM_BIRD_FEED)) {
                return InteractionResult.PASS;
            }
            if (player instanceof ServerPlayer owner) {
                if (owner.isShiftKeyDown()) {
                    ParrotInteractionMenu.open(owner, this);
                } else {
                    pauseForaging();
                    setActivity(PetParrotActivity.IDLE);
                    setOrderedToSit(!isOrderedToSit());
                    getNavigation().stop();
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void offerTamingItem(ServerLevel server, Player player, InteractionHand hand, ItemStack stack,
                                 WildParrotApproach.Lure lure) {
        if (lure == WildParrotApproach.Lure.NONE) {
            player.sendOverlayMessage(Component.translatable("message.parapet.wild_wrong"));
            return;
        }
        if (wildApproach == null || !wildApproach.acceptsFrom(player)) {
            String key = wildApproach != null && wildApproach.frightened() ? "message.parapet.wild_startled" : "message.parapet.wild_wait";
            player.sendOverlayMessage(Component.translatable(key));
            return;
        }
        usePlayerItem(player, hand, stack);
        boolean tamed = random.nextInt(lure.attempts) < lure.successes;
        if (tamed) {
            tame(player);
            if (player instanceof ServerPlayer owner) {
                ParrotFieldGuide.record(owner, this, true);
            }
            getNavigation().stop();
            pauseForaging();
        }
        server.broadcastEntityEvent(this, tamed ? EntityEvent.TAMING_SUCCEEDED : EntityEvent.TAMING_FAILED);
    }

    private void showAffinity(Player player, String key) {
        player.sendOverlayMessage(Component.translatable(key, getDisplayName(), getAffinity(), MAX_AFFINITY));
    }

    private void pauseForaging() {
        ignoreFoodUntil = tickCount + 100;
    }

    private final class StillActivityGoal extends Goal {
        StillActivityGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            PetParrotActivity activity = getActivity();
            return isStationaryActivity(activity);
        }

        @Override
        public void start() {
            navigation.stop();
        }
    }

    private final class GroundForageGoal extends Goal {
        private static final double REACH_SQUARED = 0.9 * 0.9;
        private ItemEntity target;
        private ParrotForagePatch patch;
        private int nextPatchSearch;
        private PathNavigation flightNavigation;
        private MoveControl<?> flightControl;
        private int nextSearchTick;
        private int stopAtTick;

        GroundForageGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean canForage() {
            return !cageRoost.isAssigned() && tickCount >= ignoreFoodUntil && !isOrderedToSit() && !isInSittingPose() && !isLeashed()
                && calmOnGround() && !needsToFollowOwner() && !isStartled();
        }

        private boolean isLooseFood(ItemEntity item) {
            return item.isAlive() && item.onGround() && !item.isInWater() && !item.hasPickUpDelay()
                && acceptsFood(item.getItem()) && Math.abs(item.getY() - getY()) <= 0.55 && hasLineOfSight(item);
        }

        @Override
        public boolean canUse() {
            if (tickCount < nextSearchTick || getActivity() != PetParrotActivity.IDLE || !canForage()) {
                return false;
            }
            nextSearchTick = tickCount + 40 + random.nextInt(45);
            double nearest = 4.5 * 4.5;
            target = null;
            ServerLevel server = (ServerLevel) level();
            if (server.getGameRules().get(GameRules.MOB_GRIEFING)) {
                for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(4.5, 0.65, 4.5), this::isLooseFood)) {
                    double distance = distanceToSqr(item);
                    if (distance <= nearest) {
                        nearest = distance;
                        target = item;
                    }
                }
            }
            if (target == null && !isTame() && tickCount >= nextPatchSearch) {
                nextPatchSearch = tickCount + 160 + random.nextInt(180);
                patch = ParrotForagePatch.nearby(server, PetParrotEntity.this);
            }
            return target != null || patch != null;
        }

        @Override
        public boolean canContinueToUse() {
            PetParrotActivity activity = getActivity();
            if (tickCount >= stopAtTick || !canForage() || !(level() instanceof ServerLevel server)
                || activity != PetParrotActivity.IDLE && activity != PetParrotActivity.FORAGE) return false;
            if (patch != null) return !isTame() && patch.available(server);
            return target != null && server.getGameRules().get(GameRules.MOB_GRIEFING) && isLooseFood(target);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            stopAtTick = tickCount + 120;
            // Use walking controls for this short trip so the bird approaches on foot.
            flightNavigation = navigation;
            flightControl = moveControl;
            flightNavigation.stop();
            navigation = new GroundPathNavigation(PetParrotEntity.this, level());
            moveControl = new MoveControl<>(PetParrotEntity.this);
            setNoGravity(false);
            Vec3 feet = feedingPosition();
            if (!withinReach() && !navigation.moveTo(feet.x, feet.y, feet.z, 0.85)) {
                stopAtTick = tickCount;
            }
        }

        private Vec3 feedingPosition() {
            return patch != null ? patch.feet() : target.position();
        }

        private boolean withinReach() {
            return patch != null ? distanceToSqr(patch.feet()) <= 0.36 && distanceToSqr(Vec3.atBottomCenterOf(patch.plant())) <= 1.18
                : distanceToSqr(target) <= REACH_SQUARED;
        }

        @Override
        public void tick() {
            if (!canContinueToUse()) {
                return;
            }
            Vec3 feet = feedingPosition();
            if (patch != null) getLookControl().setLookAt(Vec3.atCenterOf(patch.plant()));
            else getLookControl().setLookAt(target, 35.0F, 35.0F);
            if (!withinReach()) {
                if (getActivity() == PetParrotActivity.FORAGE) {
                    stopAtTick = tickCount;
                } else if (navigation.isDone() && distanceToSqr(feet) <= 1.4 * 1.4) {
                    moveControl.setWantedPosition(feet.x, getY(), feet.z, 0.65);
                } else if (navigation.isDone()) {
                    stopAtTick = tickCount;
                }
                return;
            }
            navigation.stop();
            setActivity(PetParrotActivity.FORAGE);
            if (getActivityTicks(0.0F) < PetParrotActivity.FORAGE.durationTicks()) {
                return;
            }
            if (patch != null) {
                patch.finish((ServerLevel) level());
                playSound(SoundEvents.PARROT_EAT, 0.32F, 1.25F);
                nextPatchSearch = tickCount + 500 + random.nextInt(480);
                stopAtTick = tickCount;
                setActivity(PetParrotActivity.IDLE);
                return;
            }
            ItemStack food = target.getItem();
            if (food.getCount() == 1) {
                target.discard();
            } else {
                target.setItem(food.copyWithCount(food.getCount() - 1));
            }
            if (getHealth() < getMaxHealth()) {
                heal(1.0F);
            }
            playSound(SoundEvents.PARROT_EAT, 0.65F, 1.10F);
            nextSearchTick = tickCount + 480 + random.nextInt(240);
            target = null;
            setActivity(PetParrotActivity.IDLE);
        }

        @Override
        public void stop() {
            navigation.stop();
            navigation = flightNavigation;
            moveControl = flightControl;
            if (getActivity() == PetParrotActivity.FORAGE) {
                setActivity(PetParrotActivity.IDLE);
            }
            target = null;
            patch = null;
            nextSearchTick = Math.max(nextSearchTick, tickCount + 80);
        }
    }

    protected final <T extends PetParrotEntity> T createChick(EntityType<T> type, ServerLevel level, AgeableMob mate) {
        if (!(mate instanceof PetParrotEntity partner) || !isSameSpecies(partner)) {
            return null;
        }
        T chick = type.create(level, EntitySpawnReason.BREEDING);
        if (chick != null) {
            passPlumageTo(chick, partner);
            PetParrotEntity parent = random.nextBoolean() ? this : partner;
            if (parent.getOwnerReference() != null) {
                chick.setOwnerReference(parent.getOwnerReference());
                chick.setTame(true, true);
            }
        }
        return chick;
    }

    private void passPlumageTo(PetParrotEntity chick, PetParrotEntity partner) {
        if (partner == this || !hasPlumageGenes()) {
            chick.setPlumageId(getPlumageId());
            chick.carriedPlumage = carriedPlumage;
            return;
        }
        boolean lovebird = this instanceof LovebirdEntity;
        boolean mixedLovebirds = lovebird && !getPlumageId().equals(partner.getPlumageId());
        List<String> palette = colors;
        if (lovebird && !mixedLovebirds) {
            String family = LovebirdPlumages.speciesOf(getPlumageId());
            palette = colors.stream().filter(color -> family.equals(LovebirdPlumages.speciesOf(color))).toList();
        }
        PlumageGenes inherited = PlumageGenes.breed(random,
            new PlumageGenes(getPlumageId(), getCarriedPlumage()), getPersonality(),
            new PlumageGenes(partner.getPlumageId(), partner.getCarriedPlumage()), partner.getPersonality(),
            palette, mixedLovebirds);
        chick.setPlumageId(inherited.visible());
        chick.carriedPlumage = inherited.carried();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getActivity() == PetParrotActivity.NIGHT_ROOST ? null : SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }
}

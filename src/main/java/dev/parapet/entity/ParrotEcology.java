package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomSupport;

public enum ParrotEcology {
    BUDGERIGAR(18, Size.SMALL, Habitat.DRY, Rarity.COMMON, 4, 8,
        List.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.PLAINS),
    COCKATIEL(31, Size.MEDIUM, Habitat.DRY, Rarity.COMMON, 3, 7,
        List.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS), Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.SUNFLOWER_PLAINS),
    GREEN_CHEEK_CONURE(26, Size.MEDIUM, Habitat.WOODLAND, Rarity.UNCOMMON, 3, 5,
        List.of(Items.PUMPKIN_SEEDS, Items.MELON_SEEDS), Biomes.SPARSE_JUNGLE, Biomes.SAVANNA_PLATEAU),
    LOVEBIRD(15, Size.SMALL, Habitat.DRY, Rarity.UNCOMMON, 3, 6,
        List.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WOODED_BADLANDS),
    MONK_PARAKEET(29, Size.MEDIUM, Habitat.WOODLAND, Rarity.COMMON, 3, 7,
        List.of(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS), Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SAVANNA),
    CAIQUE(23, Size.MEDIUM, Habitat.WOODLAND, Rarity.SCARCE, 2, 4,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
    ECLECTUS(35, Size.LARGE, Habitat.WOODLAND, Rarity.UNCOMMON, 1, 3,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.MANGROVE_SWAMP),
    SUN_CONURE(30, Size.MEDIUM, Habitat.WOODLAND, Rarity.RARE, 3, 6,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.SPARSE_JUNGLE, Biomes.SAVANNA),
    SULFUR_CRESTED_COCKATOO(50, Size.LARGE, Habitat.WOODLAND, Rarity.COMMON, 3, 7,
        List.of(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS), Biomes.FOREST, Biomes.SPARSE_JUNGLE, Biomes.PLAINS),
    AFRICAN_GREY(new AdultLength(30, 33), Size.MEDIUM, Habitat.WOODLAND, Rarity.RARE, 2, 5,
        List.of(Items.PUMPKIN_SEEDS, Items.MELON_SEEDS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.MANGROVE_SWAMP),
    JARDINES_PARROT(CompanionParrotSpecies.JARDINES_PARROT.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.SCARCE, 2, 4,
        List.of(Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS), Biomes.JUNGLE, Biomes.WINDSWEPT_FOREST),
    BOURKES_PARROT(CompanionParrotSpecies.BOURKES_PARROT.lengthCm(), Size.SMALL, Habitat.DRY, Rarity.UNCOMMON, 3, 6,
        List.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), Biomes.SAVANNA_PLATEAU, Biomes.WOODED_BADLANDS),
    RAINBOW_LORIKEET(CompanionParrotSpecies.RAINBOW_LORIKEET.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.COMMON, 3, 6,
        List.of(Items.COCOA_BEANS), Biomes.FLOWER_FOREST, Biomes.SPARSE_JUNGLE),
    RED_LORY(CompanionParrotSpecies.RED_LORY.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.SCARCE, 3, 6,
        List.of(Items.COCOA_BEANS), Biomes.JUNGLE, Biomes.MANGROVE_SWAMP),
    DUSKY_LORY(CompanionParrotSpecies.DUSKY_LORY.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.UNCOMMON, 4, 7,
        List.of(Items.COCOA_BEANS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
    BLUE_FRONTED_AMAZON(CompanionParrotSpecies.BLUE_FRONTED_AMAZON.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.UNCOMMON, 2, 4,
        List.of(Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS), Biomes.SAVANNA, Biomes.SPARSE_JUNGLE),
    YELLOW_HEADED_AMAZON(CompanionParrotSpecies.YELLOW_HEADED_AMAZON.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.RARE, 2, 3,
        List.of(Items.PUMPKIN_SEEDS, Items.COCOA_BEANS), Biomes.SPARSE_JUNGLE, Biomes.MANGROVE_SWAMP),
    ORANGE_WINGED_AMAZON(CompanionParrotSpecies.ORANGE_WINGED_AMAZON.lengthCm(), Size.LARGE, Habitat.WATERSIDE, Rarity.UNCOMMON, 2, 5,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.MANGROVE_SWAMP, Biomes.SWAMP),
    ROSE_RINGED_PARAKEET(CompanionParrotSpecies.ROSE_RINGED_PARAKEET.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.COMMON, 3, 7,
        List.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS), Biomes.SAVANNA, Biomes.SPARSE_JUNGLE, Biomes.SUNFLOWER_PLAINS),
    EASTERN_ROSELLA(CompanionParrotSpecies.EASTERN_ROSELLA.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.COMMON, 2, 4,
        List.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), Biomes.PLAINS, Biomes.FOREST, Biomes.FLOWER_FOREST),
    CRIMSON_ROSELLA(CompanionParrotSpecies.CRIMSON_ROSELLA.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.UNCOMMON, 1, 3,
        List.of(Items.BEETROOT_SEEDS, Items.MELON_SEEDS), Biomes.FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.WINDSWEPT_FOREST),
    SENEGAL_PARROT(CompanionParrotSpecies.SENEGAL_PARROT.lengthCm(), Size.MEDIUM, Habitat.DRY, Rarity.UNCOMMON, 2, 4,
        List.of(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS), Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU),
    BLUE_AND_YELLOW_MACAW(MacawSpecies.BLUE_AND_YELLOW.lengthCm(), Size.LARGE, Habitat.WATERSIDE, Rarity.SCARCE, 2, 4,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.MANGROVE_SWAMP, Biomes.SPARSE_JUNGLE),
    BLUE_THROATED_MACAW(MacawSpecies.BLUE_THROATED.lengthCm(), Size.LARGE, Habitat.WATERSIDE, Rarity.VERY_RARE, 2, 3,
        List.of(Items.PUMPKIN_SEEDS), Biomes.SAVANNA, Biomes.SWAMP),
    MILITARY_MACAW(MacawSpecies.MILITARY.lengthCm(), Size.LARGE, Habitat.CLIFF, Rarity.RARE, 2, 4,
        List.of(Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS), Biomes.WINDSWEPT_SAVANNA, Biomes.WOODED_BADLANDS),
    GREAT_GREEN_MACAW(MacawSpecies.GREAT_GREEN.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.VERY_RARE, 2, 3,
        List.of(Items.PUMPKIN_SEEDS), Biomes.JUNGLE),
    SCARLET_MACAW(MacawSpecies.SCARLET.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.SCARCE, 2, 4,
        List.of(Items.COCOA_BEANS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
    RED_AND_GREEN_MACAW(MacawSpecies.RED_AND_GREEN.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.SCARCE, 2, 3,
        List.of(Items.PUMPKIN_SEEDS, Items.MELON_SEEDS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
    RED_FRONTED_MACAW(MacawSpecies.RED_FRONTED.lengthCm(), Size.LARGE, Habitat.CLIFF, Rarity.VERY_RARE, 2, 4,
        List.of(Items.PUMPKIN_SEEDS, Items.WHEAT_SEEDS), Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WINDSWEPT_SAVANNA),
    CHESTNUT_FRONTED_MACAW(MacawSpecies.CHESTNUT_FRONTED.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.UNCOMMON, 2, 4,
        List.of(Items.MELON_SEEDS, Items.BEETROOT_SEEDS), Biomes.SPARSE_JUNGLE, Biomes.MANGROVE_SWAMP),
    HYACINTH_MACAW(MacawSpecies.HYACINTH.lengthCm(), Size.LARGE, Habitat.WOODLAND, Rarity.RARE, 2, 4,
        List.of(Items.PUMPKIN_SEEDS), Biomes.SAVANNA, Biomes.SWAMP, Biomes.SPARSE_JUNGLE),
    LEAR_MACAW(MacawSpecies.LEAR.lengthCm(), Size.LARGE, Habitat.CLIFF, Rarity.VERY_RARE, 3, 6,
        List.of(Items.PUMPKIN_SEEDS), Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS),
    SPIX_MACAW(MacawSpecies.SPIX.lengthCm(), Size.LARGE, Habitat.WATERSIDE, Rarity.REINTRODUCED, 1, 2,
        List.of(Items.MELON_SEEDS, Items.WHEAT_SEEDS), Biomes.SAVANNA, Biomes.WOODED_BADLANDS),
    RED_BELLIED_MACAW(MacawSpecies.RED_BELLIED.lengthCm(), Size.LARGE, Habitat.WATERSIDE, Rarity.UNCOMMON, 4, 7,
        List.of(Items.MELON_SEEDS), Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
    BLUE_HEADED_MACAW(MacawSpecies.BLUE_HEADED.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.RARE, 2, 3,
        List.of(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS), Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
    BLUE_WINGED_MACAW(MacawSpecies.BLUE_WINGED.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.SCARCE, 2, 3,
        List.of(Items.PUMPKIN_SEEDS, Items.MELON_SEEDS), Biomes.SPARSE_JUNGLE, Biomes.SAVANNA),
    YELLOW_COLLARED_MACAW(MacawSpecies.YELLOW_COLLARED.lengthCm(), Size.MEDIUM, Habitat.DRY, Rarity.UNCOMMON, 2, 4,
        List.of(Items.BEETROOT_SEEDS, Items.PUMPKIN_SEEDS), Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU),
    RED_SHOULDERED_MACAW(MacawSpecies.RED_SHOULDERED.lengthCm(), Size.MEDIUM, Habitat.WOODLAND, Rarity.UNCOMMON, 3, 6,
        List.of(Items.BEETROOT_SEEDS, Items.MELON_SEEDS), Biomes.SAVANNA, Biomes.SPARSE_JUNGLE, Biomes.SWAMP);

    /** Typical bill-tip to tail-tip lengths used to scale this guide entry's adult models. */
    public record AdultLength(int minCm, int maxCm) {
        public AdultLength {
            if (minCm <= 0 || maxCm < minCm) throw new IllegalArgumentException("Invalid adult length range");
        }
    }

    public enum Size { SMALL, MEDIUM, LARGE }
    public enum Rarity {
        COMMON(3, 1), UNCOMMON(2, 2), SCARCE(1, 4), RARE(1, 12), VERY_RARE(1, 40), REINTRODUCED(1, 192);

        private final int weight;
        private final int regionDenominator;

        Rarity(int weight, int regionDenominator) {
            this.weight = weight;
            this.regionDenominator = regionDenominator;
        }
    }

    private enum Habitat { WOODLAND, DRY, CLIFF, WATERSIDE }

    // Leaves allow the vanilla parrot as a perch but may reject other entity types.
    // Preserve ordinary ground clearance, fluids and border checks for both paths.
    public static final SpawnPlacementType PERCH = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> type) {
            return SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
                || level.getBlockState(pos.below()).is(BlockTags.LEAVES)
                && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, EntityTypes.PARROT);
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader level, BlockPos pos) {
            return SpawnPlacementTypes.ON_GROUND.adjustSpawnPosition(level, pos);
        }
    };

    private final String entityId = name().toLowerCase(Locale.ROOT);
    private final AdultLength adultLengthCm;
    private final Size size;
    private final Habitat habitat;
    private final Rarity rarity;
    private final int minimum;
    private final int maximum;
    private final List<Item> lures;
    private final List<ResourceKey<Biome>> biomes;
    private final long rangeSalt = RandomSupport.mixStafford13(entityId.hashCode());
    private static final Map<String, ParrotEcology> BY_ID = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(ecology -> ecology.entityId, Function.identity()));

    @SafeVarargs
    private ParrotEcology(int lengthCm, Size size, Habitat habitat, Rarity rarity, int minimum, int maximum,
                          List<Item> lures, ResourceKey<Biome>... biomes) {
        this(new AdultLength(lengthCm, lengthCm), size, habitat, rarity, minimum, maximum, lures, biomes);
    }

    @SafeVarargs
    private ParrotEcology(AdultLength adultLengthCm, Size size, Habitat habitat, Rarity rarity, int minimum, int maximum,
                          List<Item> lures, ResourceKey<Biome>... biomes) {
        this.adultLengthCm = adultLengthCm;
        this.size = size;
        this.habitat = habitat;
        this.rarity = rarity;
        this.minimum = minimum;
        this.maximum = maximum;
        this.lures = lures;
        this.biomes = List.of(biomes);
    }

    public String entityId() { return entityId; }
    public AdultLength adultLengthCm() { return adultLengthCm; }
    public Size size() { return size; }
    public Rarity rarity() { return rarity; }
    public int spawnWeight() { return rarity.weight; }
    public int chanceDenominator() { return rarity.regionDenominator; }
    public int minFlock() { return minimum; }
    public int maxFlock() { return maximum; }
    public List<ResourceKey<Biome>> biomes() { return biomes; }
    public List<Item> tamingItems() { return lures; }

    public static ParrotEcology forId(String path) {
        ParrotEcology ecology = BY_ID.get(path);
        if (ecology == null) throw new IllegalArgumentException("Unknown parrot species: " + path);
        return ecology;
    }

    public boolean acceptsTamingItem(ItemStack stack) {
        return !stack.isEmpty() && lures.contains(stack.getItem());
    }

    public static ParrotEcology forBird(PetParrotEntity bird) {
        return forType(bird.getType());
    }

    public static Optional<ParrotEcology> forEntityId(Identifier id) {
        return id.getNamespace().equals(Parapet.MOD_ID) ? Optional.ofNullable(BY_ID.get(id.getPath())) : Optional.empty();
    }

    private static ParrotEcology forType(EntityType<?> type) {
        return BY_ID.get(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
    }

    public static void registerSpawns() {
        var land = BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_OCEAN).negate());
        for (ParrotEcology entry : values()) {
            var type = BuiltInRegistries.ENTITY_TYPE.getOptional(Parapet.id(entry.entityId)).orElseThrow();
            BiomeModifications.addSpawn(land.and(BiomeSelectors.includeByKey(entry.biomes)),
                MobCategory.CREATURE, type, entry.spawnWeight(), entry.minFlock(), entry.maxFlock());
        }
    }

    public static boolean checkSpawn(EntityType<? extends PetParrotEntity> type, LevelAccessor level,
                                     EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return true;
        }
        if (!(level instanceof ServerLevelAccessor world) || !world.getLevel().dimension().equals(Level.OVERWORLD)) {
            return false;
        }
        ParrotEcology ecology = forType(type);
        var biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_OCEAN) || !biome.is(ecology.biomes::contains)
            || !ecology.occupiesRegion(world.getLevel().getSeed(), pos)
            || level.getRawBrightness(pos, 0) <= 8 || !level.getFluidState(pos).isEmpty()) {
            return false;
        }
        BlockPos supportPos = pos.below();
        BlockState support = level.getBlockState(supportPos);
        if (!support.getFluidState().isEmpty() || !ecology.supports(support)) {
            return false;
        }
        return ecology.habitat != Habitat.WATERSIDE || hasSurfaceWaterNearby(level, pos);
    }

    private boolean occupiesRegion(long worldSeed, BlockPos pos) {
        if (chanceDenominator() == 1) return true;
        // Neighbouring members of a flock share the same range check throughout a 128-block region.
        long region = ChunkPos.pack(pos.getX() >> 7, pos.getZ() >> 7);
        long sample = RandomSupport.mixStafford13(worldSeed ^ rangeSalt ^ region);
        return Math.floorMod(sample, chanceDenominator()) == 0;
    }

    private boolean supports(BlockState block) {
        if (block.is(BlockTags.ANIMALS_SPAWNABLE_ON) || block.is(BlockTags.DIRT)
            || block.is(BlockTags.LEAVES) || block.is(BlockTags.OVERWORLD_NATURAL_LOGS)
            || block.is(Blocks.MUD) || block.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            return true;
        }
        if (habitat == Habitat.CLIFF && block.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }
        return (habitat == Habitat.DRY || habitat == Habitat.CLIFF || habitat == Habitat.WATERSIDE)
            && (block.is(BlockTags.SAND) || block.is(BlockTags.BADLANDS_TERRACOTTA) || block.is(Blocks.GRAVEL));
    }

    private static boolean hasSurfaceWaterNearby(LevelAccessor level, BlockPos perch) {
        var sample = new BlockPos.MutableBlockPos();
        for (int z = perch.getZ() - 12; z <= perch.getZ() + 12; z += 3) {
            for (int x = perch.getX() - 12; x <= perch.getX() + 12; x += 3) {
                if (!level.hasChunkAt(x, z)) continue;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                if (Math.abs(perch.getY() - y) <= 24
                    && level.getFluidState(sample.set(x, y, z)).is(FluidTags.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void assignWildPlumage(PetParrotEntity bird, ServerLevelAccessor level, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return;
        if (!(bird instanceof AfricanGreyEntity || bird instanceof LovebirdEntity || bird instanceof CaiqueEntity)) return;
        BlockPos pos = bird.blockPosition();
        long seed = level.getLevel().getSeed();
        long region = Mth.getSeed(pos.getX() >> 8, (int) (seed ^ (seed >>> 32)), pos.getZ() >> 8);
        if (bird instanceof AfricanGreyEntity) {
            bird.setPlumageId(Math.floorMod(region, 3) == 0 ? "timneh" : "congo");
        } else if (bird instanceof CaiqueEntity) {
            bird.setPlumageId(Math.floorMod(region, 2) == 0 ? "black_headed" : "white_bellied");
        } else {
            String species = level.getBiome(pos).is(Biomes.WOODED_BADLANDS) ? "peach"
                : switch (Math.floorMod(region, 3)) { case 0 -> "peach"; case 1 -> "fischer"; default -> "masked"; };
            // Keep the existing chance of finding captive plumages as a rare game variation.
            if (bird.getRandom().nextInt(8) == 0) {
                var colors = LovebirdPlumages.IDS.stream().filter(id -> LovebirdPlumages.speciesOf(id).equals(species)).toList();
                bird.setPlumageId(colors.get(bird.getRandom().nextInt(colors.size())));
            } else {
                bird.setPlumageId(species + "_green");
            }
        }
    }
}

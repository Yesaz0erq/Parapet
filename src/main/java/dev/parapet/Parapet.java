package dev.parapet;

import dev.parapet.entity.AfricanGreyEntity;
import dev.parapet.entity.BudgerigarEntity;
import dev.parapet.entity.CaiqueEntity;
import dev.parapet.entity.CockatielEntity;
import dev.parapet.entity.CompanionParrotEntity;
import dev.parapet.entity.CompanionParrotSpecies;
import dev.parapet.entity.EclectusEntity;
import dev.parapet.entity.GreenCheekConureEntity;
import dev.parapet.entity.LovebirdEntity;
import dev.parapet.entity.MacawEntity;
import dev.parapet.entity.MacawSpecies;
import dev.parapet.entity.MonkParakeetEntity;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.entity.ParrotEcology;
import dev.parapet.entity.SulfurCrestedCockatooEntity;
import dev.parapet.entity.SunConureEntity;
import dev.parapet.villager.ParrotMerchant;
import dev.parapet.item.ParrotHatchingEggItem;
import dev.parapet.guide.ParrotFieldGuide;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Parapet implements ModInitializer {
    public static final String MOD_ID = "parapet";

    public static final EntityType<BudgerigarEntity> BUDGERIGAR =
        registerBird("budgerigar", BudgerigarEntity::new, 0.42F, 0.70F, 0.70F * 0.85F);
    public static final EntityType<CockatielEntity> COCKATIEL =
        registerBird("cockatiel", CockatielEntity::new, 0.55F, 1.26F, 0.82F);
    public static final EntityType<GreenCheekConureEntity> GREEN_CHEEK_CONURE =
        registerBird("green_cheek_conure", GreenCheekConureEntity::new, 0.54F, 0.90F, 0.74F);

    public static final EntityType<LovebirdEntity> LOVEBIRD =
        registerBird("lovebird", LovebirdEntity::new, 0.50F, 0.82F, 0.69F);
    public static final EntityType<MonkParakeetEntity> MONK_PARAKEET =
        registerBird("monk_parakeet", MonkParakeetEntity::new, 0.56F, 0.97F, 0.83F);

    public static final EntityType<CaiqueEntity> CAIQUE =
        registerBird("caique", CaiqueEntity::new, 0.64F, 1.17F, 0.96F);
    public static final EntityType<EclectusEntity> ECLECTUS =
        registerBird("eclectus", EclectusEntity::new, 0.85F, 1.44F, 1.20F);
    public static final EntityType<SunConureEntity> SUN_CONURE =
        registerBird("sun_conure", SunConureEntity::new, 0.51F, 0.93F, 0.77F);

    public static final EntityType<SulfurCrestedCockatooEntity> SULFUR_CRESTED_COCKATOO =
        registerBird("sulfur_crested_cockatoo", SulfurCrestedCockatooEntity::new, 1.25F, 2.40F, 1.77F);
    public static final EntityType<AfricanGreyEntity> AFRICAN_GREY =
        registerBird("african_grey", AfricanGreyEntity::new, 1.09F, 1.69F, 1.39F);

    public static final Map<MacawSpecies, EntityType<MacawEntity>> MACAWS = registerSpecies(MacawSpecies.class,
        species -> registerBird(species.entityId(), (type, level) -> new MacawEntity(type, level, species),
            species.width(), species.height(), species.eyeHeight()));
    public static final Map<CompanionParrotSpecies, EntityType<CompanionParrotEntity>> COMPANION_PARROTS =
        registerSpecies(CompanionParrotSpecies.class, species ->
            registerBird(species.id(), (type, level) -> new CompanionParrotEntity(type, level, species),
                species.width(), species.height(), species.eyeHeight()));

    public static final Item BUDGERIGAR_SPAWN_EGG = registerEgg("budgerigar_spawn_egg", BUDGERIGAR);
    public static final Item COCKATIEL_SPAWN_EGG = registerEgg("cockatiel_spawn_egg", COCKATIEL);
    public static final Item GREEN_CHEEK_CONURE_SPAWN_EGG = registerEgg("green_cheek_conure_spawn_egg", GREEN_CHEEK_CONURE);

    public static final Item LOVEBIRD_SPAWN_EGG = registerEgg("lovebird_spawn_egg", LOVEBIRD);
    public static final Item MONK_PARAKEET_SPAWN_EGG = registerEgg("monk_parakeet_spawn_egg", MONK_PARAKEET);

    public static final Item CAIQUE_SPAWN_EGG = registerEgg("caique_spawn_egg", CAIQUE);
    public static final Item ECLECTUS_SPAWN_EGG = registerEgg("eclectus_spawn_egg", ECLECTUS);
    public static final Item SUN_CONURE_SPAWN_EGG = registerEgg("sun_conure_spawn_egg", SUN_CONURE);

    public static final Item SULFUR_CRESTED_COCKATOO_SPAWN_EGG = registerEgg("sulfur_crested_cockatoo_spawn_egg", SULFUR_CRESTED_COCKATOO);
    public static final Item AFRICAN_GREY_SPAWN_EGG = registerEgg("african_grey_spawn_egg", AFRICAN_GREY);

    public static final Map<MacawSpecies, Item> MACAW_SPAWN_EGGS = registerSpecies(MacawSpecies.class,
        species -> registerEgg(species.entityId() + "_spawn_egg", MACAWS.get(species)));
    public static final Map<CompanionParrotSpecies, Item> COMPANION_PARROT_SPAWN_EGGS =
        registerSpecies(CompanionParrotSpecies.class, species ->
            registerEgg(species.id() + "_spawn_egg", COMPANION_PARROTS.get(species)));

    private static <S extends Enum<S>, V> Map<S, V> registerSpecies(Class<S> speciesClass, Function<S, V> register) {
        Map<S, V> entries = new EnumMap<>(speciesClass);
        for (S species : speciesClass.getEnumConstants()) {
            entries.put(species, register.apply(species));
        }
        return Collections.unmodifiableMap(entries);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static <T extends PetParrotEntity> EntityType<T> registerBird(
        String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight
    ) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(name));
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
            FabricEntityType.Builder.createMob(factory, MobCategory.CREATURE, builder ->
                builder.defaultAttributes(PetParrotEntity::createAttributes)
                    .spawnPlacement(ParrotEcology.PERCH, Heightmap.Types.MOTION_BLOCKING, ParrotEcology::checkSpawn))
                .sized(width, height)
                .eyeHeight(eyeHeight)
                .clientTrackingRange(8)
                .build(key));
        ParrotHatchingEggItem.register(name, type);
        return type;
    }

    private static Item registerEgg(String name, EntityType<?> type) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
    }

    private static void registerCreativeTab(Item guide) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.parapet.main"))
            .icon(guide::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(guide);
                ParapetFurniture.addCreativeEntries(output);
                addEggsInGuideOrder(output, "_hatching_egg");
                addEggsInGuideOrder(output, "_spawn_egg");
            })
            .build());
    }

    private static void addEggsInGuideOrder(CreativeModeTab.Output output, String suffix) {
        for (ParrotEcology species : ParrotEcology.values()) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(species.entityId() + suffix));
            output.accept(BuiltInRegistries.ITEM.getValueOrThrow(key));
        }
    }

    @Override
    public void onInitialize() {
        ParapetFurniture.initialize();
        dev.parapet.interaction.ParrotInteractionMenu.register();
        Item guide = ParrotFieldGuide.initialize();
        registerCreativeTab(guide);
        ParrotMerchant.initialize();
        ParrotEcology.registerSpawns();
    }
}

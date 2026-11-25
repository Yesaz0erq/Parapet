package dev.parapet;

import dev.parapet.block.ParrotBathBlock;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.block.ParrotPlayStandBlock;
import dev.parapet.block.LargeAviaryFixtureBlock;
import dev.parapet.block.ParrotToyBlock;
import dev.parapet.cage.CageBirdCargo;
import dev.parapet.cage.CageFittingsBlockEntity;
import dev.parapet.cage.CageRoost;
import dev.parapet.cage.CageWorkbenchBlock;
import dev.parapet.cage.CageWorkbenchMenu;
import dev.parapet.cage.ParrotCageItem;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ParapetFurniture {
    public static final ParrotCageBlock PARROT_CAGE = cage("parrot_cage", ParrotCageBlock.Size.STANDARD);
    public static final ParrotCageBlock LARGE_PARROT_CAGE = cage("large_parrot_cage", ParrotCageBlock.Size.LARGE);
    public static final ParrotCageBlock DELUXE_PARROT_CAGE = cage("deluxe_parrot_cage", ParrotCageBlock.Size.DELUXE);
    public static final ParrotCageBlock SUPER_DELUXE_PARROT_CAGE = cage("super_deluxe_parrot_cage", ParrotCageBlock.Size.SUPER_DELUXE);
    public static final ParrotCageBlock GIANT_PARROT_CAGE = cage("giant_parrot_cage", ParrotCageBlock.Size.GIANT);
    public static final CageWorkbenchBlock CAGE_WORKBENCH = block("cage_workbench", CageWorkbenchBlock::new, SoundType.WOOD);
    public static final ParrotPlayStandBlock PARROT_PLAY_STAND = block("parrot_play_stand", ParrotPlayStandBlock::new, SoundType.WOOD);
    public static final ParrotBathBlock PARROT_BATH = block("parrot_bath", ParrotBathBlock::new, SoundType.DECORATED_POT);
    public static final LargeAviaryFixtureBlock LARGE_PARROT_PLAY_STAND = block("large_parrot_play_stand",
        p -> new LargeAviaryFixtureBlock(LargeAviaryFixtureBlock.Kind.STAND, p), SoundType.WOOD);
    public static final LargeAviaryFixtureBlock LARGE_PARROT_BATH = block("large_parrot_bath",
        p -> new LargeAviaryFixtureBlock(LargeAviaryFixtureBlock.Kind.BATH, p), SoundType.DECORATED_POT);
    public static final Item FORAGING_BALL = toy("foraging_ball", ParrotToyBlock.Kind.BALL);
    public static final Item ROPE_SWING = toy("rope_swing", ParrotToyBlock.Kind.SWING);
    public static final Item CHEW_BLOCKS = toy("chew_blocks", ParrotToyBlock.Kind.CHEW);
    public static final Item CAGE_GARLAND = item("cage_garland");
    public static final Item HANGING_BELL = item("hanging_bell");
    public static final Item BIRD_FEED = item("bird_feed");
    public static final Item PREMIUM_BIRD_FEED = item("premium_bird_feed");

    public static final BlockEntityType<CageFittingsBlockEntity> CAGE_FITTINGS = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE, Parapet.id("cage_fittings"), new BlockEntityType<>(CageFittingsBlockEntity::new,
            Set.of(PARROT_CAGE, LARGE_PARROT_CAGE, DELUXE_PARROT_CAGE, SUPER_DELUXE_PARROT_CAGE, GIANT_PARROT_CAGE)));

    private ParapetFurniture() {}

    private static ParrotCageBlock cage(String name, ParrotCageBlock.Size size) {
        return block(name, properties -> new ParrotCageBlock(size, properties), SoundType.METAL);
    }

    private static <T extends Block> T block(String name, Function<BlockBehaviour.Properties, T> factory, SoundType sound) {
        var key = ResourceKey.create(Registries.BLOCK, Parapet.id(name));
        T block = Registry.register(BuiltInRegistries.BLOCK, key,
            factory.apply(BlockBehaviour.Properties.of().setId(key).strength(2.0F, 4.0F).sound(sound).noOcclusion()));
        var itemKey = ResourceKey.create(Registries.ITEM, Parapet.id(name));
        var properties = new Item.Properties().setId(itemKey).useBlockDescriptionPrefix();
        BlockItem item = switch (block) {
            case ParrotCageBlock cage -> new ParrotCageItem(cage, properties.stacksTo(16));
            case LargeAviaryFixtureBlock fixture -> new LargeAviaryFixtureBlock.PlacementItem(fixture, properties);
            default -> new BlockItem(block, properties);
        };
        item.registerBlocks(Item.BY_BLOCK, item);
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return block;
    }

    private static Item item(String name) {
        var key = ResourceKey.create(Registries.ITEM, Parapet.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key)));
    }

    private static Item toy(String name, ParrotToyBlock.Kind kind) {
        return block(name, p -> new ParrotToyBlock(kind, p), SoundType.WOOD).asItem();
    }

    public static void initialize() {
        CageWorkbenchMenu.register();
        CageBirdCargo.initialize();
        CageRoost.registerInteractions();
    }

    static void addCreativeEntries(CreativeModeTab.Output output) {
        output.accept(BIRD_FEED);
        output.accept(PREMIUM_BIRD_FEED);
        output.accept(CAGE_WORKBENCH);
        output.accept(PARROT_CAGE);
        output.accept(LARGE_PARROT_CAGE);
        output.accept(DELUXE_PARROT_CAGE);
        output.accept(SUPER_DELUXE_PARROT_CAGE);
        output.accept(GIANT_PARROT_CAGE);
        output.accept(PARROT_PLAY_STAND);
        output.accept(LARGE_PARROT_PLAY_STAND);
        output.accept(PARROT_BATH);
        output.accept(LARGE_PARROT_BATH);
        output.accept(FORAGING_BALL);
        output.accept(ROPE_SWING);
        output.accept(CHEW_BLOCKS);
        output.accept(CAGE_GARLAND);
        output.accept(HANGING_BELL);
    }
}

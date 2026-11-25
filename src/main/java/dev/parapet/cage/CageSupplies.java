package dev.parapet.cage;

import dev.parapet.ParapetFurniture;
import dev.parapet.block.ParrotWaterBowlBlock;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.item.BirdFeedBagItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public final class CageSupplies {
    public static final int FEED_CAPACITY = 4;

    private CageSupplies() {}

    public static boolean accepts(ItemStack stack) {
        return ParapetFurniture.isBirdFeed(stack) || stack.is(ParapetFurniture.WATER_BOWL.asItem())
            || stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET);
    }

    public static void interact(ServerLevel level, CageFittingsBlockEntity tray, List<ItemStack> contents,
                                PetParrotEntity resident, Player player, InteractionHand hand) {
        if (resident != null && !resident.isOwnedBy(player)) {
            player.sendOverlayMessage(Component.translatable("message.parapet.cage.supplies_owner_only"));
            return;
        }
        ItemStack held = player.getItemInHand(hand);
        if (ParapetFurniture.isBirdFeed(held)) {
            fillFeeder(level, tray, contents, player, hand, held);
            return;
        }
        ItemStack bowl = first(contents, ParapetFurniture.WATER_BOWL.asItem());
        if (held.is(ParapetFurniture.WATER_BOWL.asItem())) {
            if (!bowl.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("message.parapet.cage.already_installed", bowl.getHoverName()));
                return;
            }
            ItemStack installed = held.copyWithCount(1);
            contents.add(installed);
            held.consume(1, player);
            commit(level, tray, contents, player, SoundEvents.DECORATED_POT_PLACE, GameEvent.BLOCK_CHANGE);
            player.sendOverlayMessage(Component.translatable("message.parapet.cage.installed", installed.getHoverName()));
            return;
        }
        if (bowl.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.parapet.cage.bowl_needed"));
            return;
        }
        boolean filling = held.is(Items.WATER_BUCKET);
        if (bowlHasWater(bowl) == filling) return;
        setWater(bowl, filling);
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(filling ? Items.BUCKET : Items.WATER_BUCKET)));
        commit(level, tray, contents, player, filling ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL,
            filling ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP);
    }

    private static void fillFeeder(ServerLevel level, CageFittingsBlockEntity tray, List<ItemStack> contents,
                                   Player player, InteractionHand hand, ItemStack held) {
        boolean bag = held.getItem() instanceof BirdFeedBagItem;
        Item feed = ParapetFurniture.isPremiumFeed(held) ? ParapetFurniture.PREMIUM_BIRD_FEED : ParapetFurniture.BIRD_FEED;
        int count = contents.stream().filter(s -> s.is(feed)).mapToInt(ItemStack::getCount).sum();
        if (count < FEED_CAPACITY) {
            ItemStack serving = bag ? new ItemStack(feed) : held.copyWithCount(1);
            // Keep named portions and other per-stack components intact when the feeder is removed.
            var matching = contents.stream().filter(s -> ItemStack.isSameItemSameComponents(s, serving)).findFirst();
            if (matching.isPresent()) matching.get().grow(1);
            else contents.add(serving);
            if (bag) BirdFeedBagItem.serve(player, hand, held);
            else held.consume(1, player);
            commit(level, tray, contents, player, SoundEvents.GRASS_PLACE, GameEvent.BLOCK_CHANGE);
            count++;
        }
        player.sendOverlayMessage(Component.translatable("message.parapet.cage.feed_stock", feed.getDefaultInstance().getHoverName(), count, FEED_CAPACITY));
    }

    private static void commit(ServerLevel level, CageFittingsBlockEntity tray, List<ItemStack> contents,
                               Player player, SoundEvent sound, Holder<GameEvent> event) {
        tray.setFittings(ItemContainerContents.fromItems(contents));
        level.playSound(null, tray.getBlockPos(), sound, SoundSource.BLOCKS, 0.6F, 1.1F);
        level.gameEvent(player, event, tray.getBlockPos());
    }

    private static ItemStack first(List<ItemStack> contents, Item type) {
        return contents.stream().filter(stack -> stack.is(type)).findFirst().orElse(ItemStack.EMPTY);
    }

    public static boolean bowlHasWater(ItemStack bowl) {
        return Boolean.TRUE.equals(bowl.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(ParrotWaterBowlBlock.FILLED));
    }

    private static void setWater(ItemStack bowl, boolean filled) {
        var state = bowl.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        if (filled) {
            bowl.set(DataComponents.BLOCK_STATE, state.with(ParrotWaterBowlBlock.FILLED, true));
        } else {
            var remaining = new HashMap<>(state.properties());
            remaining.remove(ParrotWaterBowlBlock.FILLED.getName());
            if (remaining.isEmpty()) bowl.remove(DataComponents.BLOCK_STATE);
            else bowl.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(remaining));
        }
    }

    static void offerMeal(CageFittingsBlockEntity tray, PetParrotEntity bird, boolean drink) {
        var contents = new ArrayList<>(tray.fittings().nonEmptyItemCopyStream().toList());
        if (drink) {
            var bowl = first(contents, ParapetFurniture.WATER_BOWL.asItem());
            if (!bowl.isEmpty() && bowlHasWater(bowl)) bird.beginCageMeal(bowl);
            return;
        }
        var serving = first(contents, ParapetFurniture.BIRD_FEED);
        if (serving.isEmpty()) serving = first(contents, ParapetFurniture.PREMIUM_BIRD_FEED);
        if (serving.isEmpty() || !bird.beginCageMeal(serving)) return;
        serving.shrink(1);
        contents.removeIf(ItemStack::isEmpty);
        tray.setFittings(ItemContainerContents.fromItems(contents));
        if (bird.getHealth() < bird.getMaxHealth()) bird.heal(1.0F);
    }

    static void playMealContact(ServerLevel level, PetParrotEntity bird, Vec3 target) {
        CageMeal meal = bird.getCageMeal();
        if (meal == CageMeal.HAND) return;
        boolean water = meal == CageMeal.WATER;
        var particle = water ? ParticleTypes.SPLASH : new ItemParticleOption(ParticleTypes.ITEM,
            meal == CageMeal.SEED ? ParapetFurniture.BIRD_FEED : ParapetFurniture.PREMIUM_BIRD_FEED);
        bird.playSound(water ? SoundEvents.GENERIC_DRINK.value() : SoundEvents.PARROT_EAT, 0.45F, 1.25F);
        level.sendParticles(particle, target.x, target.y + 0.02, target.z, 3, 0.035, 0.02, 0.035, 0.009);
    }
}

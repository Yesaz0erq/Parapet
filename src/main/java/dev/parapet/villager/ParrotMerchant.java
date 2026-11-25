package dev.parapet.villager;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import dev.parapet.Parapet;
import dev.parapet.ParapetFurniture;
import dev.parapet.guide.ParrotFieldGuide;
import dev.parapet.cage.CageBirdCargo;
import dev.parapet.cage.ParrotCageItem;
import dev.parapet.entity.ParrotEcology;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class ParrotMerchant {
    public static final ResourceKey<VillagerProfession> PROFESSION =
        ResourceKey.create(Registries.VILLAGER_PROFESSION, Parapet.id("parrot_merchant"));
    private static final ResourceKey<PoiType> WORKSTATION =
        ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Parapet.id("cage_workbench"));
    private static final AttachmentType<Long> NEXT_BUYBACK =
        AttachmentRegistry.createPersistent(Parapet.id("next_parrot_buyback"), Codec.LONG);
    private static final int BUYBACK_COOLDOWN = 100;

    private ParrotMerchant() {}

    public static void initialize() {
        PoiTypes.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, WORKSTATION,
            Set.copyOf(ParapetFurniture.CAGE_WORKBENCH.getStateDefinition().getPossibleStates()), 1, 1);
        var levels = new Int2ObjectOpenHashMap<ResourceKey<TradeSet>>();
        for (int level = 1; level <= 5; level++) {
            levels.put(level, ResourceKey.create(Registries.TRADE_SET, Parapet.id("parrot_merchant/level_" + level)));
        }
        Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, PROFESSION,
            new VillagerProfession(Component.translatable("entity.minecraft.villager.parapet.parrot_merchant"),
                site -> site.is(WORKSTATION), site -> site.is(WORKSTATION), ImmutableSet.of(), ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_FLETCHER, levels));
        UseEntityCallback.EVENT.register(ParrotMerchant::interact);
    }

    private static boolean carriesBird(ItemStack stack) {
        return stack.getItem() instanceof ParrotCageItem && CageBirdCargo.hasBird(stack);
    }

    private static InteractionResult interact(Player player, Level level, InteractionHand hand, Entity target, EntityHitResult hit) {
        if (player.isSpectator() || !(target instanceof Villager villager) || !villager.isAlive() || villager.isBaby()
            || !villager.getVillagerData().profession().is(PROFESSION)) {
            return InteractionResult.PASS;
        }
        // Empty main hand would otherwise open the vanilla menu before checking a carried offhand cage.
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && carriesBird(player.getOffhandItem())) {
            hand = InteractionHand.OFF_HAND;
        }
        ItemStack cage = player.getItemInHand(hand);
        if (!carriesBird(cage)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server && player instanceof ServerPlayer buyer) {
            buyBird(server, buyer, hand, cage, villager);
        }
        return InteractionResult.SUCCESS;
    }

    private static void buyBird(ServerLevel level, ServerPlayer player, InteractionHand hand, ItemStack cage, Villager villager) {
        if (villager.isSleeping() || villager.getTradingPlayer() != null) {
            message(player, "busy");
            return;
        }
        if (!CageBirdCargo.isOwnedBy(cage, player)) {
            message(player, "not_owner");
            return;
        }
        if (!CageBirdCargo.isAdult(cage)) {
            message(player, "juvenile");
            return;
        }
        var ecology = CageBirdCargo.entityTypeId(cage).flatMap(ParrotEcology::forEntityId);
        if (cage.getCount() != 1 || ecology.isEmpty()) {
            message(player, "invalid");
            return;
        }
        long now = level.getGameTime();
        Long next = villager.getAttached(NEXT_BUYBACK);
        if (next != null && now < next) {
            message(player, "cooldown", (next - now + 19) / 20);
            return;
        }
        int price = switch (ecology.get().size()) {
            case SMALL -> 4;
            case MEDIUM -> 8;
            case LARGE -> 14;
        };
        int xp = switch (ecology.get().size()) {
            case SMALL -> 3;
            case MEDIUM -> 6;
            case LARGE -> 10;
        };
        ItemStack payment = new ItemStack(Items.EMERALD, price);
        if (!canReceive(player, payment)) {
            message(player, "inventory_full");
            return;
        }

        // Redeem the unique world record before changing the cage or crediting either party.
        ItemStack emptyCage = CageBirdCargo.emptyCage(cage);
        if (emptyCage.isEmpty() || !CageBirdCargo.redeem(level, cage, player)) {
            message(player, "invalid");
            return;
        }
        CageBirdCargo.entityTypeId(cage).ifPresent(id -> ParrotFieldGuide.recordSpecies(player, id));
        player.setItemInHand(hand, emptyCage);
        player.getInventory().placeItemBackInInventory(payment.copy(), Prediction.SERVER_ONLY);
        villager.setAttached(NEXT_BUYBACK, now + BUYBACK_COOLDOWN);
        // A fresh villager needs the current tier's offers rolled before it can gain a level.
        villager.getOffers();
        villager.setTradingPlayer(player);
        try {
            villager.notifyTrade(new MerchantOffer(new ItemCost(cage.getItem()), payment, 1, xp, 0F));
        } finally {
            villager.setTradingPlayer(null);
        }
        player.awardStat(Stats.TRADED_WITH_VILLAGER);
        message(player, "bought", price, xp);
    }

    private static boolean canReceive(Player player, ItemStack payment) {
        int space = 0;
        for (ItemStack slot : player.getInventory().getNonEquipmentItems()) {
            if (slot.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(slot, payment)) {
                space += Math.max(0, payment.getMaxStackSize() - slot.getCount());
                if (space >= payment.getCount()) return true;
            }
        }
        return false;
    }

    private static void message(Player player, String key, Object... arguments) {
        player.sendOverlayMessage(Component.translatable("message.parapet.parrot_merchant." + key, arguments));
    }
}

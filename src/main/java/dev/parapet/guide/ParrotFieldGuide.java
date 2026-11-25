package dev.parapet.guide;

import com.mojang.serialization.Codec;
import dev.parapet.Parapet;
import dev.parapet.cage.CageBirdCargo;
import dev.parapet.entity.PetParrotEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class ParrotFieldGuide extends Item {
    private static final AttachmentType<Set<String>> COLLECTION = AttachmentRegistry.create(
        Parapet.id("parrot_collection"), builder -> builder.copyOnDeath().persistent(
            Codec.STRING.listOf().xmap(Set::copyOf, ids -> ids.stream().sorted().toList())));

    public record Open(List<String> species) implements CustomPacketPayload {
        public static final Type<Open> TYPE = new Type<>(Parapet.id("open_field_guide"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128).apply(ByteBufCodecs.list(256)), Open::species, Open::new);
        @Override public Type<Open> type() { return TYPE; }
    }

    private ParrotFieldGuide(Properties properties) { super(properties); }

    public static Item initialize() {
        var key = ResourceKey.create(Registries.ITEM, Parapet.id("parrot_field_guide"));
        Item guide = Registry.register(BuiltInRegistries.ITEM, key, new ParrotFieldGuide(new Properties().setId(key).stacksTo(1)));
        PayloadTypeRegistry.clientboundPlay().register(Open.TYPE, Open.CODEC);
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof PetParrotEntity bird && bird.isTame() && bird.isOwnedBy(player)) record(player, bird, false);
        });
        return guide;
    }

    public static void record(ServerPlayer player, PetParrotEntity bird, boolean announce) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(bird.getType());
        if (recordSpecies(player, id) && announce) {
            player.sendOverlayMessage(Component.translatable("message.parapet.guide.collected", bird.getType().getDescription()));
        }
    }

    public static boolean recordSpecies(ServerPlayer player, Identifier id) {
        if (!id.getNamespace().equals("parapet")) return false;
        Set<String> known = player.getAttachedOrElse(COLLECTION, Set.of());
        if (known.contains(id.getPath())) return false;
        Set<String> updated = new HashSet<>(known);
        updated.add(id.getPath());
        player.setAttached(COLLECTION, Set.copyOf(updated));
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer owner) {
            // Also recover owned birds already packed in a cage before upgrading this world.
            for (var stack : owner.getInventory().getNonEquipmentItems()) {
                CageBirdCargo.verifiedOwnedType(owner.level(), stack, owner).ifPresent(id -> recordSpecies(owner, id));
            }
            CageBirdCargo.verifiedOwnedType(owner.level(), owner.getOffhandItem(), owner).ifPresent(id -> recordSpecies(owner, id));
            ServerPlayNetworking.send(owner, new Open(owner.getAttachedOrElse(COLLECTION, Set.of()).stream().sorted().toList()));
            owner.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.SUCCESS;
    }
}

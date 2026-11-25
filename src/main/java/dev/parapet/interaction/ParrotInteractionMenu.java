package dev.parapet.interaction;

import dev.parapet.Parapet;
import dev.parapet.entity.PetParrotEntity;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** The server binds a menu session directly to the bird the owner interacted with. */
public final class ParrotInteractionMenu extends AbstractContainerMenu {
    public static final MenuType<ParrotInteractionMenu> TYPE = new MenuType<>(ParrotInteractionMenu::new, FeatureFlags.VANILLA_SET);
    public enum Availability { READY, CAGED, BUSY, COOLING_DOWN }

    public record Profile(int menuId, int birdId, long session, String carried) implements CustomPacketPayload {
        public static final Type<Profile> TYPE = new Type<>(Parapet.id("interaction_profile"));
        public static final StreamCodec<ByteBuf, Profile> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Profile::menuId, ByteBufCodecs.VAR_INT, Profile::birdId,
            ByteBufCodecs.LONG, Profile::session, ByteBufCodecs.stringUtf8(128), Profile::carried, Profile::new);
        @Override public Type<Profile> type() { return TYPE; }
    }

    public record Choose(int menuId, long session, int action) implements CustomPacketPayload {
        public static final Type<Choose> TYPE = new Type<>(Parapet.id("interaction_choose"));
        public static final StreamCodec<ByteBuf, Choose> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Choose::menuId, ByteBufCodecs.LONG, Choose::session,
            ByteBufCodecs.VAR_INT, Choose::action, Choose::new);
        @Override public Type<Choose> type() { return TYPE; }
    }

    private static final ParrotTrick[] TRICKS = ParrotTrick.values();
    private final SimpleContainerData data = new SimpleContainerData(3);
    private final PetParrotEntity bird;
    private final long session;
    private long lastRequestTick = Long.MIN_VALUE;
    private Profile profile;

    private ParrotInteractionMenu(int id, Inventory inventory) {
        this(id, null, 0L);
    }

    private ParrotInteractionMenu(int id, PetParrotEntity bird, long session) {
        super(TYPE, id);
        this.bird = bird;
        this.session = session;
        addDataSlots(data);
        updateData();
    }

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, Parapet.id("parrot_interaction"), TYPE);
        PayloadTypeRegistry.clientboundPlay().register(Profile.TYPE, Profile.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Choose.TYPE, Choose.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Choose.TYPE, (request, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof ParrotInteractionMenu menu && request.menuId() == menu.containerId
                && request.session() == menu.session && request.action() >= 0 && request.action() < TRICKS.length) {
                menu.choose(player, TRICKS[request.action()]);
            }
        });
    }

    public static void open(ServerPlayer owner, PetParrotEntity bird) {
        if (!canReach(owner, bird)) return;
        long session = owner.getRandom().nextLong();
        owner.openMenu(new SimpleMenuProvider((id, inventory, player) -> new ParrotInteractionMenu(id, bird, session),
            bird.getDisplayName())).ifPresent(id -> {
                String carried = bird.getCarriedPlumage();
                ServerPlayNetworking.send(owner, new Profile(id, bird.getId(), session,
                    carried.equals(bird.getPlumageId()) ? "" : carried));
            });
    }

    private void choose(ServerPlayer player, ParrotTrick action) {
        long now = player.level().getGameTime();
        if (bird == null || lastRequestTick == now) return;
        lastRequestTick = now;
        if (!stillValid(player)) {
            player.closeContainer();
        } else if (player.getMainHandItem().isEmpty() && bird.tryOwnerInteraction(player, action)) {
            player.closeContainer();
        }
    }

    private static boolean canReach(Player player, PetParrotEntity bird) {
        return player.isAlive() && !player.isSpectator() && bird.isAlive() && bird.isTame() && bird.isOwnedBy(player)
            && player.level() == bird.level() && player.distanceToSqr(bird) <= 20.25 && player.hasLineOfSight(bird);
    }

    @Override public boolean stillValid(Player player) {
        return bird == null ? player.level().isClientSide() : canReach(player, bird);
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override public void broadcastChanges() {
        updateData();
        super.broadcastChanges();
    }

    private void updateData() {
        if (bird != null) {
            data.set(0, bird.getAffinity());
            data.set(1, bird.ownerInteractionAvailability().ordinal());
            data.set(2, (bird.ownerInteractionCooldown() + 19) / 20);
        }
    }

    public int affinity() { return data.get(0); }
    public Availability availability() { return Availability.values()[data.get(1)]; }
    public int cooldownSeconds() { return data.get(2); }
    public Profile profile() { return profile; }

    public void acceptProfile(Profile received) {
        if (received.menuId() == containerId && bird == null) profile = received;
    }
}

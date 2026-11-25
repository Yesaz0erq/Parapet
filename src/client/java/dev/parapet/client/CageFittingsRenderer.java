package dev.parapet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.parapet.Parapet;
import dev.parapet.ParapetFurniture;
import dev.parapet.block.BirdFeedPileBlock;
import dev.parapet.cage.CageLayout;
import dev.parapet.cage.CageMeal;
import dev.parapet.cage.CageSupplies;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.cage.CageToy;
import java.util.EnumMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import dev.parapet.cage.CageFittingsBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class CageFittingsRenderer implements BlockEntityRenderer<CageFittingsBlockEntity, CageFittingsRenderer.State> {
    static final class State extends BlockEntityRenderState {
        Fittings fitted;
        ItemStackRenderState bowl;
        ParrotCageBlock.Size size;
        float rotation;
        boolean dirty;
    }

    private static final class Fittings {
        final EnumMap<CageToy, ItemStackRenderState> toys = new EnumMap<>(CageToy.class);

        Fittings() {
            for (CageToy toy : CageToy.values()) toys.put(toy, new ItemStackRenderState());
        }
        final ItemStackRenderState bowl = new ItemStackRenderState();
        final BlockModelRenderState seed = new BlockModelRenderState();
        final BlockModelRenderState premium = new BlockModelRenderState();
        ItemStack bowlStack = ItemStack.EMPTY;
        net.minecraft.world.item.component.ItemContainerContents contents;
    }

    private final java.util.Map<CageFittingsBlockEntity, Fittings> cache = new java.util.WeakHashMap<>();
    private static final Identifier SUSPENDED_SWING = Parapet.id("cage_fitting/rope_swing");
    private static final Identifier CLIPPED_BLOCKS = Parapet.id("cage_fitting/chew_blocks");
    private static final BlockDisplayContext BLOCK_DISPLAY = BlockDisplayContext.create();
    private final ItemModelResolver models;
    private final BlockModelResolver blocks;

    public CageFittingsRenderer(BlockEntityRendererProvider.Context context) {
        models = context.itemModelResolver();
        blocks = context.blockModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public boolean shouldRender(CageFittingsBlockEntity entity, Vec3 camera) {
        return entity.getBlockState().getValue(ParrotCageBlock.PART) == ParrotCageBlock.Part.BASE
            && (entity.needsCleaning() || entity.fittings().size() > 0) && BlockEntityRenderer.super.shouldRender(entity, camera);
    }

    @Override
    public void extractRenderState(CageFittingsBlockEntity entity, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, overlay);
        var block = entity.getBlockState();
        state.size = ((ParrotCageBlock) block.getBlock()).size();
        state.dirty = entity.needsCleaning();
        state.rotation = 180 - block.getValue(ParrotCageBlock.FACING).toYRot();
        Fittings fitted = cache.get(entity);
        if (fitted != null && fitted.contents == entity.fittings()) {
            state.fitted = fitted;
            state.bowl = fitted.bowl;
            if (fitted.bowl.isAnimated()) {
                state.bowl = new ItemStackRenderState();
                models.updateForTopItem(state.bowl, fitted.bowlStack, ItemDisplayContext.NONE, entity.getLevel(), null, 0);
            }
            return;
        }
        fitted = new Fittings();
        state.fitted = fitted;
        bake(entity, fitted);
        state.bowl = fitted.bowl;
        if (fitted.toys.values().stream().noneMatch(ItemStackRenderState::isAnimated)) {
            fitted.contents = entity.fittings();
            cache.put(entity, fitted);
        } else cache.remove(entity);
    }

    private void bake(CageFittingsBlockEntity entity, Fittings state) {
        int seedCount = 0;
        int premiumCount = 0;
        for (var fitting : entity.fittings().nonEmptyItems()) {
            if (fitting.is(ParapetFurniture.BIRD_FEED)) {
                seedCount += Math.min(CageSupplies.FEED_CAPACITY, fitting.count());
            } else if (fitting.is(ParapetFurniture.PREMIUM_BIRD_FEED)) {
                premiumCount += Math.min(CageSupplies.FEED_CAPACITY, fitting.count());
            } else if (fitting.is(ParapetFurniture.WATER_BOWL.asItem())) {
                state.bowlStack = fitting.create();
                models.updateForTopItem(state.bowl, state.bowlStack, ItemDisplayContext.NONE, entity.getLevel(), null, 0);
            } else {
                ItemStack stack = fitting.create();
                CageToy.forItem(stack).ifPresent(toy -> {
                    // Only the extracted copy selects a cage-specific mounting; the physical item
                    // retains its ground stand when returned, dropped or displayed in a player's hand.
                    if (toy == CageToy.ROPE_SWING) stack.set(DataComponents.ITEM_MODEL, SUSPENDED_SWING);
                    if (toy == CageToy.CHEW_BLOCKS) stack.set(DataComponents.ITEM_MODEL, CLIPPED_BLOCKS);
                    models.updateForTopItem(state.toys.get(toy), stack, ItemDisplayContext.NONE, entity.getLevel(), null, 0);
                });
            }
        }
        updatePile(state.seed, ParapetFurniture.BIRD_FEED_PILE, seedCount);
        updatePile(state.premium, ParapetFurniture.PREMIUM_BIRD_FEED_PILE, premiumCount);
    }

    private void updatePile(BlockModelRenderState model, BirdFeedPileBlock pile, int count) {
        if (count == 0) {
            model.clear();
        } else {
            var filled = pile.defaultBlockState().setValue(BirdFeedPileBlock.PORTIONS,
                Math.min(CageSupplies.FEED_CAPACITY, count));
            blocks.update(model, filled, BLOCK_DISPLAY);
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        var size = state.size;
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.rotateDegrees(Axis.YP, state.rotation);
        poses.translate(-0.5, 0, -0.5);
        if (state.dirty) {
            poses.pushPose();
            poses.translate((size.width - 1) * 0.5, 0, (size.depth - 1) * 0.5);
            int light = state.lightCoords;
            collector.submitCustomGeometry(poses, RenderTypes.entityCutout(Parapet.id("textures/entity/cage_tray_litter.png")),
                (pose, vertices) -> {
                    trayCorner(vertices, pose, 0, 0, light);
                    trayCorner(vertices, pose, 0, 1, light);
                    trayCorner(vertices, pose, 1, 1, light);
                    trayCorner(vertices, pose, 1, 0, light);
                });
            poses.popPose();
        }
        for (var fitting : state.fitted.toys.entrySet()) {
            if (fitting.getValue().isEmpty()) continue;
            var mount = CageLayout.toyPlacement(size, fitting.getKey());
            poses.pushPose();
            poses.translate(mount.anchor());
            poses.rotateDegrees(Axis.YP, mount.yaw());
            poses.scale(mount.scale(), mount.scale(), mount.scale());
            var local = mount.mount().modelAnchor;
            poses.translate(-local.x, -local.y, -local.z);
            fitting.getValue().submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
        float dishScale = CageLayout.dishScale(size);
        Vec3 water = CageLayout.mealPosition(size, CageMeal.WATER);
        draw(state.bowl, poses, collector, state.lightCoords, new Vec3(water.x, CageLayout.shelfHeight(size), water.z), dishScale);
        drawPile(state.fitted.seed, poses, collector, state.lightCoords, size, CageMeal.SEED);
        drawPile(state.fitted.premium, poses, collector, state.lightCoords, size, CageMeal.PREMIUM);
        poses.popPose();
    }

    private static void trayCorner(VertexConsumer vertices, PoseStack.Pose pose, int u, int v, int light) {
        vertices.addVertex(pose, u, 2.05F / 16F, v).setColor(-1).setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private static void draw(ItemStackRenderState model, PoseStack poses, SubmitNodeCollector collector, int light,
                             Vec3 base, float scale) {
        if (model.isEmpty()) return;
        poses.pushPose();
        // ItemStackRenderState centres geometry around (0,0,0); keep its bottom on the support.
        poses.translate(base.x, base.y + .5 * scale, base.z);
        poses.scale(scale, scale, scale);
        model.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }

    private static void drawPile(BlockModelRenderState model, PoseStack poses, SubmitNodeCollector collector, int light,
                                 ParrotCageBlock.Size size, CageMeal meal) {
        if (model.isEmpty()) return;
        float scale = CageLayout.dishScale(size);
        Vec3 target = CageLayout.mealPosition(size, meal);
        poses.pushPose();
        // Baked block models start at the lower corner; the dish interiors are centred on the target.
        poses.translate(target.x - .5 * scale, CageLayout.shelfHeight(size) + .10 * scale, target.z - .5 * scale);
        poses.scale(scale, scale, scale);
        model.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }
}

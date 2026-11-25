package dev.parapet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.block.ParrotToyBlock;
import dev.parapet.cage.CageFittingsBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class CageFittingsRenderer implements BlockEntityRenderer<CageFittingsBlockEntity, CageFittingsRenderer.State> {
    static final class State extends BlockEntityRenderState {
        final ItemStackRenderState ball = new ItemStackRenderState();
        final ItemStackRenderState swing = new ItemStackRenderState();
        final ItemStackRenderState chew = new ItemStackRenderState();
        ParrotCageBlock.Size size;
        float rotation;
    }

    private final ItemModelResolver models;

    public CageFittingsRenderer(BlockEntityRendererProvider.Context context) {
        models = context.itemModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public boolean shouldRender(CageFittingsBlockEntity entity, Vec3 camera) {
        return entity.getBlockState().getValue(ParrotCageBlock.PART) == ParrotCageBlock.Part.BASE
            && entity.fittings().size() > 0 && BlockEntityRenderer.super.shouldRender(entity, camera);
    }

    @Override
    public void extractRenderState(CageFittingsBlockEntity entity, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, overlay);
        var block = entity.getBlockState();
        state.size = ((ParrotCageBlock) block.getBlock()).size();
        state.rotation = 180 - block.getValue(ParrotCageBlock.FACING).toYRot();
        state.ball.clear();
        state.swing.clear();
        state.chew.clear();
        entity.fittings().nonEmptyItemCopyStream().forEach(stack -> {
            if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ParrotToyBlock toy) {
                var model = switch (toy.kind()) {
                    case BALL -> state.ball;
                    case SWING -> state.swing;
                    case CHEW -> state.chew;
                };
                models.updateForTopItem(model, stack, ItemDisplayContext.NONE, entity.getLevel(), null, 0);
            }
        });
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        var size = state.size;
        float scale = size == ParrotCageBlock.Size.STANDARD ? 0.36F : 0.72F;
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.rotateDegrees(Axis.YP, state.rotation);
        poses.translate(-0.5, 0, -0.5);
        double centerY = 0.07 + scale / 2;
        draw(state.ball, poses, collector, state.lightCoords, size.width * 0.23, centerY, size.depth * 0.24, scale);
        draw(state.swing, poses, collector, state.lightCoords, size.width * 0.27, centerY, size.depth * 0.76, scale);
        draw(state.chew, poses, collector, state.lightCoords, size.width * 0.78, centerY, size.depth * 0.76, scale);
        poses.popPose();
    }

    private static void draw(ItemStackRenderState model, PoseStack poses, SubmitNodeCollector collector, int light,
                             double x, double y, double z, float scale) {
        if (model.isEmpty()) return;
        poses.pushPose();
        poses.translate(x, y, z);
        poses.scale(scale, scale, scale);
        model.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }
}

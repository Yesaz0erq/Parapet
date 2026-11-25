package dev.parapet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.parapet.Parapet;
import dev.parapet.entity.LovebirdEntity;
import dev.parapet.entity.LovebirdPlumages;
import dev.parapet.entity.MacawEntity;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.entity.SunConureEntity;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.level.BaseSpawner;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** An unspawned adult posed by the same renderer as a bird in the world. */
final class FieldGuideSpecimen {
    private static final float PLATE_FILL = 0.77F;
    private static final Identifier INK = Parapet.id("textures/gui/field_guide_ink.png");
    private static final Quaternionf CAMERA = new Quaternionf().rotationX(-0.10F).rotateY(-0.83F);
    private static final Quaternionf PORTRAIT = new Quaternionf().rotationZ((float) Math.PI).mul(CAMERA);
    private final PetParrotEntity bird;
    private final String directory;
    private final List<String> plumages;
    private Frame frame;
    private int variant;

    FieldGuideSpecimen(Minecraft client, String id, boolean collected) {
        bird = (PetParrotEntity) BuiltInRegistries.ENTITY_TYPE.getOptional(Parapet.id(id)).orElseThrow()
            .create(client.level, new EntitySpawnRequest(EntitySpawnReason.SPAWNER, true));
        // Like a vanilla spawner preview, this bird needs a renderer-safe ID without joining the world.
        BaseSpawner.SET_DISPLAY_ENTITY_ID.process(bird);
        directory = "textures/entity/" + (bird instanceof MacawEntity ? "macaw" : id) + "/";
        // An unrecorded entry never needs to enumerate or bind its coloured texture.
        var palette = new LinkedHashSet<String>();
        palette.add(bird.getVisualPlumageId());
        if (collected && bird instanceof LovebirdEntity) {
            palette.addAll(LovebirdPlumages.IDS);
        } else if (collected && !(bird instanceof MacawEntity) && !(bird instanceof SunConureEntity)) {
            client.getResourceManager().listResources(directory.substring(0, directory.length() - 1),
                resource -> resource.getNamespace().equals(Parapet.MOD_ID) && resource.getPath().endsWith(".png"))
                .keySet().stream().map(Identifier::getPath)
                .map(path -> path.substring(directory.length(), path.length() - 4))
                .filter(path -> !path.contains("/")).sorted().forEach(palette::add);
        }
        plumages = List.copyOf(palette);
    }

    int count() { return plumages.size(); }
    int index() { return variant; }
    String color() { return plumages.get(variant); }
    void cycle(int delta) { variant = Math.floorMod(variant + delta, plumages.size()); }

    void draw(Minecraft client, GuiGraphicsExtractor gui, boolean collected, int left, int top, int right, int bottom,
              float age) {
        var renderer = client.getEntityRenderDispatcher().getRenderer(bird);
        var state = (PetParrotRenderer.State) renderer.createRenderState(bird, 1F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        state.nameTag = null;
        state.scale = 1F;
        state.bodyRot = 180F;
        state.xRot = 0F;
        state.yRot = 0F;
        state.ageInTicks = age;
        // Each extracted GUI state owns this choice. World entities and their renderer keep their normal textures.
        state.texture = collected ? Parapet.id(directory + color() + ".png") : INK;
        if (frame == null) {
            frame = measure((PetParrotRenderer<?>) renderer, state);
        }
        float pixelsPerBlock = Math.min((right - left) / frame.width, (bottom - top) / frame.height) * PLATE_FILL;
        gui.entity(state, pixelsPerBlock, frame.offset, PORTRAIT, CAMERA, left, top, right, bottom);
    }

    private record Frame(Vector3f offset, float width, float height) {}

    private static Frame measure(PetParrotRenderer<?> renderer, PetParrotRenderer.State state) {
        // Match the standing LivingEntityRenderer transforms and the inventory camera.
        // Measuring the actual cubes lets a macaw's long tail and a cockatoo's crest both fit.
        var pose = new PoseStack();
        pose.rotate(PORTRAIT);
        pose.scale(-1F, -1F, 1F);
        renderer.scale(state, pose);
        pose.translate(0F, -1.501F, 0F);
        var model = renderer.getModel();
        model.setupAnim(state);
        var bounds = new Bounds();
        model.root().visit(pose, bounds::visit);
        return new Frame(new Vector3f(-(bounds.minX + bounds.maxX) / 2, -(bounds.minY + bounds.maxY) / 2, 0),
            Math.max(0.02F, bounds.maxX - bounds.minX), Math.max(0.02F, bounds.maxY - bounds.minY));
    }

    private static final class Bounds {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        final Vector3f point = new Vector3f();

        void visit(PoseStack.Pose pose, String path, int index, ModelPart.Cube cube) {
            for (int corner = 0; corner < 8; corner++) {
                float x = (corner & 1) == 0 ? cube.minX : cube.maxX;
                float y = (corner & 2) == 0 ? cube.minY : cube.maxY;
                float z = (corner & 4) == 0 ? cube.minZ : cube.maxZ;
                pose.pose().transformPosition(x / 16F, y / 16F, z / 16F, point);
                minX = Math.min(minX, point.x);
                maxX = Math.max(maxX, point.x);
                minY = Math.min(minY, point.y);
                maxY = Math.max(maxY, point.y);
            }
        }
    }
}

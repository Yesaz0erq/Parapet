package dev.parapet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.parapet.cage.CageLayout;
import dev.parapet.cage.CageMeal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Fits the authored peck to the actual bill, shelf and two connected wooden perches. */
final class CageFeedingPose {
    private static final int SAMPLES = 17;
    private static final float STEP = 0.09F;
    private final float[] billHeight = new float[SAMPLES];
    private final float[] billReach = new float[SAMPLES];
    private float tailLength;

    CageFeedingPose(ModelPart root, ModelPart head, KeyframeAnimation feeding) {
        // All rigs keep the bill boxes on the head itself. Use the lowest edge of the foremost hook.
        List<ModelPart.Cube> cubes = new ArrayList<>();
        head.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && cube.minX < 0 && cube.maxX > 0) cubes.add(cube);
        });
        float front = cubes.stream().map(cube -> cube.minZ).min(Float::compare).orElseThrow();
        var hook = cubes.stream().filter(cube -> cube.minZ <= front + 0.27F)
            .max(java.util.Comparator.comparingDouble(cube -> cube.maxY)).orElseThrow();
        var bones = root.getAllParts();
        var rotation = new Quaternionf();
        var tip = new Vector3f();
        // Measure each rig once. Interpolation during rendering also handles chicks and other scale modifiers.
        for (int i = 0; i < SAMPLES; i++) {
            billHeight[i] = Float.POSITIVE_INFINITY;
            for (int tick = 7; tick <= 23; tick++) {
                bones.forEach(ModelPart::resetPose);
                feeding.apply(tick * 50L, i * STEP);
                rotation.rotationZYX(head.zRot, head.yRot, head.xRot)
                    .transform(tip.set(0, hook.maxY, hook.minZ)).add(head.x, head.y, head.z);
                if (-tip.y < billHeight[i]) {
                    billHeight[i] = -tip.y;
                    billReach[i] = (float) Math.hypot(tip.x, tip.z);
                }
            }
        }
        bones.forEach(ModelPart::resetPose);
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!path.endsWith("/tail")) return;
            for (float y : new float[]{cube.minY, cube.maxY}) {
                // At rest the centreline of every tail points down the length of the main perch.
                var corner = pose.pose().transformPosition(0F, y / 16F, cube.maxZ / 16F, tip);
                tailLength = Math.max(tailLength, corner.z * 16F);
            }
        });
    }

    record Approach(float weight, float bodyYaw, float headYaw, float forward, float sideways) {}

    Approach approach(PetParrotRenderer.State state, float units) {
        float desiredWeight = state.cageMotion.peckDepth;
        if (state.cageMeal == CageMeal.HAND) return new Approach(desiredWeight, 0, 0, 0, 0);
        var cage = state.cage;
        var perch = CageLayout.perch(cage);
        Vec3 target = CageLayout.mealPosition(cage, state.cageMeal).subtract(perch);
        float height = ((float) target.y + 0.003F) / units;
        float maximum = Math.min((SAMPLES - 1) * STEP, desiredWeight + .21F);
        float sample = maximum / STEP;
        for (int i = 1; i < SAMPLES; i++) {
            if (billHeight[i] <= height) {
                float part = Mth.clamp((billHeight[i - 1] - height) / Math.max(.001F, billHeight[i - 1] - billHeight[i]), 0F, 1F);
                sample = Mth.clamp(i - 1 + part, 0.15F / STEP, sample);
                break;
            }
        }
        int index = Math.min((int) sample, SAMPLES - 2);
        float radius = Math.max(.06F, Mth.lerp(sample - index, billReach[index], billReach[index + 1]) * units);
        float ahead = (float) target.x;
        float side = (float) Math.abs(target.z);
        // A seed at the near edge is equally edible; long tails then need a smaller turn in narrow cages.
        float near = Math.max(0F, side - .075F * CageLayout.dishScale(cage));
        float branch = (float) (CageLayout.feedingBranchX(cage) - perch.x);
        float forward, sideways = 0;
        if (side > 0 && (near > radius * .69F || ahead - radius * .72F > branch)) {
            forward = branch;
            float lateralReach = Mth.sqrt(Math.max(0, radius * radius - (ahead - branch) * (ahead - branch)));
            sideways = Mth.clamp(near - lateralReach, 0F, (float)CageLayout.feedingBranchSpan(cage));
        } else {
            float along = Mth.sqrt(Math.max(0, radius * radius - near * near));
            float tailRoom = Math.max(0F, (float) perch.x - .12F - tailLength * units);
            float backStep = Math.min(tailRoom, cage.width == 1 ? .09F : cage.width == 2 ? .30F : .57F);
            forward = Mth.clamp(ahead - along, -backStep,
                cage.width == 1 ? .125F : cage.width == 2 ? .25F : .38F);
        }
        float sign = (float) Math.signum(target.z);
        float yaw = (float) Math.atan2(near - sideways, Math.max(.06F, ahead - forward)) * sign;
        return new Approach(sample * STEP, yaw * .95F, yaw * .18F, forward, sideways * sign);
    }

    static void moveFeet(Approach aim, float units, float phase, ModelPart bird, ModelPart left, ModelPart right) {
        float trip = Mth.clamp(Math.min(phase, 1F - phase) / .25F, 0, 1);
        boolean branch = Math.abs(aim.sideways) > .001F;
        float along = smooth(branch ? trip * 1.8F : trip);
        float across = branch ? smooth((trip - .55F) / .45F) : 0;
        bird.z -= aim.forward * along / units;
        bird.x -= aim.sideways * across / units;
        // Only one foot lifts at a time. Both settle on the branch before the bill reaches the food.
        float step = Mth.sin(trip * Mth.TWO_PI * 2F) * Mth.sin(trip * Mth.PI)
            * Math.min(.52F, (Math.abs(aim.forward) + Math.abs(aim.sideways)) / units * .12F);
        left.y -= Math.max(0, step);
        right.y += Math.min(0, step);
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3F - 2F * t);
    }
}

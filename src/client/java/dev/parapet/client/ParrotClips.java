package dev.parapet.client;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;

final class ParrotClips {
    private record Library(List<Clip> clips) {}
    private record Clip(String name, String loop, float length, Map<String, Map<String, float[][]>> tracks) {}
    private static final Map<String, AnimationDefinition> SPECIES_ACTIONS = load("species_actions");

    enum Size {
        SMALL("pet_parrots", 1.40F, 0.66F, 0.06F, 0.20F),
        MEDIUM("medium_parrots", 1.19F, 0.73F, 0.08F, 0.26F),
        LARGE("large_parrots", 0.95F, 0.61F, 0.11F, 0.35F);

        final Map<String, AnimationDefinition> definitions;
        final float strideRate, stepAngle, walkSway, shadowRadius;

        Size(String library, float strideRate, float stepAngle, float walkSway, float shadowRadius) {
            this.definitions = load(library);
            this.strideRate = strideRate;
            this.stepAngle = stepAngle;
            this.walkSway = walkSway;
            this.shadowRadius = shadowRadius;
        }
    }

    final KeyframeAnimation idle, forage, preen, greet, flap, glide, beg, crestDisplay;
    final KeyframeAnimation nightRoost, ruffle, listen, contactCall, bathe;
    final KeyframeAnimation cageIdle, cageFeed, drink;

    ParrotClips(ModelPart root, Size size, String species) {
        idle = size.definitions.get("idle").bake(root);
        forage = switch (species) {
            case "rainbow_lorikeet", "red_lory", "dusky_lory" -> SPECIES_ACTIONS.get("brush_feeding").bake(root);
            default -> size.definitions.get("forage").bake(root);
        };
        preen = size.definitions.get("preen").bake(root);
        greet = (species.equals("caique") ? SPECIES_ACTIONS.get("caique_hop") : size.definitions.get("greet")).bake(root);
        flap = size.definitions.get("flight_flap").bake(root);
        glide = size.definitions.get("glide").bake(root);
        nightRoost = size.definitions.get("night_roost").bake(root);
        ruffle = size.definitions.get("ruffle").bake(root);
        listen = size.definitions.get("listen").bake(root);
        contactCall = size.definitions.get("contact_call").bake(root);
        bathe = size.definitions.get("bathe").bake(root);
        cageIdle = size.definitions.get("cage_idle").bake(root);
        cageFeed = size.definitions.get("cage_feed").bake(root);
        drink = size.definitions.get("drink").bake(root);
        beg = species.equals("monk_parakeet") ? SPECIES_ACTIONS.get("monk_beg").bake(root) : null;
        crestDisplay = species.equals("sulfur_crested_cockatoo") ? SPECIES_ACTIONS.get("cockatoo_display").bake(root) : null;
    }

    private static Map<String, AnimationDefinition> load(String library) {
        String path = "/assets/parapet/parrot_clips/" + library + ".json";
        var stream = Objects.requireNonNull(ParrotClips.class.getResourceAsStream(path), path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var definitions = new HashMap<String, AnimationDefinition>();
            for (Clip clip : new Gson().fromJson(reader, Library.class).clips()) {
                var animation = AnimationDefinition.Builder.withLength(clip.length());
                if (clip.loop().equals("loop")) {
                    animation.looping();
                }
                clip.tracks().forEach((bone, tracks) -> tracks.forEach((channel, keys) ->
                    animation.addAnimation(bone, readChannel(channel, keys))));
                definitions.put(clip.name(), animation.build());
            }
            return Map.copyOf(definitions);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read parrot animations", e);
        }
    }

    private static AnimationChannel readChannel(String channel, float[][] keys) {
        var target = switch (channel) {
            case "position" -> AnimationChannel.Targets.POSITION;
            case "rotation" -> AnimationChannel.Targets.ROTATION;
            default -> throw new IllegalArgumentException("Unknown animation channel " + channel);
        };
        Keyframe[] frames = new Keyframe[keys.length];
        for (int i = 0; i < keys.length; i++) {
            float[] key = keys[i];
            // Blockbench and Minecraft have opposite X and Y axes. posVec also flips Y.
            var vector = target == AnimationChannel.Targets.POSITION
                ? KeyframeAnimations.posVec(-key[1], key[2], key[3])
                : KeyframeAnimations.degreeVec(-key[1], -key[2], key[3]);
            frames[i] = new Keyframe(key[0], vector, AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(target, frames);
    }
}

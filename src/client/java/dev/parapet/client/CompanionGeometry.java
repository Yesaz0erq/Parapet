package dev.parapet.client;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

final class CompanionGeometry {
    private record Geometry(int[] texture, List<Bone> bones) {}
    private record Bone(String name, float[] pivot, float[] rotation, List<Cube> cubes, List<Bone> children) {}
    private record Cube(int[] uv, float[] origin, float[] size, float inflate, boolean mirror) {}

    private CompanionGeometry() {}

    static LayerDefinition createBodyLayer(String speciesId) {
        String path = "/assets/parapet/geometry/companions/" + speciesId + ".json";
        var stream = Objects.requireNonNull(CompanionGeometry.class.getResourceAsStream(path), path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Geometry geometry = new Gson().fromJson(reader, Geometry.class);
            MeshDefinition mesh = new MeshDefinition();
            for (Bone bone : geometry.bones()) {
                addBone(mesh.getRoot(), bone);
            }
            return LayerDefinition.create(mesh, geometry.texture()[0], geometry.texture()[1]);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read parrot geometry " + path, e);
        }
    }

    private static void addBone(PartDefinition parent, Bone bone) {
        var cubes = CubeListBuilder.create();
        for (Cube cube : bone.cubes()) {
            float[] origin = cube.origin();
            float[] size = cube.size();
            cubes.texOffs(cube.uv()[0], cube.uv()[1]).mirror(cube.mirror())
                .addBox(origin[0], origin[1], origin[2], size[0], size[1], size[2], new CubeDeformation(cube.inflate()));
        }

        float[] p = bone.pivot();
        float[] r = bone.rotation();
        var pose = PartPose.offsetAndRotation(p[0], p[1], p[2], r[0], r[1], r[2]);
        PartDefinition part = parent.addOrReplaceChild(bone.name(), cubes, pose);
        for (Bone child : bone.children()) {
            addBone(part, child);
        }
    }
}

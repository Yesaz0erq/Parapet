package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

final class BudgerigarModel {
    private BudgerigarModel() {}

    // Geometry and UVs exported from art/budgerigar.bbmodel.
    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -3.6F, -2.35F, 5.0F, 7.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, -6.0F, 0.5F, 0.4014F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-2.5F, -4.25F, -2.7F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.113F))
                .texOffs(44, 0).addBox(-2.25F, -4.72F, -2.5F, 4.5F, 0.5F, 4.5F)
                .texOffs(44, 8).addBox(-1.0F, -2.5F, -3.54F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.055F))
                .texOffs(44, 12).addBox(-1.0F, -1.51F, -3.82F, 2.0F, 1.0F, 2.0F)
                .texOffs(54, 12).addBox(-0.5F, -0.75F, -3.77F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -8.25F, -1.3F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 15).addBox(0.02F, -0.67F, -1.68F, 1.0F, 5.0F, 4.0F)
                .texOffs(12, 15).addBox(-0.14F, 3.49F, 0.0F, 1.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(2.4F, -8.05F, 0.2F, 0.7679F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-1.02F, -0.67F, -1.68F, 1.0F, 5.0F, 4.0F)
                .texOffs(12, 15).addBox(-0.86F, 3.49F, 0.0F, 1.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(-2.4F, -8.05F, 0.2F, 0.7679F, 0.0F, 0.0F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(20, 15).addBox(-1.5F, -0.33F, -0.1F, 3.0F, 1.0F, 5.0F)
                .texOffs(0, 27).addBox(-1.0F, -0.37F, 4.32F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.093F))
                .texOffs(20, 27).addBox(-0.5F, -0.41F, 10.04F, 1.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.82F, 2.28F, -0.2443F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(38, 22).addBox(-0.5F, -0.03F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.093F))
                .texOffs(44, 22).addBox(-0.53F, 1.76F, -0.84F, 1.06F, 0.48F, 1.3F)
                .texOffs(44, 22).addBox(0.13F, 2.02F, -1.78F, 0.36F, 0.34F, 1.4F)
                .texOffs(44, 22).addBox(-0.49F, 2.01F, -2.06F, 0.36F, 0.34F, 1.55F)
                .texOffs(44, 22).addBox(-0.47F, 1.99F, 0.08F, 0.36F, 0.35F, 1.1F)
                .texOffs(44, 22).addBox(0.13F, 2.03F, 0.12F, 0.32F, 0.33F, 0.83F),
            PartPose.offset(1.26F, -2.42F, 0.18F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(38, 22).addBox(-0.5F, -0.03F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.093F))
                .texOffs(44, 22).addBox(-0.53F, 1.76F, -0.84F, 1.06F, 0.48F, 1.3F)
                .texOffs(44, 22).addBox(-0.49F, 2.02F, -1.78F, 0.36F, 0.34F, 1.4F)
                .texOffs(44, 22).addBox(0.13F, 2.01F, -2.06F, 0.36F, 0.34F, 1.55F)
                .texOffs(44, 22).addBox(0.11F, 1.99F, 0.08F, 0.36F, 0.35F, 1.1F)
                .texOffs(44, 22).addBox(-0.45F, 2.03F, 0.12F, 0.32F, 0.33F, 0.83F),
            PartPose.offset(-1.26F, -2.42F, 0.18F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}

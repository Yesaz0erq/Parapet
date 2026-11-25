package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Exported from art/eclectus.bbmodel.
final class EclectusModel {
    private EclectusModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-4.5F, -7.75F, -4.02F, 9.0F, 7.0F, 9.0F)
                .texOffs(38, 65).addBox(-5.0F, -3.8F, -5.96F, 10.0F, 8.0F, 10.0F)
                .texOffs(79, 65).addBox(-3.5F, 1.57F, -2.35F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.055F)),
            PartPose.offsetAndRotation(0.0F, -10.65F, 1.0F, 0.4189F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-4.5F, -6.15F, -5.42F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.09F))
                .texOffs(85, 0).addBox(-4.0F, -7.04F, -5.0F, 8.0F, 1.0F, 8.0F)
                .texOffs(85, 12).addBox(-2.5F, -1.6F, -8.4F, 5.0F, 3.0F, 4.0F)
                .texOffs(105, 12).addBox(-2.0F, 0.75F, -7.67F, 4.0F, 2.0F, 3.0F, new CubeDeformation(-0.098F))
                .texOffs(85, 22).addBox(-1.5F, 0.8F, -8.34F, 3.0F, 2.0F, 1.0F)
                .texOffs(98, 23).addBox(-2.0F, -2.46F, -6.23F, 4.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -2.9F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(42, 25).addBox(-3.5F, -0.9F, -0.38F, 7.0F, 2.0F, 10.0F)
                .texOffs(42, 40).addBox(-2.5F, -0.96F, 8.73F, 5.0F, 2.0F, 8.0F)
                .texOffs(22, 50).addBox(-1.5F, -0.5F, 15.62F, 3.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -6.0F, 4.5F, -0.2269F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 28).addBox(0.0F, -0.75F, -3.27F, 2.0F, 12.0F, 7.0F)
                .texOffs(20, 28).addBox(-0.05F, 10.28F, -0.01F, 1.0F, 9.0F, 5.0F)
                .texOffs(108, 65).addBox(0.36F, -1.03F, -3.55F, 2.0F, 6.0F, 6.0F)
                .texOffs(79, 78).addBox(1.15F, 11.16F, 1.91F, 1.0F, 7.0F, 4.0F),
            PartPose.offsetAndRotation(4.88F, -16.65F, 0.45F, 0.6981F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -3.42F, -1.28F, 3.0F, 4.0F, 3.0F)
                .texOffs(100, 28).addBox(-1.0F, -0.08F, -1.03F, 2.0F, 4.0F, 2.0F)
                .texOffs(109, 30).addBox(-0.896F, 3.57F, -1.22F, 1.792F, 0.77F, 1.848F)
                .texOffs(109, 30).addBox(-0.798F, 3.97F, -3.46F, 0.56F, 0.49F, 2.744F)
                .texOffs(109, 30).addBox(0.224F, 3.964F, -3.096F, 0.56F, 0.476F, 2.422F)
                .texOffs(109, 30).addBox(-0.728F, 3.992F, -0.16F, 0.532F, 0.448F, 2.03F)
                .texOffs(109, 30).addBox(0.182F, 4.002F, -0.14F, 0.532F, 0.448F, 1.596F),
            PartPose.offset(2.64F, -4.5F, 0.38F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(76, 34).addBox(-2.0F, -0.75F, -3.27F, 2.0F, 12.0F, 7.0F)
                .texOffs(104, 45).addBox(-0.95F, 10.28F, -0.01F, 1.0F, 9.0F, 5.0F)
                .texOffs(90, 78).addBox(-2.36F, -1.03F, -3.55F, 2.0F, 6.0F, 6.0F)
                .texOffs(107, 78).addBox(-2.15F, 11.16F, 1.91F, 1.0F, 7.0F, 4.0F),
            PartPose.offsetAndRotation(-4.88F, -16.65F, 0.45F, 0.6981F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -3.42F, -1.28F, 3.0F, 4.0F, 3.0F)
                .texOffs(100, 28).addBox(-1.0F, -0.08F, -1.03F, 2.0F, 4.0F, 2.0F)
                .texOffs(109, 30).addBox(-0.896F, 3.57F, -1.22F, 1.792F, 0.77F, 1.848F)
                .texOffs(109, 30).addBox(0.238F, 3.97F, -3.46F, 0.56F, 0.49F, 2.744F)
                .texOffs(109, 30).addBox(-0.784F, 3.964F, -3.096F, 0.56F, 0.476F, 2.422F)
                .texOffs(109, 30).addBox(0.196F, 3.992F, -0.16F, 0.532F, 0.448F, 2.03F)
                .texOffs(109, 30).addBox(-0.714F, 4.002F, -0.14F, 0.532F, 0.448F, 1.596F),
            PartPose.offset(-2.64F, -4.5F, 0.38F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}

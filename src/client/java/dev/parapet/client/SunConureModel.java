package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Exported from art/sun_conure.bbmodel.
final class SunConureModel {
    private SunConureModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-3.0F, -5.51F, -2.65F, 6.0F, 5.0F, 6.0F)
                .texOffs(26, 65).addBox(-3.5F, -2.21F, -4.48F, 7.0F, 5.0F, 7.0F)
                .texOffs(55, 65).addBox(-2.5F, 0.85F, -1.88F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.055F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, 0.7F, 0.5585F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-3.5F, -5.77F, -4.24F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.09F))
                .texOffs(85, 0).addBox(-3.0F, -6.66F, -3.73F, 6.0F, 1.0F, 6.0F)
                .texOffs(85, 12).addBox(-1.5F, -2.3F, -6.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.055F))
                .texOffs(105, 12).addBox(-1.5F, -0.48F, -5.55F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.018F))
                .texOffs(85, 22).addBox(-1.0F, -1.06F, -5.93F, 2.0F, 2.0F, 1.0F)
                .texOffs(98, 23).addBox(-1.5F, -3.27F, -5.15F, 3.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -11.6F, -2.3F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(42, 25).addBox(-2.5F, -0.22F, -0.39F, 5.0F, 1.0F, 8.0F)
                .texOffs(42, 40).addBox(-1.5F, -0.3F, 6.81F, 3.0F, 1.0F, 11.0F)
                .texOffs(22, 50).addBox(-0.5F, -0.41F, 17.03F, 1.0F, 1.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, -5.35F, 3.65F, -0.2007F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 28).addBox(0.0F, -0.76F, -2.18F, 1.0F, 8.0F, 5.0F)
                .texOffs(20, 28).addBox(-0.05F, 6.07F, 0.32F, 1.0F, 7.0F, 3.0F)
                .texOffs(76, 65).addBox(0.36F, -1.04F, -2.46F, 1.0F, 4.0F, 4.0F)
                .texOffs(87, 65).addBox(0.15F, 6.95F, 1.24F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(3.42F, -11.9F, 0.22F, 0.8203F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.0F, -1.42F, -1.28F, 2.0F, 2.0F, 2.0F)
                .texOffs(100, 28).addBox(-0.5F, -0.07F, -0.53F, 1.0F, 3.0F, 1.0F)
                .texOffs(109, 30).addBox(-0.6144F, 2.712F, -0.956F, 1.2288F, 0.528F, 1.2672F)
                .texOffs(109, 30).addBox(-0.5472F, 3.024F, -2.492F, 0.384F, 0.336F, 1.8816F)
                .texOffs(109, 30).addBox(0.1536F, 3.0136F, -2.2424F, 0.384F, 0.3264F, 1.6608F)
                .texOffs(109, 30).addBox(-0.4992F, 3.0328F, -0.16F, 0.3648F, 0.3072F, 1.392F)
                .texOffs(109, 30).addBox(0.1248F, 3.0428F, -0.14F, 0.3648F, 0.3072F, 1.0944F),
            PartPose.offset(1.66F, -3.4F, 0.38F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-1.0F, -0.76F, -2.18F, 1.0F, 8.0F, 5.0F)
                .texOffs(20, 28).addBox(-0.95F, 6.07F, 0.32F, 1.0F, 7.0F, 3.0F)
                .texOffs(96, 65).addBox(-1.36F, -1.04F, -2.46F, 1.0F, 4.0F, 4.0F)
                .texOffs(107, 65).addBox(-1.15F, 6.95F, 1.24F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(-3.42F, -11.9F, 0.22F, 0.8203F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.0F, -1.42F, -1.28F, 2.0F, 2.0F, 2.0F)
                .texOffs(100, 28).addBox(-0.5F, -0.07F, -0.53F, 1.0F, 3.0F, 1.0F)
                .texOffs(109, 30).addBox(-0.6144F, 2.712F, -0.956F, 1.2288F, 0.528F, 1.2672F)
                .texOffs(109, 30).addBox(0.1632F, 3.024F, -2.492F, 0.384F, 0.336F, 1.8816F)
                .texOffs(109, 30).addBox(-0.5376F, 3.0136F, -2.2424F, 0.384F, 0.3264F, 1.6608F)
                .texOffs(109, 30).addBox(0.1344F, 3.0328F, -0.16F, 0.3648F, 0.3072F, 1.392F)
                .texOffs(109, 30).addBox(-0.4896F, 3.0428F, -0.14F, 0.3648F, 0.3072F, 1.0944F),
            PartPose.offset(-1.66F, -3.4F, 0.38F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}

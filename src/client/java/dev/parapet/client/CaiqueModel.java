package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Exported from art/caique.bbmodel.
final class CaiqueModel {
    private CaiqueModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-3.5F, -5.53F, -2.94F, 7.0F, 5.0F, 7.0F)
                .texOffs(30, 65).addBox(-4.0F, -2.81F, -4.83F, 8.0F, 6.0F, 8.0F)
                .texOffs(63, 65).addBox(-3.0F, 0.97F, -2.05F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.055F)),
            PartPose.offsetAndRotation(0.0F, -8.1F, 0.4F, 0.2269F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-3.5F, -5.75F, -4.15F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.09F))
                .texOffs(85, 0).addBox(-3.0F, -6.63F, -3.7F, 6.0F, 1.0F, 6.0F)
                .texOffs(85, 12).addBox(-1.5F, -2.3F, -6.28F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.055F))
                .texOffs(105, 12).addBox(-1.5F, -0.53F, -5.75F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.018F))
                .texOffs(85, 22).addBox(-0.5F, -0.95F, -6.21F, 1.0F, 2.0F, 1.0F)
                .texOffs(98, 23).addBox(-1.5F, -3.25F, -5.04F, 3.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -12.1F, -1.7F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(42, 25).addBox(-3.0F, -0.28F, 0.3F, 6.0F, 1.0F, 6.0F)
                .texOffs(42, 40).addBox(-2.0F, -0.35F, 5.67F, 4.0F, 1.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, -6.4F, 3.1F, -0.3665F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 28).addBox(0.0F, -0.55F, -2.38F, 1.0F, 7.0F, 5.0F)
                .texOffs(20, 28).addBox(-0.05F, 5.76F, -0.63F, 1.0F, 4.0F, 3.0F)
                .texOffs(88, 65).addBox(0.36F, -0.83F, -2.66F, 1.0F, 4.0F, 4.0F)
                .texOffs(99, 65).addBox(0.15F, 5.64F, 0.29F, 1.0F, 3.0F, 3.0F),
            PartPose.offsetAndRotation(3.91F, -11.9F, 0.52F, 0.5934F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -2.42F, -1.28F, 3.0F, 3.0F, 3.0F)
                .texOffs(100, 28).addBox(-0.5F, 0.15F, -0.53F, 1.0F, 3.0F, 1.0F)
                .texOffs(109, 30).addBox(-0.6784F, 2.957F, -1.016F, 1.3568F, 0.583F, 1.3992F)
                .texOffs(109, 30).addBox(-0.6042F, 3.289F, -2.712F, 0.424F, 0.371F, 2.0776F)
                .texOffs(109, 30).addBox(0.1696F, 3.2796F, -2.4364F, 0.424F, 0.3604F, 1.8338F)
                .texOffs(109, 30).addBox(-0.5512F, 3.3008F, -0.16F, 0.4028F, 0.3392F, 1.537F)
                .texOffs(109, 30).addBox(0.1378F, 3.3108F, -0.14F, 0.4028F, 0.3392F, 1.2084F),
            PartPose.offset(2.16F, -3.7F, 0.38F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-1.0F, -0.55F, -2.38F, 1.0F, 7.0F, 5.0F)
                .texOffs(20, 28).addBox(-0.95F, 5.76F, -0.63F, 1.0F, 4.0F, 3.0F)
                .texOffs(108, 65).addBox(-1.36F, -0.83F, -2.66F, 1.0F, 4.0F, 4.0F)
                .texOffs(119, 65).addBox(-1.15F, 5.64F, 0.29F, 1.0F, 3.0F, 3.0F),
            PartPose.offsetAndRotation(-3.91F, -11.9F, 0.52F, 0.5934F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -2.42F, -1.28F, 3.0F, 3.0F, 3.0F)
                .texOffs(100, 28).addBox(-0.5F, 0.15F, -0.53F, 1.0F, 3.0F, 1.0F)
                .texOffs(109, 30).addBox(-0.6784F, 2.957F, -1.016F, 1.3568F, 0.583F, 1.3992F)
                .texOffs(109, 30).addBox(0.1802F, 3.289F, -2.712F, 0.424F, 0.371F, 2.0776F)
                .texOffs(109, 30).addBox(-0.5936F, 3.2796F, -2.4364F, 0.424F, 0.3604F, 1.8338F)
                .texOffs(109, 30).addBox(0.1484F, 3.3008F, -0.16F, 0.4028F, 0.3392F, 1.537F)
                .texOffs(109, 30).addBox(-0.5406F, 3.3108F, -0.14F, 0.4028F, 0.3392F, 1.2084F),
            PartPose.offset(-2.16F, -3.7F, 0.38F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}

package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Geometry exported from art/macaw_mini.bbmodel.
final class MacawMiniModel {
    private MacawMiniModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 130).addBox(-3.0F, 1.28F, -2.65F, 6.0F, 4.0F, 6.0F)
                .texOffs(52, 130).addBox(-4.0F, -3.6F, -4.57F, 8.0F, 6.0F, 9.0F)
                .texOffs(0, 165).addBox(-3.5F, -6.88F, -3.61F, 7.0F, 4.0F, 7.0F, new CubeDeformation(-0.09F))
                .texOffs(52, 165).addBox(-2.0F, -1.2F, -5.07F, 4.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -8.83F, 0.75F, 0.5236F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-3.5F, -5.11F, -4.48F, 7.0F, 7.0F, 8.0F)
                .texOffs(74, 22).addBox(-3.0F, -5.98F, -3.99F, 6.0F, 1.0F, 7.0F)
                .texOffs(0, 37).addBox(-2.0F, -2.685F, -7.78F, 4.0F, 3.0F, 4.0F)
                .texOffs(31, 37).addBox(-1.5F, -0.035F, -6.58F, 3.0F, 2.0F, 3.0F)
                .texOffs(55, 37).addBox(-1.0F, 0.115F, -7.72F, 2.0F, 2.0F, 2.0F)
                .texOffs(72, 37).addBox(-1.5F, -3.585F, -5.27F, 3.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -14.2F, -2.48F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-3.0F, -0.18F, -0.5F, 6.0F, 1.0F, 9.0F, new CubeDeformation(-0.085F))
                .texOffs(52, 82).addBox(-2.0F, -0.31F, 7.85F, 4.0F, 1.0F, 10.0F)
                .texOffs(0, 110).addBox(-1.0F, -0.44F, 17.2F, 2.0F, 1.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -6.55F, 3.68F, -0.1484F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 52).addBox(0.0F, -0.67F, -2.46F, 1.0F, 11.0F, 6.0F)
                .texOffs(68, 52).addBox(-0.09F, 8.71F, -0.6F, 1.0F, 8.0F, 4.0F)
                .texOffs(0, 200).addBox(0.37F, -0.9F, -1.98F, 1.0F, 5.0F, 4.0F)
                .texOffs(70, 200).addBox(0.24F, 8.35F, 0.85F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(3.87F, -14.23F, 0.12F, 0.7854F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.55F, -0.85F, 2.0F, 4.0F, 2.0F)
                .texOffs(108, 82).addBox(-0.7875F, 3.223F, -0.992F, 1.575F, 0.567F, 1.6275F)
                .texOffs(108, 82).addBox(0.021F, 3.532F, -2.7035F, 0.4935F, 0.378F, 2.1525F)
                .texOffs(108, 82).addBox(-0.714F, 3.5225F, -2.3255F, 0.504F, 0.3675F, 1.89F)
                .texOffs(108, 82).addBox(0.021F, 3.5135F, -0.01F, 0.462F, 0.3465F, 1.491F)
                .texOffs(108, 82).addBox(-0.6825F, 3.5125F, 0.1F, 0.4725F, 0.3675F, 1.239F),
            PartPose.offset(1.96F, -3.95F, 0.32F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(33, 52).addBox(-1.0F, -0.67F, -2.46F, 1.0F, 11.0F, 6.0F)
                .texOffs(88, 52).addBox(-0.91F, 8.71F, -0.6F, 1.0F, 8.0F, 4.0F)
                .texOffs(33, 200).addBox(-1.37F, -0.9F, -1.98F, 1.0F, 5.0F, 4.0F)
                .texOffs(96, 200).addBox(-1.24F, 8.35F, 0.85F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(-3.87F, -14.23F, 0.12F, 0.7854F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.55F, -0.85F, 2.0F, 4.0F, 2.0F)
                .texOffs(108, 82).addBox(-0.7875F, 3.223F, -0.992F, 1.575F, 0.567F, 1.6275F)
                .texOffs(108, 82).addBox(0.021F, 3.532F, -2.7035F, 0.4935F, 0.378F, 2.1525F)
                .texOffs(108, 82).addBox(-0.714F, 3.5225F, -2.3255F, 0.504F, 0.3675F, 1.89F)
                .texOffs(108, 82).addBox(0.021F, 3.5135F, -0.01F, 0.462F, 0.3465F, 1.491F)
                .texOffs(108, 82).addBox(-0.6825F, 3.5125F, 0.1F, 0.4725F, 0.3675F, 1.239F),
            PartPose.offset(-1.96F, -3.95F, 0.32F));

        return LayerDefinition.create(mesh, 128, 256);
    }
}

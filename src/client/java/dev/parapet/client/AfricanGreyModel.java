package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Geometry exported from art/african_grey.bbmodel.
final class AfricanGreyModel {
    private AfricanGreyModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 130).addBox(-4.0F, 1.72F, -3.45F, 8.0F, 4.0F, 8.0F)
                .texOffs(52, 130).addBox(-5.0F, -4.4F, -5.37F, 10.0F, 7.0F, 11.0F)
                .texOffs(0, 165).addBox(-4.5F, -7.12F, -4.41F, 9.0F, 4.0F, 9.0F, new CubeDeformation(-0.09F))
                .texOffs(52, 165).addBox(-3.0F, -2.05F, -5.87F, 6.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -9.67F, 0.75F, 0.3316F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-4.5F, -5.84F, -5.04F, 9.0F, 8.0F, 9.0F)
                .texOffs(74, 22).addBox(-4.0F, -6.71F, -4.55F, 8.0F, 1.0F, 8.0F)
                .texOffs(0, 37).addBox(-2.5F, -2.64F, -8.34F, 5.0F, 3.0F, 4.0F)
                .texOffs(31, 37).addBox(-2.0F, 0.01F, -7.14F, 4.0F, 2.0F, 3.0F)
                .texOffs(55, 37).addBox(-1.5F, 0.16F, -8.28F, 3.0F, 2.0F, 2.0F)
                .texOffs(72, 37).addBox(-2.0F, -3.54F, -5.83F, 4.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -16.12F, -2.79F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-4.0F, -1.18F, -0.5F, 8.0F, 2.0F, 7.0F, new CubeDeformation(-0.085F))
                .texOffs(52, 82).addBox(-3.0F, -1.31F, 5.85F, 6.0F, 2.0F, 4.0F)
                .texOffs(0, 110).addBox(-2.0F, -0.44F, 9.2F, 4.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -7.2F, 4.6F, -0.3316F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 52).addBox(0.0F, -0.67F, -2.85F, 2.0F, 11.0F, 7.0F)
                .texOffs(68, 52).addBox(-0.09F, 8.71F, -0.99F, 1.0F, 8.0F, 5.0F)
                .texOffs(0, 200).addBox(1.37F, -0.9F, -2.37F, 1.0F, 5.0F, 5.0F)
                .texOffs(70, 200).addBox(0.24F, 8.35F, 0.46F, 1.0F, 5.0F, 4.0F),
            PartPose.offsetAndRotation(4.87F, -15.52F, 0.12F, 0.6458F, 0.0F, 0.0F));

        PartDefinition left_leg = bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(2.45F, -4.35F, 0.32F));

        left_leg.addOrReplaceChild("left_thigh_feathers", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -0.25F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.05F, 0.26F, 0.0F, 0.0F, 0.096F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(33, 52).addBox(-2.0F, -0.67F, -2.85F, 2.0F, 11.0F, 7.0F)
                .texOffs(88, 52).addBox(-0.91F, 8.71F, -0.99F, 1.0F, 8.0F, 5.0F)
                .texOffs(33, 200).addBox(-2.37F, -0.9F, -2.37F, 1.0F, 5.0F, 5.0F)
                .texOffs(96, 200).addBox(-1.24F, 8.35F, 0.46F, 1.0F, 5.0F, 4.0F),
            PartPose.offsetAndRotation(-4.87F, -15.52F, 0.12F, 0.6458F, 0.0F, 0.0F));

        PartDefinition right_leg = bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(-2.45F, -4.35F, 0.32F));

        right_leg.addOrReplaceChild("right_thigh_feathers", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -0.25F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.05F, 0.26F, 0.0F, 0.0F, -0.096F));

        return LayerDefinition.create(mesh, 128, 256);
    }
}

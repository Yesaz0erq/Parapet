package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Geometry exported from art/macaw_short_ara.bbmodel.
final class MacawShortAraModel {
    private MacawShortAraModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 130).addBox(-4.0F, 1.16F, -3.45F, 8.0F, 5.0F, 8.0F)
                .texOffs(52, 130).addBox(-5.0F, -4.2F, -5.37F, 10.0F, 7.0F, 11.0F)
                .texOffs(0, 165).addBox(-4.5F, -7.36F, -4.41F, 9.0F, 4.0F, 9.0F, new CubeDeformation(-0.09F))
                .texOffs(52, 165).addBox(-3.0F, -1.9F, -5.87F, 6.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -10.11F, 0.75F, 0.5236F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-4.5F, -6.57F, -5.04F, 9.0F, 9.0F, 9.0F)
                .texOffs(74, 22).addBox(-4.0F, -7.44F, -4.55F, 8.0F, 1.0F, 8.0F)
                .texOffs(0, 37).addBox(-2.5F, -3.595F, -9.34F, 5.0F, 4.0F, 5.0F)
                .texOffs(31, 37).addBox(-2.0F, -0.395F, -8.14F, 4.0F, 3.0F, 4.0F)
                .texOffs(55, 37).addBox(-1.5F, 0.045F, -9.28F, 3.0F, 2.0F, 2.0F)
                .texOffs(72, 37).addBox(-2.0F, -4.495F, -5.83F, 4.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -16.6F, -2.79F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-4.0F, -1.18F, -0.5F, 8.0F, 2.0F, 12.0F, new CubeDeformation(-0.085F))
                .texOffs(52, 82).addBox(-2.5F, -1.31F, 10.85F, 5.0F, 2.0F, 13.0F)
                .texOffs(0, 110).addBox(-1.0F, -0.44F, 23.2F, 2.0F, 1.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, -7.45F, 4.6F, -0.1484F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 52).addBox(0.0F, -0.67F, -3.24F, 2.0F, 13.0F, 8.0F)
                .texOffs(68, 52).addBox(-0.09F, 10.71F, -0.38F, 1.0F, 10.0F, 5.0F)
                .texOffs(0, 200).addBox(1.37F, -0.9F, -2.76F, 1.0F, 6.0F, 6.0F)
                .texOffs(70, 200).addBox(0.24F, 10.51F, 1.07F, 1.0F, 6.0F, 4.0F),
            PartPose.offsetAndRotation(4.87F, -16.41F, 0.12F, 0.7854F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(2.45F, -4.35F, 0.32F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(33, 52).addBox(-2.0F, -0.67F, -3.24F, 2.0F, 13.0F, 8.0F)
                .texOffs(88, 52).addBox(-0.91F, 10.71F, -0.38F, 1.0F, 10.0F, 5.0F)
                .texOffs(33, 200).addBox(-2.37F, -0.9F, -2.76F, 1.0F, 6.0F, 6.0F)
                .texOffs(96, 200).addBox(-1.24F, 10.51F, 1.07F, 1.0F, 6.0F, 4.0F),
            PartPose.offsetAndRotation(-4.87F, -16.41F, 0.12F, 0.7854F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(-2.45F, -4.35F, 0.32F));

        return LayerDefinition.create(mesh, 128, 256);
    }
}

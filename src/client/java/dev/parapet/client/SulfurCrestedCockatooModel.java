package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Geometry exported from art/sulfur_crested_cockatoo.bbmodel.
final class SulfurCrestedCockatooModel {
    private SulfurCrestedCockatooModel() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 130).addBox(-4.5F, 2.04F, -3.85F, 9.0F, 5.0F, 9.0F)
                .texOffs(52, 130).addBox(-5.5F, -4.8F, -5.77F, 11.0F, 8.0F, 12.0F)
                .texOffs(0, 165).addBox(-5.0F, -8.84F, -4.81F, 10.0F, 5.0F, 10.0F, new CubeDeformation(-0.09F))
                .texOffs(52, 165).addBox(-3.5F, -1.6F, -6.27F, 7.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -10.99F, 0.75F, 0.3316F, 0.0F, 0.0F));

        PartDefinition head = bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-5.0F, -7.3F, -5.6F, 10.0F, 10.0F, 10.0F)
                .texOffs(74, 22).addBox(-4.5F, -8.17F, -5.11F, 9.0F, 1.0F, 9.0F)
                .texOffs(0, 37).addBox(-3.0F, -3.55F, -9.9F, 6.0F, 4.0F, 5.0F)
                .texOffs(31, 37).addBox(-2.5F, -0.35F, -8.7F, 5.0F, 3.0F, 4.0F)
                .texOffs(55, 37).addBox(-2.0F, 0.09F, -9.84F, 4.0F, 2.0F, 2.0F)
                .texOffs(72, 37).addBox(-2.5F, -4.45F, -6.39F, 5.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -18.97F, -3.1F));

        PartDefinition crest = head.addOrReplaceChild("crest", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, -8.01F, -0.9F, -1.1345F, 0.0F, 0.0F));

        PartDefinition fan = crest.addOrReplaceChild("fan", CubeListBuilder.create(),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        fan.addOrReplaceChild("crest_feather_0", CubeListBuilder.create()
                .texOffs(78, 110).addBox(-0.5F, -5.88F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(78, 110).addBox(-0.67F, -7.18F, -1.12F, 1.0F, 2.0F, 1.0F),
            PartPose.offset(1.5F, 0.53F, 0.26F));

        fan.addOrReplaceChild("crest_feather_1", CubeListBuilder.create()
                .texOffs(78, 110).addBox(-0.5F, -7.88F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(78, 110).addBox(-0.67F, -9.18F, -1.12F, 1.0F, 2.0F, 1.0F),
            PartPose.offset(0.82F, 0.43F, 0.97F));

        fan.addOrReplaceChild("crest_feather_2", CubeListBuilder.create()
                .texOffs(78, 110).addBox(-1.0F, -8.88F, -0.5F, 2.0F, 9.0F, 1.0F)
                .texOffs(78, 110).addBox(-0.17F, -10.18F, -1.12F, 1.0F, 2.0F, 1.0F),
            PartPose.offset(-0.3F, 0.33F, 1.92F));

        fan.addOrReplaceChild("crest_feather_3", CubeListBuilder.create()
                .texOffs(78, 110).addBox(-0.5F, -7.88F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(78, 110).addBox(-0.67F, -9.18F, -1.12F, 1.0F, 2.0F, 1.0F),
            PartPose.offset(-1.13F, 0.43F, 1.1F));

        fan.addOrReplaceChild("crest_feather_4", CubeListBuilder.create()
                .texOffs(78, 110).addBox(-0.5F, -5.88F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(78, 110).addBox(-0.67F, -7.18F, -1.12F, 1.0F, 2.0F, 1.0F),
            PartPose.offset(-1.84F, 0.53F, 0.47F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-4.0F, -1.18F, -0.5F, 8.0F, 2.0F, 11.0F)
                .texOffs(52, 82).addBox(-3.0F, -1.31F, 9.85F, 6.0F, 2.0F, 8.0F)
                .texOffs(0, 110).addBox(-2.0F, -0.44F, 17.2F, 4.0F, 1.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -7.95F, 5.06F, -0.2618F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 52).addBox(0.0F, -0.67F, -3.24F, 2.0F, 14.0F, 8.0F)
                .texOffs(68, 52).addBox(-0.09F, 11.71F, -0.38F, 1.0F, 10.0F, 5.0F)
                .texOffs(0, 200).addBox(1.37F, -0.9F, -2.76F, 1.0F, 7.0F, 6.0F)
                .texOffs(70, 200).addBox(0.24F, 11.51F, 1.07F, 1.0F, 6.0F, 4.0F),
            PartPose.offsetAndRotation(5.37F, -18.19F, 0.12F, 0.6458F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(2.695F, -4.35F, 0.32F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(33, 52).addBox(-2.0F, -0.67F, -3.24F, 2.0F, 14.0F, 8.0F)
                .texOffs(88, 52).addBox(-0.91F, 11.71F, -0.38F, 1.0F, 10.0F, 5.0F)
                .texOffs(33, 200).addBox(-2.37F, -0.9F, -2.76F, 1.0F, 7.0F, 6.0F)
                .texOffs(96, 200).addBox(-1.24F, 11.51F, 1.07F, 1.0F, 6.0F, 4.0F),
            PartPose.offsetAndRotation(-5.37F, -18.19F, 0.12F, 0.6458F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-1.5F, -3.3F, -1.24F, 3.0F, 4.0F, 3.0F)
                .texOffs(112, 67).addBox(-1.0F, -0.27F, -0.85F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.07F))
                .texOffs(108, 82).addBox(-1.0125F, 3.461F, -1.184F, 2.025F, 0.729F, 2.0925F)
                .texOffs(108, 82).addBox(0.027F, 3.824F, -3.3845F, 0.6345F, 0.486F, 2.7675F)
                .texOffs(108, 82).addBox(-0.918F, 3.8175F, -2.8985F, 0.648F, 0.4725F, 2.43F)
                .texOffs(108, 82).addBox(0.027F, 3.8145F, -0.01F, 0.594F, 0.4455F, 1.917F)
                .texOffs(108, 82).addBox(-0.8775F, 3.8075F, 0.1F, 0.6075F, 0.4725F, 1.593F),
            PartPose.offset(-2.695F, -4.35F, 0.32F));

        return LayerDefinition.create(mesh, 128, 256);
    }
}

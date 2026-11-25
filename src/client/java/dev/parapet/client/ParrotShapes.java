package dev.parapet.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Geometry and UVs exported from the species projects in art/.
final class ParrotShapes {
    private ParrotShapes() {}

    static LayerDefinition cockatiel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-3.0F, -5.58F, -2.57F, 6.0F, 5.0F, 6.0F)
                .texOffs(26, 65).addBox(-3.5F, -2.33F, -4.26F, 7.0F, 5.0F, 7.0F)
                .texOffs(1, 77).addBox(-2.5F, 0.65F, -1.68F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.055F)),
            PartPose.offsetAndRotation(0.0F, -8.3F, 0.6F, 0.3665F, 0.0F, 0.0F));

        PartDefinition head = bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-3.0F, -6.45F, -3.7F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.075F))
                .texOffs(32, 33).addBox(-2.5F, -7.27F, -3.2F, 5.0F, 1.0F, 5.0F)
                .texOffs(31, 18).addBox(-1.5F, -3.0F, -5.15F, 3.0F, 2.0F, 2.0F)
                .texOffs(42, 18).addBox(-1.0F, -1.24F, -5.04F, 2.0F, 1.0F, 1.0F)
                .texOffs(49, 18).addBox(-1.5F, -3.95F, -4.58F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.055F)),
            PartPose.offset(0.0F, -11.4F, -2.1F));

        head.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(24, 19).addBox(-0.5F, -6.8F, -0.45F, 1.0F, 7.0F, 1.0F)
                .texOffs(24, 28).addBox(0.16F, -3.87F, 0.13F, 1.0F, 4.0F, 1.0F)
                .texOffs(24, 28).addBox(-1.16F, -3.87F, 0.28F, 1.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -6.7F, -0.55F, -0.5236F, 0.0F, 0.0F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-2.5F, -0.55F, -0.2F, 5.0F, 1.0F, 8.0F)
                .texOffs(0, 46).addBox(-1.5F, -0.61F, 6.8F, 3.0F, 1.0F, 12.0F)
                .texOffs(32, 45).addBox(-0.5F, -0.67F, 17.85F, 1.0F, 1.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, -5.75F, 3.8F, -0.192F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-0.08F, -0.5F, -2.1F, 1.0F, 8.0F, 5.0F)
                .texOffs(14, 19).addBox(-0.26F, 6.6F, 0.42F, 1.0F, 6.0F, 3.0F)
                .texOffs(22, 78).addBox(0.28F, -0.78F, -2.38F, 1.0F, 4.0F, 4.0F)
                .texOffs(55, 65).addBox(0.07F, 6.48F, 1.34F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(3.48F, -12.15F, 0.3F, 0.6807F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(50, 25).addBox(-0.5F, -0.12F, -0.64F, 1.0F, 3.0F, 1.0F)
                .texOffs(55, 25).addBox(-0.68F, 2.7F, -0.93F, 1.36F, 0.5F, 1.4F)
                .texOffs(55, 25).addBox(0.22F, 2.99F, -2.56F, 0.42F, 0.36F, 2.05F)
                .texOffs(55, 25).addBox(-0.6F, 2.98F, -2.81F, 0.42F, 0.35F, 2.05F)
                .texOffs(55, 25).addBox(0.23F, 2.98F, -0.02F, 0.4F, 0.37F, 1.5F)
                .texOffs(55, 25).addBox(-0.59F, 2.97F, 0.03F, 0.4F, 0.37F, 1.2F),
            PartPose.offset(1.68F, -3.4F, 0.3F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-0.92F, -0.5F, -2.1F, 1.0F, 8.0F, 5.0F)
                .texOffs(14, 19).addBox(-0.74F, 6.6F, 0.42F, 1.0F, 6.0F, 3.0F)
                .texOffs(33, 78).addBox(-1.28F, -0.78F, -2.38F, 1.0F, 4.0F, 4.0F)
                .texOffs(55, 74).addBox(-1.07F, 6.48F, 1.34F, 1.0F, 5.0F, 3.0F),
            PartPose.offsetAndRotation(-3.48F, -12.15F, 0.3F, 0.6807F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(50, 25).addBox(-0.5F, -0.12F, -0.64F, 1.0F, 3.0F, 1.0F)
                .texOffs(55, 25).addBox(-0.68F, 2.7F, -0.93F, 1.36F, 0.5F, 1.4F)
                .texOffs(55, 25).addBox(-0.64F, 2.99F, -2.56F, 0.42F, 0.36F, 2.05F)
                .texOffs(55, 25).addBox(0.18F, 2.98F, -2.81F, 0.42F, 0.35F, 2.05F)
                .texOffs(55, 25).addBox(-0.63F, 2.98F, -0.02F, 0.4F, 0.37F, 1.5F)
                .texOffs(55, 25).addBox(0.19F, 2.97F, 0.03F, 0.4F, 0.37F, 1.2F),
            PartPose.offset(-1.68F, -3.4F, 0.3F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    static LayerDefinition greenCheekConure() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-3.0F, -5.15F, -2.83F, 6.0F, 5.0F, 6.0F)
                .texOffs(26, 65).addBox(-3.5F, -2.52F, -4.47F, 7.0F, 5.0F, 7.0F)
                .texOffs(1, 77).addBox(-2.5F, 1.22F, -1.59F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.055F)),
            PartPose.offsetAndRotation(0.0F, -7.25F, 0.85F, 0.5498F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-3.5F, -6.15F, -3.85F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.09F))
                .texOffs(32, 33).addBox(-3.0F, -6.99F, -3.42F, 6.0F, 1.0F, 6.0F)
                .texOffs(31, 18).addBox(-1.5F, -3.05F, -5.4F, 3.0F, 2.0F, 2.0F)
                .texOffs(42, 18).addBox(-1.0F, -1.22F, -5.33F, 2.0F, 1.0F, 1.0F)
                .texOffs(49, 18).addBox(-1.5F, -3.93F, -4.64F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.055F)),
            PartPose.offset(0.0F, -10.3F, -2.6F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-2.5F, -1.1F, -0.3F, 5.0F, 2.0F, 7.0F)
                .texOffs(0, 46).addBox(-1.5F, -0.68F, 6.05F, 3.0F, 1.0F, 9.0F)
                .texOffs(32, 45).addBox(-1.0F, -0.71F, 14.35F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.08F)),
            PartPose.offsetAndRotation(0.0F, -4.65F, 3.55F, -0.1745F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-0.08F, -0.65F, -1.7F, 1.0F, 7.0F, 5.0F)
                .texOffs(14, 19).addBox(-0.26F, 5.68F, 0.42F, 1.0F, 6.0F, 3.0F)
                .texOffs(22, 78).addBox(0.28F, -0.93F, -1.98F, 1.0F, 4.0F, 4.0F)
                .texOffs(55, 65).addBox(0.07F, 6.56F, 1.34F, 1.0F, 4.0F, 3.0F),
            PartPose.offsetAndRotation(3.48F, -10.85F, 0.3F, 0.8727F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(50, 25).addBox(-0.5F, -0.12F, -0.64F, 1.0F, 3.0F, 1.0F)
                .texOffs(55, 25).addBox(-0.68F, 2.7F, -0.93F, 1.36F, 0.5F, 1.4F)
                .texOffs(55, 25).addBox(0.22F, 2.99F, -2.56F, 0.42F, 0.36F, 2.05F)
                .texOffs(55, 25).addBox(-0.6F, 2.98F, -2.81F, 0.42F, 0.35F, 2.05F)
                .texOffs(55, 25).addBox(0.23F, 2.98F, -0.02F, 0.4F, 0.37F, 1.5F)
                .texOffs(55, 25).addBox(-0.59F, 2.97F, 0.03F, 0.4F, 0.37F, 1.2F),
            PartPose.offset(1.75F, -3.4F, 0.3F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-0.92F, -0.65F, -1.7F, 1.0F, 7.0F, 5.0F)
                .texOffs(14, 19).addBox(-0.74F, 5.68F, 0.42F, 1.0F, 6.0F, 3.0F)
                .texOffs(33, 78).addBox(-1.28F, -0.93F, -1.98F, 1.0F, 4.0F, 4.0F)
                .texOffs(55, 73).addBox(-1.07F, 6.56F, 1.34F, 1.0F, 4.0F, 3.0F),
            PartPose.offsetAndRotation(-3.48F, -10.85F, 0.3F, 0.8727F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(50, 25).addBox(-0.5F, -0.12F, -0.64F, 1.0F, 3.0F, 1.0F)
                .texOffs(55, 25).addBox(-0.68F, 2.7F, -0.93F, 1.36F, 0.5F, 1.4F)
                .texOffs(55, 25).addBox(-0.64F, 2.99F, -2.56F, 0.42F, 0.36F, 2.05F)
                .texOffs(55, 25).addBox(0.18F, 2.98F, -2.81F, 0.42F, 0.35F, 2.05F)
                .texOffs(55, 25).addBox(-0.63F, 2.98F, -0.02F, 0.4F, 0.37F, 1.5F)
                .texOffs(55, 25).addBox(0.19F, 2.97F, 0.03F, 0.4F, 0.37F, 1.2F),
            PartPose.offset(-1.75F, -3.4F, 0.3F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    static LayerDefinition lovebird() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -3.4F, -2.95F, 6.0F, 7.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -6.0F, 0.25F, 0.4189F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-3.0F, -4.93F, -3.1F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.09F))
                .texOffs(32, 34).addBox(-2.5F, -5.74F, -2.54F, 5.0F, 1.0F, 5.0F)
                .texOffs(32, 19).addBox(-1.5F, -2.57F, -4.66F, 3.0F, 2.0F, 2.0F)
                .texOffs(47, 19).addBox(-1.0F, -0.78F, -4.59F, 2.0F, 1.0F, 1.0F)
                .texOffs(54, 19).addBox(-1.0F, -3.39F, -3.86F, 2.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -8.35F, -1.8F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-2.5F, -0.42F, -0.47F, 5.0F, 1.0F, 4.0F)
                .texOffs(0, 50).addBox(-1.5F, -0.5F, 2.94F, 3.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -4.55F, 2.62F, -0.3491F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.05F, -0.41F, -1.91F, 1.0F, 5.0F, 4.0F)
                .texOffs(16, 20).addBox(-0.23F, 4.01F, -0.37F, 1.0F, 3.0F, 3.0F),
            PartPose.offsetAndRotation(2.95F, -8.07F, 0.19F, 0.6283F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(46, 27).addBox(-0.5F, -0.04F, -0.45F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.101F))
                .texOffs(53, 27).addBox(-0.5312F, 1.75F, -0.718F, 1.0624F, 0.5F, 1.0624F, new CubeDeformation(-0.08F))
                .texOffs(53, 27).addBox(-0.55F, 2.01F, -1.9879F, 0.4F, 0.34F, 1.5687F)
                .texOffs(53, 27).addBox(0.15F, 1.98F, -1.7887F, 0.4F, 0.34F, 1.3944F)
                .texOffs(53, 27).addBox(-0.51F, 2.01F, 0.01F, 0.38F, 0.32F, 1.0624F)
                .texOffs(53, 27).addBox(0.13F, 2.02F, 0.04F, 0.38F, 0.32F, 0.8134F),
            PartPose.offset(1.45F, -2.39F, 0.22F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.95F, -0.41F, -1.91F, 1.0F, 5.0F, 4.0F)
                .texOffs(16, 20).addBox(-0.77F, 4.01F, -0.37F, 1.0F, 3.0F, 3.0F),
            PartPose.offsetAndRotation(-2.95F, -8.07F, 0.19F, 0.6283F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(46, 27).addBox(-0.5F, -0.04F, -0.45F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.101F))
                .texOffs(53, 27).addBox(-0.5312F, 1.75F, -0.718F, 1.0624F, 0.5F, 1.0624F, new CubeDeformation(-0.08F))
                .texOffs(53, 27).addBox(0.15F, 2.01F, -1.9879F, 0.4F, 0.34F, 1.5687F)
                .texOffs(53, 27).addBox(-0.55F, 1.98F, -1.7887F, 0.4F, 0.34F, 1.3944F)
                .texOffs(53, 27).addBox(0.13F, 2.01F, 0.01F, 0.38F, 0.32F, 1.0624F)
                .texOffs(53, 27).addBox(-0.51F, 2.02F, 0.04F, 0.38F, 0.32F, 0.8134F),
            PartPose.offset(-1.45F, -2.39F, 0.22F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    static LayerDefinition monkParakeet() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bird = root.addOrReplaceChild("bird", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        bird.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 65).addBox(-3.5F, -5.78F, -2.89F, 7.0F, 5.0F, 7.0F)
                .texOffs(30, 65).addBox(-4.0F, -2.7F, -4.9F, 8.0F, 6.0F, 8.0F)
                .texOffs(1, 78).addBox(-3.0F, 1.53F, -2.1F, 6.0F, 4.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -8.75F, 0.85F, 0.4887F, 0.0F, 0.0F));

        bird.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-4.0F, -5.65F, -4.45F, 8.0F, 7.0F, 7.0F, new CubeDeformation(0.09F))
                .texOffs(32, 34).addBox(-3.5F, -6.51F, -4.0F, 7.0F, 1.0F, 6.0F)
                .texOffs(32, 19).addBox(-2.0F, -2.15F, -6.37F, 4.0F, 2.0F, 3.0F)
                .texOffs(47, 19).addBox(-1.0F, -0.61F, -6.31F, 2.0F, 2.0F, 1.0F)
                .texOffs(54, 19).addBox(-1.5F, -2.93F, -5.27F, 3.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -12.5F, -2.55F));

        bird.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-2.5F, -1.01F, -0.2F, 5.0F, 2.0F, 8.0F)
                .texOffs(0, 50).addBox(-1.5F, -0.6F, 7.06F, 3.0F, 1.0F, 10.0F)
                .texOffs(32, 48).addBox(-0.5F, -0.65F, 16.13F, 1.0F, 1.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -5.15F, 3.72F, -0.1815F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.05F, -0.63F, -2.33F, 1.0F, 9.0F, 6.0F)
                .texOffs(16, 20).addBox(-0.23F, 7.37F, 0.95F, 1.0F, 6.0F, 4.0F)
                .texOffs(26, 80).addBox(0.31F, -0.91F, -2.61F, 1.0F, 5.0F, 5.0F)
                .texOffs(39, 80).addBox(0.1F, 7.25F, 1.87F, 1.0F, 5.0F, 4.0F),
            PartPose.offsetAndRotation(3.95F, -12.2F, 0.19F, 0.7505F, 0.0F, 0.0F));

        bird.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(46, 27).addBox(-0.5F, 0.07F, -0.45F, 1.0F, 3.0F, 1.0F)
                .texOffs(53, 27).addBox(-0.704F, 2.86F, -0.88F, 1.408F, 0.5F, 1.408F, new CubeDeformation(-0.055F))
                .texOffs(53, 27).addBox(-0.55F, 3.12F, -2.563F, 0.4F, 0.34F, 2.079F)
                .texOffs(53, 27).addBox(0.15F, 3.09F, -2.299F, 0.4F, 0.34F, 1.848F)
                .texOffs(53, 27).addBox(-0.51F, 3.12F, 0.01F, 0.38F, 0.32F, 1.408F)
                .texOffs(53, 27).addBox(0.13F, 3.13F, 0.04F, 0.38F, 0.32F, 1.078F),
            PartPose.offset(1.9F, -3.5F, 0.22F));

        bird.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.95F, -0.63F, -2.33F, 1.0F, 9.0F, 6.0F)
                .texOffs(16, 20).addBox(-0.77F, 7.37F, 0.95F, 1.0F, 6.0F, 4.0F)
                .texOffs(50, 80).addBox(-1.31F, -0.91F, -2.61F, 1.0F, 5.0F, 5.0F)
                .texOffs(1, 89).addBox(-1.1F, 7.25F, 1.87F, 1.0F, 5.0F, 4.0F),
            PartPose.offsetAndRotation(-3.95F, -12.2F, 0.19F, 0.7505F, 0.0F, 0.0F));

        bird.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(46, 27).addBox(-0.5F, 0.07F, -0.45F, 1.0F, 3.0F, 1.0F)
                .texOffs(53, 27).addBox(-0.704F, 2.86F, -0.88F, 1.408F, 0.5F, 1.408F, new CubeDeformation(-0.055F))
                .texOffs(53, 27).addBox(0.15F, 3.12F, -2.563F, 0.4F, 0.34F, 2.079F)
                .texOffs(53, 27).addBox(-0.55F, 3.09F, -2.299F, 0.4F, 0.34F, 1.848F)
                .texOffs(53, 27).addBox(0.13F, 3.12F, 0.01F, 0.38F, 0.32F, 1.408F)
                .texOffs(53, 27).addBox(-0.51F, 3.13F, 0.04F, 0.38F, 0.32F, 1.078F),
            PartPose.offset(-1.9F, -3.5F, 0.22F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}

package me.marcronte.colisaocobblemon.client.model;

import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class EternatiteChestplateModel<T extends Entity> extends EntityModel<T> {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "eternatite_chestplate"), "main"
    );
    
    public final ModelPart body;
    public final ModelPart chestplate;
    public final ModelPart arm_right;
    public final ModelPart arm_plate_right;
    public final ModelPart arm_left;
    public final ModelPart arm_plate_left;

    public EternatiteChestplateModel(ModelPart root) {
        this.body = root.getChild("body");
        this.chestplate = this.body.getChild("chestplate");
        this.arm_right = root.getChild("arm_right");
        this.arm_plate_right = this.arm_right.getChild("arm_plate_right");
        this.arm_left = root.getChild("arm_left");
        this.arm_plate_left = this.arm_left.getChild("arm_plate_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestplate = body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition arm_right = partdefinition.addOrReplaceChild("arm_right", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition arm_plate_right = arm_right.addOrReplaceChild("arm_plate_right", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition arm_left = partdefinition.addOrReplaceChild("arm_left", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition arm_plate_left = arm_left.addOrReplaceChild("arm_plate_left", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
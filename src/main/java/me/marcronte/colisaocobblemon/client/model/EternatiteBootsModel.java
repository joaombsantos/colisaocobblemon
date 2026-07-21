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

public class EternatiteBootsModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "eternatita_boots"), "main"
    );

    public final ModelPart right_leg;
    public final ModelPart boot_right;
    public final ModelPart left_leg;
    public final ModelPart boot_left;

    public EternatiteBootsModel(ModelPart root) {
        this.right_leg = root.getChild("right_leg");
        this.boot_right = this.right_leg.getChild("boot_right");
        this.left_leg = root.getChild("left_leg");
        this.boot_left = this.left_leg.getChild("boot_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition boot_right = right_leg.addOrReplaceChild("boot_right", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 9.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(33, 33).addBox(-3.0F, 8.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.9F, 12.0F, 0.0F, -0.0017F, 0.0F, 0.0F));

        PartDefinition boot_left = left_leg.addOrReplaceChild("boot_left", CubeListBuilder.create().texOffs(34, 38).addBox(-2.8F, 8.0F, -3.0017F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(24, 18).addBox(-2.8F, 9.0F, -3.0017F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
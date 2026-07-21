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

public class EternatiteSwordModel<T extends Entity> extends EntityModel<T> {
    
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "eternatite_sword"), "main"
    );
    
    private final ModelPart root_item;

    public EternatiteSwordModel(ModelPart root) {
        this.root_item = root.getChild("root_item");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root_item = partdefinition.addOrReplaceChild("root_item", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(12, 21).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(0.0F, -21.0F, -3.0F, 0.0F, 17.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(12, 0).addBox(-1.0F, -21.0F, -2.0F, 2.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(24, 0).addBox(-2.0F, -4.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(0, 29).addBox(2.0F, -4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(6, 29).addBox(-3.0F, -4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(12, 30).addBox(1.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(24, 7).addBox(1.0F, -4.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(24, 19).addBox(-1.0F, -3.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(18, 30).addBox(-2.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(24, 14).addBox(0.0F, -22.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(30, 19).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(8, 23).addBox(0.0F, -23.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root_item.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
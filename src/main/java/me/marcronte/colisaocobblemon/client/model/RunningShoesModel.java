package me.marcronte.colisaocobblemon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RunningShoesModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "running_shoes"), "main"
	);

	public final ModelPart right_leg;
	public final ModelPart left_leg;

	public RunningShoesModel(ModelPart root) {
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, 12.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(0, 11).addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(0, 14).addBox(-2.0F, 9.0F, -2.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(8, 4).addBox(-2.0F, 9.0F, -2.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(10, 11).addBox(-2.0F, 9.0F, 2.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(0, 4).addBox(2.0F, 9.0F, -2.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(8, 14).addBox(-1.0F, 8.0F, -2.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, 12.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(0, 11).addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(0, 14).addBox(-2.0F, 9.0F, -2.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(8, 4).addBox(-2.0F, 9.0F, -2.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(10, 11).addBox(-2.0F, 9.0F, 2.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(0, 4).addBox(2.0F, 9.0F, -2.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(8, 14).addBox(-1.0F, 8.0F, -2.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
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
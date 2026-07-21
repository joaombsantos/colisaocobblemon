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

public class EternatiteAxeModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "eternatite_axe"), "main"
	);

	private final ModelPart main_axe;
	private final ModelPart handle;
	private final ModelPart head;

	public EternatiteAxeModel(ModelPart root) {
		this.main_axe = root.getChild("main_axe");
		this.handle = this.main_axe.getChild("handle");
		this.head = this.main_axe.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main_axe = partdefinition.addOrReplaceChild("main_axe", CubeListBuilder.create(), PartPose.offset(2.0F, 22.0F, -1.0F));

		PartDefinition handle = main_axe.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 13).addBox(4.0F, 6.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 6).addBox(-1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 8).addBox(-2.0F, -7.0F, -2.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 17).addBox(0.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 15).addBox(1.0F, 0.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 11).addBox(2.0F, 2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 16).addBox(3.0F, 4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 23).addBox(5.0F, 5.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(4.0F, 3.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 10).addBox(3.0F, 1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 13).addBox(2.0F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 19).addBox(-3.0F, -8.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(22, 3).addBox(-3.0F, -7.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 19).addBox(0.0F, -5.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 16).addBox(1.0F, -3.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -6.0F, 1.0F));

		PartDefinition head = main_axe.addOrReplaceChild("head", CubeListBuilder.create().texOffs(22, 0).addBox(-4.0F, -6.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 3).addBox(-5.0F, -5.0F, -2.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 5).addBox(-5.0F, -4.0F, -2.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-4.0F, -3.0F, -2.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-3.0F, -2.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 22).addBox(0.0F, -7.0F, -2.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 11).addBox(-1.0F, -10.0F, -2.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(22, 22).addBox(1.0F, -9.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(1.0F, -7.0F, -2.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 20).addBox(1.0F, -8.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(1.0F, -6.0F, -2.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -6.0F, 1.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		main_axe.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
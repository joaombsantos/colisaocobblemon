package me.marcronte.colisaocobblemon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import me.marcronte.colisaocobblemon.ModItems;
import me.marcronte.colisaocobblemon.client.model.*;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ModArmorRenderers {

    private static RunningShoesModel<LivingEntity> runningShoesModel;
    private static EternatiteBootsModel<LivingEntity> eternatiteBootsModel;
    private static EternatiteLeggingsModel<LivingEntity> eternatiteLeggingsModel;
    private static EternatiteChestplateModel<LivingEntity> eternatiteChestplateModel;
    private static EternatiteHelmetModel<LivingEntity> eternatiteHelmetModel;

    public static void registerAll() {
        registerRunningShoes();
        registerEternatiteArmor();
    }

    private static void registerRunningShoes() {
        ArmorRenderer.register((PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int light, HumanoidModel<LivingEntity> humanoidModel) -> {
            if (runningShoesModel == null) {
                runningShoesModel = new RunningShoesModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(RunningShoesModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(runningShoesModel);
            runningShoesModel.right_leg.copyFrom(humanoidModel.rightLeg);
            runningShoesModel.left_leg.copyFrom(humanoidModel.leftLeg);
            runningShoesModel.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/running_shoes.png");
            runningShoesModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        }, ModItems.RUNNING_SHOES);
    }

    private static void registerEternatiteArmor() {
        // --- BOOTS ---
        ArmorRenderer.register((PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int light, HumanoidModel<LivingEntity> humanoidModel) -> {
            if (eternatiteBootsModel == null) {
                eternatiteBootsModel = new EternatiteBootsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteBootsModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(eternatiteBootsModel);
            eternatiteBootsModel.right_leg.copyFrom(humanoidModel.rightLeg);
            eternatiteBootsModel.left_leg.copyFrom(humanoidModel.leftLeg);
            eternatiteBootsModel.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/eternatite_boots.png");
            eternatiteBootsModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        }, ModItems.ETERNATITE_BOOTS);

        // --- LEGGINGS ---
        ArmorRenderer.register((PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int light, HumanoidModel<LivingEntity> humanoidModel) -> {
            if (eternatiteLeggingsModel == null) {
                eternatiteLeggingsModel = new EternatiteLeggingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteLeggingsModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(eternatiteLeggingsModel);
            eternatiteLeggingsModel.body.copyFrom(humanoidModel.body);
            eternatiteLeggingsModel.leg_right.copyFrom(humanoidModel.rightLeg);
            eternatiteLeggingsModel.leg_left.copyFrom(humanoidModel.leftLeg);
            eternatiteLeggingsModel.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/eternatite_leggings.png");
            eternatiteLeggingsModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        }, ModItems.ETERNATITE_LEGGINGS);

        // --- CHESTPLATE ---
        ArmorRenderer.register((PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int light, HumanoidModel<LivingEntity> humanoidModel) -> {
            if (eternatiteChestplateModel == null) {
                eternatiteChestplateModel = new EternatiteChestplateModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteChestplateModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(eternatiteChestplateModel);

            eternatiteChestplateModel.body.copyFrom(humanoidModel.body);
            eternatiteChestplateModel.arm_right.copyFrom(humanoidModel.rightArm);
            eternatiteChestplateModel.arm_left.copyFrom(humanoidModel.leftArm);

            eternatiteChestplateModel.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/eternatite_chestplate.png");
            eternatiteChestplateModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        }, ModItems.ETERNATITE_CHESTPLATE);

        // --- HELMET ---
        ArmorRenderer.register((PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int light, HumanoidModel<LivingEntity> humanoidModel) -> {
            if (eternatiteHelmetModel == null) {
                eternatiteHelmetModel = new EternatiteHelmetModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteHelmetModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(eternatiteHelmetModel);

            eternatiteHelmetModel.root_item.copyFrom(humanoidModel.head);

            eternatiteHelmetModel.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/eternatite_helmet.png");
            eternatiteHelmetModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        }, ModItems.ETERNATITE_HELMET);
    }
}
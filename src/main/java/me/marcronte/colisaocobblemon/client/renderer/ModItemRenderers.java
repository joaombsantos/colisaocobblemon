package me.marcronte.colisaocobblemon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.marcronte.colisaocobblemon.ModItems;
import me.marcronte.colisaocobblemon.client.model.*;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ModItemRenderers {

    private static EternatiteSwordModel<?> eternatiteSwordModel;
    private static EternatiteAxeModel<?> eternatiteAxeModel;
    private static EternatitePickaxeModel<?> eternatitePickaxeModel;
    private static EternatiteShovelModel<?> eternatiteShovelModel;
    private static EternatiteHoeModel<?> eternatiteHoeModel;

    public static void registerAll() {
        registerEternatiteSword();
        registerEternatiteAxe();
        registerEternatitePickaxe();
        registerEternatiteShovel();
        registerEternatiteHoe();
    }

    private static void registerEternatiteSword() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ETERNATITE_SWORD, (ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) -> {

            if (eternatiteSwordModel == null) {
                eternatiteSwordModel = new EternatiteSwordModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteSwordModel.LAYER_LOCATION));
            }

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(1.0f, -1.0f, -1.0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/item/eternatite_sword.png");
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            eternatiteSwordModel.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);

            poseStack.popPose();
        });
    }

    private static void registerEternatiteAxe() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ETERNATITE_AXE, (ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) -> {

            if (eternatiteAxeModel == null) {
                eternatiteAxeModel = new EternatiteAxeModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteAxeModel.LAYER_LOCATION));
            }

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(1.0f, -1.0f, -1.0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/item/eternatite_axe.png");
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            eternatiteAxeModel.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);

            poseStack.popPose();
        });
    }

    private static void registerEternatitePickaxe() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ETERNATITE_PICKAXE, (ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) -> {

            if (eternatitePickaxeModel == null) {
                eternatitePickaxeModel = new EternatitePickaxeModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatitePickaxeModel.LAYER_LOCATION));
            }

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(1.0f, -1.0f, -1.0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/item/eternatite_pickaxe.png");
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            eternatitePickaxeModel.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);

            poseStack.popPose();
        });
    }

    private static void registerEternatiteShovel() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ETERNATITE_SHOVEL, (ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) -> {

            if (eternatiteShovelModel == null) {
                eternatiteShovelModel = new EternatiteShovelModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteShovelModel.LAYER_LOCATION));
            }

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(1.0f, -1.0f, -1.0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/item/eternatite_shovel.png");
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            eternatiteShovelModel.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);

            poseStack.popPose();
        });
    }

    private static void registerEternatiteHoe() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ETERNATITE_HOE, (ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) -> {

            if (eternatiteHoeModel == null) {
                eternatiteHoeModel = new EternatiteHoeModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EternatiteHoeModel.LAYER_LOCATION));
            }

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(1.0f, -1.0f, -1.0f);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/item/eternatite_hoe.png");
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            eternatiteHoeModel.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);

            poseStack.popPose();
        });
    }
}
package me.marcronte.colisaocobblemon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchNetwork;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchStatueBlock;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchStatueEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SwitchStatueRenderer implements BlockEntityRenderer<SwitchStatueEntity> {
    private final BlockRenderDispatcher blockRenderer;
    private static final ResourceLocation MODEL_A = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "block/switch_statue");
    private static final ResourceLocation MODEL_B = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "block/switch_statue_active");

    public SwitchStatueRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }


    @Override
    public void render(SwitchStatueEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof SwitchStatueBlock)) return;

        if (state.getValue(SwitchStatueBlock.PART) != SwitchStatueBlock.StatuePart.BASE) {
            return;
        }

        // 1. Get Client State
        String currentState = SwitchNetwork.CLIENT_STATE != null ? SwitchNetwork.CLIENT_STATE : "A";
        ResourceLocation modelLoc = currentState.equalsIgnoreCase("B") ? MODEL_B : MODEL_A;

        // 2. Get model
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLoc);
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) return;

        poseStack.pushPose();

        // 13 pixels translation
        poseStack.translate(0.0f, 0.8125f, 0.0f);


        poseStack.translate(0.5, 0.0, 0.5);

        Direction facing = state.getValue(SwitchStatueBlock.FACING);
        float rotationDegrees = 0f;

        switch (facing) {
            case SOUTH -> rotationDegrees = 180f;
            case WEST  -> rotationDegrees = 90f;
            case EAST  -> rotationDegrees = 270f;
            case NORTH -> rotationDegrees = 0f;
        }

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDegrees));

        poseStack.translate(-0.5, 0.0, -0.5);

        // 3. Render
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());

        this.blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                consumer,
                state,
                model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
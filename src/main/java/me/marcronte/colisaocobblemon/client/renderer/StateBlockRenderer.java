package me.marcronte.colisaocobblemon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.marcronte.colisaocobblemon.features.switchstate.StateBlock;
import me.marcronte.colisaocobblemon.features.switchstate.StateBlockEntity;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class StateBlockRenderer implements BlockEntityRenderer<StateBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public StateBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(StateBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof StateBlock)) return;

        String blockType = state.getValue(StateBlock.TYPE).getSerializedName();
        String playerState = SwitchNetwork.CLIENT_STATE;

        if (blockType.equalsIgnoreCase(playerState)) {
            poseStack.pushPose();

            ItemStack stack = new ItemStack(state.getBlock());

            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.getLevel(), null, 0);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.cutout());

            this.blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(),
                    vertexConsumer,
                    state,
                    model,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }
    }
}
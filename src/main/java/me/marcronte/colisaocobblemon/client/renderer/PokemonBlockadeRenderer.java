package me.marcronte.colisaocobblemon.client.renderer;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockadeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;

public class PokemonBlockadeRenderer implements BlockEntityRenderer<PokemonBlockadeEntity> {

    private final EntityRenderDispatcher entityRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public PokemonBlockadeRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(PokemonBlockadeEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PokemonEntity pokemon = tile.getClientFakeEntity();

        // 1. Visibility
        if (pokemon == null) {
            poseStack.pushPose();
            this.blockRenderer.renderSingleBlock(tile.getBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        poseStack.pushPose();

        // 2. Positioning
        poseStack.translate(0.5, 0.05, 0.5);

        // 3. Rotation
        if (Minecraft.getInstance().player != null) {
            Vec3 playerPos = Minecraft.getInstance().player.position();
            Vec3 blockPos = tile.getBlockPos().getCenter();

            double dX = playerPos.x - blockPos.x;
            double dZ = playerPos.z - blockPos.z;

            float yaw = (float) (Math.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;

            pokemon.setYRot(yaw);
            pokemon.yBodyRot = yaw;
            pokemon.yHeadRot = yaw;
            pokemon.yHeadRotO = yaw;
            pokemon.yBodyRotO = yaw;

            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw + 180));
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.5, 0.05, 0.5);
        }

        // 4. Scale
        float scale = pokemon.getScale();
        poseStack.scale(scale, scale, scale);

        // 5. Rendering
        try {
            pokemon.setPos(tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ());

            this.entityRenderer.render(
                    pokemon,
                    0.0, 0.0, 0.0,
                    0.0f, partialTick,
                    poseStack, bufferSource, packedLight
            );
        } catch (Exception ignored) {}

        poseStack.popPose();
    }
}
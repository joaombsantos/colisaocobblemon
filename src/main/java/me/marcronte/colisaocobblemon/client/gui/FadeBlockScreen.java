package me.marcronte.colisaocobblemon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlock;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlockMenu;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FadeBlockScreen extends AbstractContainerScreen<FadeBlockMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "textures/gui/fade_block.png");

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    public FadeBlockScreen(FadeBlockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.visibility"), button -> {
                    if (this.menu.pos != null) {
                        ClientPlayNetworking.send(new FadeNetwork.ToggleVisibilityPayload(this.menu.pos));
                    }
                })
                .pos(this.leftPos + 105, this.topPos + 35)
                .size(60, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (this.minecraft != null && this.minecraft.level != null && this.menu.pos != null) {
            boolean isVisible = this.minecraft.level.getBlockState(this.menu.pos).getValue(FadeBlock.VISIBLE);

            String status = isVisible ? "ON" : "OFF";
            int color = isVisible ? 0x00FF00 : 0xFF0000;

            guiGraphics.drawString(this.font, status, this.leftPos + 125, this.topPos + 25, color, false);
        }
    }
}
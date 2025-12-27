package me.marcronte.colisaocobblemon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootBlock;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootMenu;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PokeLootScreen extends AbstractContainerScreen<PokeLootMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "textures/gui/poke_loot.png");

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    public PokeLootScreen(PokeLootMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.visibility"), button -> {
                    if (this.menu.blockEntity != null) {
                        ClientPlayNetworking.send(new PokeLootNetwork.ToggleVisibilityPayload(this.menu.blockEntity.getBlockPos()));
                    }
                })
                .pos(this.leftPos + 105, this.topPos + 35)
                .size(60, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (this.menu.blockEntity != null) {
            boolean isVisible = this.menu.blockEntity.getBlockState().getValue(PokeLootBlock.VISIBLE);
            String status = isVisible ? "ON" : "OFF";
            int color = isVisible ? 0x00FF00 : 0xFF0000;

            guiGraphics.drawString(this.font, status, this.leftPos + 125, this.topPos + 25, color, false);
        }
    }
}